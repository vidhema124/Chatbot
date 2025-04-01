
package chatbot.config;

import com.stripe.Stripe;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
@ConfigurationProperties(prefix = "stripe") 
@Getter
@Setter
public class StripeConfig {
    private String secretKey;

    @PostConstruct
    public void init() {
        Stripe.apiKey = this.secretKey; 
    }
}
