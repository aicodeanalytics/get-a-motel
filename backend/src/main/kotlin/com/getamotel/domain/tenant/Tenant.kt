package com.getamotel.domain.tenant

import io.quarkus.hibernate.orm.panache.kotlin.PanacheEntityBase
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "tenants")
class Tenant : PanacheEntityBase {

    @Id
    @GeneratedValue
    var id: UUID? = null

    @Column(nullable = false)
    lateinit var name: String

    @Column(nullable = false, unique = true)
    lateinit var slug: String

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: TenantStatus = TenantStatus.ACTIVE

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    lateinit var createdAt: LocalDateTime

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    lateinit var updatedAt: LocalDateTime

    @Column(name = "deleted_at")
    var deletedAt: LocalDateTime? = null
}

enum class TenantStatus {
    ACTIVE, SUSPENDED, DELETED
}
