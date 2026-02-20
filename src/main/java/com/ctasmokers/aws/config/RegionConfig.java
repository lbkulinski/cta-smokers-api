package com.ctasmokers.aws.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;

@Configuration
public class RegionConfig {
    private final String awsRegion;

    @Autowired
    public RegionConfig(@Value("${app.aws.region}") String awsRegion) {
        this.awsRegion = awsRegion;
    }

    @Bean
    public Region region() {
        return Region.of(this.awsRegion);
    }
}
