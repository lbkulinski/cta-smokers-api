package com.ctasmokers.aws.config;

import com.amazonaws.secretsmanager.caching.SecretCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;

@Configuration
public class SecretCacheConfig {
    @Bean
    public SecretCache secretCache(SecretsManagerClient secretsManagerClient) {
        return new SecretCache(secretsManagerClient);
    }
}
