# Data Model

## Schema Overview

All tables include multi-tenant isolation via `tenant_id` foreign key (except `tenants` table itself).

## Core Principles

1. **Multi-Tenancy**: Every table (except `tenants`) has `tenant_id` with NOT NULL constraint
2. **Soft Deletes**: Use `deleted_at` timestamp for soft deletes (audit trail)
3. **Optimistic Locking**: Use `version` column for concurrency control on critical tables
4. **Audit Trail**: Track `created_at`, `updated_at`, `created_by`, `updated_by`
5. **Idempotency**: Use `idempotency_key` for critical operations (bookings, payments)

## Tables

### 1. Tenants

```sql
CREATE TABLE tenants (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(100) NOT NULL UNIQUE,  -- URL-friendly identifier
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',  -- ACTIVE, SUSPENDED, DELETED
    settings JSONB,  -- Tenant-specific settings
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMP
);

CREATE INDEX idx_tenants_slug ON tenants(slug) WHERE deleted_at IS NULL;
CREATE INDEX idx_tenants_status ON tenants(status) WHERE deleted_at IS NULL;
```

**Invariants**:
- `slug` must be unique and URL-safe
- `status` must be one of: ACTIVE, SUSPENDED, DELETED

---

### 2. Tenant Users

```sql
CREATE TABLE tenant_users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    firebase_uid VARCHAR(255) NOT NULL,  -- Firebase Auth UID
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    role VARCHAR(50) NOT NULL,  -- OWNER, PROPERTY_ADMIN, FRONT_DESK
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',  -- ACTIVE, INACTIVE
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMP,
    
    UNIQUE(tenant_id, firebase_uid),
    UNIQUE(tenant_id, email)
);

CREATE INDEX idx_tenant_users_firebase_uid ON tenant_users(firebase_uid);
CREATE INDEX idx_tenant_users_tenant ON tenant_users(tenant_id) WHERE deleted_at IS NULL;
```

**Invariants**:
- `firebase_uid` must be unique per tenant
- `role` must be one of: OWNER, PROPERTY_ADMIN, FRONT_DESK

---

### 3. Properties

```sql
CREATE TABLE properties (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(100) NOT NULL,  -- URL-friendly identifier
    description TEXT,
    address_line1 VARCHAR(255) NOT NULL,
    address_line2 VARCHAR(255),
    city VARCHAR(100) NOT NULL,
    state VARCHAR(2) NOT NULL,  -- US state code
    zip_code VARCHAR(10) NOT NULL,
    country VARCHAR(2) NOT NULL DEFAULT 'US',
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    phone VARCHAR(20),
    email VARCHAR(255),
    check_in_time TIME NOT NULL DEFAULT '15:00:00',
    check_out_time TIME NOT NULL DEFAULT '11:00:00',
    cancellation_policy TEXT,
    amenities JSONB,  -- ["wifi", "parking", "pool"]
    images JSONB,  -- [{"url": "...", "caption": "..."}]
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',  -- ACTIVE, INACTIVE
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by UUID,
    updated_by UUID,
    deleted_at TIMESTAMP,
    
    UNIQUE(tenant_id, slug)
);

CREATE INDEX idx_properties_tenant ON properties(tenant_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_properties_location ON properties(city, state) WHERE deleted_at IS NULL;
CREATE INDEX idx_properties_geo ON properties USING GIST(ll_to_earth(latitude, longitude));
```

**Invariants**:
- `country` must be 'US' (MVP constraint)
- `state` must be valid US state code
- `check_in_time` < `check_out_time` (or handle next-day checkout)

---

### 4. Room Types

```sql
CREATE TABLE room_types (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    property_id UUID NOT NULL REFERENCES properties(id),
    name VARCHAR(255) NOT NULL,  -- "Standard Queen", "Deluxe King"
    description TEXT,
    bed_type VARCHAR(100),  -- "1 King", "2 Queens"
    max_occupancy INT NOT NULL,
    base_price_cents INT NOT NULL,  -- Base nightly rate in cents
    total_rooms INT NOT NULL,  -- Total inventory count
    size_sqft INT,
    amenities JSONB,  -- ["mini-fridge", "microwave"]
    images JSONB,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by UUID,
    updated_by UUID,
    deleted_at TIMESTAMP,
    
    UNIQUE(property_id, name)
);

CREATE INDEX idx_room_types_property ON room_types(property_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_room_types_tenant ON room_types(tenant_id) WHERE deleted_at IS NULL;
```

**Invariants**:
- `max_occupancy` > 0
- `base_price_cents` >= 0
- `total_rooms` > 0

