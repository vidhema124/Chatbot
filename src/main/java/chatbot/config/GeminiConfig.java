package chatbot.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import lombok.Getter;
import lombok.Setter;

@Configuration
@ConfigurationProperties(prefix = "gemini.api") // ✅ Ensure prefix matches properties file
@Getter
@Setter
public class GeminiConfig {
	@Value("${gemini.api.key}")
	private String geminiApiKey;

	@Value("${gemini.api.url}")
	private String geminiApiUrl;

}
