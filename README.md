# get-a-motel

A US-only, multi-tenant motel and hotel booking platform built with **Flutter** and **Kotlin/Quarkus**.

## 🚀 Project Overview

This repository is a monorepo containing:
*   **/backend**: Kotlin + Quarkus REST API (Hexagonal/Layered Architecture).
*   **/frontend**: Flutter Multi-platform Mobile App (Riverpod, Dio, GoRouter).
*   **/infrastructure**: Docker Compose for local development and PostgreSQL setup.
*   **/docs**: Comprehensive project documentation (Architecture, API Contract, Data Model, Threat Model).

## 🛠 Tech Stack

### Backend
*   **Language**: Kotlin
*   **Framework**: Quarkus
*   **ORM**: Hibernate with Panache
*   **Migrations**: Flyway
*   **Database**: PostgreSQL
*   **Auth**: Firebase Admin SDK

### Frontend
*   **Framework**: Flutter
*   **State Management**: Riverpod
*   **Networking**: Dio
*   **Navigation**: GoRouter
*   **Payments**: Flutter Stripe

## 📖 Key Documentation

*   [Reference Architecture](docs/architecture.md)
*   [Data Model & Invariants](docs/data-model.md)
*   [API Contract](docs/api-contract.md)
*   [Security & Threat Model](docs/threat-model.md)

## 🏗 Local Development Setup

### 1. Prerequisite
*   Docker Desktop (for PostgreSQL)
*   Java 17+
*   Flutter SDK
*   Android Studio / VS Code

### 2. Start Database
```bash
cd infrastructure/docker
docker-compose up -d
```

### 3. Run Backend
```bash
cd ../../backend
./gradlew quarkusDev
```
*(Or use the Gradle tab in Android Studio: Tasks -> quarkus -> quarkusDev)*

### 4. Run Frontend
```bash
cd ../frontend
flutter pub get
flutter run
```

## 🔒 Security & Compliance
*   **Multi-tenancy**: Enforced via `TenantContext` filter on all requests.
*   **US-only SMS**: Verified via Firebase Auth with US-only allowlist.
*   **Payments**: Stripe PaymentIntents (Authorize → Capture).
*   **Audit Trail**: Automated logging for all financial and inventory changes.
