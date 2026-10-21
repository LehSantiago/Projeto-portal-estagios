package br.portal.service;

import br.portal.dao.VagaDao;
import br.portal.excecao.AcessoNegadoException;
import br.portal.excecao.EntidadeNaoEncontradaException;
import br.portal.excecao.RegraNegocioException;
import br.portal.excecao.ValidacaoException;
import br.portal.model.Area;
import br.portal.model.Modalidade;
import br.portal.model.Perfil;
import br.portal.model.SituacaoVaga;
import br.portal.model.Usuario;
import br.portal.model.Vaga;

import java.util.List;
import java.util.function.Predicate;

public class VagaService {
    private final VagaDao vagaDao;
    private final AreaService areaService;

    public VagaService(VagaDao vagaDao, AreaService areaService) {
        this.vagaDao = vagaDao;
        this.areaService = areaService;
    }

    public Vaga publicar(Usuario ator, String titulo, String descricao, long areaId,
                         String localizacao, Modalidade modalidade, int cargaHorariaSemanal) {
        Validacao.exigirPerfil(ator, Perfil.EMPRESA, "publicar vagas");
        Vaga vaga = new Vaga(null, ator,
                Validacao.textoObrigatorio(titulo, "título"),
                Validacao.textoObrigatorio(descricao, "descrição"),
                areaService.buscarPorId(areaId),
                Validacao.textoObrigatorio(localizacao, "localização"),
                exigirModalidade(modalidade),
                validarCarga(cargaHorariaSemanal),
                SituacaoVaga.ABERTA);
        return vagaDao.salvar(vaga);
    }

    public Vaga editar(Usuario ator, long vagaId, String titulo, String descricao, long areaId,
                       String localizacao, Modalidade modalidade, int cargaHorariaSemanal) {
        Vaga vaga = buscarVagaDaEmpresa(ator, vagaId, "editar");
        if (!vaga.isAberta()) {
            throw new RegraNegocioException("Vagas encerradas não podem ser editadas.");
        }
        // valida tudo antes de alterar o objeto
        String t = Validacao.textoObrigatorio(titulo, "título");
        String d = Validacao.textoObrigatorio(descricao, "descrição");
        Area area = areaService.buscarPorId(areaId);
        String l = Validacao.textoObrigatorio(localizacao, "localização");
        Modalidade m = exigirModalidade(modalidade);
        int carga = validarCarga(cargaHorariaSemanal);

        vaga.setTitulo(t);
        vaga.setDescricao(d);
        vaga.setArea(area);
        vaga.setLocalizacao(l);
        vaga.setModalidade(m);
        vaga.setCargaHorariaSemanal(carga);
        return vagaDao.salvar(vaga);
    }

    public Vaga encerrar(Usuario ator, long vagaId) {
        Vaga vaga = buscarVagaDaEmpresa(ator, vagaId, "encerrar");
        if (!vaga.isAberta()) {
            throw new RegraNegocioException("A vaga já está encerrada.");
        }
        vaga.setSituacao(SituacaoVaga.ENCERRADA);
        return vagaDao.salvar(vaga);
    }

    /** Consulta pública: vagas abertas, com filtros opcionais (null = sem filtro). */
    public List<Vaga> consultarAbertas(Long areaId, Modalidade modalidade) {
        return vagaDao.listarAbertas(areaId, modalidade);
    }

    /**
     * Interface funcional em uso: o critério de filtragem é um Predicate<Vaga> (lambda),
     * o que permite combinar condições sem criar um método para cada combinação.
     * Aqui, busca por palavra-chave no título ou na descrição, aplicada sobre as vagas abertas.
     */
    public List<Vaga> consultarAbertas(Long areaId, Modalidade modalidade, String palavraChave) {
        List<Vaga> abertas = vagaDao.listarAbertas(areaId, modalidade);
        if (palavraChave == null || palavraChave.isBlank()) {
            return abertas;
        }
        String termo = palavraChave.trim().toLowerCase();
        Predicate<Vaga> noTitulo = v -> v.getTitulo().toLowerCase().contains(termo);
        Predicate<Vaga> naDescricao = v -> v.getDescricao().toLowerCase().contains(termo);
        return abertas.stream().filter(noTitulo.or(naDescricao)).toList();
    }

    public List<Vaga> listarDaEmpresa(Usuario ator) {
        Validacao.exigirPerfil(ator, Perfil.EMPRESA, "listar suas vagas");
        return vagaDao.listarPorEmpresa(ator.getId());
    }

    public Vaga buscarPorId(long id) {
        return vagaDao.buscarPorId(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Vaga " + id + " não encontrada."));
    }

    /** Busca a vaga e confirma que o ator é empresa E dona do registro. */
    Vaga buscarVagaDaEmpresa(Usuario ator, long vagaId, String acao) {
        Validacao.exigirPerfil(ator, Perfil.EMPRESA, acao + " vagas");
        Vaga vaga = buscarPorId(vagaId);
        if (!vaga.pertenceA(ator)) {
            throw new AcessoNegadoException("Você só pode " + acao + " vagas da sua própria empresa.");
        }
        return vaga;
    }

    private static int validarCarga(int carga) {
        if (carga <= 0) {
            throw new ValidacaoException("A carga horária semanal deve ser positiva.");
        }
        return carga;
    }

    private static Modalidade exigirModalidade(Modalidade modalidade) {
        if (modalidade == null) {
            throw new ValidacaoException("A modalidade é obrigatória.");
        }
        return modalidade;
    }
}
