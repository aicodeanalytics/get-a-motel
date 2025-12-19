package com.getamotel.service

import com.getamotel.domain.booking.Booking
import com.getamotel.domain.booking.BookingStatus
import com.getamotel.domain.property.Property
import com.getamotel.domain.property.RoomType
import com.getamotel.repository.PropertyRepository
import com.getamotel.repository.RoomTypeRepository
import com.getamotel.security.TenantContext
import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import java.util.*

@ApplicationScoped
class BookingRepository : PanacheRepository<Booking> {
    fun findByIdempotencyKey(key: String, tenantId: UUID): Booking? {
        return find("idempotencyKey = ?1 and tenantId = ?2", key, tenantId).firstResult()
    }
}

@ApplicationScoped
class BookingService(
    private val bookingRepository: BookingRepository,
    private val inventoryService: InventoryService,
    private val pricingService: PricingService,
    private val paymentService: PaymentService,
    private val auditService: AuditService,
    private val tenantContext: TenantContext,
    private val propertyRepository: PropertyRepository,
    private val roomTypeRepository: RoomTypeRepository
) {

    @Transactional
    fun createBooking(request: CreateBookingRequest): Booking {
        val tenantId = tenantContext.getTenantId()
        
        // 1. Idempotency Check
        val existing = bookingRepository.findByIdempotencyKey(request.idempotencyKey, tenantId)
        if (existing != null) return existing

        // 2. Load and Validate Entities
        val property = propertyRepository.findByIdAndTenant(request.propertyId, tenantId)
            ?: throw IllegalArgumentException("Property not found")
        val roomType = roomTypeRepository.find("id = ?1 and tenantId = ?2", request.roomTypeId, tenantId).firstResult()
            ?: throw IllegalArgumentException("Room type not found")

        // 3. Allocate Inventory (Atomic)
        val allocated = inventoryService.allocateRooms(
            request.roomTypeId, request.checkInDate, request.checkOutDate, 1
        )
        if (!allocated) throw IllegalStateException("Inventory no longer available")

        // 4. Calculate Price
        val totalPrice = pricingService.calculatePrice(
            request.propertyId, request.roomTypeId, request.checkInDate, request.checkOutDate
        )

        // 5. Create Booking Record
        val booking = Booking().apply {
            this.tenantId = tenantId
            this.property = property
            this.roomType = roomType
            this.idempotencyKey = request.idempotencyKey
            this.confirmationNumber = "GAM-${System.currentTimeMillis() % 1000000}" // Simple MVP logic
            this.guestEmail = request.guestEmail
            this.guestFirstName = request.guestFirstName
            this.guestLastName = request.guestLastName
            this.checkInDate = request.checkInDate
            this.checkOutDate = request.checkOutDate
            this.numNights = request.checkInDate.until(request.checkOutDate).days.toInt()
            this.numGuests = request.numGuests
            this.totalPriceCents = totalPrice
            this.status = BookingStatus.PENDING
        }

        bookingRepository.persist(booking)
        
        auditService.log("BOOKING_CREATED", "Booking", booking.id, "Total price: ${booking.totalPriceCents}")
        
        val clientSecret = paymentService.initiatePayment(booking)
        return BookingWithPayment(booking, clientSecret)
    }
}

data class BookingWithPayment(
    val booking: Booking,
    val clientSecret: String
)

data class CreateBookingRequest(
    val propertyId: UUID,
    val roomTypeId: UUID,
    val checkInDate: LocalDate,
    val checkOutDate: LocalDate,
    val numGuests: Int,
    val guestEmail: String,
    val guestFirstName: String,
    val guestLastName: String,
    val idempotencyKey: String
)
