package com.getamotel.service

import com.getamotel.domain.property.Property
import com.getamotel.repository.PropertyRepository
import com.getamotel.security.TenantContext
import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import java.util.*

@ApplicationScoped
class PropertyService(
    private val propertyRepository: PropertyRepository,
    private val tenantContext: TenantContext
) {

    fun listProperties(): List<Property> {
        return propertyRepository.findByTenant(tenantContext.getTenantId())
    }

    fun getProperty(id: UUID): Property? {
        return propertyRepository.findByIdAndTenant(id, tenantContext.getTenantId())
    }

    @Transactional
    fun createProperty(property: Property): Property {
        property.tenantId = tenantContext.getTenantId()
        propertyRepository.persist(property)
        return property
    }

    @Transactional
    fun updateProperty(id: UUID, updated: Property): Property? {
        val existing = getProperty(id) ?: return null
        
        existing.name = updated.name
        existing.description = updated.description
        existing.addressLine1 = updated.addressLine1
        existing.addressLine2 = updated.addressLine2
        existing.city = updated.city
        existing.state = updated.state
        existing.zipCode = updated.zipCode
        existing.phone = updated.phone
        existing.email = updated.email
        existing.checkInTime = updated.checkInTime
        existing.checkOutTime = updated.checkOutTime
        existing.status = updated.status
        
        return existing
    }
}
