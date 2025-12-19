package com.getamotel.repository

import com.getamotel.domain.property.Property
import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepository
import jakarta.enterprise.context.ApplicationScoped
import java.util.*

@ApplicationScoped
class PropertyRepository : PanacheRepository<Property> {
    
    fun findByTenant(tenantId: UUID): List<Property> {
        return find("tenantId = ?1 and deletedAt is null", tenantId).list()
    }
    
    fun findByIdAndTenant(id: UUID, tenantId: UUID): Property? {
        return find("id = ?1 and tenantId = ?2 and deletedAt is null", id, tenantId).firstResult()
    }
}
