package com.tocadiscojp.seplagbackend.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.tocadiscojp.seplagbackend.model.Usuario;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class TokenService {
    @Value("${api.security.token.secret}")
    private String secret;

    public String gerarToken(Usuario usuario) {
        long expiracao = 5 * 60 * 1000; // 5 minutos
        return gerarTokenGenerico(usuario, expiracao);
    }

    public String gerarRefreshToken(Usuario usuario) {
        long expiracao = 30 * 60 * 1000; // 30 minutos
        return gerarTokenGenerico(usuario, expiracao);
    }

    private String gerarTokenGenerico(Usuario usuario, long tempoExpiracao) {
        Date expirationDate = new Date(System.currentTimeMillis() + tempoExpiracao);

        return Jwts.builder()
                .setIssuer("API Seplag")
                .setSubject(usuario.getUsername())
                .claim("id", usuario.getId())
                .setExpiration(expirationDate)
                .signWith(getChave(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String getSubject(String tokenJWT) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getChave())
                    .build()
                    .parseClaimsJws(tokenJWT)
                    .getBody()
                    .getSubject();
        } catch (Exception exception) {
            return null;
        }
    }

    private SecretKey getChave() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}