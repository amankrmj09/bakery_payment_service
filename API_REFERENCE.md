# bakery_payment_service API Report

## HealthController

### `GET` `/api/health`
- **API Name:** health
- **Type:** REST / Synchronous

**Request:**
None

**Response:**
```json
{
  "status": "String - UP",
  "service": "String - bakery-payment-service",
  "timestamp": "DateTime",
  "version": "String - 1.0.0",
  "database": "String - UP or DOWN",
  "databaseUrl": "String",
  "databaseError": "String"
}
```

---

### `GET` `/api/info`
- **API Name:** info
- **Type:** REST / Synchronous

**Request:**
None

**Response:**
```json
{
  "serviceName": "String",
  "description": "String",
  "version": "String",
  "features": {},
  "endpoints": {}
}
```

---

### `GET` `/api/metrics`
- **API Name:** metrics
- **Type:** REST / Synchronous

**Request:**
None

**Response:**
```json
{
  "uptime": "String",
  "timestamp": "DateTime",
  "memory": {
    "maxMemory": "String",
    "totalMemory": "String",
    "freeMemory": "String",
    "usedMemory": "String"
  }
}
```

---

## PaymentController

### `POST` `/api/payments`
- **API Name:** createPayment
- **Type:** REST / Synchronous
- **Request Headers:**
  - `X-User-Id` (String, optional)
  - `X-User-Role` (String, optional)

**Request:**
```json
{
  "orderId": "UUID - Required",
  "userId": "UUID - Required",
  "paymentMethod": "String - Required (e.g., CASH, CARD, DIGITAL_WALLET)",
  "paymentGateway": "String - Default MOCK",
  "amount": "BigDecimal - Required (Min 0.01)",
  "currencyCode": "String - Default 'USD'",
  "description": "String",
  "cardLastFour": "String",
  "cardBrand": "String",
  "cardType": "String",
  "digitalWalletProvider": "String",
  "bankName": "String",
  "externalTransactionId": "String",
  "notes": "String",
  "metadata": {}
}
```

**Response:**
```json
{
  "id": "UUID",
  "paymentReference": "String",
  "orderId": "UUID",
  "userId": "UUID",
  "paymentMethod": "String",
  "paymentGateway": "String",
  "status": "String (e.g., PENDING, COMPLETED, FAILED)",
  "amount": "BigDecimal",
  "currencyCode": "String",
  "description": "String",
  "cardLastFour": "String",
  "cardBrand": "String",
  "cardType": "String",
  "digitalWalletProvider": "String",
  "bankName": "String",
  "gatewayPaymentId": "String",
  "externalTransactionId": "String",
  "gatewayResponse": "String",
  "failureReason": "String",
  "failureCode": "String",
  "retryCount": "Integer",
  "lastRetryAt": "DateTime",
  "gatewayFee": "BigDecimal",
  "netAmount": "BigDecimal",
  "settlementDate": "DateTime",
  "transactions": [
    {
      "id": "UUID",
      "transactionType": "String (e.g., AUTHORIZATION, CAPTURE, REFUND)",
      "status": "String",
      "amount": "BigDecimal",
      "currencyCode": "String",
      "gatewayTransactionId": "String",
      "gatewayResponse": "String",
      "failureReason": "String",
      "failureCode": "String",
      "description": "String",
      "createdAt": "DateTime",
      "processedAt": "DateTime",
      "metadata": {}
    }
  ],
  "refunds": [
    {
      "id": "UUID",
      "refundReference": "String",
      "paymentId": "UUID",
      "paymentReference": "String",
      "status": "String (e.g., PENDING, APPROVED, COMPLETED, REJECTED, FAILED)",
      "amount": "BigDecimal",
      "currencyCode": "String",
      "reason": "String",
      "gatewayRefundId": "String",
      "gatewayResponse": "String",
      "failureReason": "String",
      "failureCode": "String",
      "requestedBy": "UUID",
      "approvedBy": "UUID",
      "createdAt": "DateTime",
      "updatedAt": "DateTime",
      "processedAt": "DateTime",
      "completedAt": "DateTime",
      "failedAt": "DateTime",
      "notes": "String",
      "metadata": {}
    }
  ],
  "totalRefundedAmount": "BigDecimal",
  "refundableAmount": "BigDecimal",
  "canBeRefunded": "Boolean",
  "isExpired": "Boolean",
  "canBeRetried": "Boolean",
  "createdAt": "DateTime",
  "updatedAt": "DateTime",
  "authorizedAt": "DateTime",
  "capturedAt": "DateTime",
  "failedAt": "DateTime",
  "cancelledAt": "DateTime",
  "expiresAt": "DateTime",
  "notes": "String",
  "metadata": {}
}
```