---

### 5. Inventory (Daily Availability)

```sql
CREATE TABLE inventory (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    room_type_id UUID NOT NULL REFERENCES room_types(id),
    date DATE NOT NULL,
    total_count INT NOT NULL,  -- Total rooms available
    allocated_count INT NOT NULL DEFAULT 0,  -- Rooms allocated to bookings
    stop_sell BOOLEAN NOT NULL DEFAULT FALSE,  -- Block all sales for this date
    min_stay INT,  -- Minimum nights required
    max_stay INT,  -- Maximum nights allowed
    version INT NOT NULL DEFAULT 0,  -- Optimistic locking
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    
    UNIQUE(room_type_id, date),
    CHECK (allocated_count <= total_count),
    CHECK (allocated_count >= 0)
);

CREATE INDEX idx_inventory_room_type_date ON inventory(room_type_id, date);
CREATE INDEX idx_inventory_tenant ON inventory(tenant_id);
```

**Critical Invariants**:
- `allocated_count <= total_count` (enforced via CHECK constraint)
- `allocated_count >= 0`
- Optimistic locking via `version` column prevents oversell
- Updates must use: `UPDATE inventory SET allocated_count = allocated_count + X, version = version + 1 WHERE id = ? AND version = ?`

---

### 6. Rate Plans

```sql
CREATE TABLE rate_plans (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    property_id UUID NOT NULL REFERENCES properties(id),
    name VARCHAR(255) NOT NULL,  -- "Best Available Rate", "Corporate"
    description TEXT,
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by UUID,
    updated_by UUID,
    deleted_at TIMESTAMP,
    
    UNIQUE(property_id, name)
);

CREATE INDEX idx_rate_plans_property ON rate_plans(property_id) WHERE deleted_at IS NULL;
```

---

### 7. Nightly Rates

```sql
CREATE TABLE nightly_rates (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    rate_plan_id UUID NOT NULL REFERENCES rate_plans(id),
    room_type_id UUID NOT NULL REFERENCES room_types(id),
    date DATE NOT NULL,
    rate_cents INT NOT NULL,  -- Nightly rate in cents
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by UUID,
    updated_by UUID,
    
    UNIQUE(rate_plan_id, room_type_id, date)
);

CREATE INDEX idx_nightly_rates_room_type_date ON nightly_rates(room_type_id, date);
CREATE INDEX idx_nightly_rates_rate_plan ON nightly_rates(rate_plan_id);
```

**Invariants**:
- `rate_cents` >= 0

---

### 8. Bookings

```sql
CREATE TABLE bookings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    property_id UUID NOT NULL REFERENCES properties(id),
    room_type_id UUID NOT NULL REFERENCES room_types(id),
    idempotency_key VARCHAR(255) NOT NULL,  -- Client-generated idempotency key
    confirmation_number VARCHAR(50) NOT NULL UNIQUE,  -- Human-readable confirmation
    guest_email VARCHAR(255) NOT NULL,
    guest_phone VARCHAR(20),
    guest_first_name VARCHAR(100) NOT NULL,
    guest_last_name VARCHAR(100) NOT NULL,
    check_in_date DATE NOT NULL,
    check_out_date DATE NOT NULL,
    num_nights INT NOT NULL,
    num_guests INT NOT NULL,
    total_price_cents INT NOT NULL,  -- Total price including taxes/fees
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',  -- PENDING, CONFIRMED, CHECKED_IN, CHECKED_OUT, CANCELLED
    cancellation_reason TEXT,
    cancelled_at TIMESTAMP,
    special_requests TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by UUID,  -- Firebase UID or NULL for guest bookings
    
    UNIQUE(tenant_id, idempotency_key),
    CHECK (check_out_date > check_in_date),
    CHECK (num_nights > 0),
    CHECK (num_guests > 0),
    CHECK (total_price_cents >= 0)
);

CREATE INDEX idx_bookings_tenant ON bookings(tenant_id);
CREATE INDEX idx_bookings_property ON bookings(property_id);
CREATE INDEX idx_bookings_confirmation ON bookings(confirmation_number);
CREATE INDEX idx_bookings_guest_email ON bookings(guest_email);
CREATE INDEX idx_bookings_dates ON bookings(check_in_date, check_out_date);
CREATE INDEX idx_bookings_status ON bookings(status);
```

**Critical Invariants**:
- `idempotency_key` ensures duplicate booking prevention
- `confirmation_number` is unique and human-readable (e.g., "GAM-2024-001234")
- `check_out_date > check_in_date`
- `num_nights = check_out_date - check_in_date`
- Status transitions: PENDING → CONFIRMED → CHECKED_IN → CHECKED_OUT or CANCELLED

