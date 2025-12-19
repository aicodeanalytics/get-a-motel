package com.getamotel.domain.payment

import com.getamotel.domain.TenantEntity
import com.getamotel.domain.booking.Booking
import jakarta.persistence.*

@Entity
@Table(name = "payments")
class Payment : TenantEntity() {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    lateinit var booking: Booking

    @Column(name = "stripe_payment_intent_id", nullable = false, unique = true)
    lateinit var stripePaymentIntentId: String

    @Column(name = "amount_cents", nullable = false)
    var amountCents: Long = 0

    @Column(name = "currency", nullable = false)
    var currency: String = "usd"

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    var status: PaymentStatus = PaymentStatus.PENDING
}

enum class PaymentStatus {
    PENDING, SUCCEEDED, REQUIRES_ACTION, FAILED, REFUNDED
}
