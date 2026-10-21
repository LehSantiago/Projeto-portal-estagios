package br.portal.dao;

import br.portal.model.Perfil;
import br.portal.model.Usuario;

import java.util.List;
import java.util.Optional;

public interface UsuarioDao {
    /** Insere o usuário e preenche o id gerado. */
    Usuario salvar(Usuario usuario);

    Optional<Usuario> buscarPorId(long id);

    Optional<Usuario> buscarPorEmail(String email);

    List<Usuario> listarTodos();

    long contarPorPerfil(Perfil perfil);
}
