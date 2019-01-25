package com.example.spring.boot.security.jwt.service;

import com.example.spring.boot.security.jwt.exception.UnAuthenticationException;
import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Component
public class JwtTokenProvider {
    @Value("${jwt.secret:hello}")
    private String secretKey;
    @Value("${jwt.expiration.time:2}")
    private Long expirationTime;

    public String encode(Authentication auth) {
        org.springframework.security.core.userdetails.User user = (User) auth.getPrincipal();
        LocalDate localDate = LocalDate.now();
        Date now = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        LocalDate expiredLocalDate = localDate.plus(expirationTime, ChronoUnit.DAYS);
        Date expiredDate = Date.from(expiredLocalDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        return Jwts.builder()
                .setSubject(user.getUsername())
                .setIssuedAt(now)
                .setExpiration(expiredDate)
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    public String decode(String token) {
        String username;
        try {
            Claims claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody();
            username = claims.getSubject();
        } catch (ExpiredJwtException e) {
            throw new UnAuthenticationException("expired token");
        } catch (UnsupportedJwtException e) {
            throw new UnAuthenticationException("unsupported token");
        } catch (MalformedJwtException e) {
            throw new UnAuthenticationException("malformed token");
        } catch (SignatureException | IllegalArgumentException e) {
            throw new UnAuthenticationException("Invalid token");
        }
        return username;
    }

}
