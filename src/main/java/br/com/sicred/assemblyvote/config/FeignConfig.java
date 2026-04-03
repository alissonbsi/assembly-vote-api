package br.com.sicred.assemblyvote.config;

import br.com.sicred.assemblyvote.exception.CustomFeignClientException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Logger;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static feign.FeignException.errorStatus;

@Configuration
@RequiredArgsConstructor
public class FeignConfig {

    private final ObjectMapper objectMapper;

    @Value("${feign.clients.default-logger-level}")
    private Logger.Level defaultLoggerLevel;

    @Bean
    ErrorDecoder errorDecoder() {
        return this::decode;
    }

    @Bean
    Logger.Level loggerLevel() {
        return defaultLoggerLevel;
    }

    @SneakyThrows
    private Exception decode(final String methodKey, final Response response) {
        if (response.body() != null) {
            return objectMapper.readValue(response.body().asInputStream(), CustomFeignClientException.class);
        }
        return errorStatus(methodKey, response);
    }
}
