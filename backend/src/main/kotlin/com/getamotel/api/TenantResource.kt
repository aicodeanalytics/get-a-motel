package com.getamotel.api

import com.getamotel.security.TenantContext
import jakarta.inject.Inject
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import java.util.UUID

@Path("/tenants")
class TenantResource {

    @Inject
    lateinit var tenantContext: TenantContext

    @GET
    @Path("/ping")
    @Produces(MediaType.APPLICATION_JSON)
    fun ping(): Map<String, Any> {
        val response = mutableMapOf<String, Any>(
            "status" to "OK",
            "message" to "Quarkus + Kotlin Backend is live",
            "timestamp" to System.currentTimeMillis()
        )
        
        if (tenantContext.hasTenantId()) {
            response["active_tenant"] = tenantContext.getTenantId()
        }
        
        return response
    }
}
