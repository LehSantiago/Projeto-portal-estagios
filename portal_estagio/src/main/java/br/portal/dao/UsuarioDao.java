package br.portal.dao;

import br.portal.model.Perfil;
import br.portal.model.Usuario;

import java.util.List;
import java.util.Optional;

public interface UsuarioDao {
    /**
     * Credencial usada só pelo login. O hash da senha NÃO faz parte do modelo Usuario:
     * assim ele não tem como vazar numa resposta da API.
     */
    record Credencial(String email, Perfil perfil, String senhaHash) {
    }

    /** Insere o usuário (com o hash BCrypt da senha) e preenche o id gerado. */
    Usuario salvar(Usuario usuario, String senhaHash);

    /** Insere sem senha (contas da Entrega 1 / console). Essas contas não conseguem fazer login. */
    default Usuario salvar(Usuario usuario) {
        return salvar(usuario, null);
    }

    Optional<Usuario> buscarPorId(long id);

    Optional<Usuario> buscarPorEmail(String email);

    /** E-mail, perfil e hash da senha, para o Spring Security. */
    Optional<Credencial> buscarCredencialPorEmail(String email);

    List<Usuario> listarTodos();

    long contarPorPerfil(Perfil perfil);
}
