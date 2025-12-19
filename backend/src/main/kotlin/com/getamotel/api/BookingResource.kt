package com.getamotel.api

import com.getamotel.domain.booking.Booking
import com.getamotel.service.BookingService
import com.getamotel.service.CreateBookingRequest
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response

@Path("/bookings")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class BookingResource(
    private val bookingService: BookingService
) {

    @POST
    fun createBooking(request: CreateBookingRequest): Response {
        val booking = bookingService.createBooking(request)
        return Response.status(Response.Status.CREATED).entity(booking).build()
    }
}