---

### `GET` `/api/payments`
- **API Name:** getAllPayments
- **Type:** REST / Synchronous
- **Query Parameters:** `page` (int), `size` (int), `sortBy` (String), `sortDir` (String)
- **Request Headers:** `X-User-Role` (optional)

**Request:**
None

**Response:**
*(Page of PaymentResponse)*

---

### `GET` `/api/payments/{paymentId}`
- **API Name:** getPaymentById
- **Type:** REST / Synchronous
- **Path Variable:** `paymentId` (UUID)
- **Request Headers:** `X-User-Id` (optional), `X-User-Role` (optional)

**Request:**
None

**Response:**
*(Same as `createPayment` PaymentResponse)*

---

### `GET` `/api/payments/reference/{paymentReference}`
- **API Name:** getPaymentByReference
- **Type:** REST / Synchronous
- **Path Variable:** `paymentReference` (String)
- **Request Headers:** `X-User-Id` (optional), `X-User-Role` (optional)

**Request:**
None

**Response:**
*(Same as `createPayment` PaymentResponse)*

---

### `GET` `/api/payments/order/{orderId}`
- **API Name:** getPaymentByOrderId
- **Type:** REST / Synchronous
- **Path Variable:** `orderId` (UUID)
- **Request Headers:** `X-User-Id` (optional), `X-User-Role` (optional)

**Request:**
None

**Response:**
*(Same as `createPayment` PaymentResponse)*

---

### `GET` `/api/payments/user/{userId}`
- **API Name:** getPaymentsByUserId
- **Type:** REST / Synchronous
- **Path Variable:** `userId` (UUID)
- **Request Headers:** `X-User-Id` (optional), `X-User-Role` (optional)

**Request:**
None

**Response:**
*(List of PaymentResponse)*

---

### `GET` `/api/payments/status/{status}`
- **API Name:** getPaymentsByStatus
- **Type:** REST / Synchronous
- **Path Variable:** `status` (String)
- **Request Headers:** `X-User-Role` (optional)

**Request:**
None

**Response:**
*(List of PaymentResponse)*

---

### `PATCH` `/api/payments/{paymentId}/status`
- **API Name:** updatePaymentStatus
- **Type:** REST / Synchronous
- **Path Variable:** `paymentId` (UUID)
- **Request Headers:** `X-User-Role` (optional)

**Request:**
```json
{
  "status": "String - Required (e.g., PENDING, COMPLETED, FAILED)",
  "reason": "String",
  "notes": "String",
  "gatewayResponse": "String"
}
```

**Response:**
*(Same as `createPayment` PaymentResponse)*

---

### `POST` `/api/payments/{paymentId}/cancel`
- **API Name:** cancelPayment
- **Type:** REST / Synchronous
- **Path Variable:** `paymentId` (UUID)
- **Request Headers:** `X-User-Id` (optional), `X-User-Role` (optional)

**Request:**
```json
{
  "reason": "String"
}
```

**Response:**
*(Same as `createPayment` PaymentResponse)*

---

### `POST` `/api/payments/{paymentId}/retry`
- **API Name:** retryPayment
- **Type:** REST / Synchronous
- **Path Variable:** `paymentId` (UUID)
- **Request Headers:** `X-User-Id` (optional), `X-User-Role` (optional)

**Request:**
None

**Response:**
*(Same as `createPayment` PaymentResponse)*

---

### `GET` `/api/payments/statistics`
- **API Name:** getPaymentStatistics
- **Type:** REST / Synchronous
- **Query Parameters:** `startDate` (DateTime), `endDate` (DateTime)
- **Request Headers:** `X-User-Role` (optional)

