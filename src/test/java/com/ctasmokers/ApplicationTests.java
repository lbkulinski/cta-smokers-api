package com.ctasmokers;

import com.ctasmokers.aws.client.AwsSecretsClient;
import com.ctasmokers.aws.dto.Secret;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

@SpringBootTest
@Import(ApplicationTests.TestConfig.class)
class ApplicationTests {
    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        AwsSecretsClient awsSecretsClient() {
            AwsSecretsClient client = Mockito.mock(AwsSecretsClient.class);

            Mockito.when(client.getAppSecret())
                   .thenReturn(new Secret(
                       new Secret.CloudflareSecret("test-origin-verify")
                   ));

            return client;
        }
    }

    @Test
    void contextLoads() {
    }
}
