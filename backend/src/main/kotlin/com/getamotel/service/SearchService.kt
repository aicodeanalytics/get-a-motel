package com.getamotel.service

import com.getamotel.domain.property.Property
import com.getamotel.repository.PropertyRepository
import com.getamotel.repository.RoomTypeRepository
import jakarta.enterprise.context.ApplicationScoped
import java.time.LocalDate
import java.util.*

@ApplicationScoped
class SearchService(
    private val propertyRepository: PropertyRepository,
    private val roomTypeRepository: RoomTypeRepository,
    private val inventoryService: InventoryService,
    private val pricingService: PricingService
) {

    fun searchProperties(
        city: String?,
        state: String?,
        startDate: LocalDate,
        endDate: LocalDate,
        guests: Int
    ): List<PropertySearchResult> {
        // 1. Find properties in the location
        // Note: In MVP we use simple filtering. Future: Geo search.
        val properties = if (city != null && state != null) {
            propertyRepository.find("city = ?1 and state = ?2 and status = 'ACTIVE' and deletedAt is null", city, state).list()
        } else if (state != null) {
            propertyRepository.find("state = ?1 and status = 'ACTIVE' and deletedAt is null", state).list()
        } else {
            propertyRepository.find("status = 'ACTIVE' and deletedAt is null").list()
        }

        return properties.mapNotNull { property ->
            val availableRoomTypes = roomTypeRepository.findByProperty(property.id!!, property.tenantId).filter { roomType ->
                // Check if max occupancy is enough
                if (roomType.maxOccupancy < guests) return@filter false
                
                // Check availability
                val availability = inventoryService.getAvailability(roomType.id!!, startDate, endDate)
                val numNights = startDate.until(endDate).days.toInt()
                
                availability.size == numNights && availability.all { it.totalCount - it.allocatedCount > 0 && !it.stopSell }
            }

            if (availableRoomTypes.isEmpty()) return@mapNotNull null

            // Find lowest price
            val lowestPrice = availableRoomTypes.minOf { roomType ->
                try {
                    pricingService.calculatePrice(property.id!!, roomType.id!!, startDate, endDate)
                } catch (e: Exception) {
                    Long.MAX_VALUE
                }
            }

            if (lowestPrice == Long.MAX_VALUE) return@mapNotNull null

            PropertySearchResult(
                property = property,
                lowestPriceCents = lowestPrice,
                availableRoomTypeCount = availableRoomTypes.size
            )
        }
    }
}

data class PropertySearchResult(
    val property: Property,
    val lowestPriceCents: Long,
    val availableRoomTypeCount: Int
)
