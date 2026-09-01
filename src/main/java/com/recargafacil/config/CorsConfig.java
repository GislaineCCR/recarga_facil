package com.recargafacil.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * ============================================================================
 * CLASSE DE CONFIGURAÇÃO DE CORS (Cross-Origin Resource Sharing)
 * ============================================================================
 * FUNÇÃO: Permite que a aplicação Front-end rodando em navegadores Web 
 * (seja via Live Server, localhost ou arquivo local) faça requisições HTTP 
 * para os endpoints REST deste Back-end Java Spring Boot sem ser bloqueada.
 * 
 * CONEXÃO COM O FRONT-END:
 * - Libera requisições originadas do front-end para as rotas /api/**
 * - Suporta todos os métodos necessários: GET (busca), POST (reservas e pagamentos),
 *   PUT (atualizações) e DELETE (cancelamentos).
 * ============================================================================
 */
@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                // Aplica a liberação de CORS para todos os endpoints sob /api/** e /login
                registry.addMapping("/**")
                        // Permite qualquer origem durante o desenvolvimento (localhost, Live Server, file://)
                        .allowedOriginPatterns("*")
                        // Métodos HTTP aceitos pelo Back-end para integração com o Front-end
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                        // Cabeçalhos HTTP permitidos (Content-Type, Authorization, etc.)
                        .allowedHeaders("*")
                        // Permite envio de credenciais/cookies
                        .allowCredentials(true)
                        // Tempo de cache do pre-flight
                        .maxAge(3600);
            }
        };
    }
}
