package com.getamotel.service

import com.getamotel.domain.audit.AuditLog
import com.getamotel.security.TenantContext
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

class AuditServiceTest {

    private lateinit var auditLogRepository: AuditLogRepository
    private lateinit var tenantContext: TenantContext
    private lateinit var auditService: AuditService

    private val tenantId = UUID.randomUUID()

    @BeforeEach
    fun setup() {
        auditLogRepository = mockk(relaxed = true)
        tenantContext = mockk()
        auditService = AuditService(auditLogRepository, tenantContext)
        
        every { tenantContext.getCheckTenantId() } returns tenantId
    }

    @Test
    fun `test log event persists correctly`() {
        val entityId = UUID.randomUUID()
        
        auditService.log("TEST_ACTION", "TestEntity", entityId, "Detail msg")

        verify { auditLogRepository.persist(match<AuditLog> {
            it.action == "TEST_ACTION" &&
            it.entityType == "TestEntity" &&
            it.entityId == entityId &&
            it.tenantId == tenantId
        }) }
    }
}
