package com.getamotel.service

import com.getamotel.domain.pricing.NightlyRate
import com.getamotel.domain.pricing.RatePlan
import com.getamotel.repository.NightlyRateRepository
import com.getamotel.repository.RatePlanRepository
import com.getamotel.security.TenantContext
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate
import java.util.*

class PricingServiceTest {

    private lateinit var nightlyRateRepository: NightlyRateRepository
    private lateinit var ratePlanRepository: RatePlanRepository
    private lateinit var tenantContext: TenantContext
    private lateinit var pricingService: PricingService

    private val tenantId = UUID.randomUUID()
    private val propertyId = UUID.randomUUID()
    private val roomTypeId = UUID.randomUUID()
    private val ratePlanId = UUID.randomUUID()

    @BeforeEach
    fun setup() {
        nightlyRateRepository = mockk()
        ratePlanRepository = mockk()
        tenantContext = mockk()
        pricingService = PricingService(nightlyRateRepository, ratePlanRepository, tenantContext)
        
        every { tenantContext.getTenantId() } returns tenantId
    }

    @Test
    fun `test price calculation with multiple nights`() {
        val startDate = LocalDate.of(2024, 12, 20)
        val endDate = LocalDate.of(2024, 12, 22) // 2 nights
        
        val rate1 = NightlyRate().apply { rateCents = 10000; date = startDate }
        val rate2 = NightlyRate().apply { rateCents = 12000; date = startDate.plusDays(1) }

        every { 
            nightlyRateRepository.findByRatePlanAndRoomTypeAndDateRange(ratePlanId, roomTypeId, startDate, endDate, tenantId) 
        } returns listOf(rate1, rate2)

        val total = pricingService.calculatePrice(propertyId, roomTypeId, startDate, endDate, ratePlanId)
        
        assertEquals(22000L, total)
    }

    @Test
    fun `test calculation fails on missing rates`() {
        val startDate = LocalDate.of(2024, 12, 20)
        val endDate = LocalDate.of(2024, 12, 22) // 2 nights
        
        val rate1 = NightlyRate().apply { rateCents = 10000; date = startDate }

        every { 
            nightlyRateRepository.findByRatePlanAndRoomTypeAndDateRange(ratePlanId, roomTypeId, startDate, endDate, tenantId) 
        } returns listOf(rate1) // Missing second night

        assertThrows<IllegalStateException> {
            pricingService.calculatePrice(propertyId, roomTypeId, startDate, endDate, ratePlanId)
        }
    }

    @Test
    fun `test fallback to default rate plan`() {
        val startDate = LocalDate.of(2024, 12, 20)
        val endDate = LocalDate.of(2024, 12, 21)
        
        val defaultPlan = RatePlan().apply { id = ratePlanId }
        val rate = NightlyRate().apply { rateCents = 10000; date = startDate }

        every { 
            ratePlanRepository.findDefaultByProperty(propertyId, tenantId) 
        } returns defaultPlan
        
        every { 
            nightlyRateRepository.findByRatePlanAndRoomTypeAndDateRange(ratePlanId, roomTypeId, startDate, endDate, tenantId) 
        } returns listOf(rate)

        val total = pricingService.calculatePrice(propertyId, roomTypeId, startDate, endDate, null)
        
        assertEquals(10000L, total)
    }
}
