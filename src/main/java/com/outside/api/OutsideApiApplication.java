package com.outside.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class OutsideApiApplication {
    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(OutsideApiApplication.class, args);
        
        // Debug: Print all bean names to see what Spring found
        String[] beanNames = context.getBeanDefinitionNames();
        System.out.println("\n=== ALL SPRING BEANS ===");
        for (String name : beanNames) {
            if (name.contains("Venue") || name.contains("Repository") || name.contains("Service")) {
                System.out.println("FOUND: " + name);
            }
        }
        System.out.println("=== END BEANS ===\n");
    }
}