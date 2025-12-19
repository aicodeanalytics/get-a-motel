package com.getamotel.domain.property

import com.getamotel.domain.TenantEntity
import jakarta.persistence.*

@Entity
@Table(name = "room_types")
class RoomType : TenantEntity() {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false)
    lateinit var property: Property

    @Column(nullable = false)
    lateinit var name: String

    @Column(columnDefinition = "TEXT")
    var description: String? = null

    @Column(name = "bed_type")
    var bedType: String? = null

    @Column(name = "max_occupancy", nullable = false)
    var maxOccupancy: Int = 1

    @Column(name = "base_price_cents", nullable = false)
    var basePriceCents: Int = 0

    @Column(name = "total_rooms", nullable = false)
    var totalRooms: Int = 0

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    var status: RoomTypeStatus = RoomTypeStatus.ACTIVE
}

enum class RoomTypeStatus {
    ACTIVE, INACTIVE
}
