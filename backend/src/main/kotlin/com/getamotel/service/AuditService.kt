package com.getamotel.service

import com.getamotel.domain.audit.AuditLog
import com.getamotel.security.TenantContext
import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import java.util.*

@ApplicationScoped
class AuditLogRepository : PanacheRepository<AuditLog>

@ApplicationScoped
class AuditService(
    private val auditLogRepository: AuditLogRepository,
    private val tenantContext: TenantContext
) {

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    fun log(action: String, entityType: String? = null, entityId: UUID? = null, details: String? = null) {
        val log = AuditLog().apply {
            this.tenantId = tenantContext.getCheckTenantId() ?: UUID.fromString("00000000-0000-0000-0000-000000000000")
            this.action = action
            this.entityType = entityType
            this.entityId = entityId
            this.details = details
            this.createdBy = "system" // Future: Get from SecurityContext
        }
        auditLogRepository.persist(log)
    }
}
