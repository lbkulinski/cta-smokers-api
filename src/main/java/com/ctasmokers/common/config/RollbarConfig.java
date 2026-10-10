package com.ctasmokers.common.config;

import com.ctasmokers.aws.client.SecretsClient;
import com.rollbar.notifier.Rollbar;
import com.rollbar.notifier.config.ConfigBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RollbarConfig {
    @Bean
    public Rollbar rollbar(
        SecretsClient secretsClient,
        BuildProperties buildProperties,
        @Value("${app.rollbar.environment}") String environment
    ) {
        String accessToken = secretsClient.getAppSecret()
                                          .rollbar()
                                          .accessToken();
        String codeVersion = buildProperties.getVersion();

        var config = ConfigBuilder.withAccessToken(accessToken)
                                  .environment(environment)
                                  .codeVersion(codeVersion)
                                  .build();

        return Rollbar.init(config);
    }
}
