package com.getamotel.domain

import io.quarkus.hibernate.orm.panache.kotlin.PanacheEntityBase
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime
import java.util.*

@MappedSuperclass
abstract class TenantEntity : PanacheEntityBase {

    @Id
    @GeneratedValue
    var id: UUID? = null

    @Column(name = "tenant_id", nullable = false, updatable = false)
    lateinit var tenantId: UUID

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    lateinit var createdAt: LocalDateTime

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    lateinit var updatedAt: LocalDateTime

    @Column(name = "created_by")
    var createdBy: String? = null

    @Column(name = "updated_by")
    var updatedBy: String? = null

    @Column(name = "deleted_at")
    var deletedAt: LocalDateTime? = null
}
