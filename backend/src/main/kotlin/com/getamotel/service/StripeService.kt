package com.getamotel.service

import com.stripe.Stripe
import com.stripe.model.PaymentIntent
import com.stripe.param.PaymentIntentCreateParams
import jakarta.annotation.PostConstruct
import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.config.inject.ConfigProperty
import java.util.*

@ApplicationScoped
class StripeService {

    @ConfigProperty(name = "stripe.api.key")
    lateinit var apiKey: String

    @PostConstruct
    fun init() {
        Stripe.apiKey = apiKey
    }

    fun createPaymentIntent(amountCents: Long, bookingId: UUID, tenantId: UUID): PaymentIntent {
        val params = PaymentIntentCreateParams.builder()
            .setAmount(amountCents)
            .setCurrency("usd")
            .putMetadata("booking_id", bookingId.toString())
            .putMetadata("tenant_id", tenantId.toString())
            .setCaptureMethod(PaymentIntentCreateParams.CaptureMethod.MANUAL) // Authorize now, capture at check-in
            .build()

        return PaymentIntent.create(params)
    }

    fun capturePayment(paymentIntentId: String): PaymentIntent {
        val intent = PaymentIntent.retrieve(paymentIntentId)
        return intent.capture()
    }
}
