CREATE TABLE inventory (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    room_type_id UUID NOT NULL REFERENCES room_types(id),
    date DATE NOT NULL,
    total_count INT NOT NULL,
    allocated_count INT NOT NULL DEFAULT 0,
    stop_sell BOOLEAN NOT NULL DEFAULT FALSE,
    min_stay INT,
    max_stay INT,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    deleted_at TIMESTAMP,
    UNIQUE(room_type_id, date),
    CHECK (allocated_count <= total_count),
    CHECK (allocated_count >= 0)
);

CREATE TABLE rate_plans (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    property_id UUID NOT NULL REFERENCES properties(id),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    deleted_at TIMESTAMP,
    UNIQUE(property_id, name)
);

CREATE TABLE nightly_rates (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    rate_plan_id UUID NOT NULL REFERENCES rate_plans(id),
    room_type_id UUID NOT NULL REFERENCES room_types(id),
    date DATE NOT NULL,
    rate_cents INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    deleted_at TIMESTAMP,
    UNIQUE(rate_plan_id, room_type_id, date)
);

CREATE INDEX idx_inventory_date ON inventory(date);
CREATE INDEX idx_nightly_rates_date ON nightly_rates(date);
