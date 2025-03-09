package spring.fitlinkbe.support.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import spring.fitlinkbe.domain.common.enums.UserRole;
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
    static final String CLAIM_KEY_USER_ROLE = "userRole";

    @PostConstruct
    private void init() {
        key = Keys.hmacShaKeyFor(tokenSecret.getBytes());
        refreshKey = Keys.hmacShaKeyFor(refreshTokenSecret.getBytes());
    }

    public String createAccessToken(Status status, Long personalDetailId, UserRole userRole) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + tokenExpiry);

        Map<String, Object> claims = Map.of(
                CLAIM_KEY_STATUS, status,
                CLAIM_KEY_DETAIL_ID, personalDetailId,
                CLAIM_KEY_USER_ROLE, userRole
        );

        return Jwts.builder()
                .addClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .signWith(key)
                .compact();
    }

    public String createRefreshToken(Long personalDetailId, UserRole userRole) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshTokenExpiry);

        Map<String, Object> claims = Map.of(CLAIM_KEY_DETAIL_ID, personalDetailId,
                CLAIM_KEY_USER_ROLE, userRole);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .signWith(refreshKey)
                .compact();
    }

    public Long getPersonalDetailIdFromAccessToken(String token) {
        Claims claims = getClaims(token, key);
        if (claims == null) {
            return null;
        }
        return claims.get(CLAIM_KEY_DETAIL_ID, Long.class);
    }

    public Status getStatusFromAccessToken(String token) {
        Claims claims = getClaims(token, key);
        if (claims == null) {
            return null;
        }
        return Status.valueOf(claims.get(CLAIM_KEY_STATUS, String.class));
    }

    private Claims getClaims(String token, Key key) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
