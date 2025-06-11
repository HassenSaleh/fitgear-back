package com.ecommerce.fitgearback;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
@OpenAPIDefinition(info = @Info(title = "Fitgear API", version = "1.0", description = "API documentation for Fitgear e-commerce application"))
public class FitgearBackApplication {

    public static void main(String[] args) {
        SpringApplication.run(FitgearBackApplication.class, args);
    }

}
