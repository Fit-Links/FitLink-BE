package spring.fitlinkbe.infra.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import spring.fitlinkbe.domain.common.model.PersonalDetail.Status;

import java.security.Key;
import java.util.Date;
import java.util.Map;

@Slf4j
@Component
public class AuthTokenProvider {

    @Value("${app.auth.token-secret}")
    private String tokenSecret;

    @Value("${app.auth.token-expiry}")
    private Long tokenExpiry;

    @Value("${app.auth.refresh-token-secret}")
    private String refreshTokenSecret;

    @Value("${app.auth.refresh-token-expiry}")
    private Long refreshTokenExpiry;

    private Key key;
    private Key refreshKey;

    static final String CLAIM_KEY_STATUS = "status";
    static final String CLAIM_KEY_DETAIL_ID = "detailId";


    @PostConstruct
    public void init() {
        key = Keys.hmacShaKeyFor(tokenSecret.getBytes());
        refreshKey = Keys.hmacShaKeyFor(refreshTokenSecret.getBytes());
    }

    public String createAccessToken(Status status, Long personalDetailId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + tokenExpiry);

        Map<String, Object> claims = Map.of(
                CLAIM_KEY_STATUS, status,
                CLAIM_KEY_DETAIL_ID, personalDetailId
        );

        return Jwts.builder()
                .addClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .signWith(key)
                .compact();
    }

    public String createRefreshToken(Long personalDetailId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshTokenExpiry);

        Map<String, Object> claims = Map.of(CLAIM_KEY_DETAIL_ID, personalDetailId);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .signWith(refreshKey)
                .compact();
    }

    private Claims getClaims(String token, Key key) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (SecurityException e) {
            log.info("Invalid JWT signature.");
        } catch (MalformedJwtException e) {
            log.info("Invalid JWT token.");
        } catch (ExpiredJwtException e) {
            log.info("Expired JWT token.");
        } catch (UnsupportedJwtException e) {
            log.info("Unsupported JWT token.");
        } catch (IllegalArgumentException e) {
            log.info("JWT token compact of handler are invalid.");
        }
        return null;
    }

}
