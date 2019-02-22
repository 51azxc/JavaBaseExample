package com.example.spring.boot.webflux.security.jwt.service;

import io.jsonwebtoken.*;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class TokenProvider {

    private final static String SECRET_KEY = "HelloWorld";
    private final static int EXPIRATION_DAY = 7;
    private final static String PAYLOAD_ROLES = "roles";

    public String encode(Authentication auth) {
        String subject = String.valueOf(auth.getPrincipal());
        List<String> roles = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).collect(Collectors.toList());

        LocalDate localDate = LocalDate.now();
        Date now = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        LocalDate expiredLocalDate = localDate.plusDays(EXPIRATION_DAY);
        Date expiredDate = Date.from(expiredLocalDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

        return Jwts.builder()
                .setSubject(subject)
                .claim(PAYLOAD_ROLES, roles)
                .setIssuedAt(now)
                .setExpiration(expiredDate)
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

    public Authentication decode(String token) {
        Claims claims;
        try {
            claims = Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token).getBody();
        } catch (ExpiredJwtException e) {
            throw new BadCredentialsException("Expired token");
        } catch (UnsupportedJwtException e) {
            throw new BadCredentialsException("Unsupported token");
        } catch (MalformedJwtException e) {
            throw new BadCredentialsException("Malformed token");
        } catch (SignatureException | IllegalArgumentException e) {
            throw new BadCredentialsException("Invalid token");
        }
        List<String> roles = (List<String>)claims.get(PAYLOAD_ROLES, List.class);
        List<GrantedAuthority> authorities = roles.stream()
                .map(SimpleGrantedAuthority::new).collect(Collectors.toList());
        return new UsernamePasswordAuthenticationToken(claims.getSubject(), token, authorities);
    }
}
