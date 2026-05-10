package com.ctasmokers;

import com.ctasmokers.aws.config.DynamoDbTableProperties;
import com.ctasmokers.common.config.properties.CorsProperties;
import com.ctasmokers.common.config.properties.OpenAPIProperties;
import com.ctasmokers.smoking.report.config.CtaReportsProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({
    CorsProperties.class,
    OpenAPIProperties.class,
    DynamoDbTableProperties.class,
    CtaReportsProperties.class
})
public class Application {
    static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
