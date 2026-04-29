package com.ctasmokers.common.config;

import com.ctasmokers.common.config.properties.OpenAPIProperties;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenAPIConfig {
    private final OpenAPIProperties openAPIProperties;
    private final BuildProperties buildProperties;

    @Autowired
    public OpenAPIConfig(
        OpenAPIProperties openAPIProperties,
        BuildProperties buildProperties
    ) {
        this.openAPIProperties = openAPIProperties;
        this.buildProperties = buildProperties;
    }

    @Bean
    public OpenAPI openAPI() {
        OpenAPIProperties.Contact contactProperties = this.openAPIProperties.contact();

        Contact contact = new Contact()
            .name(contactProperties.name())
            .email(contactProperties.email());

        OpenAPIProperties.License licenseProperties = this.openAPIProperties.license();

        License apacheLicense = new License()
            .name(licenseProperties.name())
            .url(licenseProperties.url());

        OpenAPIProperties.Info infoProperties = this.openAPIProperties.info();
        String version = this.buildProperties.getVersion();

        Info apiInfo = new Info()
            .title(infoProperties.title())
            .description(infoProperties.description())
            .version(version)
            .contact(contact)
            .license(apacheLicense);

        OpenAPIProperties.Server serverProperties = this.openAPIProperties.server();

        Server productionServer = new Server()
            .url(serverProperties.url())
            .description(serverProperties.description());

        return new OpenAPI().info(apiInfo)
                            .servers(List.of(productionServer));
    }
}
