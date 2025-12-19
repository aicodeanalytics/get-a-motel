# API Contract

## Base URL
- **Development**: `http://localhost:8080/api/v1`
- **Production**: `https://api.getamotel.com/api/v1`

## Authentication

All authenticated endpoints require a Firebase JWT token in the `Authorization` header:

```
Authorization: Bearer <firebase_jwt_token>
```

The backend verifies the token using Firebase Admin SDK and extracts:
- `firebase_uid` (user identifier)
- `email`
- Custom claims: `tenant_id`, `role`

## Headers

### Required for All Requests
```
Content-Type: application/json
```

### Required for Authenticated Requests
```
Authorization: Bearer <firebase_jwt_token>
X-Tenant-ID: <tenant_uuid>  # Validated against JWT claims
```

### Required for Idempotent Operations
```
Idempotency-Key: <client_generated_uuid>
```

## Error Response Format

```json
{
  "error": {
    "code": "INVENTORY_UNAVAILABLE",
    "message": "No rooms available for selected dates",
    "details": {
      "room_type_id": "123e4567-e89b-12d3-a456-426614174000",
      "check_in_date": "2024-12-20",
      "check_out_date": "2024-12-22"
    }
  },
  "timestamp": "2024-12-19T20:54:25Z",
  "path": "/api/v1/bookings"
}
```

## Endpoints

---

## Guest Flow

### 1. Search Properties

**Endpoint**: `GET /properties/search`

**Auth**: None (public)

**Query Parameters**:
```
location: string (city or zip code, required)
check_in: date (YYYY-MM-DD, required)
check_out: date (YYYY-MM-DD, required)
guests: integer (required, min: 1)
max_price: integer (optional, in cents)
amenities: string[] (optional, comma-separated)
```

**Example Request**:
```
GET /properties/search?location=Austin&check_in=2024-12-20&check_out=2024-12-22&guests=2
```

**Response** (200 OK):
```json
{
  "results": [
    {
      "property_id": "123e4567-e89b-12d3-a456-426614174000",
      "name": "Downtown Motel",
      "slug": "downtown-motel",
      "address": {
        "line1": "123 Main St",
        "city": "Austin",
        "state": "TX",
        "zip_code": "78701"
      },
      "location": {
        "latitude": 30.2672,
        "longitude": -97.7431
      },
      "amenities": ["wifi", "parking", "pool"],
      "images": [
        {
          "url": "https://cdn.getamotel.com/properties/123/main.jpg",
          "caption": "Front entrance"
        }
      ],
      "lowest_price_cents": 8900,
      "available_room_types": 3
    }
  ],
  "total_results": 15,
  "page": 1,
  "page_size": 20
}
```

---

### 2. Get Property Availability

**Endpoint**: `GET /properties/{property_id}/availability`

**Auth**: None (public)

**Path Parameters**:
- `property_id`: UUID

**Query Parameters**:
```
check_in: date (YYYY-MM-DD, required)
check_out: date (YYYY-MM-DD, required)
guests: integer (required)
rate_plan: string (optional, defaults to "BAR")
```

**Example Request**:
```
GET /properties/123e4567-e89b-12d3-a456-426614174000/availability?check_in=2024-12-20&check_out=2024-12-22&guests=2
```

