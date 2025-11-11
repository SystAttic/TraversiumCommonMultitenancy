package traversium.commonmultitenancy

/**
 * @author Maja Razinger
 */
object TenantContext {
    private val currentTenant = ThreadLocal<String>()
    
    fun setTenant(tenantId: String) {
        currentTenant.set(tenantId)
    }
    
    fun getTenant(): String {
        return currentTenant.get() ?: "public"
    }
    
    fun clear() {
        currentTenant.remove()
    }
}
