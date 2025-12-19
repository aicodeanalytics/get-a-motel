package com.getamotel.domain.property

import com.getamotel.domain.TenantEntity
import jakarta.persistence.*
import java.time.LocalTime

@Entity
@Table(name = "properties")
class Property : TenantEntity() {

    @Column(nullable = false)
    lateinit var name: String

    @Column(nullable = false)
    lateinit var slug: String

    @Column(columnDefinition = "TEXT")
    var description: String? = null

    @Column(name = "address_line1", nullable = false)
    lateinit var addressLine1: String

    @Column(name = "address_line2")
    var addressLine2: String? = null

    @Column(nullable = false)
    lateinit var city: String

    @Column(nullable = false, length = 2)
    lateinit var state: String

    @Column(name = "zip_code", nullable = false, length = 10)
    lateinit var zipCode: String

    @Column(nullable = false, length = 2)
    var country: String = "US"

    var latitude: Double? = null
    var longitude: Double? = null

    var phone: String? = null
    var email: String? = null

    @Column(name = "check_in_time", nullable = false)
    var checkInTime: LocalTime = LocalTime.of(15, 0)

    @Column(name = "check_out_time", nullable = false)
    var checkOutTime: LocalTime = LocalTime.of(11, 0)

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    var status: PropertyStatus = PropertyStatus.ACTIVE
}

enum class PropertyStatus {
    ACTIVE, INACTIVE
}
