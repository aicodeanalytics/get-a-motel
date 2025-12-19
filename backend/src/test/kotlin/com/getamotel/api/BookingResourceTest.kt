package com.getamotel.api

import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.*

@QuarkusTest
class BookingResourceTest {

    @Test
    fun `test booking creation requires tenant header`() {
        val request = mapOf(
            "propertyId" to UUID.randomUUID().toString(),
            "roomTypeId" to UUID.randomUUID().toString(),
            "checkInDate" to "2024-12-20",
            "checkOutDate" to "2024-12-22",
            "numGuests" to 2,
            "guestEmail" to "john@example.com",
            "guestFirstName" to "John",
            "guestLastName" to "Doe",
            "idempotencyKey" to UUID.randomUUID().toString()
        )

        given()
            .contentType(ContentType.JSON)
            .body(request)
            .`when`().post("/bookings")
            .then()
            .statusCode(400) // Rejection due to missing X-Tenant-ID
    }
}
