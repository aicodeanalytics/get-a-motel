package com.getamotel.domain.inventory

import com.getamotel.domain.TenantEntity
import com.getamotel.domain.property.RoomType
import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name = "inventory", uniqueConstraints = [
    UniqueConstraint(columnNames = ["room_type_id", "date"])
])
class Inventory : TenantEntity() {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_type_id", nullable = false)
    lateinit var roomType: RoomType

    @Column(nullable = false)
    lateinit var date: LocalDate

    @Column(name = "total_count", nullable = false)
    var totalCount: Int = 0

    @Column(name = "allocated_count", nullable = false)
    var allocatedCount: Int = 0

    @Column(name = "stop_sell", nullable = false)
    var stopSell: Boolean = false

    @Column(name = "min_stay")
    var minStay: Int? = null

    @Column(name = "max_stay")
    var maxStay: Int? = null

    @Version
    var version: Long = 0
}
