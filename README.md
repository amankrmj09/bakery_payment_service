# 🧁 Payment Service

![Java](https://img.shields.io/badge/Java-21%2B-orange.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen.svg)

Welcome to the **Payment Service**, a core component of the Shah's Bakery Microservice Platform.

## 📑 Table of Contents
- [Features](#-features)
- [Folder Structure](#-folder-structure)
- [Dependencies](#-dependencies)
- [Endpoints](#-endpoints)
- [How to Run](#-how-to-run)
- [Related Links](#-related-links)

## ✨ Features
- Secure transaction management and processing.
- Integration with third-party payment gateways (e.g., Stripe, PayPal).
- Refund and transaction history tracking.
- Webhook endpoints for asynchronous payment confirmations.
- Kafka-based Event Publishing & Consumption for payment lifecycle tracking.
- Internal Statistics synchronization.

## 📁 Folder Structure
The main `src/main/java` directory is organized as follows:
```text
src/
└── main/
    └── java/.../bakery_payment_service/
        ├── client/     # Feign clients for syncing internal stats to the Admin Dashboard.
        ├── config/     # Configuration classes.
        ├── controller/ # REST endpoints and third-party Webhooks for payments.
        ├── dto/        # Data Transfer Objects.
        ├── entity/     # Database entities (Payment, Refund, Transaction).
        ├── exception/  # Custom payment and gateway exceptions.
        ├── kafka/      # Event consumers to handle asynchronous payment requests.
        ├── repository/ # Spring Data JPA interfaces.
        └── service/    # Business logic covering third-party Gateways and Event publishing.
```

## 🛠️ Dependencies
- **Framework:** Spring Boot
- **Database:** PostgreSQL
- **Key Modules:** Eureka Client, Spring Data JPA, OpenFeign

## 🌐 Endpoints
> [!NOTE]
> For complete and detailed API definitions, please refer to the OpenAPI Reference available via the API Gateway's Swagger UI.

- `POST /api/payments` - Initiates a new payment transaction.
- `GET /api/payments/{id}` - Retrieves details of a specific transaction.
- `POST /api/payments/webhook` - Endpoint for third-party gateway callbacks.
- `POST /api/payments/{id}/refund` - Processes a refund for a transaction.

## 🚀 How to Run

1. **Clone the repository:**
   ```bash
   git clone https://github.com/amankrmj01/bakery_payment_service.git
   cd bakery_payment_service
   ```

2. **Configure Environment:**
   Ensure your `.env` or `application.yml` properties (including DB settings and payment gateway secrets) are set.

3. **Run the application:**
   ```bash
   ./gradlew bootRun
   ```

## 🔗 Related Links
- [Main Platform README](../README.md)