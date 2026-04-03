package br.com.sicred.assemblyvote.client.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.util.TimeZone;

@Configuration
public class ObjectMapperConfiguration {

    @Autowired
    public ObjectMapper objectMapper;

    @PostConstruct
    void objectMapperSetup() {
        objectMapper.setTimeZone(TimeZone.getDefault());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
}
