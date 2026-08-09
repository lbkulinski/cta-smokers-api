package com.ctasmokers.aws.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SecretTest {
    @Test
    void toString_redactsCloudflareAndRollbarSecrets() {
        Secret secret = new Secret(
            new Secret.CloudflareSecret("real-origin-verify-value"),
            new Secret.RollbarSecret("real-access-token-value")
        );

        String result = secret.toString();

        assertThat(result).doesNotContain("real-origin-verify-value", "real-access-token-value");
        assertThat(result).contains("REDACTED");
    }
}