package traversium.commonmultitenancy

import org.hibernate.engine.jdbc.connections.spi.AbstractMultiTenantConnectionProvider
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider
import org.slf4j.LoggerFactory
import java.sql.Connection
import javax.sql.DataSource

class SchemaBasedMultiTenantConnectionProvider(
    private val dataSource: DataSource,
    private val flywayTenantMigration: FlywayTenantMigration
) : AbstractMultiTenantConnectionProvider<String>() {

    private val logger = LoggerFactory.getLogger(SchemaBasedMultiTenantConnectionProvider::class.java)

    override fun getAnyConnectionProvider(): ConnectionProvider {
        return TenantConnectionProvider("public")
    }

    override fun selectConnectionProvider(tenantIdentifier: String?): ConnectionProvider {
        val tenant = tenantIdentifier ?: "public"
        logger.info("Selecting connection provider for tenant: '$tenant'")
        return TenantConnectionProvider(tenant)
    }

    private inner class TenantConnectionProvider(
        private val tenantIdentifier: String
    ) : ConnectionProvider {

        override fun getConnection(): Connection {
            val rawConnection = dataSource.connection

            val schemaName = when (tenantIdentifier) {
                "public" -> "public"
                else -> "tenant_${TenantUtils.sanitizeTenantIdForSchema(tenantIdentifier)}"
            }

            logger.info("Getting connection for tenant '$tenantIdentifier', setting schema to: '$schemaName'")

            if (schemaName != "public") {
                if (!schemaExists(rawConnection, schemaName)) {
                    logger.info("Schema '$schemaName' does not exist, running Flyway migration for it.")

                    rawConnection.close()

                    flywayTenantMigration.migrateTenant(tenantIdentifier)

                    return dataSource.connection.also { conn ->
                        setSearchPath(conn, schemaName)
                    }
                }
            }

            setSearchPath(rawConnection, schemaName)
            return rawConnection
        }

        override fun closeConnection(connection: Connection) {
            try {
                connection.createStatement().use { stmt ->
                    stmt.execute("SET search_path TO public")
                }
                logger.debug("Connection closed and reset to public schema for tenant: $tenantIdentifier")
            } catch (e: Exception) {
                logger.warn("Failed to reset schema to public when closing connection", e)
            }
            connection.close()
        }

        override fun supportsAggressiveRelease(): Boolean {
            return false
        }

        override fun <T : Any?> unwrap(unwrapType: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return dataSource as T
        }

        override fun isUnwrappableAs(unwrapType: Class<*>): Boolean {
            return DataSource::class.java.isAssignableFrom(unwrapType)
        }
    }

    private fun schemaExists(connection: Connection, schemaName: String): Boolean {
        val sql = """
        SELECT EXISTS (
            SELECT 1
            FROM information_schema.schemata
            WHERE schema_name = ?
        )
    """
        return connection.prepareStatement(sql).use { stmt ->
            stmt.setString(1, schemaName)
            stmt.executeQuery().use { resultSet ->
                resultSet.next() && resultSet.getBoolean(1)
            }
        }
    }

    private fun setSearchPath(connection: Connection, schema: String) {
        connection.createStatement().use { stmt ->
            stmt.execute("SET search_path TO $schema")
        }
    }
}
