package com.getamotel.security

import jakarta.enterprise.context.RequestScoped
import java.util.UUID

@RequestScoped
class TenantContext {
    private var tenantId: UUID? = null

    fun setTenantId(id: UUID) {
        this.tenantId = id
    }

    fun getTenantId(): UUID {
        return tenantId ?: throw IllegalStateException("Tenant context not set")
    }
    
    fun hasTenantId(): Boolean = tenantId != null
}
