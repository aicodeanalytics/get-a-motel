package com.getamotel.service

import com.getamotel.repository.NightlyRateRepository
import com.getamotel.repository.RatePlanRepository
import com.getamotel.security.TenantContext
import jakarta.enterprise.context.ApplicationScoped
import java.time.LocalDate
import java.util.*

@ApplicationScoped
class PricingService(
    private val nightlyRateRepository: NightlyRateRepository,
    private val ratePlanRepository: RatePlanRepository,
    private val tenantContext: TenantContext
) {

    fun calculatePrice(propertyId: UUID, roomTypeId: UUID, startDate: LocalDate, endDate: LocalDate, ratePlanId: UUID? = null): Long {
        val effectiveRatePlanId = ratePlanId ?: ratePlanRepository.findDefaultByProperty(propertyId, tenantContext.getTenantId())?.id
            ?: throw IllegalStateException("No rate plan specified and no default found")
        
        val rates = nightlyRateRepository.findByRatePlanAndRoomTypeAndDateRange(
            effectiveRatePlanId, roomTypeId, startDate, endDate, tenantContext.getTenantId()
        )
        
        val numNights = startDate.until(endDate).days.toInt()
        if (rates.size < numNights) {
            throw IllegalStateException("Missing nightly rates for the selected period")
        }
        
        return rates.sumOf { it.rateCents.toLong() }
    }
}
