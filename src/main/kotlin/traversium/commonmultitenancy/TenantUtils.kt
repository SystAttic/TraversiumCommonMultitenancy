package traversium.commonmultitenancy

/**
 * Utility functions for tenant ID operations.
 *
 * @author Maja Razinger
 */
object TenantUtils {

    fun sanitizeTenantIdForSchema(tenantId: String): String {
        return tenantId.replace("-", "_")
    }

    fun desanitizeTenantIdFromSchema(tenantId: String): String {
        return tenantId.replace("_", "-")
    }
}
