package com.ctasmokers.common.config.properties;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.NullMarked;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties("app.openapi")
@Validated
@NullMarked
public record OpenAPIProperties(
    @NotNull @Valid Info info,
    @NotNull @Valid Contact contact,
    @NotNull @Valid License license,
    @NotNull @Valid Server server
) {
    public record Info(
        @NotBlank String title,
        @NotBlank String description
    ) {}

    public record Contact(
        @NotBlank String name,
        @NotBlank String email
    ) {}

    public record License(
        @NotBlank String name,
        @NotBlank String url
    ) {}

    public record Server(
        @NotBlank String url,
        @NotBlank String description
    ) {}
}
