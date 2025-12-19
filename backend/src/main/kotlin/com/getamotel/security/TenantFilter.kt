package com.getamotel.security

import jakarta.annotation.Priority
import jakarta.inject.Inject
import jakarta.ws.rs.Priorities
import jakarta.ws.rs.container.ContainerRequestContext
import jakarta.ws.rs.container.ContainerRequestFilter
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.ext.Provider
import org.eclipse.microprofile.jwt.JsonWebToken
import org.jboss.logging.Logger
import java.util.UUID

@Provider
@Priority(Priorities.AUTHENTICATION + 1)
class TenantFilter : ContainerRequestFilter {

    private val log = Logger.getLogger(TenantFilter::class.java)

    @Inject
    lateinit var jwt: JsonWebToken

    @Inject
    lateinit var tenantContext: TenantContext

    override fun filter(requestContext: ContainerRequestContext) {
        val path = requestContext.uriInfo.path
        
        // Skip tenant check for public endpoints (search, health, etc.)
        if (isPublicPath(path)) {
            return
        }

        // 1. Try to get tenant_id from JWT custom claim
        val jwtTenantId = jwt.getClaim<String>("tenant_id")
        
        // 2. Fallback/Verification: Check X-Tenant-ID header
        val headerTenantId = requestContext.getHeaderString("X-Tenant-ID")

        if (jwtTenantId == null && headerTenantId == null) {
            log.warn("Missing tenant identifier for path: $path")
            requestContext.abortWith(
                Response.status(Response.Status.BAD_REQUEST)
                    .entity(mapOf("error" to "Missing X-Tenant-ID header or tenant claim")).build()
            )
            return
        }

        val effectiveTenantId = jwtTenantId ?: headerTenantId

        try {
            tenantContext.setTenantId(UUID.fromString(effectiveTenantId))
            log.debug("Set tenant context: $effectiveTenantId")
        } catch (e: IllegalArgumentException) {
            log.error("Invalid UUID format for tenant: $effectiveTenantId")
            requestContext.abortWith(
                Response.status(Response.Status.BAD_REQUEST)
                    .entity(mapOf("error" to "Invalid tenant ID format")).build()
            )
        }
    }

    private fun isPublicPath(path: String): Boolean {
        val publicPrefixes = listOf(
            "properties/search",
            "properties/", // and availability
            "health",
            "openapi",
            "swagger-ui"
        )
        return publicPrefixes.any { path.contains(it) }
    }
}
