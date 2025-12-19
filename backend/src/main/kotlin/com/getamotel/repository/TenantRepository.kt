package com.getamotel.repository

import com.getamotel.domain.tenant.Tenant
import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepository
import jakarta.enterprise.context.ApplicationScoped
import java.util.*

@ApplicationScoped
class TenantRepository : PanacheRepository<Tenant> {
    
    fun findBySlug(slug: String): Tenant? {
        return find("slug = ?1 and deletedAt is null", slug).firstResult()
    }
    
    fun findActiveById(id: UUID): Tenant? {
        return find("id = ?1 and status = 'ACTIVE' and deletedAt is null", id).firstResult()
    }
}
