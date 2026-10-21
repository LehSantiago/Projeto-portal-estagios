package br.portal.dao.memoria;

import br.portal.dao.UsuarioDao;
import br.portal.model.Perfil;
import br.portal.model.Usuario;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Implementação TEMPORÁRIA em memória. Será substituída por UsuarioDaoJdbc. */
public class UsuarioDaoMemoria implements UsuarioDao {
    private final Map<Long, Usuario> dados = new LinkedHashMap<>();
    private long proximoId = 1;

    @Override
    public Usuario salvar(Usuario usuario) {
        if (usuario.getId() == null) {
            usuario.setId(proximoId++);
        }
        dados.put(usuario.getId(), usuario);
        return usuario;
    }

    @Override
    public Optional<Usuario> buscarPorId(long id) {
        return Optional.ofNullable(dados.get(id));
    }

    @Override
    public Optional<Usuario> buscarPorEmail(String email) {
        return dados.values().stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(email))
                .findFirst();
    }

    @Override
    public List<Usuario> listarTodos() {
        return new ArrayList<>(dados.values());
    }

    @Override
    public long contarPorPerfil(Perfil perfil) {
        return dados.values().stream().filter(u -> u.getPerfil() == perfil).count();
    }
}
