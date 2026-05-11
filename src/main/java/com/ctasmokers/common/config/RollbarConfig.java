package com.ctasmokers.common.config;

import com.ctasmokers.aws.client.AwsSecretsClient;
import com.rollbar.notifier.Rollbar;
import com.rollbar.notifier.config.Config;
import com.rollbar.notifier.config.ConfigBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RollbarConfig {
    @Bean
    public Rollbar rollbar(
        AwsSecretsClient awsSecretsClient,
        BuildProperties buildProperties,
        @Value("${app.rollbar.environment}") String environment
    ) {
        String accessToken = awsSecretsClient.getAppSecret()
                                             .rollbar()
                                             .accessToken();
        String codeVersion = buildProperties.getVersion();

        Config config = ConfigBuilder.withAccessToken(accessToken)
                                     .environment(environment)
                                     .codeVersion(codeVersion)
                                     .build();

        return Rollbar.init(config);
    }
}
