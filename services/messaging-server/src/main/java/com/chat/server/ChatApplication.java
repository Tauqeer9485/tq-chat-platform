package com.chat.server;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.SpringApplication;

import java.util.logging.Logger;

/**
 * Spring Boot Configuration and Entry Point
 * Runs REST API on port 8081 while Netty WebSocket runs on 8080
 */
@SpringBootApplication
public class ChatApplication {
    private static final Logger logger = Logger.getLogger(ChatApplication.class.getName());

    public static void main(String[] args) {
        SpringApplication.run(ChatApplication.class, args);

        logger.info("\n===============================================");
        logger.info("Spring Boot REST API started on port 8081");
        logger.info("REST Endpoints:");
        logger.info("  POST /api/auth/signup");
        logger.info("  POST /api/auth/login");
        logger.info("  POST /api/auth/verify");
        logger.info("===============================================\n");

        try {
            logger.info("Starting Health Check Server on port 9090...");
            HealthCheckServer.start();
            logger.info("Starting Netty WebSocket Server on port 8080...");
        } catch (Exception e) {
            logger.severe("Failed to start server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
