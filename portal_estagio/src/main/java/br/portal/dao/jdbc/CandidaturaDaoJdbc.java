package br.portal.dao.jdbc;

import br.portal.dao.CandidaturaDao;
import br.portal.model.Candidatura;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CandidaturaDaoJdbc implements CandidaturaDao {
    private final ConexaoFactory conexoes;

    public CandidaturaDaoJdbc(ConexaoFactory conexoes) {
        this.conexoes = conexoes;
    }

    @Override
    public Candidatura salvar(Candidatura candidatura) {
        String sql = "INSERT INTO candidatura (estudante_id, vaga_id, data_inscricao) VALUES (?, ?, ?) RETURNING id";
        try (Connection c = conexoes.abrir(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, candidatura.getEstudante().getId());
            ps.setLong(2, candidatura.getVaga().getId());
            ps.setDate(3, Date.valueOf(candidatura.getDataInscricao()));
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                candidatura.setId(rs.getLong(1));
            }
            return candidatura;
        } catch (SQLException e) {
            // o UNIQUE (estudante_id, vaga_id) cai aqui como "registro duplicado"
            throw ConexaoFactory.traduzir(e, "salvar candidatura");
        }
    }

    @Override
    public boolean existePorEstudanteEVaga(long estudanteId, long vagaId) {
        String sql = "SELECT EXISTS (SELECT 1 FROM candidatura WHERE estudante_id = ? AND vaga_id = ?)";
        try (Connection c = conexoes.abrir(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, estudanteId);
            ps.setLong(2, vagaId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getBoolean(1);
            }
        } catch (SQLException e) {
            throw ConexaoFactory.traduzir(e, "verificar candidatura");
        }
    }

    @Override
    public List<Candidatura> listarPorEstudante(long estudanteId) {
        return listar(Mapeadores.SELECT_CANDIDATURA + " WHERE c.estudante_id = ? ORDER BY c.id",
                estudanteId, "listar candidaturas do estudante");
    }

    @Override
    public List<Candidatura> listarPorVaga(long vagaId) {
        return listar(Mapeadores.SELECT_CANDIDATURA + " WHERE c.vaga_id = ? ORDER BY c.id",
                vagaId, "listar candidaturas da vaga");
    }

    @Override
    public long contar() {
        try (Connection c = conexoes.abrir();
             PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM candidatura");
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getLong(1);
        } catch (SQLException e) {
            throw ConexaoFactory.traduzir(e, "contar candidaturas");
        }
    }

    private List<Candidatura> listar(String sql, long parametro, String operacao) {
        try (Connection c = conexoes.abrir(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, parametro);
            try (ResultSet rs = ps.executeQuery()) {
                List<Candidatura> lista = new ArrayList<>();
                while (rs.next()) {
                    lista.add(Mapeadores.candidatura(rs));
                }
                return lista;
            }
        } catch (SQLException e) {
            throw ConexaoFactory.traduzir(e, operacao);
        }
    }
}
