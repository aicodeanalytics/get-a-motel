package com.getamotel.service

import com.getamotel.domain.booking.Booking
import com.getamotel.domain.payment.Payment
import com.getamotel.domain.payment.PaymentStatus
import com.getamotel.security.TenantContext
import com.stripe.model.PaymentIntent
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

class PaymentServiceTest {

    private lateinit var stripeService: StripeService
    private lateinit var paymentRepository: PaymentRepository
    private lateinit var tenantContext: TenantContext
    private lateinit var paymentService: PaymentService

    private val tenantId = UUID.randomUUID()

    @BeforeEach
    fun setup() {
        stripeService = mockk()
        paymentRepository = mockk(relaxed = true)
        tenantContext = mockk()
        paymentService = PaymentService(stripeService, paymentRepository, tenantContext)
        
        every { tenantContext.getTenantId() } returns tenantId
    }

    @Test
    fun `test initiate payment creates stripe intent and records it`() {
        val booking = Booking().apply { 
            id = UUID.randomUUID()
            totalPriceCents = 15000L
        }
        val mockIntent = mockk<PaymentIntent>()
        every { mockIntent.id } returns "pi_123"
        every { mockIntent.clientSecret } returns "secret_123"

        every { 
            stripeService.createPaymentIntent(15000L, booking.id!!, tenantId) 
        } returns mockIntent

        val secret = paymentService.initiatePayment(booking)

        assertEquals("secret_123", secret)
        verify { paymentRepository.persist(any<Payment>()) }
    }
}
