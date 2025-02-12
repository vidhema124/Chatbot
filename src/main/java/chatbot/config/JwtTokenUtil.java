package chatbot.config;

import java.util.Date;
import javax.crypto.SecretKey;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

public class JwtTokenUtil {

    public static String generateJwtToken() { 
        SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS256); 
        String token = Jwts.builder()
                .setIssuedAt(new Date()) 
                .setExpiration(new Date(System.currentTimeMillis() + 3600000)) 
                .signWith(key, SignatureAlgorithm.HS256)  
                .compact();  
        return token;  
    }
}
