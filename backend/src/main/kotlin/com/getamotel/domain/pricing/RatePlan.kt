package com.getamotel.domain.pricing

import com.getamotel.domain.TenantEntity
import com.getamotel.domain.property.Property
import jakarta.persistence.*

@Entity
@Table(name = "rate_plans")
class RatePlan : TenantEntity() {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false)
    lateinit var property: Property

    @Column(nullable = false)
    lateinit var name: String

    @Column(columnDefinition = "TEXT")
    var description: String? = null

    @Column(name = "is_default", nullable = false)
    var isDefault: Boolean = false

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    var status: RatePlanStatus = RatePlanStatus.ACTIVE
}

enum class RatePlanStatus {
    ACTIVE, INACTIVE
}
