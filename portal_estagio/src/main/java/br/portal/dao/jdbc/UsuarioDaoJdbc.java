package br.portal.dao.jdbc;

import br.portal.dao.UsuarioDao;
import br.portal.model.Perfil;
import br.portal.model.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UsuarioDaoJdbc implements UsuarioDao {
    private static final String COLUNAS =
            "id, nome, email, perfil, curso, nome_empresa";

    private final ConexaoFactory conexoes;

    public UsuarioDaoJdbc(ConexaoFactory conexoes) {
        this.conexoes = conexoes;
    }

    @Override
    public Usuario salvar(Usuario usuario) {
        String sql = "INSERT INTO usuario (nome, email, perfil, curso, nome_empresa) "
                + "VALUES (?, ?, ?, ?, ?) RETURNING id";
        try (Connection c = conexoes.abrir(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, usuario.getNome());
            ps.setString(2, usuario.getEmail());
            ps.setString(3, usuario.getPerfil().name());
            ps.setString(4, usuario.getCurso());
            ps.setString(5, usuario.getNomeEmpresa());
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                usuario.setId(rs.getLong(1));
            }
            return usuario;
        } catch (SQLException e) {
            throw ConexaoFactory.traduzir(e, "salvar usuário");
        }
    }

    @Override
    public Optional<Usuario> buscarPorId(long id) {
        return buscarUnico("SELECT " + COLUNAS + " FROM usuario WHERE id = ?", ps -> ps.setLong(1, id));
    }

    @Override
    public Optional<Usuario> buscarPorEmail(String email) {
        return buscarUnico("SELECT " + COLUNAS + " FROM usuario WHERE LOWER(email) = LOWER(?)",
                ps -> ps.setString(1, email));
    }

    @Override
    public List<Usuario> listarTodos() {
        String sql = "SELECT " + COLUNAS + " FROM usuario ORDER BY id";
        try (Connection c = conexoes.abrir();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Usuario> lista = new ArrayList<>();
            while (rs.next()) {
                lista.add(Mapeadores.usuario(rs, ""));
            }
            return lista;
        } catch (SQLException e) {
            throw ConexaoFactory.traduzir(e, "listar usuários");
        }
    }

    @Override
    public long contarPorPerfil(Perfil perfil) {
        String sql = "SELECT COUNT(*) FROM usuario WHERE perfil = ?";
        try (Connection c = conexoes.abrir(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, perfil.name());
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            throw ConexaoFactory.traduzir(e, "contar usuários");
        }
    }

    /** Interface funcional: define como preencher os parâmetros de um PreparedStatement. */
    @FunctionalInterface
    private interface Parametros {
        void preencher(PreparedStatement ps) throws SQLException;
    }

    private Optional<Usuario> buscarUnico(String sql, Parametros parametros) {
        try (Connection c = conexoes.abrir(); PreparedStatement ps = c.prepareStatement(sql)) {
            parametros.preencher(ps);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(Mapeadores.usuario(rs, "")) : Optional.empty();
            }
        } catch (SQLException e) {
            throw ConexaoFactory.traduzir(e, "buscar usuário");
        }
    }
}
