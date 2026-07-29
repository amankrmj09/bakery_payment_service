package com.blubugtech.bakery_payment_service.client;

import lombok.extern.slf4j.Slf4j;
import org.blubakery.common.core.exception.common.ServiceUnavailableException;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class UserClientFallbackFactory implements FallbackFactory<UserClient> {
    @Override
    public UserClient create(Throwable cause) {
        return new UserClient() {
            @Override
            public UserDto getUserById(UUID id) {
                log.error("Error calling user service for user id: {}", id, cause);
                throw new ServiceUnavailableException("User service is currently unavailable. Please try again later.");
            }
        };
    }
}
