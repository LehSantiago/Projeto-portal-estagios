package br.portal.model;

import java.time.LocalDate;

/** Associação entre estudante e vaga (N:N), com identificador próprio e data da inscrição. */
public class Candidatura {
    private Long id;
    private final Usuario estudante;
    private final Vaga vaga;
    private final LocalDate dataInscricao;

    public Candidatura(Long id, Usuario estudante, Vaga vaga, LocalDate dataInscricao) {
        this.id = id;
        this.estudante = estudante;
        this.vaga = vaga;
        this.dataInscricao = dataInscricao;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Usuario getEstudante() { return estudante; }
    public Vaga getVaga() { return vaga; }
    public LocalDate getDataInscricao() { return dataInscricao; }

    @Override
    public String toString() {
        return "#" + id + " " + estudante.getNome() + " -> " + vaga.getTitulo() + " (" + dataInscricao + ")";
    }
}
