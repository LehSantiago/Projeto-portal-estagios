package br.portal.config;

import br.portal.dao.AreaDao;
import br.portal.dao.CandidaturaDao;
import br.portal.dao.UsuarioDao;
import br.portal.dao.VagaDao;
import br.portal.dao.jdbc.AreaDaoJdbc;
import br.portal.dao.jdbc.CandidaturaDaoJdbc;
import br.portal.dao.jdbc.ConexaoFactory;
import br.portal.dao.jdbc.UsuarioDaoJdbc;
import br.portal.dao.jdbc.VagaDaoJdbc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;

/** DAOs JDBC sobre o PostgreSQL (DataSource configurado em application.properties). Perfil padrão. */
@Configuration
@Profile("!memoria")
public class JdbcConfig {
    @Bean
    ConexaoFactory conexaoFactory(DataSource dataSource) {
        return new ConexaoFactory(dataSource);
    }

    @Bean
    UsuarioDao usuarioDao(ConexaoFactory conexoes) {
        return new UsuarioDaoJdbc(conexoes);
    }

    @Bean
    AreaDao areaDao(ConexaoFactory conexoes) {
        return new AreaDaoJdbc(conexoes);
    }

    @Bean
    VagaDao vagaDao(ConexaoFactory conexoes) {
        return new VagaDaoJdbc(conexoes);
    }

    @Bean
    CandidaturaDao candidaturaDao(ConexaoFactory conexoes) {
        return new CandidaturaDaoJdbc(conexoes);
    }
}
