package br.com.sicred.assemblyvote.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${doc-application.description}")
    private String description;

    @Value("${doc-application.version}")
    private String version;

    @Bean
    OpenAPI openApi() {
        final var openAPI = new OpenAPI();

        final var info = new Info();
        info.title(applicationName);
        info.description(description);
        info.version(version);

        return openAPI.info(info);
    }
}
