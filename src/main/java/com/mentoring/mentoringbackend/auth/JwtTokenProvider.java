package com.mentoring.mentoringbackend.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import com.mentoring.mentoringbackend.user.domain.Role;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final UserDetailsService userDetailsService;
    private final SecretKey key;
    private final long validityInMillis;

    public JwtTokenProvider(
            UserDetailsService userDetailsService,
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration-ms:3600000}") long validityInMillis
    ) {
        this.userDetailsService = userDetailsService;
        // HS256용으로 최소 32바이트 이상 secret를 넣어야 안전함
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.validityInMillis = validityInMillis;
    }

    /** Access Token 생성 */
    public String createToken(Long userId, String email, String role) {
        Instant now = Instant.now();
        Instant expiry = now.plusMillis(validityInMillis);

        return Jwts.builder()
                .subject(email)
                .claim("uid", userId)
                .claim("role", role)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    public String generateToken(String email, Long userId, Role role) {
        String roleStr = (role != null) ? role.name() : null;
        return createToken(userId, email, roleStr);
    }

    /** 토큰 유효성 검사 */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            // 만료
            return false;
        } catch (JwtException | IllegalArgumentException e) {
            // 위·변조, 형식 오류 등
            return false;
        }
    }

    /** 토큰에서 Authentication 객체 생성 */
    public Authentication getAuthentication(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        String email = claims.getSubject();
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        return new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                userDetails,
                "",
                userDetails.getAuthorities()
        );
    }

    /** "Bearer xxx" 헤더에서 실제 토큰만 추출 */
    public String resolveToken(String bearerToken) {
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
