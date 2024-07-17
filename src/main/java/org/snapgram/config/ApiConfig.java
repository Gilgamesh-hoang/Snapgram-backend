package org.snapgram.config;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApiConfig {

    @Value("${API_PREFIX}")
    @Getter
    private String apiPrefix;
}

