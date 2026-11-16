package br.portal.config;

import br.portal.dao.AreaDao;
import br.portal.dao.CandidaturaDao;
import br.portal.dao.UsuarioDao;
import br.portal.dao.VagaDao;
import br.portal.service.AreaService;
import br.portal.service.AutenticacaoService;
import br.portal.service.CandidaturaService;
import br.portal.service.RelatorioService;
import br.portal.service.UsuarioService;
import br.portal.service.VagaService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Clock;

/**
 * Cria os Services (classes Java puras, sem anotações Spring) injetando as interfaces DAO pelo
 * construtor. Quais DAOs existem depende do perfil: JdbcConfig (PostgreSQL, padrão) ou
 * MemoriaConfig (perfil "memoria", usado nos testes).
 */
@Configuration
public class PortalConfig {
    @Bean
    UsuarioService usuarioService(UsuarioDao usuarioDao) {
        return new UsuarioService(usuarioDao);
    }

    @Bean
    AutenticacaoService autenticacaoService(UsuarioService usuarioService, PasswordEncoder encoder) {
        return new AutenticacaoService(usuarioService, encoder);
    }

    @Bean
    AreaService areaService(AreaDao areaDao, VagaDao vagaDao) {
        return new AreaService(areaDao, vagaDao);
    }

    @Bean
    VagaService vagaService(VagaDao vagaDao, AreaService areaService) {
        return new VagaService(vagaDao, areaService);
    }

    @Bean
    CandidaturaService candidaturaService(CandidaturaDao candidaturaDao, VagaService vagaService) {
        return new CandidaturaService(candidaturaDao, vagaService, Clock.systemDefaultZone());
    }

    @Bean
    RelatorioService relatorioService(UsuarioDao usuarioDao, VagaDao vagaDao, CandidaturaDao candidaturaDao) {
        return new RelatorioService(usuarioDao, vagaDao, candidaturaDao);
    }
}
