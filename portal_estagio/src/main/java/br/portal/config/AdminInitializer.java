package br.portal.config;

import br.portal.dao.UsuarioDao;
import br.portal.excecao.PortalException;
import br.portal.service.AutenticacaoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Inicialização controlada do administrador de demonstração. Só cria a conta se as variáveis
 * PORTAL_ADMIN_EMAIL e PORTAL_ADMIN_PASSWORD estiverem definidas e o e-mail ainda não existir.
 * A senha é gravada com BCrypt. Não existe endpoint público para criar administradores.
 */
@Configuration
public class AdminInitializer {
    private static final Logger log = LoggerFactory.getLogger(AdminInitializer.class);

    @Bean
    CommandLineRunner criarAdminInicial(AutenticacaoService autenticacao, UsuarioDao usuarios,
                                        @Value("${portal.admin.email:}") String email,
                                        @Value("${portal.admin.password:}") String senha) {
        return args -> {
            if (email.isBlank() || senha.isBlank()) {
                log.info("PORTAL_ADMIN_EMAIL/PORTAL_ADMIN_PASSWORD não definidos: nenhum administrador criado.");
                return;
            }
            if (usuarios.buscarPorEmail(email.trim()).isPresent()) {
                return;
            }
            try {
                autenticacao.cadastrarAdministradorDemonstracao("Administrador Demo", email, senha);
                log.info("Administrador de demonstração criado: {}", email.trim().toLowerCase());
            } catch (PortalException e) {
                // não derruba a aplicação e nunca imprime a senha
                log.warn("Administrador não criado: {}", e.getMessage());
            }
        };
    }
}
