# API Reference

## 🧁 Payment Service

### Payment Controller (`/api/payments`)
- `POST /api/payments` - Create a new payment.
- `GET /api/payments/{paymentId}` - Retrieve payment details by ID.
- `GET /api/payments/reference/{paymentReference}` - Retrieve payment details by reference.
- `GET /api/payments/order/{orderId}` - Retrieve payment details by order ID.
- `GET /api/payments/user/{userId}` - Retrieve payments by user ID (paginated).
- `POST /api/payments/{paymentId}/cancel` - Cancel a payment.
- `POST /api/payments/{paymentId}/retry` - Retry a payment.
- `POST /api/payments/{paymentId}/send-otp` - Send OTP for a payment.
- `POST /api/payments/{paymentId}/verify-otp` - Verify OTP for a payment.

### Payment Admin Controller (`/api/admin/payments`)
- `GET /api/admin/payments` - List all payments (Admin).
- `GET /api/admin/payments/status/{status}` - List payments by status (Admin).
- `PATCH /api/admin/payments/{paymentId}/status` - Update payment status (Admin).
- `GET /api/admin/payments/statistics` - Get payment statistics (Admin).

### Refund Controller (`/api/refunds`)
- `POST /api/refunds` - Create a new refund.
- `GET /api/refunds` - List all refunds (Admin).
- `GET /api/refunds/{refundId}` - Get refund details by ID.
- `GET /api/refunds/reference/{refundReference}` - Get refund details by reference.
- `GET /api/refunds/payment/{paymentId}` - Get refunds by payment ID.
- `GET /api/refunds/user/{userId}` - Get refunds by user ID.
- `GET /api/refunds/status/{status}` - Get refunds by status (Admin).
- `POST /api/refunds/{refundId}/approve` - Approve a refund (Admin).
- `POST /api/refunds/{refundId}/reject` - Reject a refund (Admin).
- `GET /api/refunds/pending` - List pending refunds (Admin).
- `GET /api/refunds/completed` - List completed refunds (Admin).
- `GET /api/refunds/failed` - List failed refunds (Admin).
- `GET /api/refunds/search` - Search refunds (Admin).
- `GET /api/refunds/filter` - Advanced search refunds (Admin).
- `GET /api/refunds/statistics` - Get refund statistics (Admin).

### Transaction Controller (`/api/transactions`)
- `GET /api/transactions/{transactionId}` - Get transaction by ID.
- `GET /api/transactions/payment/{paymentId}` - Get transactions by payment ID.
- `GET /api/transactions/status/{status}` - Get transactions by status (Admin).
- `GET /api/transactions/type/{type}` - Get transactions by type (Admin).
- `GET /api/transactions/pending` - List pending transactions (Admin).
- `GET /api/transactions/failed` - Get failed transactions (Admin).
- `GET /api/transactions/statistics` - Get transaction statistics (Admin).

### Payment OTP Controller (`/api/payments/mock`)
- `POST /api/payments/mock/{paymentId}/send-otp` - Send mock OTP.
- `POST /api/payments/mock/{paymentId}/verify-otp` - Verify mock OTP.
- `POST /api/payments/mock/{paymentId}/resend-otp` - Resend mock OTP.
