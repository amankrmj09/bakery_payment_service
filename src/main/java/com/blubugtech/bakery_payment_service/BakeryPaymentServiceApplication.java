package com.blubugtech.bakery_payment_service;

import org.blubakery.common.feign.feign.FeignConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.blubakery.common.security.security.MethodSecurityConfig;
import org.blubakery.common.feign.security.FeignClientInterceptor;
import org.blubakery.common.messaging.kafka.KafkaConfig;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@Import({MethodSecurityConfig.class, FeignClientInterceptor.class, KafkaConfig.class, FeignConfig.class})
@EnableAsync
@EnableScheduling
@EnableDiscoveryClient
@EnableFeignClients
public class BakeryPaymentServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(BakeryPaymentServiceApplication.class, args);
    }

}
