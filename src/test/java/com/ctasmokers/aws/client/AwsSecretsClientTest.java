package com.ctasmokers.aws.client;

import com.amazonaws.secretsmanager.caching.SecretCache;
import com.ctasmokers.aws.dto.Secret;
import com.ctasmokers.aws.exception.SecretsClientException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AwsSecretsClientTest {
    private static final String APP_SECRET_ID = "app-secret-id";

    @Mock
    private SecretCache secretCache;

    private AwsSecretsClient client;

    @BeforeEach
    void setUp() {
        client = new AwsSecretsClient(secretCache, JsonMapper.shared(), APP_SECRET_ID);
    }

    @Test
    void getAppSecret_validJson_returnsParsedSecret() {
        String json = """
        {"cloudflare":{"originVerify":"test-origin-verify"},"rollbar":{"accessToken":"test-access-token"}}
        """;

        when(secretCache.getSecretString(APP_SECRET_ID)).thenReturn(json);

        Secret secret = client.getAppSecret();

        assertThat(secret.cloudflare().originVerify()).isEqualTo("test-origin-verify");
        assertThat(secret.rollbar().accessToken()).isEqualTo("test-access-token");
    }

    @Test
    void getAppSecret_malformedJson_throwsSecretsClientException() {
        when(secretCache.getSecretString(APP_SECRET_ID)).thenReturn("not-valid-json");

        assertThatThrownBy(() -> client.getAppSecret())
            .isInstanceOf(SecretsClientException.class)
            .hasMessage("Failed to parse application secret JSON")
            .hasCauseInstanceOf(JacksonException.class);
    }
}