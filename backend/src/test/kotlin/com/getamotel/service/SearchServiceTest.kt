package com.getamotel.service

import com.getamotel.domain.inventory.Inventory
import com.getamotel.domain.property.Property
import com.getamotel.domain.property.RoomType
import com.getamotel.repository.PropertyRepository
import com.getamotel.repository.RoomTypeRepository
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.*

class SearchServiceTest {

    private lateinit var propertyRepository: PropertyRepository
    private lateinit var roomTypeRepository: RoomTypeRepository
    private lateinit var inventoryService: InventoryService
    private lateinit var pricingService: PricingService
    private lateinit var searchService: SearchService

    @BeforeEach
    fun setup() {
        propertyRepository = mockk()
        roomTypeRepository = mockk()
        inventoryService = mockk()
        pricingService = mockk()
        searchService = SearchService(propertyRepository, roomTypeRepository, inventoryService, pricingService)
    }

    @Test
    fun `test search returns available properties`() {
        val startDate = LocalDate.of(2024, 12, 20)
        val endDate = LocalDate.of(2024, 12, 21)
        val tenantId = UUID.randomUUID()
        val propId = UUID.randomUUID()
        val roomTypeId = UUID.randomUUID()

        val property = Property().apply { 
            id = propId
            this.tenantId = tenantId
            city = "Austin"
            state = "TX"
        }
        val roomType = RoomType().apply { 
            id = roomTypeId
            maxOccupancy = 2
        }
        val inventory = Inventory().apply { 
            totalCount = 5
            allocatedCount = 0
            stopSell = false
        }

        every { propertyRepository.find(any(), *anyVararg()) } returns mockk {
            every { list<Property>() } returns listOf(property)
        }
        every { roomTypeRepository.findByProperty(propId, tenantId) } returns listOf(roomType)
        every { inventoryService.getAvailability(roomTypeId, startDate, endDate) } returns listOf(inventory)
        every { pricingService.calculatePrice(propId, roomTypeId, startDate, endDate) } returns 10000L

        val results = searchService.searchProperties("Austin", "TX", startDate, endDate, 2)

        assertEquals(1, results.size)
        assertEquals(propId, results[0].property.id)
        assertEquals(10000L, results[0].lowestPriceCents)
    }

    @Test
    fun `test search filters out occupied rooms`() {
        val startDate = LocalDate.of(2024, 12, 20)
        val endDate = LocalDate.of(2024, 12, 21)
        val tenantId = UUID.randomUUID()
        val propId = UUID.randomUUID()
        val roomTypeId = UUID.randomUUID()

        val property = Property().apply { 
            id = propId
            this.tenantId = tenantId
        }
        val roomType = RoomType().apply { id = roomTypeId; maxOccupancy = 2 }
        val inventory = Inventory().apply { 
            totalCount = 5
            allocatedCount = 5 // Fully booked
        }

        every { propertyRepository.find(any(), any(), any(), any(), any()) } returns mockk {
            every { list<Property>() } returns listOf(property)
        }
        every { roomTypeRepository.findByProperty(propId, tenantId) } returns listOf(roomType)
        every { inventoryService.getAvailability(roomTypeId, startDate, endDate) } returns listOf(inventory)

        val results = searchService.searchProperties("Austin", "TX", startDate, endDate, 2)

        assertEquals(0, results.size)
    }
}
