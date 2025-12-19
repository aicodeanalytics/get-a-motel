package com.getamotel.domain.pricing

import com.getamotel.domain.TenantEntity
import com.getamotel.domain.property.RoomType
import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name = "nightly_rates", uniqueConstraints = [
    UniqueConstraint(columnNames = ["rate_plan_id", "room_type_id", "date"])
])
class NightlyRate : TenantEntity() {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rate_plan_id", nullable = false)
    lateinit var ratePlan: RatePlan

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_type_id", nullable = false)
    lateinit var roomType: RoomType

    @Column(nullable = false)
    lateinit var date: LocalDate

    @Column(name = "rate_cents", nullable = false)
    var rateCents: Int = 0
}
