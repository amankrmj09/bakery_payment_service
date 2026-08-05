# 🧁 Payment Service

![Java](https://img.shields.io/badge/Java-21%2B-orange.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen.svg)
![Database](https://img.shields.io/badge/Database-PostgreSQL-blue.svg)
![Cache](https://img.shields.io/badge/Cache-Redis-red.svg)
![Messaging](https://img.shields.io/badge/Messaging-Kafka-black.svg)

The Payment Service is a core component of the Shah's Bakery Microservice Platform responsible for secure transaction management, processing payments via third-party gateways, handling refunds, and synchronizing payment statistics.

## 📑 Table of Contents
- [Architecture & Design](#-architecture--design)
- [Features](#-features)
- [Folder Structure](#-folder-structure)
- [API Reference](#-api-reference)
- [Configuration](#-configuration)
- [How to Run Locally](#-how-to-run-locally)
- [Testing](#-testing)
- [Dependencies](#-dependencies)
- [Related Links](#-related-links)

## 🏗️ Architecture & Design
Provide a brief overview of the architecture of this service.
- **Data Storage**: PostgreSQL for persistent relational data (Transactions, Payments, Refunds), and Redis for caching or temporary payment states. Flyway is used for database migrations.
- **Communication**: REST API for synchronous operations, Kafka for asynchronous event-driven payment lifecycle tracking, and OpenFeign clients for inter-service communication (e.g., syncing internal stats to the Admin Dashboard).
- **Key Design Patterns**: MVC Pattern, Repository Pattern for data access, DTO Pattern for data transfer. Multi-gateway strategy (Stripe, PayPal, Square).

## ✨ Features
List the core capabilities and features of this service.
- Secure transaction management and processing.
- Integration with third-party payment gateways (Stripe, PayPal, Square).
- Refund and transaction history tracking.
- Webhook endpoints for asynchronous payment confirmations from gateways.
- Kafka-based Event Publishing & Consumption for payment lifecycle tracking.
- Internal Statistics synchronization to the Admin Dashboard.

## 📁 Folder Structure
```text
bakery_payment_service/
├── .env
├── .env.example
├── .gitattributes
├── .gitignore
├── API_REFERENCE.md
├── Dockerfile
├── README.md
├── build.gradle.kts
├── gradle.properties
├── gradlew
├── gradlew.bat
├── settings.gradle.kts
└── src/
    ├── main/
    │   ├── java/
    │   │   └── com/
    │   │       └── blubugtech/
    │   │           └── bakery_payment_service/
    │   │               ├── BakeryPaymentServiceApplication.java
    │   │               ├── client/
    │   │               │   ├── OrderClient.java
    │   │               │   ├── OrderClientFallbackFactory.java
    │   │               │   ├── UserClient.java
    │   │               │   ├── UserClientFallbackFactory.java
    │   │               │   └── statistics/
    │   │               │       ├── InternalStatsClient.java
    │   │               │       └── InternalStatsClientFallbackFactory.java
    │   │               ├── controller/
    │   │               │   ├── PaymentAdminController.java
    │   │               │   ├── PaymentController.java
    │   │               │   ├── PaymentOtpController.java
    │   │               │   ├── RefundController.java
    │   │               │   └── TransactionController.java
    │   │               ├── dto/
    │   │               │   ├── DateRangeResponse.java
    │   │               │   ├── PaymentGatewayStatResponse.java
    │   │               │   ├── PaymentMethodStatResponse.java
    │   │               │   ├── PaymentStatisticsResponse.java
    │   │               │   ├── PaymentStatusStatResponse.java
    │   │               │   ├── otp/
    │   │               │   │   ├── OtpErrorResponse.java
    │   │               │   │   ├── OtpMessageResponse.java
    │   │               │   │   ├── OtpSendResponse.java
    │   │               │   │   └── VerifyOtpRequest.java
    │   │               │   ├── payment/
    │   │               │   │   ├── PaymentRequest.java
    │   │               │   │   ├── PaymentResponse.java
    │   │               │   │   └── PaymentStatusUpdateRequest.java
    │   │               │   ├── refund/
    │   │               │   │   ├── RefundRequest.java
    │   │               │   │   └── RefundResponse.java
    │   │               │   └── transaction/
    │   │               │       └── PaymentTransactionResponse.java
    │   │               ├── entity/
    │   │               │   ├── Payment.java
    │   │               │   ├── PaymentTransaction.java
    │   │               │   └── Refund.java
    │   │               ├── enums/
    │   │               │   ├── ErrorCode.java
    │   │               │   ├── PaymentGatewayProvider.java
    │   │               │   ├── PaymentMethod.java
    │   │               │   ├── PaymentStatus.java
    │   │               │   ├── RefundStatus.java
    │   │               │   ├── TransactionStatus.java
    │   │               │   └── TransactionType.java
    │   │               ├── event/
    │   │               │   ├── PaymentEventListener.java
    │   │               │   ├── PaymentStatusUpdatedApplicationEvent.java
    │   │               │   └── RefundProcessedApplicationEvent.java
    │   │               ├── exception/
    │   │               │   ├── GlobalExceptionHandler.java
    │   │               │   ├── gateway/
    │   │               │   │   └── PaymentGatewayException.java
    │   │               │   ├── order/
    │   │               │   │   └── OrderNotFoundException.java
    │   │               │   ├── payment/
    │   │               │   │   ├── DuplicatePaymentException.java
    │   │               │   │   ├── InvalidPaymentAmountException.java
    │   │               │   │   ├── InvalidPaymentStatusException.java
    │   │               │   │   ├── PaymentNotFoundException.java
    │   │               │   │   ├── PaymentServiceException.java
    │   │               │   │   └── PaymentTimeoutException.java
    │   │               │   └── refund/
    │   │               │       ├── InvalidRefundException.java
    │   │               │       └── RefundFailedException.java
    │   │               ├── integration/
    │   │               │   ├── kafka/
    │   │               │   │   ├── consumer/
    │   │               │   │   │   ├── OrderEventListener.java
    │   │               │   │   │   └── PaymentRequestedEventConsumer.java
    │   │               │   │   └── producer/
    │   │               │   │       ├── PaymentEventPublisher.java
    │   │               │   │       └── UserEventPublisher.java
    │   │               │   └── payment/
    │   │               │       ├── ManualGateway.java
    │   │               │       ├── MockGateway.java
    │   │               │       ├── OtpPaymentGateway.java
    │   │               │       ├── PaymentGateway.java
    │   │               │       └── PaymentGatewayResult.java
    │   │               ├── mapper/
    │   │               │   ├── OtpMapper.java
    │   │               │   ├── PaymentMapper.java
    │   │               │   ├── PaymentTransactionMapper.java
    │   │               │   └── RefundMapper.java
    │   │               ├── repository/
    │   │               │   ├── PaymentRepository.java
    │   │               │   ├── PaymentTransactionRepository.java
    │   │               │   └── RefundRepository.java
    │   │               ├── service/
    │   │               │   ├── OtpService.java
    │   │               │   ├── PaymentProcessingService.java
    │   │               │   ├── PaymentService.java
    │   │               │   ├── PaymentTransactionService.java
    │   │               │   ├── RefundService.java
    │   │               │   └── impl/
    │   │               │       ├── OtpServiceImpl.java
    │   │               │       ├── PaymentProcessingServiceImpl.java
    │   │               │       ├── PaymentServiceImpl.java
    │   │               │       ├── PaymentTransactionServiceImpl.java
    │   │               │       └── RefundServiceImpl.java
    │   │               └── validation/
    │   │                   ├── PaymentValidator.java
    │   │                   └── RefundValidator.java
    │   └── resources/
    │       ├── db/
    │       │   └── migration/
    │       │       └── V1__init.sql
    │       ├── application.yml
    │       ├── application-dev.yml
    │       ├── application-docker.yml
    │       ├── application-prod.yml
    │       └── logback-spring.xml
    └── test/
        ├── java/
        │   └── com/
        │       └── blubugtech/
        │           └── bakery_payment_service/
        │               └── BakeryPaymentServiceApplicationTests.java
        └── resources/
            └── application-test.yml
```

## 🌐 API Reference
> [!NOTE]
> For detailed API definitions, request/response bodies, and schemas, please refer to the OpenAPI Reference available via the API Gateway's Swagger UI.

**Key Endpoints:**
- `POST /api/payments` - Initiates a new payment transaction.
- `GET /api/payments/{id}` - Retrieves details of a specific transaction.
- `POST /api/payments/webhook` - Endpoint for third-party gateway callbacks (Stripe, etc.).
- `POST /api/payments/{id}/refund` - Processes a refund for a transaction.

## ⚙️ Configuration
List required environment variables and configurations.
You can copy `.env.example` to `.env` and fill in the values.

| Variable | Description | Default / Example |
|----------|-------------|-------------------|
| `ACTIVE_PROFILE` | Spring active profile | `dev` |
| `CONFIG_SERVER_URL` | Spring Cloud Config Server URL | `http://localhost:8888` |
| `EUREKA_URL` | Eureka discovery server URL | `http://localhost:8761/eureka/` |
| `KAFKA_BOOTSTRAP_SERVERS` | Kafka broker servers | `localhost:9092` |
| `PAYMENT_DB_PASSWORD` | PostgreSQL database password | `password` |
| `PAYMENT_DB_URL` | PostgreSQL database URL | `jdbc:postgresql://localhost:5432/payment_db` |
| `PAYMENT_DB_USER` | PostgreSQL database user | `postgres` |
| `PAYPAL_CLIENT_ID` | PayPal client ID | - |
| `PAYPAL_CLIENT_SECRET` | PayPal client secret | - |
| `SQUARE_ACCESS_TOKEN` | Square access token | - |
| `STRIPE_API_KEY` | Stripe API key | - |
| `STRIPE_WEBHOOK_SECRET` | Stripe webhook secret | - |
| `SERVER_PORT` | Port for the service | `8085` |
| `PAYMENT_MIN_AMOUNT` | Minimum payment amount | `1.00` |
| `PAYMENT_MAX_AMOUNT` | Maximum payment amount | `10000.00` |
| `PAYMENT_DAILY_LIMIT` | Daily limit for payments | `50000.00` |

## 🚀 How to Run Locally

### Prerequisites
- JDK 21+
- Gradle
- PostgreSQL
- Redis
- Kafka

### Steps
1. **Clone the repository:**
   ```bash
   git clone https://github.com/amankrmj01/bakery_payment_service.git
   cd bakery_payment_service
   ```

2. **Configure Environment:**
   Set up your `.env` file based on `.env.example`. Make sure backing services (like PostgreSQL, Redis, Kafka) are running.
   You can use the provided Docker Compose file (if available):
   ```bash
   docker-compose up -d
   ```

3. **Run the application:**
   ```bash
   ./gradlew bootRun
   ```

## 🧪 Testing
To run the test suite:
```bash
./gradlew test
```

## 🛠️ Dependencies
- **Framework:** Spring Boot 3.5.15
- **Database:** PostgreSQL, Redis
- **Messaging:** Apache Kafka
- **Key Modules:** Spring Web, Spring Data JPA, Spring Data Redis, Eureka Client, OpenFeign, Spring Security, Flyway, OpenAPI

## 🔗 Related Links

*For overall architecture, contribution guidelines, and security policies, please refer to the main [Blu's Bakery](https://github.com/amankrmj09/Blu_s_Bakery) repository.*

