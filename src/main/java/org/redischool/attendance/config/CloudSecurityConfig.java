package org.redischool.attendance.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Profile("cloud")
@Configuration
class CloudSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    void configureGlobal(AuthenticationManagerBuilder auth, Environment environment) throws Exception {
        auth.inMemoryAuthentication()
                .withUser(environment.getProperty("credentials.username"))
                .password(String.format("{noop}%s", environment.getProperty("credentials.password")))
                .authorities("ROLE_USER");
    }

    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.authorizeRequests()
                .antMatchers("/", "/static/**/*").permitAll()
                .anyRequest().authenticated()
                .and()
                .httpBasic()
                .and()
                .csrf().disable();
    }
}
