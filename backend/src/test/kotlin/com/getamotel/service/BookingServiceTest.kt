package com.getamotel.service

import com.getamotel.domain.booking.Booking
import com.getamotel.domain.property.Property
import com.getamotel.domain.property.RoomType
import com.getamotel.repository.PropertyRepository
import com.getamotel.repository.RoomTypeRepository
import com.getamotel.security.TenantContext
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate
import java.util.*

class BookingServiceTest {

    private lateinit var bookingRepository: BookingRepository
    private lateinit var inventoryService: InventoryService
    private lateinit var pricingService: PricingService
    private lateinit var tenantContext: TenantContext
    private lateinit var propertyRepository: PropertyRepository
    private lateinit var roomTypeRepository: RoomTypeRepository
    private lateinit var bookingService: BookingService

    private val tenantId = UUID.randomUUID()

    @BeforeEach
    fun setup() {
        bookingRepository = mockk(relaxed = true)
        inventoryService = mockk()
        pricingService = mockk()
        tenantContext = mockk()
        propertyRepository = mockk()
        roomTypeRepository = mockk()
        
        bookingService = BookingService(
            bookingRepository, inventoryService, pricingService, 
            tenantContext, propertyRepository, roomTypeRepository
        )
        
        every { tenantContext.getTenantId() } returns tenantId
    }

    @Test
    fun `test successful booking creation`() {
        val request = CreateBookingRequest(
            propertyId = UUID.randomUUID(),
            roomTypeId = UUID.randomUUID(),
            checkInDate = LocalDate.of(2024, 12, 20),
            checkOutDate = LocalDate.of(2024, 12, 22),
            numGuests = 2,
            guestEmail = "test@example.com",
            guestFirstName = "John",
            guestLastName = "Doe",
            idempotencyKey = "key-123"
        )

        every { bookingRepository.findByIdempotencyKey("key-123", tenantId) } returns null
        every { propertyRepository.findByIdAndTenant(request.propertyId, tenantId) } returns Property()
        every { roomTypeRepository.find(any(), *anyVararg()) } returns mockk {
            every { firstResult() } returns RoomType()
        }
        every { inventoryService.allocateRooms(any(), any(), any(), any()) } returns true
        every { pricingService.calculatePrice(any(), any(), any(), any()) } returns 20000L

        val booking = bookingService.createBooking(request)

        assertEquals("test@example.com", booking.guestEmail)
        assertEquals(20000L, booking.totalPriceCents)
        verify { bookingRepository.persist(any<Booking>()) }
    }

    @Test
    fun `test booking fails if inventory unavailable`() {
        val request = CreateBookingRequest(
            propertyId = UUID.randomUUID(),
            roomTypeId = UUID.randomUUID(),
            checkInDate = LocalDate.of(2024, 12, 20),
            checkOutDate = LocalDate.of(2024, 12, 22),
            numGuests = 2,
            guestEmail = "test@example.com",
            guestFirstName = "John",
            guestLastName = "Doe",
            idempotencyKey = "key-123"
        )

        every { bookingRepository.findByIdempotencyKey("key-123", tenantId) } returns null
        every { propertyRepository.findByIdAndTenant(any(), any()) } returns Property()
        every { roomTypeRepository.find(any(), *anyVararg()) } returns mockk {
            every { firstResult() } returns RoomType()
        }
        every { inventoryService.allocateRooms(any(), any(), any(), any()) } returns false

        assertThrows<IllegalStateException> {
            bookingService.createBooking(request)
        }
    }

    @Test
    fun `test idempotency returns existing booking`() {
        val request = CreateBookingRequest(
            propertyId = UUID.randomUUID(),
            roomTypeId = UUID.randomUUID(),
            checkInDate = LocalDate.of(2024, 12, 20),
            checkOutDate = LocalDate.of(2024, 12, 22),
            numGuests = 2,
            guestEmail = "test@example.com",
            guestFirstName = "John",
            guestLastName = "Doe",
            idempotencyKey = "key-123"
        )
        val existingBooking = Booking().apply { guestEmail = "existing@example.com" }

        every { bookingRepository.findByIdempotencyKey("key-123", tenantId) } returns existingBooking

        val result = bookingService.createBooking(request)

        assertEquals("existing@example.com", result.guestEmail)
        verify(exactly = 0) { inventoryService.allocateRooms(any(), any(), any(), any()) }
    }
}
