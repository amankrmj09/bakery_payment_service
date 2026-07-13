# Bakery Payment Service API Reference

This document provides a comprehensive reference for all the REST API endpoints exposed by the Bakery Payment Service. 

---

## 1. Health & Info (`/api`)

### 1.1 Main Service Health Check
*   **API Name:** Health Check
*   **Method:** `GET`
*   **Path:** `/api/health`
*   **Request Body:** None
*   **Response:**
    ```json
    {
      "status": "UP",
      "service": "bakery-payment-service",
      "timestamp": "2023-10-27T10:00:00",
      "version": "1.0.0",
      "database": "UP",
      "databaseUrl": "jdbc:postgresql://localhost:5432/bakery"
    }
    ```

### 1.2 Service Info
*   **API Name:** Service Information
*   **Method:** `GET`
*   **Path:** `/api/info`
*   **Request Body:** None
*   **Response:**
    ```json
    {
      "serviceName": "Bakery Payment Service",
      "description": "Payment processing and gateway integration service",
      "version": "1.0.0",
      "features": {
        "payments": "Multi-gateway payment processing",
        "refunds": "Full and partial refund management",
        "transactions": "Complete transaction history tracking",
        "gateways": "Support for Stripe, PayPal, Square",
        "analytics": "Payment and refund analytics"
      },
      "endpoints": {
        "payments": "/api/payments",
        "refunds": "/api/refunds",
        "transactions": "/api/transactions"
      }
    }
    ```

### 1.3 Service Metrics
*   **API Name:** Service Metrics
*   **Method:** `GET`
*   **Path:** `/api/metrics`
*   **Request Body:** None
*   **Response:**
    ```json
    {
      "uptime": "1 days, 2 hours, 30 minutes, 15 seconds",
      "timestamp": "2023-10-27T10:00:00",
      "memory": {
        "maxMemory": "1024 MB",
        "totalMemory": "512 MB",
        "freeMemory": "256 MB",
        "usedMemory": "256 MB"
      }
    }
    ```

---

## 2. Payments (`/api/payments`)

### Common Response Object: `PaymentResponseDto`
All payment endpoints return this structure (or a list/page of this structure):
```json
{
  "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "paymentReference": "PAY-123456",
  "orderId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "userId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "paymentMethod": "CREDIT_CARD",
  "paymentGateway": "STRIPE",
  "status": "COMPLETED",
  "amount": 100.00,
  "currencyCode": "USD",
  "description": "Payment for order #123",
  "cardLastFour": "4242",
  "cardBrand": "Visa",
  "cardType": "Credit",
  "digitalWalletProvider": "string",
  "bankName": "string",
  "gatewayPaymentId": "pi_12345",
  "externalTransactionId": "ext_tx_123",
  "gatewayResponse": "Success",
  "failureReason": "string",
  "failureCode": "string",
  "retryCount": 0,
  "lastRetryAt": "2023-10-27T10:00:00",
  "gatewayFee": 2.50,
  "netAmount": 97.50,
  "settlementDate": "2023-10-27T10:00:00",
  "transactions": [
    {
      "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
      "transactionType": "CHARGE",
      "status": "SUCCESS",
      "amount": 100.00,
      "currencyCode": "USD",
      "gatewayTransactionId": "ch_123",
      "gatewayResponse": "Success",
      "failureReason": "string",
      "failureCode": "string",
      "description": "Initial charge",
      "createdAt": "2023-10-27T10:00:00",
      "processedAt": "2023-10-27T10:00:00",
      "metadata": {}
    }
  ],
  "refunds": [
    {
       // RefundResponseDto object
    }
  ],
  "totalRefundedAmount": 0.00,
  "refundableAmount": 100.00,
  "canBeRefunded": true,
  "isExpired": false,
  "canBeRetried": false,
  "createdAt": "2023-10-27T10:00:00",
  "updatedAt": "2023-10-27T10:00:00",
  "authorizedAt": "2023-10-27T10:00:00",
  "capturedAt": "2023-10-27T10:00:00",
  "failedAt": "2023-10-27T10:00:00",
  "cancelledAt": "2023-10-27T10:00:00",
  "expiresAt": "2023-10-27T10:00:00",
  "notes": "string",
  "metadata": {}
}
```

### 2.1 Create Payment
*   **API Name:** Create Payment
*   **Method:** `POST`
*   **Path:** `/api/payments`
*   **Headers:** `X-User-Id` (optional), `X-User-Role` (optional)
*   **Request Body:**
    ```json
    {
      "orderId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
      "userId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
      "paymentMethod": "CREDIT_CARD",
      "paymentGateway": "MOCK",
      "amount": 100.00,
      "currencyCode": "USD",
      "description": "Payment for order",
      "cardLastFour": "4242",
      "cardBrand": "Visa",
      "cardType": "Credit",
      "digitalWalletProvider": "Apple Pay",
      "bankName": "Bank of America",
      "externalTransactionId": "ext_123",
      "notes": "Customer note",
      "metadata": {}
    }
    ```
