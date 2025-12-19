package com.getamotel.api

import com.getamotel.domain.inventory.Inventory
import com.getamotel.domain.pricing.NightlyRate
import com.getamotel.domain.property.RoomType
import com.getamotel.domain.pricing.RatePlan
import com.getamotel.repository.InventoryRepository
import com.getamotel.repository.NightlyRateRepository
import com.getamotel.security.TenantContext
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.transaction.Transactional
import java.time.LocalDate
import java.util.*

@Path("/admin/inventory")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class InventoryAdminResource(
    private val inventoryRepository: InventoryRepository,
    private val nightlyRateRepository: NightlyRateRepository,
    private val auditService: AuditService,
    private val tenantContext: TenantContext
) {

    @POST
    @Path("/bulk-update")
    @Transactional
    fun bulkUpdate(request: BulkUpdateRequest): Map<String, Int> {
        val tenantId = tenantContext.getTenantId()
        var inventoryUpdates = 0
        var rateUpdates = 0
        
        val dates = request.startDate.datesUntil(request.endDate.plusDays(1)).toList()
        
        dates.forEach { date ->
            // Update Inventory
            val existingInv = inventoryRepository.find("roomType.id = ?1 and date = ?2 and tenantId = ?3", 
                request.roomTypeId, date, tenantId).firstResult()
            
            if (existingInv != null) {
                existingInv.totalCount = request.totalCount ?: existingInv.totalCount
                existingInv.stopSell = request.stopSell ?: existingInv.stopSell
            } else {
                val newInv = Inventory().apply {
                    this.tenantId = tenantId
                    this.roomType = RoomType.findById(request.roomTypeId) ?: throw NotFoundException("Room type not found")
                    this.date = date
                    this.totalCount = request.totalCount ?: 0
                    this.stopSell = request.stopSell ?: false
                }
                inventoryRepository.persist(newInv)
            }
            inventoryUpdates++
            
            // Update Nightly Rate
            if (request.rateCents != null && request.ratePlanId != null) {
                val existingRate = nightlyRateRepository.find("ratePlan.id = ?1 and roomType.id = ?2 and date = ?3 and tenantId = ?4",
                    request.ratePlanId, request.roomTypeId, date, tenantId).firstResult()
                
                if (existingRate != null) {
                    existingRate.rateCents = request.rateCents
                } else {
                    val newRate = NightlyRate().apply {
                        this.tenantId = tenantId
                        this.ratePlan = RatePlan.findById(request.ratePlanId) ?: throw NotFoundException("Rate plan not found")
                        this.roomType = RoomType.findById(request.roomTypeId) ?: throw NotFoundException("Room type not found")
                        this.date = date
                        this.rateCents = request.rateCents
                    }
                    nightlyRateRepository.persist(newRate)
                }
                rateUpdates++
            }
        }
        
        auditService.log("BULK_INVENTORY_UPDATE", "RoomType", request.roomTypeId, 
            "Updated range ${request.startDate} to ${request.endDate}. Counts: ${request.totalCount}, Price: ${request.rateCents}")
        
        return mapOf("inventory_updates" to inventoryUpdates, "rate_updates" to rateUpdates)
    }
}

data class BulkUpdateRequest(
    val roomTypeId: UUID,
    val ratePlanId: UUID?,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val totalCount: Int?,
    val stopSell: Boolean?,
    val rateCents: Int?
)
