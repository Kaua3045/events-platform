package com.kaua.events.platform.infrastructure.configurations;

import com.kaua.events.platform.infrastructure.configurations.authentication.JwtConverter;
import com.kaua.events.platform.infrastructure.configurations.properties.CorsProperties;
import com.kaua.events.platform.infrastructure.configurations.properties.OAuthClients;
import com.kaua.events.platform.infrastructure.constants.Constants;
import com.kaua.events.platform.infrastructure.oauth.OAuthAuthenticateFilter;
import com.kaua.events.platform.infrastructure.services.rsakey.RsaKeyProvider;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.util.Arrays;
import java.util.Objects;

@Configuration(proxyBeanMethods = false)
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true, jsr250Enabled = true)
public class SecurityConfig {

    private final JwtConverter jwtConverter;

    public SecurityConfig(final JwtConverter jwtConverter) {
        this.jwtConverter = Objects.requireNonNull(jwtConverter);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            final HttpSecurity http,
            final OAuthClients oAuthClients,
            final @Qualifier("handlerExceptionResolver") HandlerExceptionResolver handlerExceptionResolver
    ) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(authorize ->
                        authorize.requestMatchers("/v1/users", "/actuator/**", "/webhooks/pix").permitAll()
                                .requestMatchers(HttpMethod.GET, "/.well-known/**").permitAll()
                                .requestMatchers("/v1/users/me/user").hasAnyAuthority("USER")
                                .anyRequest().authenticated())
                .oauth2ResourceServer(oauth -> oauth.jwt(j -> j.jwtAuthenticationConverter(jwtConverter)))
                .sessionManagement(sessions -> sessions.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(new OAuthAuthenticateFilter(oAuthClients, handlerExceptionResolver), BearerTokenAuthenticationFilter.class)
                .build();
    }

    @Bean
    public JwtDecoder jwtDecoder(final RsaKeyProvider rsaKeyProvider, final OAuthClients oAuthClients) {
        final var aJwtDecoder = NimbusJwtDecoder
                .withPublicKey(rsaKeyProvider.getPublicKey(Constants.JWT_RSA_KEY_NAME))
                .build();

        OAuth2TokenValidator<Jwt> issuerValidator = JwtValidators.createDefaultWithIssuer(oAuthClients.getIssuer());
        OAuth2TokenValidator<Jwt> validator = new DelegatingOAuth2TokenValidator<>(issuerValidator);
        aJwtDecoder.setJwtValidator(validator);
        return aJwtDecoder;
    }

    @Bean
    public JwtEncoder jwtEncoder(JWKSource<SecurityContext> jwkSource) {
        return new NimbusJwtEncoder(jwkSource);
    }

    @Bean
    public JWKSource<SecurityContext> jwkSource(final RsaKeyProvider rsaKeyProvider) {
        final var aKeyPublic = rsaKeyProvider.getPublicKey(Constants.JWT_RSA_KEY_NAME);
        final var aKeyPrivate = rsaKeyProvider.getPrivateKey(Constants.JWT_RSA_KEY_NAME);

        final var aRsaKey = new RSAKey.Builder(aKeyPublic)
                .privateKey(aKeyPrivate)
                .keyID(aKeyPublic.getPublicExponent().toString()) // TODO aqui deveriamos pegar do rsa key provider
                .build();

        JWKSet jwkSet = new JWKSet(aRsaKey);
        return new ImmutableJWKSet<>(jwkSet);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(CorsProperties corsProperties) {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(corsProperties.getAllowedOrigins()); // Permite acesso apenas a este domínio
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(corsProperties.getAllowedHeaders());
        configuration.setAllowCredentials(corsProperties.isAllowCredentials());
        configuration.setMaxAge(corsProperties.getMaxAgePreflight());
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
