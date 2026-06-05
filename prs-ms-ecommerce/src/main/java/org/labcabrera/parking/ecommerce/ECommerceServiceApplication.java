package org.labcabrera.parking.ecommerce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ECommerceServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ECommerceServiceApplication.class, args);
    }
}
