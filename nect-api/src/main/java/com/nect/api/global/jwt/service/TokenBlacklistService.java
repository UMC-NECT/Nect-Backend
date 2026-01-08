package com.nect.api.global.jwt.service;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Slf4j
@Service
public class TokenBlacklistService {

    private final RedisTemplate<String, String> redisTemplate;
    private final SecretKey secretKey;
    private static final String BLACKLIST_PREFIX = "blacklist:token:";
    private static final String USER_TOKENS_PREFIX = "user:tokens:";

    public TokenBlacklistService(RedisTemplate<String, String> redisTemplate,
                                 @Value("${jwt.secret}") String secret) {
        this.redisTemplate = redisTemplate;
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 토큰을 블랙리스트에 추가
     */
    public void blacklistToken(String token, long expirationTime) {
        try {
            long currentTime = System.currentTimeMillis();
            long remainingTime = expirationTime - currentTime;

            if (remainingTime > 0) {
                String key = BLACKLIST_PREFIX + token;
                Duration ttl = Duration.ofMillis(remainingTime);
                redisTemplate.opsForValue().set(key, "blacklisted", ttl);
                log.info("Token blacklisted successfully with TTL: {} ms", remainingTime);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to blacklist token", e);
        }
    }

    /**
     * 토큰이 블랙리스트에 있는지 확인
     */
    public boolean isBlacklisted(String token) {
        try {
            String key = BLACKLIST_PREFIX + token;
            return redisTemplate.hasKey(key);
        } catch (Exception e) {
            log.error("Failed to check token blacklist status", e);
            return false;
        }
    }

    /**
     * 특정 토큰을 블랙리스트에서 제거 (테스트용)
     */
    public void removeFromBlacklist(String token) {
        try {
            String key = BLACKLIST_PREFIX + token;
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.error("Failed to remove token from blacklist", e);
        }
    }

    /**
     * 사용자의 토큰을 추적 리스트에 추가
     */
    public void trackUserToken(Long memberId, String token, Duration expiration) {
        try {
            String userTokensKey = USER_TOKENS_PREFIX + memberId;
            redisTemplate.opsForList().leftPush(userTokensKey, token);
            redisTemplate.expire(userTokensKey, expiration);
        } catch (Exception e) {
            log.error("Failed to track token for user: {}", memberId, e);
        }
    }

    /**
     * 특정 사용자의 모든 토큰 무효화
     */
    public void invalidateUserTokens(Long memberId) {
        try {
            String userTokensKey = USER_TOKENS_PREFIX + memberId;

            // 사용자의 모든 토큰을 가져와서 블랙리스트에 추가
            Long listSize = redisTemplate.opsForList().size(userTokensKey);
            if (listSize != null && listSize > 0) {
                while (listSize > 0) {
                    String token = redisTemplate.opsForList().rightPop(userTokensKey);
                    if (token != null) {
                        long expirationTime = extractExpirationTime(token);
                        blacklistToken(token, expirationTime);
                    }
                    listSize = redisTemplate.opsForList().size(userTokensKey);
                    if (listSize == null) break;
                }
            }

            redisTemplate.delete(userTokensKey);
        } catch (Exception e) {
            log.error("Failed to invalidate user tokens for user: {}", memberId, e);
        }
    }

    /**
     * 특정 크루의 모든 멤버 토큰 무효화
     */
    public void invalidateCrewMemberTokens(Long crewId, java.util.List<Long> memberIds) {
        for (Long memberId : memberIds) {
            invalidateUserTokens(memberId);
        }

        log.info("Completed token invalidation for crew: {} ({} members processed)", crewId, memberIds.size());
    }

    /**
     * JWT 토큰에서 만료시간 추출
     */
    private long extractExpirationTime(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return claims.getExpiration().getTime();
        } catch (Exception e) {
            log.error("Failed to extract expiration time from token, using default: {}", e.getMessage());
            return System.currentTimeMillis() + 3600000;
        }
    }
}