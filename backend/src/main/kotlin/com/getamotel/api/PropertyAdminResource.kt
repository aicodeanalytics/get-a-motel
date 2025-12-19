package com.getamotel.api

import com.getamotel.domain.property.Property
import com.getamotel.service.PropertyService
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import java.util.*

@Path("/admin/properties")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class PropertyAdminResource(
    private val propertyService: PropertyService
) {

    @GET
    fun listProperties(): List<Property> {
        return propertyService.listProperties()
    }

    @GET
    @Path("/{id}")
    fun getProperty(@PathParam("id") id: UUID): Property {
        return propertyService.getProperty(id) 
            ?: throw NotFoundException("Property not found")
    }

    @POST
    fun createProperty(property: Property): Response {
        val created = propertyService.createProperty(property)
        return Response.status(Response.Status.CREATED).entity(created).build()
    }

    @PUT
    @Path("/{id}")
    fun updateProperty(@PathParam("id") id: UUID, property: Property): Property {
        return propertyService.updateProperty(id, property)
            ?: throw NotFoundException("Property not found")
    }
}
