package chatbot.config;

//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.PropertySource;
//
//@Configuration
//@PropertySource("classpath:application.properties")  // Ensure properties file is loaded
//public class GeminiConfig {
//
//    @Value("${gemini.api.url}")
//    private String url;
//
//    @Value("${gemini.api.key}")
//    private String key;
//
//    public String getUrl() {
//        return url;
//    }
//
//    public String getKey() {
//        return key;
//    }
//}

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class GeminiConfig {

    private final String url;
    private final String key;

    public GeminiConfig(
        @Value("${gemini.api.url}") String url,
        @Value("${gemini.api.key}") String key
    ) {
        this.url = url;
        this.key = key;
    }

    public String getUrl() {
        return url;
    }

    public String getKey() {
        return key;
    }

}
