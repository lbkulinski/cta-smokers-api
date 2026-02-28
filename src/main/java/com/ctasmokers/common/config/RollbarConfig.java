package com.ctasmokers.common.config;

import com.ctasmokers.aws.client.AwsSecretsClient;
import com.rollbar.notifier.Rollbar;
import com.rollbar.notifier.config.Config;
import com.rollbar.notifier.config.ConfigBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RollbarConfig {
    private final AwsSecretsClient awsSecretsClient;
    private final BuildProperties buildProperties;

    private final String environment;

    @Autowired
    public RollbarConfig(
        AwsSecretsClient awsSecretsClient,
        BuildProperties buildProperties,
        @Value("${app.rollbar.environment}") String environment
    ) {
        this.awsSecretsClient = awsSecretsClient;
        this.buildProperties = buildProperties;
        this.environment = environment;
    }

    @Bean
    public Rollbar rollbar() {
        String accessToken = this.awsSecretsClient.getAppSecret()
                                                  .rollbar()
                                                  .accessToken();
        String codeVersion = this.buildProperties.getVersion();


        Config config = ConfigBuilder.withAccessToken(accessToken)
                                     .environment(this.environment)
                                     .codeVersion(codeVersion)
                                     .build();

        return Rollbar.init(config);
    }
}
