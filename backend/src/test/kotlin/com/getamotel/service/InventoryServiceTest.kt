package com.getamotel.service

import com.getamotel.domain.inventory.Inventory
import com.getamotel.repository.InventoryRepository
import com.getamotel.security.TenantContext
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.*

class InventoryServiceTest {

    private lateinit var inventoryRepository: InventoryRepository
    private lateinit var tenantContext: TenantContext
    private lateinit var inventoryService: InventoryService

    private val tenantId = UUID.randomUUID()
    private val roomTypeId = UUID.randomUUID()

    @BeforeEach
    fun setup() {
        inventoryRepository = mockk()
        tenantContext = mockk()
        inventoryService = InventoryService(inventoryRepository, tenantContext)
        
        every { tenantContext.getTenantId() } returns tenantId
    }

    @Test
    fun `test successful room allocation`() {
        val startDate = LocalDate.of(2024, 12, 20)
        val endDate = LocalDate.of(2024, 12, 22)
        
        val inv1 = Inventory().apply { 
            totalCount = 10
            allocatedCount = 5
            date = startDate
        }
        val inv2 = Inventory().apply { 
            totalCount = 10
            allocatedCount = 5
            date = startDate.plusDays(1)
        }

        every { 
            inventoryRepository.findByRoomTypeAndDateRange(roomTypeId, startDate, endDate, tenantId) 
        } returns listOf(inv1, inv2)

        val result = inventoryService.allocateRooms(roomTypeId, startDate, endDate, 1)
        
        assertTrue(result)
        assertTrue(inv1.allocatedCount == 6)
        assertTrue(inv2.allocatedCount == 6)
    }

    @Test
    fun `test allocation fails on oversell`() {
        val startDate = LocalDate.of(2024, 12, 20)
        val endDate = LocalDate.of(2024, 12, 21)
        
        val inv1 = Inventory().apply { 
            totalCount = 10
            allocatedCount = 10
            date = startDate
        }

        every { 
            inventoryRepository.findByRoomTypeAndDateRange(roomTypeId, startDate, endDate, tenantId) 
        } returns listOf(inv1)

        val result = inventoryService.allocateRooms(roomTypeId, startDate, endDate, 1)
        
        assertFalse(result)
        assertTrue(inv1.allocatedCount == 10) // No change
    }

    @Test
    fun `test allocation fails on stop sell`() {
        val startDate = LocalDate.of(2024, 12, 20)
        val endDate = LocalDate.of(2024, 12, 21)
        
        val inv1 = Inventory().apply { 
            totalCount = 10
            allocatedCount = 0
            stopSell = true
            date = startDate
        }

        every { 
            inventoryRepository.findByRoomTypeAndDateRange(roomTypeId, startDate, endDate, tenantId) 
        } returns listOf(inv1)

        val result = inventoryService.allocateRooms(roomTypeId, startDate, endDate, 1)
        
        assertFalse(result)
    }
}
