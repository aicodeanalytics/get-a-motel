package com.getamotel.api

import com.getamotel.service.PropertySearchResult
import com.getamotel.service.SearchService
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import java.time.LocalDate

@Path("/properties")
@Produces(MediaType.APPLICATION_JSON)
class PropertySearchResource(
    private val searchService: SearchService
) {

    @GET
    @Path("/search")
    fun search(
        @QueryParam("city") city: String?,
        @QueryParam("state") state: String?,
        @QueryParam("check_in") checkIn: String,
        @QueryParam("check_out") checkOut: String,
        @QueryParam("guests") @DefaultValue("1") guests: Int
    ): List<PropertySearchResult> {
        val startDate = LocalDate.parse(checkIn)
        val endDate = LocalDate.parse(checkOut)
        
        require(endDate.isAfter(startDate)) { "Check-out must be after check-in" }
        
        return searchService.searchProperties(city, state, startDate, endDate, guests)
    }
}
