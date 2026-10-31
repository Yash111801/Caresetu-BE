package in.gov.npncd.caresetu.security;

import in.gov.npncd.caresetu.config.SecurityProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Set;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final SecurityProperties properties;
    private final SecretKey key;

    public JwtService(SecurityProperties properties) {
        this.properties = properties;
        this.key = buildKey(properties.jwt().secret());
    }

    public String createAccessToken(UserPrincipal principal) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(properties.jwt().accessTokenTtlMinutes() * 60);
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(principal.getPublicId())
                .claim("mobile", principal.getUsername())
                .claim("name", principal.getFullName())
                .claim("roles", principal.getRoles())
                .claim("perms", principal.getPermissions())
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(key)
                .compact();
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public long accessTokenTtlSeconds() {
        return properties.jwt().accessTokenTtlMinutes() * 60;
    }

    @SuppressWarnings("unchecked")
    public Set<String> rolesFrom(Claims claims) {
        Object roles = claims.get("roles");
        if (roles instanceof Set<?> set) {
            return (Set<String>) set;
        }
        if (roles instanceof java.util.Collection<?> col) {
            return Set.copyOf(col.stream().map(Object::toString).toList());
        }
        return Set.of();
    }

    private static SecretKey buildKey(String secret) {
        byte[] bytes;
        try {
            bytes = Decoders.BASE64.decode(secret);
        } catch (Exception ex) {
            bytes = secret.getBytes(StandardCharsets.UTF_8);
        }
        if (bytes.length < 32) {
            throw new IllegalStateException("JWT secret must be at least 256 bits");
        }
        return Keys.hmacShaKeyFor(bytes);
    }
}
