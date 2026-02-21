package com.ctasmokers.aws.client;

import com.amazonaws.secretsmanager.caching.SecretCache;
import com.ctasmokers.aws.dto.Secret;
import com.ctasmokers.aws.exception.SecretsClientException;
import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Component
@NullMarked
public final class AwsSecretsClient {
    private final SecretCache secretCache;
    private final ObjectMapper objectMapper;

    private final String appSecretId;

    @Autowired
    public AwsSecretsClient(
        SecretsManagerClient secretsManagerClient,
        ObjectMapper objectMapper,
        @Value("${app.aws.secrets-manager.id}") String appSecretId
    ) {
        this.secretCache = new SecretCache(secretsManagerClient);
        this.objectMapper = objectMapper;
        this.appSecretId = appSecretId;
    }

    public Secret getAppSecret() {
        String secretString = this.secretCache.getSecretString(this.appSecretId);

        Secret secret;

        try {
            secret = this.objectMapper.readValue(secretString, Secret.class);
        } catch (JacksonException e) {
            throw new SecretsClientException("Failed to parse application secret JSON", e);
        }

        return secret;
    }
}
