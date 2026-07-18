# Bakery Payment Service API Reference

This document provides a comprehensive reference for all the REST API endpoints exposed by the Bakery Payment Service. 

---

## 1. System & Monitoring (Actuator)
**Base Path:** `/actuator`

Standard Spring Boot Actuator endpoints are used for monitoring and metrics.

### 1.1 Health Check
- **Method:** `GET`
- **Path:** `/actuator/health`
- **Type of API:** `Public`
- **Response Body:** `200 OK` (Standard Actuator Health JSON)

### 1.2 Service Info
- **Method:** `GET`
- **Path:** `/actuator/info`
- **Type of API:** `Public`
- **Response Body:** `200 OK` (Standard Actuator Info JSON)

### 1.3 Prometheus Metrics
- **Method:** `GET`
- **Path:** `/actuator/prometheus`
- **Type of API:** `Public`
- **Response Body:** `200 OK` (Prometheus Text Format)

---

## 2. Payments
**Base Path:** `/api/payments`

### 2.1 Create Payment
- **Method:** `POST`
- **Path:** `/api/payments`
- **Type of API:** `User`
- **Request Body:**
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
- **Response Body:** `200 OK`
  `PaymentResponseDto`

### 2.2 Get All Payments
- **Method:** `GET`
- **Path:** `/api/payments`
- **Type of API:** `Admin`
- **Request Body:** None
- **Response Body:** `200 OK`
  `Page<PaymentResponseDto>`

### 2.3 Get Payment by ID
- **Method:** `GET`
- **Path:** `/api/payments/{paymentId}`
- **Type of API:** `User`
- **Request Body:** None
- **Response Body:** `200 OK`
  `PaymentResponseDto`

### 2.4 Get Payment by Reference
- **Method:** `GET`
- **Path:** `/api/payments/reference/{paymentReference}`
- **Type of API:** `User`
- **Request Body:** None
- **Response Body:** `200 OK`
  `PaymentResponseDto`

### 2.5 Get Payment by Order ID
- **Method:** `GET`
- **Path:** `/api/payments/order/{orderId}`
- **Type of API:** `User`
- **Request Body:** None
- **Response Body:** `200 OK`
  `PaymentResponseDto`

### 2.6 Get Payments by User ID
- **Method:** `GET`
- **Path:** `/api/payments/user/{userId}`
- **Type of API:** `User`
- **Request Body:** None
- **Response Body:** `200 OK`
  `List<PaymentResponseDto>`

### 2.7 Get Payments by Status
- **Method:** `GET`
- **Path:** `/api/payments/status/{status}`
- **Type of API:** `Admin`
- **Request Body:** None
- **Response Body:** `200 OK`
  `List<PaymentResponseDto>`

### 2.8 Update Payment Status
- **Method:** `PATCH`
- **Path:** `/api/payments/{paymentId}/status`
- **Type of API:** `Admin`
- **Request Body:**
  ```json
  {
    "status": "COMPLETED",
    "reason": "Manual status update",
    "notes": "Admin note",
    "gatewayResponse": "Success"
  }
  ```
- **Response Body:** `200 OK`
  `PaymentResponseDto`

### 2.9 Cancel Payment
- **Method:** `POST`
- **Path:** `/api/payments/{paymentId}/cancel`
- **Type of API:** `User`
- **Request Body:**
  ```json
  {
    "reason": "User requested cancellation"
  }
  ```
- **Response Body:** `200 OK`
  `PaymentResponseDto`

### 2.10 Retry Payment
- **Method:** `POST`
- **Path:** `/api/payments/{paymentId}/retry`
- **Type of API:** `User`
- **Request Body:** None
- **Response Body:** `200 OK`
  `PaymentResponseDto`

### 2.11 Get Payment Statistics
- **Method:** `GET`
- **Path:** `/api/payments/statistics`
- **Type of API:** `Admin`
- **Request Body:** None
- **Response Body:** `200 OK`
  ```json
  {
    "totalPayments": 150,
    "totalAmount": 15000.00,
    "successfulPayments": 140,
    "failedPayments": 10
  }
  ```


---

## 3. Refunds
**Base Path:** `/api/refunds`

### 3.1 Create Refund
- **Method:** `POST`
- **Path:** `/api/refunds`
- **Type of API:** `User`
- **Request Body:**
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
- **Response Body:** `200 OK`
  `RefundResponseDto`

### 3.2 Get All Refunds
- **Method:** `GET`
- **Path:** `/api/refunds`
- **Type of API:** `Admin`
- **Request Body:** None
- **Response Body:** `200 OK`
  `Page<RefundResponseDto>`

### 3.3 Get Refund by ID
- **Method:** `GET`
- **Path:** `/api/refunds/{refundId}`
- **Type of API:** `User`
- **Request Body:** None
- **Response Body:** `200 OK`
  `RefundResponseDto`

### 3.4 Get Refund by Reference
- **Method:** `GET`
- **Path:** `/api/refunds/reference/{refundReference}`
- **Type of API:** `User`
- **Request Body:** None
- **Response Body:** `200 OK`
  `RefundResponseDto`

