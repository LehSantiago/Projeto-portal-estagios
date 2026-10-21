package br.portal.model;

import java.util.Objects;

/**
 * Usuário do portal. Os três perfis ficam em uma única classe, diferenciados pelo enum Perfil:
 * curso só é preenchido para estudante e nomeEmpresa só para empresa.
 * (Decisão para justificar: sem herança, a tabela é única e a identidade simulada fica simples.)
 * A senha será acrescentada na Entrega 2.
 */
public class Usuario {
    private Long id;
    private final String nome;
    private final String email;
    private final Perfil perfil;
    private final String curso;
    private final String nomeEmpresa;

    public Usuario(Long id, String nome, String email, Perfil perfil, String curso, String nomeEmpresa) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.perfil = perfil;
        this.curso = curso;
        this.nomeEmpresa = nomeEmpresa;
    }

    public static Usuario estudante(String nome, String email, String curso) {
        return new Usuario(null, nome, email, Perfil.ESTUDANTE, curso, null);
    }

    public static Usuario empresa(String nome, String email, String nomeEmpresa) {
        return new Usuario(null, nome, email, Perfil.EMPRESA, null, nomeEmpresa);
    }

    public static Usuario administrador(String nome, String email) {
        return new Usuario(null, nome, email, Perfil.ADMINISTRADOR, null, null);
    }

    public boolean isEstudante() { return perfil == Perfil.ESTUDANTE; }
    public boolean isEmpresa() { return perfil == Perfil.EMPRESA; }
    public boolean isAdministrador() { return perfil == Perfil.ADMINISTRADOR; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public String getEmail() { return email; }
    public Perfil getPerfil() { return perfil; }
    public String getCurso() { return curso; }
    public String getNomeEmpresa() { return nomeEmpresa; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Usuario outro)) return false;
        return id != null && Objects.equals(id, outro.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        String detalhe = switch (perfil) {
            case ESTUDANTE -> "curso: " + curso;
            case EMPRESA -> "empresa: " + nomeEmpresa;
            case ADMINISTRADOR -> "administrador";
        };
        return "#" + id + " " + nome + " <" + email + "> [" + perfil + ", " + detalhe + "]";
    }
}
