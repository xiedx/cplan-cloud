package com.cplan.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT utility for token generation, parsing, and validation.
 *
 * <p>Default algorithm: HS256. The secret key and expiration are configured
 * via Nacos Config (cplan.jwt.secret / cplan.jwt.expire-seconds).
 */
public class JwtUtil {

    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);

    /**
     * Default secret — overridden by Nacos Config at runtime.
     */
    private String secret = "cplan-default-secret-key-change-in-production-32chars-min";

    /**
     * Default token expiration in seconds (24 hours).
     */
    private long expireSeconds = 86400;

    public JwtUtil() {
    }

    public JwtUtil(String secret, long expireSeconds) {
        this.secret = secret;
        this.expireSeconds = expireSeconds;
    }

    // ---- Getters / Setters (for Nacos @ConfigurationProperties injection) ----

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public void setExpireSeconds(long expireSeconds) {
        this.expireSeconds = expireSeconds;
    }

    // ---- Public API ----

    /**
     * Generate a JWT token for the given user.
     *
     * @param userId   the user's ID
     * @param username the username
     * @return signed JWT string
     */
    public String generateToken(Long userId, String username) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + expireSeconds * 1000);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("username", username)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(getKey())
                .compact();
    }

    /**
     * Parse and validate a JWT token, returning its claims.
     *
     * @param token the JWT string
     * @return claims extracted from the token
     * @throws io.jsonwebtoken.JwtException if the token is invalid or expired
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Check whether the token is expired.
     */
    public boolean isExpired(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Extract userId from token.
     */
    public Long getUserId(String token) {
        try {
            Claims claims = parseToken(token);
            return Long.parseLong(claims.getSubject());
        } catch (Exception e) {
            log.warn("Failed to parse userId from token", e);
            return null;
        }
    }

    /**
     * Validate token and return true if it is well-formed and not expired.
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (SecurityException | MalformedJwtException | UnsupportedJwtException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.warn("Expired JWT token: {}", e.getMessage());
        } catch (Exception e) {
            log.warn("JWT validation error: {}", e.getMessage());
        }
        return false;
    }

    // ---- Private helpers ----

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}
