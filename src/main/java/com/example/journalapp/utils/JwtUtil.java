package com.example.journalapp.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    @Value("${jwt.secret}") // “Take the value from application.properties where key = jwt.secret.”
    private String SECRET_KEY;

    private SecretKey getSigningKey() {
        // The getSigningKey() method converts your string key into a SecretKey object using:
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes()); //  We have converted our SECRET_KEY to Keys type object so that we can use it in signWith() method.
    }

    public String extractUsername(String token) {
        Claims claims = extractAllClaims(token);
        return claims.getSubject();
    }

    public Date extractExpiration(String token) {
        return extractAllClaims(token).getExpiration();
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser() // Creates a parser object (similar to builder but for reading tokens). But this parser doesn't know your secret key yet.
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token) // This method does 3 things: 1️⃣ Splits the token into header.payload.signature 2️⃣ Recomputes the signature using your secret key 3️⃣ Compares the recomputed signature with the token signature
                .getPayload();
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public String generateToken(String username) {
        /*
        Think of claims as “extra information inside JWT payload”.
        Examples:
        claims.put("role", "ADMIN");
        claims.put("email", "vedansh@gmail.com");
        claims.put("isPremium", true);
        */
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, username);
    }

    // Payload = “claims + subject + issuedAt + expiration”.
    public String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()   // A "builder" is just a normal object that helps you build another thing step-by-step, using a clean chain of methods.
                .claims(claims) // Whatever is inside the claims map will be added to the payload of the token.
                .subject(subject)
                .header().empty().add("typ","JWT")
                .and() // .header() → go into header section, .empty() → start with empty header, .add("typ","JWT") → add type, .and() → go back to main builder
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
                .signWith(getSigningKey()) // Signature = HMACSHA256(header + payload + SECRET_KEY)
                .compact(); // FINALLY when you do this step, the library encodes header, payload, signs it, combines them and return a JWT string
    }
    // It’s instructions to the JWT library telling how to build it. The order of your method calls is irrelevant.

    public Boolean validateToken(String token) {
        return !isTokenExpired(token);
    }
}
