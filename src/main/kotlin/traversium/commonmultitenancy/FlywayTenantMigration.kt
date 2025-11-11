package traversium.commonmultitenancy

import org.flywaydb.core.Flyway
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import javax.sql.DataSource

/**
 * @author Maja Razinger
 */
@Component
class FlywayTenantMigration(
    private val dataSource: DataSource
) {
    private val logger = LoggerFactory.getLogger(FlywayTenantMigration::class.java)

    fun migrateTenant(tenantId: String) {
        val sanitizedTenantId = TenantUtils.sanitizeTenantIdForSchema(tenantId)
        val schemaName = "tenant_$sanitizedTenantId"

        logger.info("Starting tenant provisioning for tenant: $tenantId (schema: $schemaName)")

        try {
            dataSource.connection.use { conn ->
                conn.createStatement().execute("CREATE SCHEMA IF NOT EXISTS $schemaName")
                logger.info("Schema $schemaName created or already exists")
            }

            val result = Flyway.configure()
                .dataSource(dataSource)
                .schemas(schemaName)
                .locations("classpath:db/migration/tenant")
                .load()
                .migrate()

            logger.info("Flyway migration completed for $schemaName. Migrations executed: ${result.migrationsExecuted}")
        } catch (e: Exception) {
            logger.error("Failed to provision tenant $tenantId", e)
            throw e
        }
    }

    fun deleteTenant(tenantId: String) {
        val sanitizedTenantId = TenantUtils.sanitizeTenantIdForSchema(tenantId)
        val schemaName = "tenant_$sanitizedTenantId"

        logger.info("Starting tenant deletion for tenant: $tenantId (schema: $schemaName)")

        try {
            dataSource.connection.use { conn ->
                val checkSchemaQuery = """
                    SELECT schema_name
                    FROM information_schema.schemata
                    WHERE schema_name = ?
                """.trimIndent()

                val schemaExists = conn.prepareStatement(checkSchemaQuery).use { stmt ->
                    stmt.setString(1, schemaName)
                    val rs = stmt.executeQuery()
                    rs.next()
                }

                if (!schemaExists) {
                    logger.warn("Schema $schemaName does not exist, nothing to delete")
                    return
                }

                conn.createStatement().execute("DROP SCHEMA IF EXISTS $schemaName CASCADE")
                logger.info("Schema $schemaName and all its objects have been dropped successfully")
            }
        } catch (e: Exception) {
            logger.error("Failed to delete tenant $tenantId", e)
            throw e
        }
    }
}