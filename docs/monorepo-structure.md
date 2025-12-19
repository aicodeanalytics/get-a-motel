# Monorepo Structure

```
get-a-motel/
├── .github/
│   └── workflows/
│       ├── backend-ci.yml          # Backend lint, test, build
│       ├── frontend-ci.yml         # Flutter analyze, test, build
│       └── deploy.yml              # Deployment pipeline
│
├── backend/                        # Kotlin + Quarkus backend
│   ├── src/
│   │   ├── main/
│   │   │   ├── kotlin/
│   │   │   │   └── com/getamotel/
│   │   │   │       ├── api/                    # REST controllers
│   │   │   │       │   ├── TenantResource.kt
│   │   │   │       │   ├── PropertyResource.kt
│   │   │   │       │   ├── InventoryResource.kt
│   │   │   │       │   ├── BookingResource.kt
│   │   │   │       │   ├── PricingResource.kt
│   │   │   │       │   └── PaymentResource.kt
│   │   │   │       │
│   │   │   │       ├── domain/                 # Domain models
│   │   │   │       │   ├── tenant/
│   │   │   │       │   │   ├── Tenant.kt
│   │   │   │       │   │   ├── TenantUser.kt
│   │   │   │       │   │   └── TenantRole.kt
│   │   │   │       │   ├── property/
│   │   │   │       │   │   ├── Property.kt
│   │   │   │       │   │   └── RoomType.kt
│   │   │   │       │   ├── inventory/
│   │   │   │       │   │   ├── Inventory.kt
│   │   │   │       │   │   └── InventoryAllocation.kt
│   │   │   │       │   ├── booking/
│   │   │   │       │   │   ├── Booking.kt
│   │   │   │       │   │   ├── BookingGuest.kt
│   │   │   │       │   │   └── BookingStatus.kt
│   │   │   │       │   ├── pricing/
│   │   │   │       │   │   ├── RatePlan.kt
│   │   │   │       │   │   ├── NightlyRate.kt
│   │   │   │       │   │   └── PriceCalculation.kt
│   │   │   │       │   ├── payment/
│   │   │   │       │   │   ├── Payment.kt
│   │   │   │       │   │   └── PaymentTransaction.kt
│   │   │   │       │   └── audit/
│   │   │   │       │       └── AuditLog.kt
│   │   │   │       │
│   │   │   │       ├── service/                # Business logic
│   │   │   │       │   ├── TenantService.kt
│   │   │   │       │   ├── PropertyService.kt
│   │   │   │       │   ├── InventoryService.kt
│   │   │   │       │   ├── BookingService.kt
│   │   │   │       │   ├── PricingService.kt
│   │   │   │       │   ├── PaymentService.kt
│   │   │   │       │   └── AuditService.kt
│   │   │   │       │
│   │   │   │       ├── repository/             # Data access
│   │   │   │       │   ├── TenantRepository.kt
│   │   │   │       │   ├── PropertyRepository.kt
│   │   │   │       │   ├── InventoryRepository.kt
│   │   │   │       │   ├── BookingRepository.kt
│   │   │   │       │   ├── PricingRepository.kt
│   │   │   │       │   ├── PaymentRepository.kt
│   │   │   │       │   └── AuditRepository.kt
│   │   │   │       │
│   │   │   │       ├── security/               # Auth & authorization
│   │   │   │       │   ├── FirebaseAuthFilter.kt
│   │   │   │       │   ├── TenantContext.kt
│   │   │   │       │   ├── TenantFilter.kt
│   │   │   │       │   └── RoleCheck.kt
│   │   │   │       │
│   │   │   │       ├── integration/            # External integrations
│   │   │   │       │   ├── stripe/
│   │   │   │       │   │   ├── StripeClient.kt
│   │   │   │       │   │   └── StripeWebhookHandler.kt
│   │   │   │       │   └── firebase/
│   │   │   │       │       └── FirebaseAdminClient.kt
│   │   │   │       │
│   │   │   │       ├── dto/                    # Request/Response DTOs
│   │   │   │       │   ├── request/
│   │   │   │       │   │   ├── CreateBookingRequest.kt
│   │   │   │       │   │   ├── SearchAvailabilityRequest.kt
│   │   │   │       │   │   └── CreatePaymentRequest.kt
│   │   │   │       │   └── response/
│   │   │   │       │       ├── BookingResponse.kt
│   │   │   │       │       ├── AvailabilityResponse.kt
│   │   │   │       │       └── PaymentResponse.kt
│   │   │   │       │
│   │   │   │       ├── exception/              # Custom exceptions
│   │   │   │       │   ├── TenantNotFoundException.kt
│   │   │   │       │   ├── InventoryUnavailableException.kt
│   │   │   │       │   ├── PaymentFailedException.kt
│   │   │   │       │   └── GlobalExceptionHandler.kt
│   │   │   │       │
│   │   │   │       └── util/                   # Utilities
│   │   │   │           ├── DateUtils.kt
│   │   │   │           ├── IdempotencyUtils.kt
│   │   │   │           └── ValidationUtils.kt
│   │   │   │
│   │   │   └── resources/
102: │   │   │       ├── application.properties      # Quarkus config
103: │   │   │       ├── application-dev.properties  # Dev overrides
104: │   │   │       ├── application-prod.properties # Prod overrides
105: │   │   │       └── db/
106: │   │   │           └── migration/              # Flyway migrations
107: │   │   │               ├── V1__initial_schema.sql
108: │   │   │               ├── V2__add_audit_logs.sql
109: │   │   │               └── V3__add_short_stay_blocks.sql
110: │   │   │
111: │   │   └── test/
112: │   │       └── kotlin/
113: │   │           └── com/getamotel/
114: │   │               ├── service/
115: │   │               │   ├── InventoryServiceTest.kt
116: │   │               │   ├── BookingServiceTest.kt
117: │   │               │   └── PricingServiceTest.kt
118: │   │               ├── api/
119: │   │               │   └── BookingResourceTest.kt
120: │   │               └── integration/
121: │   │                   └── BookingFlowIntegrationTest.kt
122: │   │
123: │   ├── build.gradle.kts                # Gradle build config
124: │   ├── settings.gradle.kts
125: │   ├── gradle.properties
126: │   └── README.md
127: │
128: ├── frontend/                           # Flutter app
129: │   ├── lib/
130: │   │   ├── main.dart
131: │   │   ├── app.dart                    # App root with routing
132: │   │   │
133: │   │   ├── core/                       # Core utilities
134: │   │   │   ├── config/
135: │   │   │   │   ├── app_config.dart
136: │   │   │   │   └── env_config.dart
137: │   │   │   ├── network/
138: │   │   │   │   ├── dio_client.dart
139: │   │   │   │   ├── api_interceptor.dart
140: │   │   │   │   └── api_endpoints.dart
141: │   │   │   ├── auth/
142: │   │   │   │   ├── auth_service.dart
143: │   │   │   │   └── token_storage.dart
144: │   │   │   ├── router/
145: │   │   │   │   └── app_router.dart
146: │   │   │   └── theme/
147: │   │   │       ├── app_theme.dart
148: │   │   │       └── app_colors.dart
149: │   │   │
150: │   │   ├── features/                   # Feature modules
151: │   │   │   ├── auth/
152: │   │   │   │   ├── presentation/
153: │   │   │   │   │   ├── login_screen.dart
154: │   │   │   │   │   ├── signup_screen.dart
155: │   │   │   │   │   └── phone_verification_screen.dart
156: │   │   │   │   ├── providers/
157: │   │   │   │   │   └── auth_provider.dart
158: │   │   │   │   └── models/
159: │   │   │   │       └── user_model.dart
160: │   │   │   │
161: │   │   │   ├── search/
162: │   │   │   │   ├── presentation/
163: │   │   │   │   │   ├── search_screen.dart
164: │   │   │   │   │   └── widgets/
165: │   │   │   │   │       ├── search_form.dart
166: │   │   │   │   │       └── property_card.dart
167: │   │   │   │   ├── providers/
168: │   │   │   │   │   └── search_provider.dart
169: │   │   │   │   └── models/
170: │   │   │   │       ├── search_criteria.dart
171: │   │   │   │       └── property_summary.dart
172: │   │   │   │
173: │   │   │   ├── booking/
174: │   │   │   │   ├── presentation/
175: │   │   │   │   │   ├── availability_screen.dart
176: │   │   │   │   │   ├── booking_details_screen.dart
177: │   │   │   │   │   ├── payment_screen.dart
178: │   │   │   │   │   └── confirmation_screen.dart
179: │   │   │   │   ├── providers/
180: │   │   │   │   │   ├── booking_provider.dart
181: │   │   │   │   │   └── payment_provider.dart
182: │   │   │   │   └── models/
183: │   │   │   │       ├── booking_model.dart
184: │   │   │   │       ├── room_type.dart
185: │   │   │   │       └── price_breakdown.dart
186: │   │   │   │
187: │   │   │   ├── admin/
188: │   │   │   │   ├── presentation/
189: │   │   │   │   │   ├── property_list_screen.dart
190: │   │   │   │   │   ├── property_edit_screen.dart
191: │   │   │   │   │   ├── room_type_edit_screen.dart
192: │   │   │   │   │   ├── rate_plan_screen.dart
193: │   │   │   │   │   └── reservations_screen.dart
194: │   │   │   │   ├── providers/
195: │   │   │   │   │   ├── property_provider.dart
196: │   │   │   │   │   └── rate_plan_provider.dart
197: │   │   │   │   └── models/
198: │   │   │   │       ├── property_model.dart
199: │   │   │   │       └── rate_plan_model.dart
200: │   │   │   │
201: │   │   │   └── profile/
202: │   │   │       ├── presentation/
203: │   │   │       │   ├── profile_screen.dart
204: │   │   │       │   └── booking_history_screen.dart
205: │   │   │       └── providers/
206: │   │   │           └── profile_provider.dart
207: │   │   │
208: │   │   ├── shared/                     # Shared widgets
209: │   │   │   ├── widgets/
210: │   │   │   │   ├── loading_indicator.dart
211: │   │   │   │   ├── error_view.dart
212: │   │   │   │   └── custom_button.dart
213: │   │   │   └── utils/
214: │   │   │       ├── date_formatter.dart
215: │   │   │       └── validators.dart
216: │   │   │
217: │   │   └── models/                     # Shared models
218: │   │       └── api_response.dart
219: │   │
220: │   ├── test/
221: │   │   ├── widget_test.dart
222: │   │   └── unit/
223: │   │       └── providers/
224: │   │           └── booking_provider_test.dart
225: │   │
226: │   ├── web/                            # Web-specific files
227: │   │   └── index.html
228: │   │
229: │   ├── ios/                            # iOS-specific files
230: │   ├── android/                        # Android-specific files
231: │   │
232: │   ├── pubspec.yaml
233: │   ├── analysis_options.yaml
234: │   └── README.md
235: │
236: ├── infrastructure/                     # Infrastructure as code
237: │   ├── docker/
238: │   │   ├── docker-compose.yml          # Local dev environment
239: │   │   ├── docker-compose.prod.yml     # Production stack
240: │   │   ├── Dockerfile.backend          # Backend container
241: │   │   └── postgres/
242: │   │       └── init.sql                # Initial DB setup
243: │   │
244: │   ├── k8s/                            # Kubernetes manifests (future)
245: │   │   ├── backend-deployment.yaml
246: │   │   ├── backend-service.yaml
247: │   │   └── ingress.yaml
248: │   │
249: │   └── terraform/                      # Cloud infrastructure (future)
250: │       ├── main.tf
251: │       └── variables.tf
252: │
253: ├── docs/                               # Documentation
254: │   ├── api/
255: │   │   └── openapi.yaml                # OpenAPI spec
256: │   ├── architecture/
257: │   │   ├── architecture.md
258: │   │   ├── data-model.md
259: │   │   └── threat-model.md
260: │   └── guides/
261: │       ├── local-development.md
262: │       └── deployment.md
263: │
264: ├── scripts/                            # Utility scripts
265: │   ├── setup-dev.sh                    # Local dev setup
266: │   ├── run-tests.sh                    # Run all tests
267: │   └── deploy.sh                       # Deployment script
268: │
269: ├── .gitignore
270: ├── .editorconfig
271: ├── README.md                           # Root README
272: └── LICENSE
```

