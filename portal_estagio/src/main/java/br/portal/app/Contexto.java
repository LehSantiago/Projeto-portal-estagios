package br.portal.app;

import br.portal.dao.AreaDao;
import br.portal.dao.CandidaturaDao;
import br.portal.dao.UsuarioDao;
import br.portal.dao.VagaDao;
import br.portal.dao.jdbc.AreaDaoJdbc;
import br.portal.dao.jdbc.CandidaturaDaoJdbc;
import br.portal.dao.jdbc.ConexaoFactory;
import br.portal.dao.jdbc.UsuarioDaoJdbc;
import br.portal.dao.jdbc.VagaDaoJdbc;
import br.portal.dao.memoria.AreaDaoMemoria;
import br.portal.dao.memoria.CandidaturaDaoMemoria;
import br.portal.dao.memoria.UsuarioDaoMemoria;
import br.portal.dao.memoria.VagaDaoMemoria;
import br.portal.service.AreaService;
import br.portal.service.CandidaturaService;
import br.portal.service.RelatorioService;
import br.portal.service.UsuarioService;
import br.portal.service.VagaService;

/**
 * Ponto único onde as dependências são montadas (injeção manual pelo construtor).
 * Os serviços só conhecem as interfaces DAO: trocar memória por PostgreSQL muda apenas
 * a fábrica usada (criarEmMemoria x criarComJdbc), nenhum serviço é alterado.
 */
public class Contexto {
    public final UsuarioService usuarioService;
    public final AreaService areaService;
    public final VagaService vagaService;
    public final CandidaturaService candidaturaService;
    public final RelatorioService relatorioService;

    public Contexto(UsuarioDao usuarioDao, AreaDao areaDao, VagaDao vagaDao, CandidaturaDao candidaturaDao) {
        this.usuarioService = new UsuarioService(usuarioDao);
        this.areaService = new AreaService(areaDao, vagaDao);
        this.vagaService = new VagaService(vagaDao, areaService);
        this.candidaturaService = new CandidaturaService(candidaturaDao, vagaService);
        this.relatorioService = new RelatorioService(usuarioDao, vagaDao, candidaturaDao);
    }

    public static Contexto criarComJdbc(ConexaoFactory conexoes) {
        return new Contexto(new UsuarioDaoJdbc(conexoes), new AreaDaoJdbc(conexoes),
                new VagaDaoJdbc(conexoes), new CandidaturaDaoJdbc(conexoes));
    }

    /** Usada nos testes automatizados e no modo --memoria (sem banco). */
    public static Contexto criarEmMemoria() {
        return new Contexto(new UsuarioDaoMemoria(), new AreaDaoMemoria(),
                new VagaDaoMemoria(), new CandidaturaDaoMemoria());
    }
}
