package com.ecommerce.fitgearback;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class FitgearBackApplication {

    public static void main(String[] args) {
        SpringApplication.run(FitgearBackApplication.class, args);
    }

}