**Response** (200 OK):
```json
{
  "property": {
    "id": "123e4567-e89b-12d3-a456-426614174000",
    "name": "Downtown Motel",
    "check_in_time": "15:00:00",
    "check_out_time": "11:00:00"
  },
  "check_in_date": "2024-12-20",
  "check_out_date": "2024-12-22",
  "num_nights": 2,
  "available_room_types": [
    {
      "room_type_id": "456e4567-e89b-12d3-a456-426614174000",
      "name": "Standard Queen",
      "description": "Comfortable room with queen bed",
      "bed_type": "1 Queen",
      "max_occupancy": 2,
      "size_sqft": 250,
      "amenities": ["mini-fridge", "microwave", "wifi"],
      "images": [
        {
          "url": "https://cdn.getamotel.com/rooms/456/main.jpg",
          "caption": "Room view"
        }
      ],
      "available_count": 5,
      "price_breakdown": {
        "nightly_rates": [
          {
            "date": "2024-12-20",
            "rate_cents": 8900
          },
          {
            "date": "2024-12-21",
            "rate_cents": 8900
          }
        ],
        "subtotal_cents": 17800,
        "taxes": [
          {
            "name": "State Tax",
            "rate": 0.0625,
            "amount_cents": 1113
          },
          {
            "name": "Local Tax",
            "rate": 0.02,
            "amount_cents": 356
          }
        ],
        "fees": [
          {
            "name": "Resort Fee",
            "amount_cents": 1000
          }
        ],
        "total_cents": 20269
      }
    }
  ]
}
```

---

### 3. Create Booking

**Endpoint**: `POST /bookings`

**Auth**: Optional (guest bookings allowed, but authenticated bookings get profile benefits)

**Headers**:
```
Idempotency-Key: <client_generated_uuid>
```

**Request Body**:
```json
{
  "property_id": "123e4567-e89b-12d3-a456-426614174000",
  "room_type_id": "456e4567-e89b-12d3-a456-426614174000",
  "check_in_date": "2024-12-20",
  "check_out_date": "2024-12-22",
  "num_guests": 2,
  "guest": {
    "email": "john.doe@example.com",
    "phone": "+15125551234",
    "first_name": "John",
    "last_name": "Doe"
  },
  "special_requests": "Late check-in expected",
  "rate_plan_id": "789e4567-e89b-12d3-a456-426614174000"
}
```

**Response** (201 Created):
```json
{
  "booking_id": "abc12345-e89b-12d3-a456-426614174000",
  "confirmation_number": "GAM-2024-001234",
  "status": "PENDING",
  "property": {
    "id": "123e4567-e89b-12d3-a456-426614174000",
    "name": "Downtown Motel"
  },
  "room_type": {
    "id": "456e4567-e89b-12d3-a456-426614174000",
    "name": "Standard Queen"
  },
  "check_in_date": "2024-12-20",
  "check_out_date": "2024-12-22",
  "num_nights": 2,
  "num_guests": 2,
  "guest": {
    "email": "john.doe@example.com",
    "first_name": "John",
    "last_name": "Doe"
  },
  "price_breakdown": {
    "subtotal_cents": 17800,
    "taxes_cents": 1469,
    "fees_cents": 1000,
    "total_cents": 20269
  },
  "payment_required": true,
  "payment_deadline": "2024-12-19T23:54:25Z",
  "created_at": "2024-12-19T20:54:25Z"
}
```

**Error Responses**:
- `400 Bad Request`: Invalid input (e.g., check_out <= check_in)
- `409 Conflict`: Duplicate idempotency key with different payload
- `422 Unprocessable Entity`: No availability for selected dates

---

### 4. Create Payment

**Endpoint**: `POST /bookings/{booking_id}/payments`

**Auth**: Optional (must match booking creator if authenticated)

**Headers**:
```
Idempotency-Key: <client_generated_uuid>
```

**Request Body**:
```json
{
  "payment_method_id": "pm_1234567890",  // Stripe PaymentMethod ID
  "amount_cents": 20269,
  "capture": false  // Authorize only, capture later
}
```

**Response** (201 Created):
```json
{
  "payment_id": "def12345-e89b-12d3-a456-426614174000",
  "stripe_payment_intent_id": "pi_1234567890",
  "status": "AUTHORIZED",
  "amount_cents": 20269,
  "currency": "USD",
  "client_secret": "pi_1234567890_secret_xyz",  // For 3D Secure
  "created_at": "2024-12-19T20:55:00Z"
}
```

---

### 5. Get Booking Details

**Endpoint**: `GET /bookings/{booking_id}`

