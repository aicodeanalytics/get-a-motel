# Threat Model & Abuse Controls

## Threat Categories

1. **SMS Abuse** (High Priority - Cost Impact)
2. **Booking Fraud** (High Priority - Revenue Impact)
3. **Inventory Manipulation** (High Priority - Operational Impact)
4. **Payment Fraud** (High Priority - Financial Impact)
5. **Data Breaches** (High Priority - Compliance Impact)
6. **API Abuse** (Medium Priority - Performance Impact)
7. **Account Takeover** (Medium Priority - Trust Impact)

---

## 1. SMS Abuse

### Threat Scenarios

**T1.1: SMS Bombing**
- **Description**: Attacker repeatedly requests SMS verification codes to drain budget
- **Impact**: High cost overruns ($0.05-0.10 per SMS)
- **Likelihood**: High (automated bots)

**T1.2: International SMS Fraud**
- **Description**: Attacker bypasses US-only restriction to send expensive international SMS
- **Impact**: Very high cost ($0.50+ per SMS)
- **Likelihood**: Medium (requires bypassing country code validation)

**T1.3: SMS Resale**
- **Description**: Attacker uses platform to send SMS for other purposes (OTP resale)
- **Impact**: Medium cost + reputation damage
- **Likelihood**: Low (requires significant effort)

### Mitigation Controls

| Control | Implementation | Priority |
|---------|---------------|----------|
| **US-Only Allowlist** | Firebase Auth: Restrict SMS to US country code (+1) only | **CRITICAL** |
| **reCAPTCHA Enterprise** | Require reCAPTCHA v3 score ≥ 0.5 for SMS requests | **CRITICAL** |
| **Rate Limiting** | Max 3 SMS per phone number per hour | **CRITICAL** |
| **Cooldown Period** | Min 60 seconds between SMS to same number | **CRITICAL** |
| **IP-Based Throttling** | Max 10 SMS requests per IP per hour | **HIGH** |
| **Phone Number Validation** | Validate US phone format (E.164: +1XXXXXXXXXX) | **HIGH** |
| **Suspicious Pattern Detection** | Flag sequential phone numbers (e.g., +15125551001, +15125551002) | **MEDIUM** |
| **Cost Monitoring** | Alert if daily SMS cost exceeds $10 | **HIGH** |
| **SMS Quota per Tenant** | Limit free SMS to 100/month per tenant, charge overage | **MEDIUM** |

### Implementation (Backend)

```kotlin
// FirebaseAuthService.kt
@ApplicationScoped
class FirebaseAuthService(
    private val recaptchaClient: RecaptchaClient,
    private val smsRateLimiter: SmsRateLimiter
) {
    
    suspend fun sendSmsVerification(
        phoneNumber: String,
        recaptchaToken: String,
        ipAddress: String
    ) {
        // 1. Validate US phone number
        require(phoneNumber.matches(Regex("^\\+1[2-9]\\d{9}$"))) {
            "Only US phone numbers are supported"
        }
        
        // 2. Verify reCAPTCHA
        val score = recaptchaClient.verify(recaptchaToken, "sms_verification")
        require(score >= 0.5) {
            "reCAPTCHA verification failed"
        }
        
        // 3. Check rate limits
        smsRateLimiter.checkPhoneLimit(phoneNumber) // 3/hour
        smsRateLimiter.checkIpLimit(ipAddress) // 10/hour
        smsRateLimiter.checkCooldown(phoneNumber) // 60s
        
        // 4. Send SMS via Firebase
        firebaseAuth.sendSmsVerificationCode(phoneNumber)
        
        // 5. Log for monitoring
        auditService.log("SMS_SENT", phoneNumber, ipAddress)
    }
}
```

### Implementation (Flutter)

```dart
// auth_service.dart
Future<void> sendSmsVerification(String phoneNumber) async {
  // 1. Get reCAPTCHA token
  final recaptchaToken = await _recaptchaClient.execute('sms_verification');
  
  // 2. Call backend to send SMS
  await _dio.post('/auth/sms/send', data: {
    'phone_number': phoneNumber,
    'recaptcha_token': recaptchaToken,
  });
}
```

---

## 2. Booking Fraud

### Threat Scenarios

**T2.1: Fake Bookings**
- **Description**: Attacker creates fake bookings to block inventory (competitor sabotage)
- **Impact**: Lost revenue, inventory unavailable for real guests
- **Likelihood**: Medium