**Request:**
None

**Response:**
*(Map of statistics)*

---

### `GET` `/api/payments/health`
- **API Name:** health
- **Type:** REST / Synchronous

**Request:**
None

**Response:**
```json
{
  "status": "String - UP",
  "service": "String - payment-service-payments",
  "timestamp": "DateTime"
}
```

---

## RefundController

### `POST` `/api/refunds`
- **API Name:** createRefund
- **Type:** REST / Synchronous
- **Request Headers:**
  - `X-User-Id` (String, optional)
  - `X-User-Role` (String, optional)

**Request:**
```json
{
  "paymentId": "UUID - Required",
  "amount": "BigDecimal - Required",
  "reason": "String - Required",
  "requestedBy": "UUID - Required",
  "notes": "String",
  "metadata": {}
}
```

**Response:**
*(RefundResponse object - see refunds array in PaymentResponse)*

---

### `GET` `/api/refunds`
- **API Name:** getAllRefunds
- **Type:** REST / Synchronous
- **Query Parameters:** `page` (int), `size` (int), `sortBy` (String), `sortDir` (String)
- **Request Headers:** `X-User-Role` (optional)

**Request:**
None

**Response:**
*(Page of RefundResponse)*

---

### `GET` `/api/refunds/{refundId}`
- **API Name:** getRefundById
- **Type:** REST / Synchronous
- **Path Variable:** `refundId` (UUID)
- **Request Headers:** `X-User-Id` (optional), `X-User-Role` (optional)

**Request:**
None

**Response:**
*(RefundResponse)*

---

### `GET` `/api/refunds/reference/{refundReference}`
- **API Name:** getRefundByReference
- **Type:** REST / Synchronous
- **Path Variable:** `refundReference` (String)
- **Request Headers:** `X-User-Id` (optional), `X-User-Role` (optional)

**Request:**
None

**Response:**
*(RefundResponse)*

---

### `GET` `/api/refunds/payment/{paymentId}`
- **API Name:** getRefundsByPaymentId
- **Type:** REST / Synchronous
- **Path Variable:** `paymentId` (UUID)
- **Request Headers:** `X-User-Id` (optional), `X-User-Role` (optional)

**Request:**
None

**Response:**
*(List of RefundResponse)*

---

### `GET` `/api/refunds/user/{userId}`
- **API Name:** getRefundsByUser
- **Type:** REST / Synchronous
- **Path Variable:** `userId` (UUID)
- **Request Headers:** `X-User-Id` (optional), `X-User-Role` (optional)

**Request:**
None

**Response:**
*(List of RefundResponse)*

---

### `GET` `/api/refunds/status/{status}`
- **API Name:** getRefundsByStatus
- **Type:** REST / Synchronous
- **Path Variable:** `status` (String)
- **Request Headers:** `X-User-Role` (optional)

**Request:**
None

**Response:**
*(List of RefundResponse)*

---

### `POST` `/api/refunds/{refundId}/approve`
- **API Name:** approveRefund
- **Type:** REST / Synchronous
- **Path Variable:** `refundId` (UUID)
- **Request Headers:** `X-User-Id` (optional), `X-User-Role` (optional)

**Request:**
None

**Response:**
*(RefundResponse)*

---

### `POST` `/api/refunds/{refundId}/reject`
- **API Name:** rejectRefund
- **Type:** REST / Synchronous
- **Path Variable:** `refundId` (UUID)
- **Request Headers:** `X-User-Id` (optional), `X-User-Role` (optional)

**Request:**
```json
{
  "reason": "String"
}
```

**Response:**
*(RefundResponse)*

---

### `GET` `/api/refunds/pending`
- **API Name:** getPendingRefunds
- **Type:** REST / Synchronous
- **Request Headers:** `X-User-Role` (optional)

**Request:**
None

**Response:**
*(List of RefundResponse)*

---

### `GET` `/api/refunds/completed`
- **API Name:** getCompletedRefunds
- **Type:** REST / Synchronous
- **Request Headers:** `X-User-Role` (optional)

**Request:**
None

