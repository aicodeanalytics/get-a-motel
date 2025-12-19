package com.getamotel.api

import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import org.hamcrest.CoreMatchers.`is`
import org.junit.jupiter.api.Test
import java.util.*

@QuarkusTest
class PropertyAdminResourceTest {

    @Test
    fun `test unauthorized access`() {
        given()
            .`when`().get("/admin/properties")
            .then()
            .statusCode(400) // Fails due to missing X-Tenant-ID in our current filter
    }

    @Test
    fun `test list properties with tenant header`() {
        val tenantId = UUID.randomUUID().toString()
        
        given()
            .header("X-Tenant-ID", tenantId)
            .`when`().get("/admin/properties")
            .then()
            .statusCode(200)
            .body("size()", `is`(0))
    }
}