*   **Response:** `PaymentResponseDto`

### 2.2 Get All Payments (Admin Only)
*   **API Name:** Get All Payments
*   **Method:** `GET`
*   **Path:** `/api/payments?page=0&size=20&sortBy=createdAt&sortDir=DESC`
*   **Headers:** `X-User-Role`
*   **Request Body:** None
*   **Response:** `Page<PaymentResponseDto>`

### 2.3 Get Payment by ID
*   **API Name:** Get Payment by ID
*   **Method:** `GET`
*   **Path:** `/api/payments/{paymentId}`
*   **Headers:** `X-User-Id`, `X-User-Role`
*   **Request Body:** None
*   **Response:** `PaymentResponseDto`

### 2.4 Get Payment by Reference
*   **API Name:** Get Payment by Reference
*   **Method:** `GET`
*   **Path:** `/api/payments/reference/{paymentReference}`
*   **Headers:** `X-User-Id`, `X-User-Role`
*   **Request Body:** None
*   **Response:** `PaymentResponseDto`

### 2.5 Get Payment by Order ID
*   **API Name:** Get Payment by Order ID
*   **Method:** `GET`
*   **Path:** `/api/payments/order/{orderId}`
*   **Headers:** `X-User-Id`, `X-User-Role`
*   **Request Body:** None
*   **Response:** `PaymentResponseDto`

### 2.6 Get Payments by User ID
*   **API Name:** Get Payments by User ID
*   **Method:** `GET`
*   **Path:** `/api/payments/user/{userId}`
*   **Headers:** `X-User-Id`, `X-User-Role`
*   **Request Body:** None
*   **Response:** `List<PaymentResponseDto>`

### 2.7 Get Payments by Status (Admin Only)
*   **API Name:** Get Payments by Status
*   **Method:** `GET`
*   **Path:** `/api/payments/status/{status}`
*   **Headers:** `X-User-Role`
*   **Request Body:** None
*   **Response:** `List<PaymentResponseDto>`

### 2.8 Update Payment Status (Admin Only)
*   **API Name:** Update Payment Status
*   **Method:** `PATCH`
*   **Path:** `/api/payments/{paymentId}/status`
*   **Headers:** `X-User-Role`
*   **Request Body:**
    ```json
    {
      "status": "COMPLETED",
      "reason": "Manual status update",
      "notes": "Admin note",
      "gatewayResponse": "Success"
    }
    ```
*   **Response:** `PaymentResponseDto`

### 2.9 Cancel Payment
*   **API Name:** Cancel Payment
*   **Method:** `POST`
*   **Path:** `/api/payments/{paymentId}/cancel`
*   **Headers:** `X-User-Id`, `X-User-Role`
*   **Request Body:**
    ```json
    {
      "reason": "User requested cancellation"
    }
    ```
*   **Response:** `PaymentResponseDto`

### 2.10 Retry Payment
*   **API Name:** Retry Payment
*   **Method:** `POST`
*   **Path:** `/api/payments/{paymentId}/retry`
*   **Headers:** `X-User-Id`, `X-User-Role`
*   **Request Body:** None
*   **Response:** `PaymentResponseDto`

### 2.11 Get Payment Statistics (Admin Only)
*   **API Name:** Get Payment Statistics
*   **Method:** `GET`
*   **Path:** `/api/payments/statistics?startDate={ISO_DATE_TIME}&endDate={ISO_DATE_TIME}`
*   **Headers:** `X-User-Role`
*   **Request Body:** None
*   **Response:**
    ```json
    {
      "totalPayments": 150,
      "totalAmount": 15000.00,
      "successfulPayments": 140,
      "failedPayments": 10
    }
    ```

### 2.12 Payment Service Health
*   **API Name:** Payment Health Check
*   **Method:** `GET`
*   **Path:** `/api/payments/health`
*   **Request Body:** None
*   **Response:**
    ```json
    {
      "status": "UP",
      "service": "payment-service-payments",
      "timestamp": "2023-10-27T10:00:00"
    }
    ```

---

## 3. Refunds (`/api/refunds`)

