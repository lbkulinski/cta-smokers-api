package com.ctasmokers.aws.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;

@Configuration
public class RegionConfig {
    @Bean
    public Region region(@Value("${app.aws.region}") String region) {
        return Region.of(region);
    }
}
