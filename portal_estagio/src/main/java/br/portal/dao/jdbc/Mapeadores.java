package br.portal.dao.jdbc;

import br.portal.model.Area;
import br.portal.model.Candidatura;
import br.portal.model.Modalidade;
import br.portal.model.Perfil;
import br.portal.model.SituacaoVaga;
import br.portal.model.Usuario;
import br.portal.model.Vaga;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Trechos de SQL e conversões ResultSet -> objeto compartilhados pelos DAOs.
 * Cada tabela entra nas consultas com um alias e as colunas recebem prefixo
 * (est_, emp_, a_, v_, c_), para que os JOINs não gerem colunas ambíguas.
 */
final class Mapeadores {
    private Mapeadores() {
    }

    static final String COLUNAS_ESTUDANTE =
            "est.id est_id, est.nome est_nome, est.email est_email, est.perfil est_perfil, "
                    + "est.curso est_curso, est.nome_empresa est_nome_empresa";

    static final String COLUNAS_EMPRESA =
            "emp.id emp_id, emp.nome emp_nome, emp.email emp_email, emp.perfil emp_perfil, "
                    + "emp.curso emp_curso, emp.nome_empresa emp_nome_empresa";

    static final String COLUNAS_AREA = "a.id a_id, a.nome a_nome";

    static final String COLUNAS_VAGA =
            "v.id v_id, v.titulo v_titulo, v.descricao v_descricao, v.localizacao v_localizacao, "
                    + "v.modalidade v_modalidade, v.carga_horaria_semanal v_carga, v.situacao v_situacao";

    /** SELECT completo de vaga (com empresa e área já carregadas). */
    static final String SELECT_VAGA =
            "SELECT " + COLUNAS_VAGA + ", " + COLUNAS_EMPRESA + ", " + COLUNAS_AREA
                    + " FROM vaga v JOIN usuario emp ON emp.id = v.empresa_id JOIN area a ON a.id = v.area_id";

    /** SELECT completo de candidatura (estudante, vaga, empresa e área carregados). */
    static final String SELECT_CANDIDATURA =
            "SELECT c.id c_id, c.data_inscricao c_data, " + COLUNAS_ESTUDANTE + ", "
                    + COLUNAS_VAGA + ", " + COLUNAS_EMPRESA + ", " + COLUNAS_AREA
                    + " FROM candidatura c"
                    + " JOIN usuario est ON est.id = c.estudante_id"
                    + " JOIN vaga v ON v.id = c.vaga_id"
                    + " JOIN usuario emp ON emp.id = v.empresa_id"
                    + " JOIN area a ON a.id = v.area_id";

    static Usuario usuario(ResultSet rs, String prefixo) throws SQLException {
        return new Usuario(
                rs.getLong(prefixo + "id"),
                rs.getString(prefixo + "nome"),
                rs.getString(prefixo + "email"),
                Perfil.valueOf(rs.getString(prefixo + "perfil")),
                rs.getString(prefixo + "curso"),
                rs.getString(prefixo + "nome_empresa"));
    }

    static Area area(ResultSet rs) throws SQLException {
        return new Area(rs.getLong("a_id"), rs.getString("a_nome"));
    }

    static Vaga vaga(ResultSet rs) throws SQLException {
        return new Vaga(
                rs.getLong("v_id"),
                usuario(rs, "emp_"),
                rs.getString("v_titulo"),
                rs.getString("v_descricao"),
                area(rs),
                rs.getString("v_localizacao"),
                Modalidade.valueOf(rs.getString("v_modalidade")),
                rs.getInt("v_carga"),
                SituacaoVaga.valueOf(rs.getString("v_situacao")));
    }

    static Candidatura candidatura(ResultSet rs) throws SQLException {
        return new Candidatura(
                rs.getLong("c_id"),
                usuario(rs, "est_"),
                vaga(rs),
                rs.getDate("c_data").toLocalDate());
    }
}
