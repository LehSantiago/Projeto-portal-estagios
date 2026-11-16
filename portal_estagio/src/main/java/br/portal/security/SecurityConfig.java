package br.portal.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

import java.io.IOException;

/**
 * Autenticação por sessão HTTP, autorização por perfil e proteção CSRF.
 * A autorização por PROPRIEDADE (empresa só mexe nas próprias vagas etc.) fica nos Services.
 * As regras abaixo valem na ordem: a primeira que combina com a requisição é a usada.
 */
@Configuration
public class SecurityConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration configuracao) throws Exception {
        return configuracao.getAuthenticationManager();
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // CSRF: o token fica no cookie XSRF-TOKEN e deve voltar no cabeçalho X-XSRF-TOKEN
        // em toda requisição que altera dados (POST, PUT, PATCH, DELETE).
        CookieCsrfTokenRepository csrf = CookieCsrfTokenRepository.withHttpOnlyFalse();
        csrf.setCookiePath("/");
        http.csrf(c -> c.csrfTokenRepository(csrf).csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler()));

        http.authorizeHttpRequests(a -> a
                // interface web estática
                .requestMatchers("/", "/index.html", "/app.js", "/estilo.css", "/favicon.ico", "/error").permitAll()
                // autenticação e cadastro público (somente ESTUDANTE e EMPRESA; não existe cadastro de admin)
                .requestMatchers(HttpMethod.GET, "/api/auth/csrf").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/login",
                        "/api/auth/cadastro/estudante", "/api/auth/cadastro/empresa").permitAll()
                // empresa
                .requestMatchers(HttpMethod.GET, "/api/vagas/minhas", "/api/vagas/*/candidatos").hasRole("EMPRESA")
                // estudante
                .requestMatchers(HttpMethod.POST, "/api/vagas/*/candidaturas").hasRole("ESTUDANTE")
                .requestMatchers("/api/candidaturas/**").hasRole("ESTUDANTE")
                // consulta pública de vagas e áreas
                .requestMatchers(HttpMethod.GET, "/api/vagas", "/api/vagas/*", "/api/areas").permitAll()
                // administração
                .requestMatchers("/api/admin/**").hasRole("ADMINISTRADOR")
                // demais operações de vagas: somente empresa
                .requestMatchers(HttpMethod.POST, "/api/vagas").hasRole("EMPRESA")
                .requestMatchers(HttpMethod.PUT, "/api/vagas/*").hasRole("EMPRESA")
                .requestMatchers(HttpMethod.PATCH, "/api/vagas/*/encerrar").hasRole("EMPRESA")
                // todo o resto exige login (inclui GET /api/auth/me)
                .anyRequest().authenticated());

        http.formLogin(f -> f.disable()).httpBasic(b -> b.disable());

        http.logout(l -> l.logoutUrl("/api/auth/logout")
                .deleteCookies("JSESSIONID")
                .logoutSuccessHandler((req, res, auth) -> res.setStatus(HttpServletResponse.SC_NO_CONTENT)));

        http.exceptionHandling(e -> e
                .authenticationEntryPoint((req, res, ex) ->
                        erroJson(res, HttpServletResponse.SC_UNAUTHORIZED, "Autenticação necessária."))
                .accessDeniedHandler((req, res, ex) ->
                        erroJson(res, HttpServletResponse.SC_FORBIDDEN, "Acesso negado.")));

        // Política de conteúdo: só recursos do próprio servidor (reduz o estrago de um XSS)
        http.headers(h -> h.contentSecurityPolicy(c -> c.policyDirectives("default-src 'self'")));

        return http.build();
    }

    private static void erroJson(HttpServletResponse res, int status, String mensagem) throws IOException {
        res.setStatus(status);
        res.setContentType("application/json;charset=UTF-8");
        res.getWriter().write("{\"erro\":\"" + mensagem + "\"}");
    }
}
