package com.servicedesk.config;

import com.servicedesk.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Optionally creates a username/password admin at startup (see app.bootstrap-admin.* in application.properties). */
@Configuration
public class AdminBootstrap {
    @Bean
    ApplicationRunner createAdmin(UserService users,
                                  @Value("${app.bootstrap-admin.username:}") String username,
                                  @Value("${app.bootstrap-admin.email:admin@servicedesk.local}") String email,
                                  @Value("${app.bootstrap-admin.password:}") String password) {
        return args -> {
            if (!username.isBlank() && password.length() >= 8) users.bootstrapAdmin(username, email, password);
        };
    }
}
