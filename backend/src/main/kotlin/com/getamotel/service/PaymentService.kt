package com.getamotel.service

import com.getamotel.domain.payment.Payment
import com.getamotel.domain.payment.PaymentStatus
import com.getamotel.domain.booking.Booking
import com.getamotel.security.TenantContext
import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import java.util.*

@ApplicationScoped
class PaymentRepository : PanacheRepository<Payment>

@ApplicationScoped
class PaymentService(
    private val stripeService: StripeService,
    private val paymentRepository: PaymentRepository,
    private val tenantContext: TenantContext
) {

    @Transactional
    fun initiatePayment(booking: Booking): String {
        val intent = stripeService.createPaymentIntent(
            booking.totalPriceCents,
            booking.id!!,
            tenantContext.getTenantId()
        )

        val payment = Payment().apply {
            this.tenantId = tenantContext.getTenantId()
            this.booking = booking
            this.stripePaymentIntentId = intent.id
            this.amountCents = booking.totalPriceCents
            this.status = PaymentStatus.PENDING
        }

        paymentRepository.persist(payment)
        return intent.clientSecret
    }
}
