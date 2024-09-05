package com.tengo.book.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;

@OpenAPIDefinition(
        info = @Info(
                contact = @Contact(
                        name = "Tengo",
                        email = "tengo@test.com", // its fake
                        url = "https://tengo.com" // its fake
                ),
                description = "Book Network API Documentation",
                title = "Book Network API specification",
                version = "1.0.0",
                license = @License(
                        name = "Licence name",
                        url = "https://some-licence.com" // its fake
                ),
                termsOfService = "Terms of service"
        ),
        servers = {
                @Server(
                        description = "Local server",
                        url = "http://localhost:8088/api/v1"
                ),
                @Server(
                        description = "Production server",
                        url = "https://book-network.com/api/v1" // its fake
                )
        },
        security = {
                @SecurityRequirement(
                        name = "bearerAuth"
                )
        }
)
@SecurityScheme(
        name = "bearerAuth",
        description = "JWT auth token",
        scheme = "bearer",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
)
public class OpenApiConfig {

}
