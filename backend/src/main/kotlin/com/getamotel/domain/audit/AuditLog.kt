package com.getamotel.domain.audit

import com.getamotel.domain.TenantEntity
import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "audit_logs")
class AuditLog : TenantEntity() {

    @Column(nullable = false)
    lateinit var action: String

    @Column(name = "entity_type")
    var entityType: String? = null

    @Column(name = "entity_id")
    var entityId: UUID? = null

    @Column(columnDefinition = "TEXT")
    var details: String? = null

    @Column(name = "ip_address")
    var ipAddress: String? = null

    @Column(name = "user_agent")
    var userAgent: String? = null
}