### 3.5 Get Refunds by Payment ID
- **Method:** `GET`
- **Path:** `/api/refunds/payment/{paymentId}`
- **Type of API:** `User`
- **Request Body:** None
- **Response Body:** `200 OK`
  `List<RefundResponseDto>`

### 3.6 Get Refunds by User ID
- **Method:** `GET`
- **Path:** `/api/refunds/user/{userId}`
- **Type of API:** `User`
- **Request Body:** None
- **Response Body:** `200 OK`
  `List<RefundResponseDto>`

### 3.7 Get Refunds by Status
- **Method:** `GET`
- **Path:** `/api/refunds/status/{status}`
- **Type of API:** `Admin`
- **Request Body:** None
- **Response Body:** `200 OK`
  `List<RefundResponseDto>`

### 3.8 Approve Refund
- **Method:** `POST`
- **Path:** `/api/refunds/{refundId}/approve`
- **Type of API:** `Admin`
- **Request Body:** None
- **Response Body:** `200 OK`
  `RefundResponseDto`

### 3.9 Reject Refund
- **Method:** `POST`
- **Path:** `/api/refunds/{refundId}/reject`
- **Type of API:** `Admin`
- **Request Body:**
  ```json
  {
    "reason": "Refund not applicable as per policy"
  }
  ```
- **Response Body:** `200 OK`
  `RefundResponseDto`

### 3.10 Get Pending Refunds
- **Method:** `GET`
- **Path:** `/api/refunds/pending`
- **Type of API:** `Admin`
- **Request Body:** None
- **Response Body:** `200 OK`
  `List<RefundResponseDto>`

### 3.11 Get Completed Refunds
- **Method:** `GET`
- **Path:** `/api/refunds/completed`
- **Type of API:** `Admin`
- **Request Body:** None
- **Response Body:** `200 OK`
  `List<RefundResponseDto>`

### 3.12 Get Failed Refunds
- **Method:** `GET`
- **Path:** `/api/refunds/failed`
- **Type of API:** `Admin`
- **Request Body:** None
- **Response Body:** `200 OK`
  `List<RefundResponseDto>`

### 3.13 Search Refunds
- **Method:** `GET`
- **Path:** `/api/refunds/search`
- **Type of API:** `Admin`
- **Request Body:** None
- **Response Body:** `200 OK`
  `List<RefundResponseDto>`

### 3.14 Advanced Search with Filters
- **Method:** `GET`
- **Path:** `/api/refunds/filter`
- **Type of API:** `Admin`
- **Request Body:** None
- **Response Body:** `200 OK`
  `List<RefundResponseDto>`

### 3.15 Get Refund Statistics
- **Method:** `GET`
- **Path:** `/api/refunds/statistics`
- **Type of API:** `Admin`
- **Request Body:** None
- **Response Body:** `200 OK`
  ```json
  {
    "totalRefunds": 20,
    "totalRefundedAmount": 1500.00
  }
  ```


---

## 4. Transactions
**Base Path:** `/api/transactions`

### 4.1 Get Transaction by ID
- **Method:** `GET`
- **Path:** `/api/transactions/{transactionId}`
- **Type of API:** `User`
- **Request Body:** None
- **Response Body:** `200 OK`
  `PaymentTransactionResponseDto`

### 4.2 Get Transactions by Payment ID
- **Method:** `GET`
- **Path:** `/api/transactions/payment/{paymentId}`
- **Type of API:** `User`
- **Request Body:** None
- **Response Body:** `200 OK`
  `List<PaymentTransactionResponseDto>`

### 4.3 Get Transactions by Status
- **Method:** `GET`
- **Path:** `/api/transactions/status/{status}`
- **Type of API:** `Admin`
- **Request Body:** None
- **Response Body:** `200 OK`
  `List<PaymentTransactionResponseDto>`

### 4.4 Get Transactions by Type
- **Method:** `GET`
- **Path:** `/api/transactions/type/{type}`
- **Type of API:** `Admin`
- **Request Body:** None
- **Response Body:** `200 OK`
  `List<PaymentTransactionResponseDto>`

### 4.5 Get Pending Transactions
- **Method:** `GET`
- **Path:** `/api/transactions/pending`
- **Type of API:** `Admin`
- **Request Body:** None
- **Response Body:** `200 OK`
  `List<PaymentTransactionResponseDto>`

### 4.6 Get Failed Transactions
- **Method:** `GET`
- **Path:** `/api/transactions/failed`
- **Type of API:** `Admin`
- **Request Body:** None
- **Response Body:** `200 OK`
  `List<PaymentTransactionResponseDto>`

### 4.7 Get Transaction Statistics
- **Method:** `GET`
- **Path:** `/api/transactions/statistics`
- **Type of API:** `Admin`
- **Request Body:** None
- **Response Body:** `200 OK`
  ```json
  {
    "totalTransactions": 200,
    "successfulTransactions": 190,
    "failedTransactions": 10
  }
  ```


---

## Common DTOs

### PaymentResponseDto
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
    { /* PaymentTransactionResponseDto */ }
  ],
  "refunds": [
    { /* RefundResponseDto */ }
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

### RefundResponseDto
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

### PaymentTransactionResponseDto
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
