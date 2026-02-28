package com.ctasmokers.common.config.properties;

import lombok.Data;
import org.jspecify.annotations.NullMarked;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@NullMarked
@Configuration
@ConfigurationProperties("app.openapi")
public class OpenAPIProperties {
    Info info;

    Contact contact;

    License license;

    Server server;

    @Data
    public static class Info {
        String title;
        String description;
        String version;
    }

    @Data
    public static class Contact {
        String name;
        String email;
    }

    @Data
    public static class License {
        String name;
        String url;
    }

    @Data
    public static class Server {
        String url;
        String description;
    }
}
