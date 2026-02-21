package com.ctasmokers.aws.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;

@Configuration
public class SecretsManagerClientConfig {
    private final Region region;

    @Autowired
    public SecretsManagerClientConfig(Region region) {
        this.region = region;
    }

    @Bean
    public SecretsManagerClient secretsManagerClient() {
        return SecretsManagerClient.builder()
                                   .region(this.region)
                                   .build();
    }
}
