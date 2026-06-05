package org.labcabrera.parking.facilities;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FacilitiesServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(FacilitiesServiceApplication.class, args);
    }
}
