package com.getamotel.service

import com.getamotel.domain.inventory.Inventory
import com.getamotel.repository.InventoryRepository
import com.getamotel.security.TenantContext
import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import java.time.LocalDate
import java.util.*

@ApplicationScoped
class InventoryService(
    private val inventoryRepository: InventoryRepository,
    private val tenantContext: TenantContext
) {

    fun getAvailability(roomTypeId: UUID, startDate: LocalDate, endDate: LocalDate): List<Inventory> {
        return inventoryRepository.findByRoomTypeAndDateRange(
            roomTypeId, startDate, endDate, tenantContext.getTenantId()
        )
    }

    @Transactional
    fun allocateRooms(roomTypeId: UUID, startDate: LocalDate, endDate: LocalDate, count: Int): Boolean {
        val inventoryList = getAvailability(roomTypeId, startDate, endDate)
        val numNights = startDate.until(endDate).days.toInt()
        
        if (inventoryList.size < numNights) return false // Missing inventory for some dates
        
        for (inventory in inventoryList) {
            if (inventory.stopSell || (inventory.totalCount - inventory.allocatedCount) < count) {
                return false // Over capacity or stop sell
            }
            inventory.allocatedCount += count
        }
        
        return true
    }
    
    @Transactional
    fun releaseRooms(roomTypeId: UUID, startDate: LocalDate, endDate: LocalDate, count: Int) {
        val inventoryList = getAvailability(roomTypeId, startDate, endDate)
        for (inventory in inventoryList) {
            inventory.allocatedCount -= count
        }
    }
}
