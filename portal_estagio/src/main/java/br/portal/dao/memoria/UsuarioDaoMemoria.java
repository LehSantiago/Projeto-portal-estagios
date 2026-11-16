package br.portal.dao.memoria;

import br.portal.dao.UsuarioDao;
import br.portal.model.Perfil;
import br.portal.model.Usuario;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Implementação em memória: usada nos testes, no modo console --memoria e no perfil Spring "memoria". */
public class UsuarioDaoMemoria implements UsuarioDao {
    private final Map<Long, Usuario> dados = new LinkedHashMap<>();
    private final Map<Long, String> hashes = new LinkedHashMap<>();
    private long proximoId = 1;

    @Override
    public Usuario salvar(Usuario usuario, String senhaHash) {
        if (usuario.getId() == null) {
            usuario.setId(proximoId++);
        }
        dados.put(usuario.getId(), usuario);
        if (senhaHash != null) {
            hashes.put(usuario.getId(), senhaHash);
        }
        return usuario;
    }

    @Override
    public Optional<Credencial> buscarCredencialPorEmail(String email) {
        return buscarPorEmail(email)
                .map(u -> new Credencial(u.getEmail(), u.getPerfil(), hashes.get(u.getId())));
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
