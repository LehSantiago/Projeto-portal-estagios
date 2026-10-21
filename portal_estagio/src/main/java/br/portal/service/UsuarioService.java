package br.portal.service;

import br.portal.dao.UsuarioDao;
import br.portal.excecao.EntidadeNaoEncontradaException;
import br.portal.excecao.RegraNegocioException;
import br.portal.model.Usuario;

import java.util.List;

public class UsuarioService {
    private final UsuarioDao usuarioDao;

    public UsuarioService(UsuarioDao usuarioDao) {
        this.usuarioDao = usuarioDao;
    }

    public Usuario cadastrarEstudante(String nome, String email, String curso) {
        String n = Validacao.textoObrigatorio(nome, "nome");
        String e = Validacao.emailValido(email);
        String c = Validacao.textoObrigatorio(curso, "curso");
        garantirEmailLivre(e);
        return usuarioDao.salvar(Usuario.estudante(n, e, c));
    }

    public Usuario cadastrarEmpresa(String nome, String email, String nomeEmpresa) {
        String n = Validacao.textoObrigatorio(nome, "nome");
        String e = Validacao.emailValido(email);
        String emp = Validacao.textoObrigatorio(nomeEmpresa, "nome da empresa");
        garantirEmailLivre(e);
        return usuarioDao.salvar(Usuario.empresa(n, e, emp));
    }

    /**
     * Administradores NÃO são cadastrados pelo menu público. Este método é usado apenas
     * na carga de dados de demonstração (Entrega 2: inicialização controlada).
     */
    public Usuario cadastrarAdministradorDemonstracao(String nome, String email) {
        String n = Validacao.textoObrigatorio(nome, "nome");
        String e = Validacao.emailValido(email);
        garantirEmailLivre(e);
        return usuarioDao.salvar(Usuario.administrador(n, e));
    }

    public Usuario buscarPorId(long id) {
        return usuarioDao.buscarPorId(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Usuário " + id + " não encontrado."));
    }

    /** Usado pelo menu para a identidade simulada. */
    public List<Usuario> listarTodos() {
        return usuarioDao.listarTodos();
    }

    private void garantirEmailLivre(String email) {
        if (usuarioDao.buscarPorEmail(email).isPresent()) {
            throw new RegraNegocioException("Já existe um usuário com o e-mail " + email + ".");
        }
    }
}
