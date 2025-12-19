package com.getamotel.domain.booking

import com.getamotel.domain.TenantEntity
import com.getamotel.domain.property.Property
import com.getamotel.domain.property.RoomType
import jakarta.persistence.*
import java.time.LocalDate
import java.util.*

@Entity
@Table(name = "bookings", uniqueConstraints = [
    UniqueConstraint(columnNames = ["tenant_id", "idempotency_key"])
])
class Booking : TenantEntity() {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false)
    lateinit var property: Property

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_type_id", nullable = false)
    lateinit var roomType: RoomType

    @Column(name = "idempotency_key", nullable = false)
    lateinit var idempotencyKey: String

    @Column(name = "confirmation_number", nullable = false, unique = true)
    lateinit var confirmationNumber: String

    @Column(name = "guest_email", nullable = false)
    lateinit var guestEmail: String

    @Column(name = "guest_phone")
    var guestPhone: String? = null

    @Column(name = "guest_first_name", nullable = false)
    lateinit var guestFirstName: String

    @Column(name = "guest_last_name", nullable = false)
    lateinit var guestLastName: String

    @Column(name = "check_in_date", nullable = false)
    lateinit var checkInDate: LocalDate

    @Column(name = "check_out_date", nullable = false)
    lateinit var checkOutDate: LocalDate

    @Column(name = "num_nights", nullable = false)
    var numNights: Int = 0

    @Column(name = "num_guests", nullable = false)
    var numGuests: Int = 0

    @Column(name = "total_price_cents", nullable = false)
    var totalPriceCents: Long = 0

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    var status: BookingStatus = BookingStatus.PENDING

    @Column(name = "special_requests", columnDefinition = "TEXT")
    var specialRequests: String? = null
}

enum class BookingStatus {
    PENDING, CONFIRMED, CHECKED_IN, CHECKED_OUT, CANCELLED
}
