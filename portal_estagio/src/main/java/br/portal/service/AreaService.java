package br.portal.service;

import br.portal.dao.AreaDao;
import br.portal.dao.VagaDao;
import br.portal.excecao.EntidadeNaoEncontradaException;
import br.portal.excecao.RegraNegocioException;
import br.portal.model.Area;
import br.portal.model.Perfil;
import br.portal.model.Usuario;

import java.util.List;

public class AreaService {
    private final AreaDao areaDao;
    private final VagaDao vagaDao;

    public AreaService(AreaDao areaDao, VagaDao vagaDao) {
        this.areaDao = areaDao;
        this.vagaDao = vagaDao;
    }

    public Area cadastrar(Usuario ator, String nome) {
        Validacao.exigirPerfil(ator, Perfil.ADMINISTRADOR, "cadastrar áreas");
        String n = Validacao.textoObrigatorio(nome, "nome da área");
        garantirNomeLivre(n, null);
        return areaDao.salvar(new Area(null, n));
    }

    public Area editar(Usuario ator, long id, String novoNome) {
        Validacao.exigirPerfil(ator, Perfil.ADMINISTRADOR, "editar áreas");
        String n = Validacao.textoObrigatorio(novoNome, "nome da área");
        Area area = buscarPorId(id);
        garantirNomeLivre(n, id);
        area.setNome(n);
        return areaDao.salvar(area);
    }

    public void excluir(Usuario ator, long id) {
        Validacao.exigirPerfil(ator, Perfil.ADMINISTRADOR, "excluir áreas");
        Area area = buscarPorId(id);
        if (vagaDao.existeVagaNaArea(id)) {
            throw new RegraNegocioException("A área '" + area.getNome() + "' é usada por vagas e não pode ser excluída.");
        }
        areaDao.excluir(id);
    }

    public List<Area> listar() {
        return areaDao.listar();
    }

    public Area buscarPorId(long id) {
        return areaDao.buscarPorId(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Área " + id + " não encontrada."));
    }

    private void garantirNomeLivre(String nome, Long idIgnorado) {
        areaDao.buscarPorNome(nome).ifPresent(existente -> {
            if (idIgnorado == null || !existente.getId().equals(idIgnorado)) {
                throw new RegraNegocioException("Já existe uma área chamada '" + nome + "'.");
            }
        });
    }
}