**Response:**
*(List of RefundResponse)*

---

### `GET` `/api/refunds/failed`
- **API Name:** getFailedRefunds
- **Type:** REST / Synchronous
- **Request Headers:** `X-User-Role` (optional)

**Request:**
None

**Response:**
*(List of RefundResponse)*

---

### `GET` `/api/refunds/search`
- **API Name:** searchRefunds
- **Type:** REST / Synchronous
- **Query Parameters:** `query` (String)
- **Request Headers:** `X-User-Role` (optional)

**Request:**
None

**Response:**
*(List of RefundResponse)*

---

### `GET` `/api/refunds/filter`
- **API Name:** getRefundsWithFilters
- **Type:** REST / Synchronous
- **Query Parameters:** `status` (String), `requestedBy` (UUID), `approvedBy` (UUID), `minAmount` (BigDecimal), `maxAmount` (BigDecimal), `startDate` (DateTime), `endDate` (DateTime)
- **Request Headers:** `X-User-Role` (optional)

**Request:**
None

**Response:**
*(List of RefundResponse)*

---

### `GET` `/api/refunds/statistics`
- **API Name:** getRefundStatistics
- **Type:** REST / Synchronous
- **Query Parameters:** `startDate` (DateTime), `endDate` (DateTime)
- **Request Headers:** `X-User-Role` (optional)

**Request:**
None

**Response:**
*(Map of statistics)*

---

### `GET` `/api/refunds/health`
- **API Name:** health
- **Type:** REST / Synchronous

**Request:**
None

**Response:**
```json
{
  "status": "String - UP",
  "service": "String - payment-service-refunds",
  "timestamp": "DateTime"
}
```

---

## TransactionController

### `GET` `/api/transactions/{transactionId}`
- **API Name:** getTransactionById
- **Type:** REST / Synchronous
- **Path Variable:** `transactionId` (UUID)
- **Request Headers:** `X-User-Role` (optional)

**Request:**
None

**Response:**
*(PaymentTransactionResponse object - see transactions array in PaymentResponse)*

---

### `GET` `/api/transactions/payment/{paymentId}`
- **API Name:** getTransactionsByPaymentId
- **Type:** REST / Synchronous
- **Path Variable:** `paymentId` (UUID)
- **Request Headers:** `X-User-Role` (optional)

**Request:**
None

**Response:**
*(List of PaymentTransactionResponse)*

---

### `GET` `/api/transactions/status/{status}`
- **API Name:** getTransactionsByStatus
- **Type:** REST / Synchronous
- **Path Variable:** `status` (String)
- **Request Headers:** `X-User-Role` (optional)

**Request:**
None

**Response:**
*(List of PaymentTransactionResponse)*

---

### `GET` `/api/transactions/type/{type}`
- **API Name:** getTransactionsByType
- **Type:** REST / Synchronous
- **Path Variable:** `type` (String)
- **Request Headers:** `X-User-Role` (optional)

**Request:**
None

**Response:**
*(List of PaymentTransactionResponse)*

---

### `GET` `/api/transactions/pending`
- **API Name:** getPendingTransactions
- **Type:** REST / Synchronous
- **Request Headers:** `X-User-Role` (optional)

**Request:**
None

**Response:**
*(List of PaymentTransactionResponse)*

---

### `GET` `/api/transactions/failed`
- **API Name:** getFailedTransactions
- **Type:** REST / Synchronous
- **Request Headers:** `X-User-Role` (optional)

**Request:**
None

**Response:**
*(List of PaymentTransactionResponse)*

---

### `GET` `/api/transactions/statistics`
- **API Name:** getTransactionStatistics
- **Type:** REST / Synchronous
- **Query Parameters:** `startDate` (DateTime), `endDate` (DateTime)
- **Request Headers:** `X-User-Role` (optional)

**Request:**
None

**Response:**
*(Map of statistics)*

---

### `GET` `/api/transactions/health`
- **API Name:** health
- **Type:** REST / Synchronous

**Request:**
None

**Response:**
```json
{
  "status": "String - UP",
  "service": "String - payment-service-transactions",
  "timestamp": "DateTime"
}
```
