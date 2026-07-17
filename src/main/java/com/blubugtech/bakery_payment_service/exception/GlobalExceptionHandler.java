package com.blubugtech.bakery_payment_service.exception;

import com.blubugtech.common.exception.handler.BaseExceptionHandler;

import com.blubugtech.bakery_payment_service.enums.PaymentStatus;
import com.blubugtech.bakery_payment_service.exception.payment.*;
import com.blubugtech.bakery_payment_service.exception.refund.*;
import com.blubugtech.bakery_payment_service.exception.order.*;
import com.blubugtech.bakery_payment_service.enums.ErrorCode;

import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.blubugtech.common.exception.handler.ErrorResponse;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;



@RestControllerAdvice
public class GlobalExceptionHandler extends BaseExceptionHandler  {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(PaymentServiceException.class)
    public ResponseEntity<ErrorResponse> handlePaymentServiceException(PaymentServiceException ex, WebRequest request) {
        logger.error("Payment service error: {}", ex.getMessage());

        ErrorResponse error = new ErrorResponse(
            ErrorCode.PAYMENT_SERVICE_ERROR.name(),
            ex.getMessage(),
            LocalDateTime.now(),
            request.getDescription(false)
        );

        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<ErrorResponse> handleFeignException(FeignException ex, WebRequest request) {
        logger.error("External service error: {}", ex.getMessage());

        String message = "External service unavailable";
        HttpStatus status = HttpStatus.SERVICE_UNAVAILABLE;

        if (ex.status() == 404) {
            message = "Requested resource not found";
            status = HttpStatus.NOT_FOUND;
        } else if (ex.status() == 400) {
            message = "Invalid request to external service";
            status = HttpStatus.BAD_REQUEST;
        }

        ErrorResponse error = new ErrorResponse(
            ErrorCode.EXTERNAL_SERVICE_ERROR.name(),
            message,
            LocalDateTime.now(),
            request.getDescription(false)
        );

        return ResponseEntity.status(status).body(error);
    }

    

    

    // Error Response Class
    
    @ExceptionHandler(PaymentNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePaymentNotFoundException(PaymentNotFoundException ex, WebRequest request) {
        ErrorResponse error = new ErrorResponse(ErrorCode.PAYMENT_NOT_FOUND.name(), ex.getMessage(), LocalDateTime.now(), request.getDescription(false));
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleOrderNotFoundException(OrderNotFoundException ex, WebRequest request) {
        ErrorResponse error = new ErrorResponse(ErrorCode.ORDER_NOT_FOUND.name(), ex.getMessage(), LocalDateTime.now(), request.getDescription(false));
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    @ExceptionHandler({InvalidPaymentAmountException.class, InvalidPaymentStatusException.class, InvalidRefundException.class})
    public ResponseEntity<ErrorResponse> handleInvalidPaymentException(RuntimeException ex, WebRequest request) {
        ErrorResponse error = new ErrorResponse(ErrorCode.INVALID_PAYMENT_REQUEST.name(), ex.getMessage(), LocalDateTime.now(), request.getDescription(false));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    

}

