package com.getamotel.repository

import com.getamotel.domain.inventory.Inventory
import com.getamotel.domain.pricing.NightlyRate
import com.getamotel.domain.pricing.RatePlan
import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepository
import jakarta.enterprise.context.ApplicationScoped
import java.time.LocalDate
import java.util.*

@ApplicationScoped
class InventoryRepository : PanacheRepository<Inventory> {
    fun findByRoomTypeAndDateRange(roomTypeId: UUID, startDate: LocalDate, endDate: LocalDate, tenantId: UUID): List<Inventory> {
        return find("roomType.id = ?1 and date >= ?2 and date < ?3 and tenantId = ?4", 
            roomTypeId, startDate, endDate, tenantId).list()
    }
}

@ApplicationScoped
class RatePlanRepository : PanacheRepository<RatePlan> {
    fun findDefaultByProperty(propertyId: UUID, tenantId: UUID): RatePlan? {
        return find("property.id = ?1 and isDefault = true and tenantId = ?2", propertyId, tenantId).firstResult()
    }
}

@ApplicationScoped
class NightlyRateRepository : PanacheRepository<NightlyRate> {
    fun findByRatePlanAndRoomTypeAndDateRange(ratePlanId: UUID, roomTypeId: UUID, startDate: LocalDate, endDate: LocalDate, tenantId: UUID): List<NightlyRate> {
        return find("ratePlan.id = ?1 and roomType.id = ?2 and date >= ?3 and date < ?4 and tenantId = ?5", 
            ratePlanId, roomTypeId, startDate, endDate, tenantId).list()
    }
}