### Common Response Object: `RefundResponseDto`
All refund endpoints return this structure (or a list/page of this structure):
```json
{
  "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "refundReference": "REF-123456",
  "paymentId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "paymentReference": "PAY-123456",
  "status": "COMPLETED",
  "amount": 50.00,
  "currencyCode": "USD",
  "reason": "Customer requested refund",
  "gatewayRefundId": "re_123",
  "gatewayResponse": "Success",
  "failureReason": "string",
  "failureCode": "string",
  "requestedBy": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "approvedBy": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "createdAt": "2023-10-27T10:00:00",
  "updatedAt": "2023-10-27T10:00:00",
  "processedAt": "2023-10-27T10:00:00",
  "completedAt": "2023-10-27T10:00:00",
  "failedAt": "2023-10-27T10:00:00",
  "notes": "string",
  "metadata": {}
}
```

### 3.1 Create Refund
*   **API Name:** Create Refund
*   **Method:** `POST`
*   **Path:** `/api/refunds`
*   **Headers:** `X-User-Id` (optional), `X-User-Role` (optional)
*   **Request Body:**
    ```json
    {
      "paymentId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
      "amount": 50.00,
      "reason": "Item out of stock",
      "requestedBy": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
      "notes": "Partial refund",
      "metadata": {}
    }
    ```
*   **Response:** `RefundResponseDto`

### 3.2 Get All Refunds (Admin Only)
*   **API Name:** Get All Refunds
*   **Method:** `GET`
*   **Path:** `/api/refunds?page=0&size=20&sortBy=createdAt&sortDir=DESC`
*   **Headers:** `X-User-Role`
*   **Request Body:** None
*   **Response:** `Page<RefundResponseDto>`

### 3.3 Get Refund by ID
*   **API Name:** Get Refund by ID
*   **Method:** `GET`
*   **Path:** `/api/refunds/{refundId}`
*   **Headers:** `X-User-Id`, `X-User-Role`
*   **Request Body:** None
*   **Response:** `RefundResponseDto`

### 3.4 Get Refund by Reference
*   **API Name:** Get Refund by Reference
*   **Method:** `GET`
*   **Path:** `/api/refunds/reference/{refundReference}`
*   **Headers:** `X-User-Id`, `X-User-Role`
*   **Request Body:** None
*   **Response:** `RefundResponseDto`

### 3.5 Get Refunds by Payment ID
*   **API Name:** Get Refunds by Payment ID
*   **Method:** `GET`
*   **Path:** `/api/refunds/payment/{paymentId}`
*   **Headers:** `X-User-Id`, `X-User-Role`
*   **Request Body:** None
*   **Response:** `List<RefundResponseDto>`

### 3.6 Get Refunds by User ID
*   **API Name:** Get Refunds by User ID
*   **Method:** `GET`
*   **Path:** `/api/refunds/user/{userId}`
*   **Headers:** `X-User-Id`, `X-User-Role`
*   **Request Body:** None
*   **Response:** `List<RefundResponseDto>`

### 3.7 Get Refunds by Status (Admin Only)
*   **API Name:** Get Refunds by Status
*   **Method:** `GET`
*   **Path:** `/api/refunds/status/{status}`
*   **Headers:** `X-User-Role`
*   **Request Body:** None
*   **Response:** `List<RefundResponseDto>`

### 3.8 Approve Refund (Admin Only)
*   **API Name:** Approve Refund
*   **Method:** `POST`
*   **Path:** `/api/refunds/{refundId}/approve`
*   **Headers:** `X-User-Id`, `X-User-Role`
*   **Request Body:** None
*   **Response:** `RefundResponseDto`

### 3.9 Reject Refund (Admin Only)
*   **API Name:** Reject Refund
*   **Method:** `POST`
*   **Path:** `/api/refunds/{refundId}/reject`
*   **Headers:** `X-User-Id`, `X-User-Role`
*   **Request Body:**
    ```json
    {
      "reason": "Refund not applicable as per policy"
    }
    ```
*   **Response:** `RefundResponseDto`

### 3.10 Get Pending Refunds (Admin Only)
*   **API Name:** Get Pending Refunds
*   **Method:** `GET`
*   **Path:** `/api/refunds/pending`
*   **Headers:** `X-User-Role`
*   **Request Body:** None
*   **Response:** `List<RefundResponseDto>`

### 3.11 Get Completed Refunds (Admin Only)
*   **API Name:** Get Completed Refunds
*   **Method:** `GET`
*   **Path:** `/api/refunds/completed`
*   **Headers:** `X-User-Role`
*   **Request Body:** None
*   **Response:** `List<RefundResponseDto>`

### 3.12 Get Failed Refunds (Admin Only)
*   **API Name:** Get Failed Refunds
*   **Method:** `GET`
*   **Path:** `/api/refunds/failed`
*   **Headers:** `X-User-Role`
*   **Request Body:** None
*   **Response:** `List<RefundResponseDto>`

### 3.13 Search Refunds (Admin Only)
*   **API Name:** Search Refunds
*   **Method:** `GET`
*   **Path:** `/api/refunds/search?query={query}`
*   **Headers:** `X-User-Role`
*   **Request Body:** None
*   **Response:** `List<RefundResponseDto>`