**Auth**: Optional (public for confirmation lookup, authenticated for user's bookings)

**Query Parameters** (for guest lookup):
```
email: string (required if not authenticated)
confirmation_number: string (required if not authenticated)
```

**Response** (200 OK):
```json
{
  "booking_id": "abc12345-e89b-12d3-a456-426614174000",
  "confirmation_number": "GAM-2024-001234",
  "status": "CONFIRMED",
  "property": {
    "id": "123e4567-e89b-12d3-a456-426614174000",
    "name": "Downtown Motel",
    "address": "123 Main St, Austin, TX 78701",
    "phone": "+15125559999"
  },
  "room_type": {
    "name": "Standard Queen",
    "bed_type": "1 Queen"
  },
  "check_in_date": "2024-12-20",
  "check_out_date": "2024-12-22",
  "num_nights": 2,
  "num_guests": 2,
  "guest": {
    "email": "john.doe@example.com",
    "first_name": "John",
    "last_name": "Doe"
  },
  "total_price_cents": 20269,
  "payment_status": "CAPTURED",
  "cancellation_policy": "Free cancellation until 24 hours before check-in",
  "can_cancel": true,
  "created_at": "2024-12-19T20:54:25Z"
}
```

---

### 6. Cancel Booking

**Endpoint**: `POST /bookings/{booking_id}/cancel`

**Auth**: Optional (must match booking email or authenticated user)

**Request Body**:
```json
{
  "reason": "Change of plans",
  "email": "john.doe@example.com"  // Required if not authenticated
}
```

**Response** (200 OK):
```json
{
  "booking_id": "abc12345-e89b-12d3-a456-426614174000",
  "status": "CANCELLED",
  "refund": {
    "amount_cents": 20269,
    "status": "PENDING",
    "estimated_arrival": "2024-12-26"
  },
  "cancelled_at": "2024-12-19T21:00:00Z"
}
```

---

## Admin Flow

### 7. Create Property

**Endpoint**: `POST /admin/properties`

**Auth**: Required (OWNER or PROPERTY_ADMIN role)

**Request Body**:
```json
{
  "name": "Downtown Motel",
  "slug": "downtown-motel",
  "description": "Comfortable motel in downtown Austin",
  "address": {
    "line1": "123 Main St",
    "city": "Austin",
    "state": "TX",
    "zip_code": "78701"
  },
  "location": {
    "latitude": 30.2672,
    "longitude": -97.7431
  },
  "phone": "+15125559999",
  "email": "info@downtownmotel.com",
  "check_in_time": "15:00:00",
  "check_out_time": "11:00:00",
  "amenities": ["wifi", "parking", "pool"]
}
```

**Response** (201 Created):
```json
{
  "property_id": "123e4567-e89b-12d3-a456-426614174000",
  "name": "Downtown Motel",
  "slug": "downtown-motel",
  "status": "ACTIVE",
  "created_at": "2024-12-19T21:05:00Z"
}
```

---

### 8. Create Room Type

**Endpoint**: `POST /admin/properties/{property_id}/room-types`

**Auth**: Required (OWNER or PROPERTY_ADMIN role)

**Request Body**:
```json
{
  "name": "Standard Queen",
  "description": "Comfortable room with queen bed",
  "bed_type": "1 Queen",
  "max_occupancy": 2,
  "base_price_cents": 8900,
  "total_rooms": 20,
  "size_sqft": 250,
  "amenities": ["mini-fridge", "microwave", "wifi"]
}
```

**Response** (201 Created):
```json
{
  "room_type_id": "456e4567-e89b-12d3-a456-426614174000",
  "name": "Standard Queen",
  "status": "ACTIVE",
  "created_at": "2024-12-19T21:10:00Z"
}
```

---

### 9. Set Nightly Rates

**Endpoint**: `POST /admin/room-types/{room_type_id}/rates`

**Auth**: Required (OWNER or PROPERTY_ADMIN role)

**Request Body**:
```json
{
  "rate_plan_id": "789e4567-e89b-12d3-a456-426614174000",
  "date_range": {
    "start_date": "2024-12-20",
    "end_date": "2024-12-31"
  },
  "rate_cents": 9900,
  "days_of_week": ["FRI", "SAT"]  // Optional: only apply to specific days
}
```

**Response** (200 OK):
```json
{
  "rates_created": 4,
  "date_range": {
    "start_date": "2024-12-20",
    "end_date": "2024-12-31"
  },
  "affected_dates": ["2024-12-20", "2024-12-21", "2024-12-27", "2024-12-28"]
}
```

---

### 10. Update Inventory

**Endpoint**: `PUT /admin/room-types/{room_type_id}/inventory`

**Auth**: Required (OWNER or PROPERTY_ADMIN role)

**Request Body**:
```json
{
  "date_range": {
    "start_date": "2024-12-20",
    "end_date": "2024-12-31"
  },
  "total_count": 18,  // Reduce available rooms
  "stop_sell": false,
  "min_stay": 2,
  "max_stay": 7
}
```

**Response** (200 OK):
```json
{
  "inventory_updated": 12,
  "date_range": {
    "start_date": "2024-12-20",
    "end_date": "2024-12-31"
  }
}
```

---

### 11. List Reservations

**Endpoint**: `GET /admin/properties/{property_id}/reservations`

**Auth**: Required (any admin role)

**Query Parameters**:
```
start_date: date (optional, defaults to today)
end_date: date (optional, defaults to +30 days)
status: string (optional, PENDING|CONFIRMED|CHECKED_IN|CHECKED_OUT|CANCELLED)
page: integer (optional, defaults to 1)
page_size: integer (optional, defaults to 50, max 100)
```

**Response** (200 OK):
```json
{
  "reservations": [
    {
      "booking_id": "abc12345-e89b-12d3-a456-426614174000",
      "confirmation_number": "GAM-2024-001234",
      "status": "CONFIRMED",
      "guest_name": "John Doe",
      "guest_email": "john.doe@example.com",
      "room_type": "Standard Queen",
      "check_in_date": "2024-12-20",
      "check_out_date": "2024-12-22",
      "num_nights": 2,
      "num_guests": 2,
      "total_price_cents": 20269,
      "created_at": "2024-12-19T20:54:25Z"
    }
  ],
  "total_results": 150,
  "page": 1,
  "page_size": 50
}
```

---

## Webhooks

### Stripe Webhook

**Endpoint**: `POST /webhooks/stripe`

**Auth**: Stripe signature verification

**Events Handled**:
- `payment_intent.succeeded`
- `payment_intent.payment_failed`
- `charge.refunded`

**Response**: `200 OK` (acknowledge receipt)

---

## Rate Limiting

- **Guest (unauthenticated)**: 100 requests/minute per IP
- **Authenticated**: 1000 requests/minute per user
- **Admin**: 2000 requests/minute per user

**Rate Limit Headers**:
```
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 95
X-RateLimit-Reset: 1703024400
```

**Rate Limit Exceeded** (429 Too Many Requests):
```json
{
  "error": {
    "code": "RATE_LIMIT_EXCEEDED",
    "message": "Too many requests. Please try again later.",
    "retry_after": 60
  }
}
```

---

## Idempotency

For `POST` endpoints that create resources (bookings, payments), clients **MUST** provide an `Idempotency-Key` header with a unique UUID.

**Behavior**:
1. **First Request**: Process normally, store idempotency key + response
2. **Duplicate Key (same payload)**: Return cached response (200 OK)
3. **Duplicate Key (different payload)**: Return 409 Conflict

**Idempotency Key Storage**: 24 hours

---

## Pagination

List endpoints support pagination via query parameters:

```
page: integer (1-indexed, defaults to 1)
page_size: integer (defaults to 20, max 100)
```

**Response Metadata**:
```json
{
  "results": [...],
  "total_results": 150,
  "page": 1,
  "page_size": 20,
  "total_pages": 8
}
```

---

## OpenAPI Specification

Full OpenAPI 3.0 spec available at:
- **Development**: `http://localhost:8080/q/openapi`
- **Swagger UI**: `http://localhost:8080/q/swagger-ui`