---

### 9. Booking Line Items

```sql
CREATE TABLE booking_line_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    booking_id UUID NOT NULL REFERENCES bookings(id),
    date DATE NOT NULL,  -- Specific night
    description VARCHAR(255) NOT NULL,  -- "Room rate - 2024-12-20"
    amount_cents INT NOT NULL,
    type VARCHAR(50) NOT NULL,  -- ROOM_RATE, TAX, FEE
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_booking_line_items_booking ON booking_line_items(booking_id);
```

**Invariants**:
- Sum of line items should equal `bookings.total_price_cents`

---

### 10. Payments

```sql
CREATE TABLE payments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    booking_id UUID NOT NULL REFERENCES bookings(id),
    idempotency_key VARCHAR(255) NOT NULL,
    stripe_payment_intent_id VARCHAR(255) NOT NULL UNIQUE,
    amount_cents INT NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    status VARCHAR(50) NOT NULL,  -- PENDING, AUTHORIZED, CAPTURED, REFUNDED, FAILED
    payment_method_type VARCHAR(50),  -- card, us_bank_account
    last4 VARCHAR(4),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    
    UNIQUE(tenant_id, idempotency_key)
);

CREATE INDEX idx_payments_booking ON payments(booking_id);
CREATE INDEX idx_payments_stripe_intent ON payments(stripe_payment_intent_id);
```

---

### 11. Payment Transactions

```sql
CREATE TABLE payment_transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    payment_id UUID NOT NULL REFERENCES payments(id),
    transaction_type VARCHAR(50) NOT NULL,  -- AUTHORIZE, CAPTURE, REFUND
    amount_cents INT NOT NULL,
    stripe_charge_id VARCHAR(255),
    stripe_refund_id VARCHAR(255),
    status VARCHAR(50) NOT NULL,  -- SUCCESS, FAILED
    error_message TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_payment_transactions_payment ON payment_transactions(payment_id);
```

---

### 12. Audit Logs

```sql
CREATE TABLE audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    entity_type VARCHAR(100) NOT NULL,  -- "Booking", "Inventory", "RatePlan"
    entity_id UUID NOT NULL,
    action VARCHAR(50) NOT NULL,  -- CREATE, UPDATE, DELETE
    user_id UUID,  -- Firebase UID
    changes JSONB,  -- {"field": {"old": "...", "new": "..."}}
    ip_address INET,
    user_agent TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_audit_logs_tenant ON audit_logs(tenant_id);
CREATE INDEX idx_audit_logs_entity ON audit_logs(entity_type, entity_id);
CREATE INDEX idx_audit_logs_created_at ON audit_logs(created_at);
```

**Retention**: 7 years (compliance)

---

## Critical Invariants Summary

### Inventory Consistency
1. **No Oversell**: `inventory.allocated_count <= inventory.total_count` (DB constraint)
2. **Atomic Allocation**: Booking creation + inventory allocation in single transaction
3. **Optimistic Locking**: Use `version` column to prevent race conditions
4. **Rollback on Failure**: If payment fails, release inventory allocation

### Idempotency
1. **Booking Idempotency**: `bookings.idempotency_key` prevents duplicate bookings
2. **Payment Idempotency**: `payments.idempotency_key` prevents duplicate charges
3. **Client-Generated Keys**: Client generates UUID for idempotency_key

### Multi-Tenancy
1. **Tenant Isolation**: Every query must filter by `tenant_id`
2. **Foreign Key Constraints**: All tenant_id references are enforced
3. **Row-Level Security**: Consider PostgreSQL RLS for additional safety (future)

### Data Integrity
1. **Soft Deletes**: Use `deleted_at` instead of hard deletes for audit trail
2. **Audit Trail**: All critical changes logged to `audit_logs`
3. **Price Consistency**: `booking_line_items` sum equals `bookings.total_price_cents`

## Indexes Strategy

- **Primary Keys**: All tables use UUID primary keys
- **Foreign Keys**: Indexed for join performance
- **Tenant Filtering**: `tenant_id` indexed on all tables
- **Date Ranges**: Composite indexes on date columns for availability queries
- **Unique Constraints**: Enforce business rules (e.g., confirmation_number)

## Future Enhancements

1. **Partitioning**: Partition `inventory` and `audit_logs` by date for performance
2. **Archival**: Move old bookings to archive tables after 2 years
3. **Read Replicas**: Use PostgreSQL read replicas for reporting queries
4. **Materialized Views**: Cache complex queries (e.g., property availability calendar)
