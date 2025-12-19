package com.getamotel.repository

import com.getamotel.domain.property.RoomType
import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepository
import jakarta.enterprise.context.ApplicationScoped
import java.util.*

@ApplicationScoped
class RoomTypeRepository : PanacheRepository<RoomType> {
    
    fun findByProperty(propertyId: UUID, tenantId: UUID): List<RoomType> {
        return find("property.id = ?1 and tenantId = ?2 and deletedAt is null", propertyId, tenantId).list()
    }
}
