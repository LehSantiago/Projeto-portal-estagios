package br.portal.app;

import br.portal.dao.AreaDao;
import br.portal.dao.CandidaturaDao;
import br.portal.dao.UsuarioDao;
import br.portal.dao.VagaDao;
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
 * Para trocar a memória pelo banco, basta criar os DAOs JDBC aqui em criarEmMemoria()
 * (ou em um novo método criarComJdbc()) — nenhum serviço muda.
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

    public static Contexto criarEmMemoria() {
        return new Contexto(new UsuarioDaoMemoria(), new AreaDaoMemoria(),
                new VagaDaoMemoria(), new CandidaturaDaoMemoria());
    }
}
