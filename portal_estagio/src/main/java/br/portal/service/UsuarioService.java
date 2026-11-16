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
        return cadastrarEstudante(nome, email, curso, null);
    }

    /** Versão da Entrega 2: recebe o hash BCrypt já calculado (a senha nunca chega aqui em texto puro). */
    public Usuario cadastrarEstudante(String nome, String email, String curso, String senhaHash) {
        String n = Validacao.textoObrigatorio(nome, "nome");
        String e = Validacao.emailValido(email);
        String c = Validacao.textoObrigatorio(curso, "curso");
        garantirEmailLivre(e);
        return usuarioDao.salvar(Usuario.estudante(n, e, c), senhaHash);
    }

    public Usuario cadastrarEmpresa(String nome, String email, String nomeEmpresa) {
        return cadastrarEmpresa(nome, email, nomeEmpresa, null);
    }

    public Usuario cadastrarEmpresa(String nome, String email, String nomeEmpresa, String senhaHash) {
        String n = Validacao.textoObrigatorio(nome, "nome");
        String e = Validacao.emailValido(email);
        String emp = Validacao.textoObrigatorio(nomeEmpresa, "nome da empresa");
        garantirEmailLivre(e);
        return usuarioDao.salvar(Usuario.empresa(n, e, emp), senhaHash);
    }

    /**
     * Administradores NÃO são cadastrados pelo menu nem pela API pública. Este método é usado
     * apenas na carga de demonstração e pelo inicializador controlado (AdminInitializer).
     */
    public Usuario cadastrarAdministradorDemonstracao(String nome, String email) {
        return cadastrarAdministradorDemonstracao(nome, email, null);
    }

    public Usuario cadastrarAdministradorDemonstracao(String nome, String email, String senhaHash) {
        String n = Validacao.textoObrigatorio(nome, "nome");
        String e = Validacao.emailValido(email);
        garantirEmailLivre(e);
        return usuarioDao.salvar(Usuario.administrador(n, e), senhaHash);
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
