package com.hostistock.security;

import com.hostistock.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Date;

@Component
public class JwtUtil {
    private final JwtProperties props;
    private SecretKey clave;

    public JwtUtil(JwtProperties props) {
        this.props = props;
    }

    @PostConstruct
    public void init() {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(props.secreto().getBytes(StandardCharsets.UTF_8));
            this.clave = new SecretKeySpec(hash, "HmacSHA256");
        } catch (Exception e) {
            throw new RuntimeException("Error al inicializar la clave JWT", e);
        }
    }

    public String generarToken(Long barId, String email) {
        Date ahora = new Date();
        Date expiracion = new Date(ahora.getTime() + props.expiracionMs());

        return Jwts.builder()
            .subject(email)
            .claim("barId", barId)
            .issuedAt(ahora)
            .expiration(expiracion)
            .signWith(clave)
            .compact();
    }
    
    private Claims parsearClaims(String token) {
        return Jwts.parser()
            .verifyWith(clave)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }
    
    public Long obtenerBarId(String token) {
        return parsearClaims(token).get("barId", Long.class);
    }
    
    public boolean validarToken(String token) {
        try {
            parsearClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}