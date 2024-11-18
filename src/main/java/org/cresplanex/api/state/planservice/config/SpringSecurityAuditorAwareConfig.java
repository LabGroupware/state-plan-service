package org.cresplanex.api.state.planservice.config;

import org.cresplanex.api.state.planservice.audit.AuditAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing
public class SpringSecurityAuditorAwareConfig {
    @Bean
    public AuditorAware<String> auditorProvider() {
        return new AuditAware();
    }
}
