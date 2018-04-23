package org.redischool.attendance.config;

import org.springframework.boot.autoconfigure.security.oauth2.client.EnableOAuth2Sso;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2SsoDefaultConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

@Profile("!test")
@Configuration
@EnableOAuth2Sso
class CloudSecurityConfig extends OAuth2SsoDefaultConfiguration {

    public CloudSecurityConfig(ApplicationContext applicationContext) {
        super(applicationContext);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                .anyRequest().permitAll()
                .and()
                .csrf().disable();

        super.configure(http);
        http.logout().logoutSuccessUrl("/");
    }
}
