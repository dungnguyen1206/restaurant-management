package com.rroms.restaurantmanagement.config;

import com.rroms.restaurantmanagement.security.CustomUserDetailsService;
import com.rroms.restaurantmanagement.security.CustomOidcUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

    private final CustomUserDetailsService userDetailsService;
    private final CustomOidcUserService oidcUserService;

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationSuccessHandler customSuccessHandler() {
        return (request, response, authentication) -> {
            String redirectUrl = "/";
            if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                redirectUrl = "/admin/dashboard";
            } else if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_MANAGER"))) {
                redirectUrl = "/manager/dashboard";
            } else if (authentication.getAuthorities().stream().anyMatch(a ->
                    a.getAuthority().equals("ROLE_RECEPTIONIST")
                            || a.getAuthority().equals("ROLE_RECEPTIONLIST"))) {
                redirectUrl = "/receptionist/dashboard";
            } else if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_CHEF"))) {
                redirectUrl = "/chef/dashboard";
            } else if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_WAITER"))) {
                redirectUrl = "/waiter/reservations";
            } else if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_CUSTOMER"))) {
                redirectUrl = "/customer/home";
            }
            response.sendRedirect(request.getContextPath() + redirectUrl);
        };
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(
                                "/auths/**",
                                "/oauth2/**",
                                "/login/oauth2/**",
                                "/register",
                                "/css/**",
                                "/js/**"
                        ).permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/manager/**").hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers("/receptionist/**").hasAnyRole("RECEPTIONIST", "RECEPTIONLIST")
                        .requestMatchers("/chef/**").hasRole("CHEF")
                        .requestMatchers("/waiter/**").hasRole("WAITER")
                        .requestMatchers("/customer/**").hasRole("CUSTOMER")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/auths/login")
                        .loginProcessingUrl("/login")
                        .successHandler(customSuccessHandler())
                        .permitAll()
                )
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/auths/login")
                        .userInfoEndpoint(userInfo -> userInfo
                                .oidcUserService(oidcUserService::loadUser)
                        )
                        .successHandler(customSuccessHandler())
                        .failureUrl("/auths/login?oauthError")
                )
                .sessionManagement(session -> session
                        .sessionFixation(fixation -> fixation.migrateSession())
                )
                .logout(logout -> logout
                        .logoutUrl("/auths/logout")
                        .logoutSuccessUrl("/auths/login?logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                .authenticationProvider(authenticationProvider());

        return http.build();
    }

}
