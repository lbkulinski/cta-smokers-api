package com.ctasmokers.smoking.report.config;

import com.cta4j.train.station.StationsApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StationsApiConfig {
    @Bean
    public StationsApi stationsApi() {
        return StationsApi.builder()
                          .build();
    }
}