**T2.2: Chargeback Fraud**
- **Description**: Guest books, stays, then disputes charge with bank
- **Impact**: Lost revenue + chargeback fees ($15-25)
- **Likelihood**: Low (Stripe handles most fraud)

**T2.3: Promo Code Abuse**
- **Description**: Attacker exploits promo codes via multiple accounts
- **Impact**: Revenue loss
- **Likelihood**: Medium

### Mitigation Controls

| Control | Implementation | Priority |
|---------|---------------|----------|
| **Payment Authorization** | Authorize payment immediately on booking (don't just hold) | **CRITICAL** |
| **Stripe Radar** | Enable Stripe Radar for fraud detection | **CRITICAL** |
| **Email Verification** | Require email verification before booking confirmation | **HIGH** |
| **Booking Limits** | Max 3 pending bookings per email/phone | **HIGH** |
| **Cancellation Policy** | Charge cancellation fee for late cancellations | **MEDIUM** |
| **Promo Code Limits** | One-time use per email/phone | **MEDIUM** |
| **Manual Review** | Flag bookings >$500 for manual review | **LOW** |

### Implementation

```kotlin
// BookingService.kt
@Transactional
suspend fun createBooking(request: CreateBookingRequest): BookingResponse {
    // 1. Check pending booking limit
    val pendingCount = bookingRepository.countPendingByEmail(request.guest.email)
    require(pendingCount < 3) {
        "Maximum 3 pending bookings allowed per email"
    }
    
    // 2. Check inventory availability (with optimistic locking)
    val allocated = inventoryService.allocateRooms(
        roomTypeId = request.roomTypeId,
        checkIn = request.checkInDate,
        checkOut = request.checkOutDate,
        count = 1
    )
    require(allocated) {
        "No rooms available for selected dates"
    }
    
    // 3. Create booking (idempotent)
    val booking = bookingRepository.create(request)
    
    // 4. Authorize payment immediately
    try {
        paymentService.authorizePayment(booking)
    } catch (e: PaymentException) {
        // Rollback inventory allocation
        inventoryService.releaseRooms(...)
        throw e
    }
    
    return booking.toResponse()
}
```

---

## 3. Inventory Manipulation

### Threat Scenarios

**T3.1: Oversell (Race Condition)**
- **Description**: Two concurrent bookings for the last room succeed
- **Impact**: Double booking, guest dissatisfaction, refunds
- **Likelihood**: Medium (high concurrency)

**T3.2: Inventory Hoarding**
- **Description**: Attacker creates multiple bookings to monopolize inventory
- **Impact**: Lost revenue, legitimate guests can't book
- **Likelihood**: Low

**T3.3: Admin Abuse**
- **Description**: Malicious admin manipulates inventory to favor certain bookings
- **Impact**: Revenue loss, unfair access
- **Likelihood**: Low

### Mitigation Controls

| Control | Implementation | Priority |
|---------|---------------|----------|
| **Optimistic Locking** | Use `version` column on `inventory` table | **CRITICAL** |
| **DB Constraints** | `CHECK (allocated_count <= total_count)` | **CRITICAL** |
| **Atomic Transactions** | Booking + inventory allocation in single transaction | **CRITICAL** |
| **Audit Logging** | Log all inventory changes with user/timestamp | **HIGH** |
| **Inventory Reconciliation** | Daily job to verify allocated_count matches bookings | **HIGH** |
| **Booking Timeout** | Auto-cancel unpaid bookings after 15 minutes | **MEDIUM** |

### Implementation

```kotlin
// InventoryService.kt
@Transactional
fun allocateRooms(
    roomTypeId: UUID,
    checkIn: LocalDate,
    checkOut: LocalDate,
    count: Int
): Boolean {
    val dates = checkIn.datesUntil(checkOut).toList()
    
    dates.forEach { date ->
        // Optimistic locking: version check
        val updated = entityManager.createQuery("""
            UPDATE Inventory i
            SET i.allocatedCount = i.allocatedCount + :count,
                i.version = i.version + 1
            WHERE i.roomTypeId = :roomTypeId
              AND i.date = :date
              AND i.allocatedCount + :count <= i.totalCount
              AND i.stopSell = false
              AND i.version = :version
        """)
        .setParameter("count", count)
        .setParameter("roomTypeId", roomTypeId)
        .setParameter("date", date)
        .setParameter("version", getCurrentVersion(roomTypeId, date))
        .executeUpdate()
        
        if (updated == 0) {
            // Allocation failed (no availability or version mismatch)
            throw InventoryUnavailableException("No rooms available for $date")
        }
    }
    
    return true
}
```

---

## 4. Payment Fraud

### Threat Scenarios

**T4.1: Stolen Credit Cards**
- **Description**: Attacker uses stolen card to book rooms
- **Impact**: Chargebacks, fees, reputation damage
- **Likelihood**: Medium (Stripe handles most)

**T4.2: Refund Abuse**
- **Description**: Guest requests refund after stay, claiming service issues
- **Impact**: Revenue loss
- **Likelihood**: Low

**T4.3: Payment Replay**
- **Description**: Attacker replays payment request to charge multiple times
- **Impact**: Overcharging, refunds, reputation damage
- **Likelihood**: Low (idempotency prevents)

### Mitigation Controls

| Control | Implementation | Priority |
|---------|---------------|----------|
| **Stripe Radar** | Automatic fraud detection and blocking | **CRITICAL** |
| **3D Secure (SCA)** | Require 3DS for high-risk transactions | **CRITICAL** |
| **Idempotency Keys** | Prevent duplicate payment charges | **CRITICAL** |
| **Payment Webhooks** | Verify payment status via Stripe webhooks | **CRITICAL** |
| **Refund Policy** | Clear refund policy, manual review for disputes | **HIGH** |
| **Address Verification (AVS)** | Verify billing address matches card | **MEDIUM** |
| **CVV Verification** | Require CVV for all card payments | **MEDIUM** |

### Implementation

```kotlin
// PaymentService.kt
@Transactional
suspend fun authorizePayment(
    booking: Booking,
    paymentMethodId: String,
    idempotencyKey: String
): Payment {
    // 1. Check idempotency
    val existing = paymentRepository.findByIdempotencyKey(idempotencyKey)
    if (existing != null) {
        return existing // Return cached response
    }
    
    // 2. Create Stripe PaymentIntent
    val intent = stripeClient.createPaymentIntent(
        amount = booking.totalPriceCents,
        currency = "usd",
        paymentMethod = paymentMethodId,
        captureMethod = "manual", // Authorize only
        metadata = mapOf(
            "booking_id" to booking.id.toString(),
            "tenant_id" to booking.tenantId.toString()
        ),
        idempotencyKey = idempotencyKey
    )
    
    // 3. Save payment record
    val payment = Payment(
        tenantId = booking.tenantId,
        bookingId = booking.id,
        idempotencyKey = idempotencyKey,
        stripePaymentIntentId = intent.id,
        amountCents = booking.totalPriceCents,
        status = PaymentStatus.AUTHORIZED
    )
    paymentRepository.save(payment)
    
    // 4. Update booking status
    booking.status = BookingStatus.CONFIRMED
    bookingRepository.save(booking)
    
    return payment
}
```

---

## 5. Data Breaches

### Threat Scenarios

**T5.1: SQL Injection**
- **Description**: Attacker injects SQL to access other tenants' data
- **Impact**: Data breach, compliance violations
- **Likelihood**: Low (ORM prevents)

**T5.2: Tenant Isolation Bypass**
- **Description**: Attacker accesses another tenant's data via API
- **Impact**: Data breach, privacy violations
- **Likelihood**: Medium (developer error)

**T5.3: Credential Theft**
- **Description**: Attacker steals Firebase credentials or JWT tokens
- **Impact**: Account takeover, data access
- **Likelihood**: Medium

### Mitigation Controls

| Control | Implementation | Priority |
|---------|---------------|----------|
| **Parameterized Queries** | Use Hibernate ORM (no raw SQL) | **CRITICAL** |
| **Tenant Filtering** | Automatic tenant_id filter on all queries | **CRITICAL** |
| **JWT Verification** | Verify Firebase JWT on every request | **CRITICAL** |
| **HTTPS Only** | Enforce TLS 1.3 for all traffic | **CRITICAL** |
| **Row-Level Security** | PostgreSQL RLS (future enhancement) | **HIGH** |
| **Audit Logging** | Log all data access with user/tenant | **HIGH** |
| **Secrets Management** | Use environment variables, never commit secrets | **HIGH** |
| **CORS Restrictions** | Strict origin allowlist | **MEDIUM** |

### Implementation

```kotlin
// TenantFilter.kt (JAX-RS filter)
@Provider
@Priority(Priorities.AUTHENTICATION + 1)
class TenantFilter : ContainerRequestFilter {
    
    @Inject
    lateinit var tenantContext: TenantContext
    
    override fun filter(requestContext: ContainerRequestContext) {
        val tenantId = requestContext.getHeaderString("X-Tenant-ID")
        val jwtTenantId = requestContext.securityContext.userPrincipal
            .getAttribute("tenant_id")
        
        // Verify tenant_id in header matches JWT claim
        require(tenantId == jwtTenantId) {
            "Tenant ID mismatch"
        }
        
        // Set tenant context for this request
        tenantContext.setTenantId(UUID.fromString(tenantId))
    }
}

// TenantAwareRepository.kt (base repository)
abstract class TenantAwareRepository<T : TenantEntity> {
    
    @Inject
    lateinit var tenantContext: TenantContext
    
    fun findAll(): List<T> {
        return find("tenantId", tenantContext.getTenantId()).list()
    }
    
    fun findById(id: UUID): T? {
        return find("id = ?1 and tenantId = ?2", id, tenantContext.getTenantId())
            .firstResult()
    }
}
```

---

## 6. API Abuse

### Threat Scenarios

**T6.1: DDoS Attack**
- **Description**: Attacker floods API with requests to cause downtime
- **Impact**: Service unavailability
- **Likelihood**: Medium

**T6.2: Scraping**
- **Description**: Competitor scrapes property/pricing data
- **Impact**: Competitive disadvantage
- **Likelihood**: Medium

### Mitigation Controls

| Control | Implementation | Priority |
|---------|---------------|----------|
| **Rate Limiting** | 100 req/min (guest), 1000 req/min (auth) | **CRITICAL** |
| **Cloudflare DDoS Protection** | Free tier DDoS mitigation | **HIGH** |
| **API Key Rotation** | Rotate Firebase service account keys quarterly | **MEDIUM** |
| **Request Throttling** | Exponential backoff for repeated failures | **MEDIUM** |

---

## 7. Account Takeover

### Threat Scenarios

**T7.1: Credential Stuffing**
- **Description**: Attacker uses leaked credentials from other sites
- **Impact**: Account takeover, unauthorized bookings
- **Likelihood**: Medium

**T7.2: Session Hijacking**
- **Description**: Attacker steals JWT token to impersonate user
- **Impact**: Account takeover
- **Likelihood**: Low

### Mitigation Controls

| Control | Implementation | Priority |
|---------|---------------|----------|
| **Firebase Auth** | Delegated auth with strong password requirements | **CRITICAL** |
| **JWT Expiry** | 1-hour token expiry, refresh token rotation | **HIGH** |
| **Multi-Factor Auth** | Optional SMS verification for high-value actions | **MEDIUM** |
| **Login Anomaly Detection** | Flag logins from new devices/locations | **LOW** |

---

## Monitoring & Alerting

### Key Metrics to Monitor

1. **SMS Cost**: Alert if daily cost > $10
2. **Failed Payments**: Alert if failure rate > 5%
3. **Inventory Conflicts**: Alert on optimistic locking failures
4. **API Error Rate**: Alert if 5xx errors > 1%
5. **Rate Limit Hits**: Alert if rate limit hit > 100/hour

### Implementation

```yaml
# Grafana alerts
alerts:
  - name: High SMS Cost
    condition: sum(sms_cost_usd) > 10
    interval: 1d
    
  - name: Payment Failure Spike
    condition: rate(payment_failures) > 0.05
    interval: 5m
    
  - name: Inventory Oversell Attempt
    condition: count(inventory_conflict_errors) > 0
    interval: 1m
```

---

## Security Checklist

- [ ] Firebase Auth configured with US-only SMS
- [ ] reCAPTCHA Enterprise enabled (free tier)
- [ ] Rate limiting enabled on all endpoints
- [ ] Optimistic locking on inventory table
- [ ] Idempotency keys required for bookings/payments
- [ ] Stripe Radar enabled
- [ ] HTTPS enforced (redirect HTTP → HTTPS)
- [ ] CORS restricted to known origins
- [ ] Secrets stored in environment variables
- [ ] Audit logging enabled
- [ ] Monitoring alerts configured
- [ ] Incident response plan documented
