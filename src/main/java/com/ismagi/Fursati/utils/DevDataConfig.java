package com.ismagi.Fursati.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

/**
 * Configuration class to automatically generate test data in development environment
 */
@Configuration
@Profile("dev") // This will only be active in dev profile
public class DevDataConfig {

    private static final Logger logger = LoggerFactory.getLogger(DevDataConfig.class);

    @Autowired
    private PostgresDataGenerator dataGenerator;

    @Autowired
    private Environment environment;

    /**
     * CommandLineRunner bean that will execute after application startup
     * to generate test data in development environment
     */
    @Bean
    public CommandLineRunner generateDevData() {
        return args -> {
            logger.info("Development mode detected: Starting test data generation...");

            // First, update existing company records to set default values for is_verified
            // This helps prevent issues with adding NOT NULL constraints
            try {
                logger.info("Updating existing company records to ensure is_verified field is not null");
                dataGenerator.updateExistingCompanies();
            } catch (Exception e) {
                logger.warn("Could not update existing company records: {}", e.getMessage());
                // Continue with the rest of the initialization
            }

            // Default values for test data generation
            int adminCount = getPropertyIntValue("fursati.dev.testdata.admin-count", 3);
            int recruiterCount = getPropertyIntValue("fursati.dev.testdata.recruiter-count", 10);
            int candidateCount = getPropertyIntValue("fursati.dev.testdata.candidate-count", 25);
            int offersPerRecruiter = getPropertyIntValue("fursati.dev.testdata.offers-per-recruiter", 3);
            int companyCount = getPropertyIntValue("fursati.dev.testdata.company-count", 5);
            boolean clearExisting = getPropertyBooleanValue("fursati.dev.testdata.clear-existing", true);
            boolean enabled = getPropertyBooleanValue("fursati.dev.testdata.enabled", true);

            if (!enabled) {
                logger.info("Test data generation is disabled in configuration");
                return;
            }

            try {
                long startTime = System.currentTimeMillis();

                // Validate test data parameters
                if (recruiterCount > 0 && companyCount == 0) {
                    logger.warn("Adjusting company count to match recruiter count");
                    companyCount = Math.max(1, (int) Math.ceil(recruiterCount / 2.0));
                }

                dataGenerator.generateTestData(
                        adminCount,
                        recruiterCount,
                        candidateCount,
                        offersPerRecruiter,
                        companyCount,
                        clearExisting);

                long duration = System.currentTimeMillis() - startTime;

                logger.info("Test data generated successfully in {} ms.", duration);
                logger.info("Generated: {} admins, {} recruiters, {} candidates, {} job offers, {} companies",
                        adminCount, recruiterCount, candidateCount, recruiterCount * offersPerRecruiter, companyCount);
            } catch (Exception e) {
                logger.error("Failed to generate test data: {}", e.getMessage(), e);

                // Provide more detailed error information
                Throwable cause = e.getCause();
                if (cause != null) {
                    logger.error("Root cause: {}", cause.getMessage());
                }

                logger.info("Application will continue to start normally despite data generation failure");
            }
        };
    }

    /**
     * Helper method to get integer property with default value
     */
    private int getPropertyIntValue(String key, int defaultValue) {
        String value = environment.getProperty(key);
        if (value != null && !value.isEmpty()) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                logger.warn("Invalid integer value for {}: {}. Using default: {}", key, value, defaultValue);
            }
        }
        return defaultValue;
    }

    /**
     * Helper method to get boolean property with default value
     */
    private boolean getPropertyBooleanValue(String key, boolean defaultValue) {
        String value = environment.getProperty(key);
        if (value != null && !value.isEmpty()) {
            return Boolean.parseBoolean(value);
        }
        return defaultValue;
    }

}