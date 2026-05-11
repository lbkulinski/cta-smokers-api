package com.ctasmokers.aws.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;

@Configuration
public class SecretsManagerClientConfig {
    @Bean
    public SecretsManagerClient secretsManagerClient(Region region) {
        return SecretsManagerClient.builder()
                                   .region(region)
                                   .build();
    }
}
