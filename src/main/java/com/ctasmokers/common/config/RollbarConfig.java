package com.ctasmokers.common.config;

import com.ctasmokers.aws.client.AwsSecretsClient;
import com.rollbar.notifier.Rollbar;
import com.rollbar.notifier.config.Config;
import com.rollbar.notifier.config.ConfigBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RollbarConfig {
    private final AwsSecretsClient awsSecretsClient;

    private final String environment;
    private final String codeVersion;

    @Autowired
    public RollbarConfig(
        AwsSecretsClient awsSecretsClient,
        @Value("${app.rollbar.environment}") String environment,
        @Value("${app.version}") String codeVersion
    ) {
        this.awsSecretsClient = awsSecretsClient;
        this.environment = environment;
        this.codeVersion = codeVersion;
    }

    @Bean
    public Rollbar rollbar() {
        String accessToken = this.awsSecretsClient.getAppSecret()
                                                  .rollbar()
                                                  .accessToken();


        Config config = ConfigBuilder.withAccessToken(accessToken)
                                     .environment(this.environment)
                                     .codeVersion(this.codeVersion)
                                     .build();

        return Rollbar.init(config);
    }
}