### 3.14 Advanced Search with Filters (Admin Only)
*   **API Name:** Filter Refunds
*   **Method:** `GET`
*   **Path:** `/api/refunds/filter?status={status}&requestedBy={UUID}&approvedBy={UUID}&minAmount={0.0}&maxAmount={100.0}&startDate={ISO_DATE}&endDate={ISO_DATE}`
*   **Headers:** `X-User-Role`
*   **Request Body:** None
*   **Response:** `List<RefundResponseDto>`

### 3.15 Get Refund Statistics (Admin Only)
*   **API Name:** Get Refund Statistics
*   **Method:** `GET`
*   **Path:** `/api/refunds/statistics?startDate={ISO_DATE}&endDate={ISO_DATE}`
*   **Headers:** `X-User-Role`
*   **Request Body:** None
*   **Response:**
    ```json
    {
      "totalRefunds": 20,
      "totalRefundedAmount": 1500.00
    }
    ```

### 3.16 Refund Service Health
*   **API Name:** Refund Health Check
*   **Method:** `GET`
*   **Path:** `/api/refunds/health`
*   **Request Body:** None
*   **Response:**
    ```json
    {
      "status": "UP",
      "service": "payment-service-refunds",
      "timestamp": "2023-10-27T10:00:00"
    }
    ```

---

## 4. Transactions (`/api/transactions`)

### Common Response Object: `PaymentTransactionResponseDto`
All transaction endpoints return this structure (or a list of this structure):
```json
{
  "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "transactionType": "CHARGE",
  "status": "SUCCESS",
  "amount": 100.00,
  "currencyCode": "USD",
  "gatewayTransactionId": "ch_123",
  "gatewayResponse": "Success",
  "failureReason": "string",
  "failureCode": "string",
  "description": "Initial charge",
  "createdAt": "2023-10-27T10:00:00",
  "processedAt": "2023-10-27T10:00:00",
  "metadata": {}
}
```

### 4.1 Get Transaction by ID
*   **API Name:** Get Transaction by ID
*   **Method:** `GET`
*   **Path:** `/api/transactions/{transactionId}`
*   **Headers:** `X-User-Role`
*   **Request Body:** None
*   **Response:** `PaymentTransactionResponseDto`

### 4.2 Get Transactions by Payment ID
*   **API Name:** Get Transactions by Payment ID
*   **Method:** `GET`
*   **Path:** `/api/transactions/payment/{paymentId}`
*   **Headers:** `X-User-Role`
*   **Request Body:** None
*   **Response:** `List<PaymentTransactionResponseDto>`

### 4.3 Get Transactions by Status (Admin Only)
*   **API Name:** Get Transactions by Status
*   **Method:** `GET`
*   **Path:** `/api/transactions/status/{status}`
*   **Headers:** `X-User-Role`
*   **Request Body:** None
*   **Response:** `List<PaymentTransactionResponseDto>`

### 4.4 Get Transactions by Type (Admin Only)
*   **API Name:** Get Transactions by Type
*   **Method:** `GET`
*   **Path:** `/api/transactions/type/{type}`
*   **Headers:** `X-User-Role`
*   **Request Body:** None
*   **Response:** `List<PaymentTransactionResponseDto>`

### 4.5 Get Pending Transactions (Admin Only)
*   **API Name:** Get Pending Transactions
*   **Method:** `GET`
*   **Path:** `/api/transactions/pending`
*   **Headers:** `X-User-Role`
*   **Request Body:** None
*   **Response:** `List<PaymentTransactionResponseDto>`

### 4.6 Get Failed Transactions (Admin Only)
*   **API Name:** Get Failed Transactions
*   **Method:** `GET`
*   **Path:** `/api/transactions/failed`
*   **Headers:** `X-User-Role`
*   **Request Body:** None
*   **Response:** `List<PaymentTransactionResponseDto>`

### 4.7 Get Transaction Statistics (Admin Only)
*   **API Name:** Get Transaction Statistics
*   **Method:** `GET`
*   **Path:** `/api/transactions/statistics?startDate={ISO_DATE}&endDate={ISO_DATE}`
*   **Headers:** `X-User-Role`
*   **Request Body:** None
*   **Response:**
    ```json
    {
      "totalTransactions": 200,
      "successfulTransactions": 190,
      "failedTransactions": 10
    }
    ```

### 4.8 Transaction Service Health
*   **API Name:** Transaction Health Check
*   **Method:** `GET`
*   **Path:** `/api/transactions/health`
*   **Request Body:** None
*   **Response:**
    ```json
    {
      "status": "UP",
      "service": "payment-service-transactions",
      "timestamp": "2023-10-27T10:00:00"
    }
    ```
