package aula.microservicos.login;

import static org.springframework.security.config.Customizer.withDefaults;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {

        http.csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .formLogin(config -> config.disable())
                .httpBasic(withDefaults())
                // necessario para o console do H2 (usa frames)
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/login").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/trocarsenha/**").permitAll()
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/actuator/**", "/h2-console/**", "/error").permitAll()
                        .requestMatchers(HttpMethod.POST, "/").hasRole("MANUTENCAO")
                        .requestMatchers(HttpMethod.GET, "/").hasRole("MANUTENCAO")
                        .requestMatchers(HttpMethod.PUT, "/unlock/**").hasRole("ADMIN")
                        .anyRequest().authenticated());

        return http.build();
    }

    // Operadores (admin / manutencao) ficam no schema fixo users / authorities
    @Bean
    public JdbcUserDetailsManager userDetailsService(DataSource datasource) {
        return new JdbcUserDetailsManager(datasource);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
