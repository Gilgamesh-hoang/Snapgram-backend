package org.snapgram.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class EnvConfig {
    @Bean
    public Dotenv dotenv(ConfigurableEnvironment environment) {

        Dotenv dotenv = Dotenv.configure().directory("./").filename(".env").ignoreIfMissing().load();

        Map<String, Object> envMap = new HashMap<>();
        envMap.put("JWT_PRIVATE_KEY", dotenv.get("JWT_PRIVATE_KEY"));

        environment.getPropertySources().addFirst(new MapPropertySource("dotenvProperties", envMap));
        return dotenv;
    }

}