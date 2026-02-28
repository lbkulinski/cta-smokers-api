package com.ctasmokers.common.config;

import com.ctasmokers.common.config.properties.OpenAPIProperties;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenAPIConfig {
    private final OpenAPIProperties openAPIProperties;

    @Autowired
    public OpenAPIConfig(OpenAPIProperties openAPIProperties) {
        this.openAPIProperties = openAPIProperties;
    }

    @Bean
    public OpenAPI openAPI() {
        Contact contact = new Contact()
            .name(this.openAPIProperties.getContact()
                                        .getName())
            .email(this.openAPIProperties.getContact()
                                         .getEmail());

        License apacheLicense = new License()
            .name(this.openAPIProperties.getLicense()
                                        .getName())
            .url(this.openAPIProperties.getLicense()
                                       .getUrl());

        Info apiInfo = new Info()
            .title(this.openAPIProperties.getInfo()
                                         .getTitle())
            .description(this.openAPIProperties.getInfo()
                                               .getDescription())
            .version(this.openAPIProperties.getInfo()
                                           .getVersion())
            .contact(contact)
            .license(apacheLicense);

        Server productionServer = new Server()
            .url(this.openAPIProperties.getServer()
                                        .getUrl())
            .description(this.openAPIProperties.getServer()
                                               .getDescription());

        return new OpenAPI().info(apiInfo)
                            .servers(List.of(productionServer));
    }
}
