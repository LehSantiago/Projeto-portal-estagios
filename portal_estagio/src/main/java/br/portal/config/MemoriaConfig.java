package br.portal.config;

import br.portal.dao.AreaDao;
import br.portal.dao.CandidaturaDao;
import br.portal.dao.UsuarioDao;
import br.portal.dao.VagaDao;
import br.portal.dao.memoria.AreaDaoMemoria;
import br.portal.dao.memoria.CandidaturaDaoMemoria;
import br.portal.dao.memoria.UsuarioDaoMemoria;
import br.portal.dao.memoria.VagaDaoMemoria;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Perfil "memoria": DAOs em memória, sem PostgreSQL. Serve para os testes automatizados e para
 * uma demonstração rápida (os dados somem ao encerrar). NÃO é o modo de produção.
 * Ative com: --spring.profiles.active=memoria
 */
@Configuration
@Profile("memoria")
public class MemoriaConfig {
    @Bean
    UsuarioDao usuarioDao() {
        return new UsuarioDaoMemoria();
    }

    @Bean
    AreaDao areaDao() {
        return new AreaDaoMemoria();
    }

    @Bean
    VagaDao vagaDao() {
        return new VagaDaoMemoria();
    }

    @Bean
    CandidaturaDao candidaturaDao() {
        return new CandidaturaDaoMemoria();
    }
}
