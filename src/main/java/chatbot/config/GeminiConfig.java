package chatbot.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:application.properties")  // Ensure properties file is loaded
public class GeminiConfig {

    @Value("${gemini.api.url}")
    private String url;

    @Value("${gemini.api.key}")
    private String key;

    public String getUrl() {
        return url;
    }

    public String getKey() {
        return key;
    }
}