## Key Design Decisions

### 1. **Monorepo Benefits**
- **Atomic Changes**: Update API contract + backend + frontend in single PR
- **Shared Tooling**: Single CI/CD pipeline, consistent code quality
- **Simplified Versioning**: No version skew between client/server
- **Developer Experience**: Single checkout, easier onboarding

### 2. **Backend Module Organization**
- **Layered Architecture**: API → Service → Repository → Domain
- **Domain-Driven**: Modules align with business domains (tenant, property, booking)
- **Clear Boundaries**: Each module has its own models, services, repositories
- **Testability**: Service layer isolated for unit testing

### 3. **Frontend Feature Structure**
- **Feature-First**: Each feature is self-contained (presentation + providers + models)
- **Separation of Concerns**: Core (infrastructure) vs Features (business logic)
- **Scalability**: Easy to add new features without touching existing code
- **Reusability**: Shared widgets and utilities in dedicated folders

### 4. **Infrastructure Separation**
- **Environment Parity**: Docker Compose mirrors production setup
- **IaC Ready**: Terraform/K8s folders prepared for future scaling
- **Documentation**: API specs and guides in dedicated docs folder

## Folder Naming Conventions

- **Backend**: PascalCase for Kotlin files (e.g., `TenantService.kt`)
- **Frontend**: snake_case for Dart files (e.g., `booking_provider.dart`)
- **Config**: kebab-case for config files (e.g., `docker-compose.yml`)
- **Docs**: kebab-case for markdown files (e.g., `local-development.md`)
