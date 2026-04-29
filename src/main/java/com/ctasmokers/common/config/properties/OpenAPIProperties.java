package com.ctasmokers.common.config.properties;

import org.jspecify.annotations.NullMarked;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.openapi")
@NullMarked
public record OpenAPIProperties(
    Info info,
    Contact contact,
    License license,
    Server server
) {
    public record Info(
        String title,
        String description
    ) {}

    public record Contact(
        String name,
        String email
    ) {}

    public record License(
        String name,
        String url
    ) {}

    public record Server(
        String url,
        String description
    ) {}
}
