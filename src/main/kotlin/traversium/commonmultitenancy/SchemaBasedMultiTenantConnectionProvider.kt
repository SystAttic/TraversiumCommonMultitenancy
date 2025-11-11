package traversium.commonmultitenancy

import org.hibernate.engine.jdbc.connections.spi.AbstractMultiTenantConnectionProvider
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider
import org.slf4j.LoggerFactory
import java.sql.Connection
import javax.sql.DataSource

class SchemaBasedMultiTenantConnectionProvider(
    private val dataSource: DataSource
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
            val connection = dataSource.connection
            val schemaName = if (tenantIdentifier == "public") {
                "public"
            } else {
                val sanitizedTenantId = TenantUtils.sanitizeTenantIdForSchema(tenantIdentifier)
                "tenant_$sanitizedTenantId"
            }

            logger.info("Getting connection for tenant '$tenantIdentifier', setting schema to: '$schemaName'")

            connection.createStatement().use { stmt ->
                stmt.execute("SET search_path TO $schemaName")
            }

            return connection
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
}
