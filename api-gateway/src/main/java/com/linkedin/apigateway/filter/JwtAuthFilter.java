package com.linkedin.apigateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import java.security.Key;


/**
 * JWT Authentication Filter
 *
 * Applied to all route except /api/v1/auth/**
 */

/**
 * flow
 * 1. extract jwt token
 * 2.validate jwt signature
 * 3.extracts user id
 * 4.add user id request header
 * 5.forward to downstream services
 */

@Component
@Slf4j
public class JwtAuthFilter extends AbstractGatewayFilterFactory<JwtAuthFilter.Config> {

    public JwtAuthFilter(){
        super(Config.class);

    }
    @Value("${jwt.secret-key}")
    private String secretKey;

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String authHeader = exchange.getRequest()
                    .getHeaders()
                    .getFirst(HttpHeaders.AUTHORIZATION);

            if(authHeader == null || !authHeader.startsWith("Bearer") ){
                log.warn("Missing or invalid authorization Header");
                return unauthorized(exchange);
            }

            String token = authHeader.substring(7);
            try{
                Claims claims = extractClaims(token);
                String userId = claims.get("userId",String.class);
                String email = claims.getSubject();
                
                log.info("Jwt validated for user: {}",userId);
                
                // Add user id to request header
                
                ServerWebExchange modifiedExchange = exchange.mutate()
                        .request(r -> r.header("X-User-Id",userId)
                                .header("X-User-email",email)
                        ).build();
                
                return chain.filter(modifiedExchange);
                
            }catch (Exception e){
                  log.error("JWT validation failed: {}",e.getMessage());
                  return unauthorized(exchange);
            }
        };
    }

    private Claims extractClaims(String token) {
        
        return (Claims) Jwts.parser()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64URL.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);

        return exchange.getResponse().setComplete();
    }


    public static class Config{

}
}
