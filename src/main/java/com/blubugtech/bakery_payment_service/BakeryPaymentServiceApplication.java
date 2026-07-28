package com.blubugtech.bakery_payment_service;

import org.blubakery.bakery_common_libs.feign.FeignConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.blubakery.bakery_common_libs.security.MethodSecurityConfig;
import org.blubakery.bakery_common_libs.security.FeignClientInterceptor;
import org.blubakery.bakery_common_libs.kafka.KafkaConfig;
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
