CREATE TABLE bookings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    property_id UUID NOT NULL REFERENCES properties(id),
    room_type_id UUID NOT NULL REFERENCES room_types(id),
    idempotency_key VARCHAR(255) NOT NULL,
    confirmation_number VARCHAR(50) NOT NULL UNIQUE,
    guest_email VARCHAR(255) NOT NULL,
    guest_phone VARCHAR(20),
    guest_first_name VARCHAR(100) NOT NULL,
    guest_last_name VARCHAR(100) NOT NULL,
    check_in_date DATE NOT NULL,
    check_out_date DATE NOT NULL,
    num_nights INT NOT NULL,
    num_guests INT NOT NULL,
    total_price_cents BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    special_requests TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    deleted_at TIMESTAMP,
    UNIQUE(tenant_id, idempotency_key)
);

CREATE INDEX idx_bookings_tenant ON bookings(tenant_id);
CREATE INDEX idx_bookings_confirmation ON bookings(confirmation_number);
CREATE INDEX idx_bookings_guest_email ON bookings(guest_email);
