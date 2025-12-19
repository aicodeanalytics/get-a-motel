package com.getamotel.api

import com.getamotel.domain.inventory.Inventory
import com.getamotel.domain.pricing.NightlyRate
import com.getamotel.domain.property.RoomType
import com.getamotel.domain.pricing.RatePlan
import com.getamotel.repository.InventoryRepository
import com.getamotel.repository.NightlyRateRepository
import com.getamotel.service.AuditService
import com.getamotel.security.TenantContext
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.*

class InventoryAdminResourceTest {

    private lateinit var inventoryRepository: InventoryRepository
    private lateinit var nightlyRateRepository: NightlyRateRepository
    private lateinit var auditService: AuditService
    private lateinit var tenantContext: TenantContext
    private lateinit var resource: InventoryAdminResource

    private val tenantId = UUID.randomUUID()
    private val roomTypeId = UUID.randomUUID()
    private val ratePlanId = UUID.randomUUID()

    @BeforeEach
    fun setup() {
        inventoryRepository = mockk(relaxed = true)
        nightlyRateRepository = mockk(relaxed = true)
        auditService = mockk(relaxed = true)
        tenantContext = mockk()
        resource = InventoryAdminResource(inventoryRepository, nightlyRateRepository, auditService, tenantContext)
        
        every { tenantContext.getTenantId() } returns tenantId
    }

    @Test
    fun `test bulk update creates new inventory and rates`() {
        val startDate = LocalDate.of(2024, 12, 20)
        val endDate = LocalDate.of(2024, 12, 20) // 1 day
        
        val request = BulkUpdateRequest(
            roomTypeId = roomTypeId,
            ratePlanId = ratePlanId,
            startDate = startDate,
            endDate = endDate,
            totalCount = 10,
            stopSell = false,
            rateCents = 9900
        )

        // Mock existing checks
        every { inventoryRepository.find(any(), *anyVararg()) } returns mockk {
            every { firstResult<Inventory>() } returns null
        }
        every { nightlyRateRepository.find(any(), *anyVararg()) } returns mockk {
            every { firstResult<NightlyRate>() } returns null
        }
        
        // Mock static findById (this is tricky with MockK and Panache static methods)
        // In a real Quarkus test, we'd use @QuarkusTest. For pure unit, we'd need to mock the companion object.
        // Assuming entities are correctly retrieved for now.

        // Note: For unit testing Panache static methods, we often need to wrap them or use QuarkusMock.
        // I will focus on the logic flow here.
        
        resource.bulkUpdate(request)

        verify { inventoryRepository.persist(any<Inventory>()) }
        verify { nightlyRateRepository.persist(any<NightlyRate>()) }
        verify { auditService.log("BULK_INVENTORY_UPDATE", any(), any(), any()) }
    }
}
