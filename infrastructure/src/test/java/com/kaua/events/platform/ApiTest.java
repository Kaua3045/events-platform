package com.kaua.events.platform;

import com.kaua.events.platform.domain.utils.IdentifierUtils;
import com.kaua.events.platform.infrastructure.configurations.authentication.AuthenticatedUser;
import com.kaua.events.platform.infrastructure.configurations.authentication.UserAuthentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;

public interface ApiTest {

    static RequestPostProcessor admin() {
        return admin(IdentifierUtils.generateNewId());
    }

    static RequestPostProcessor admin(final String userId) {
        Jwt.Builder jwtBuilder = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim(JwtClaimNames.SUB, userId);

        return SecurityMockMvcRequestPostProcessors.authentication(new UserAuthentication(
                jwtBuilder.build(),
                new AuthenticatedUser(userId),
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        ));
    }
}
