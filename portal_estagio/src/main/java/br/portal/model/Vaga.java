package br.portal.model;

import java.util.Objects;

public class Vaga {
    private Long id;
    private final Usuario empresa;
    private String titulo;
    private String descricao;
    private Area area;
    private String localizacao;
    private Modalidade modalidade;
    private int cargaHorariaSemanal;
    private SituacaoVaga situacao;

    public Vaga(Long id, Usuario empresa, String titulo, String descricao, Area area,
                String localizacao, Modalidade modalidade, int cargaHorariaSemanal, SituacaoVaga situacao) {
        this.id = id;
        this.empresa = empresa;
        this.titulo = titulo;
        this.descricao = descricao;
        this.area = area;
        this.localizacao = localizacao;
        this.modalidade = modalidade;
        this.cargaHorariaSemanal = cargaHorariaSemanal;
        this.situacao = situacao;
    }

    public boolean isAberta() { return situacao == SituacaoVaga.ABERTA; }

    public boolean pertenceA(Usuario usuario) {
        return usuario != null && empresa != null && Objects.equals(empresa.getId(), usuario.getId());
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Usuario getEmpresa() { return empresa; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public Area getArea() { return area; }
    public void setArea(Area area) { this.area = area; }
    public String getLocalizacao() { return localizacao; }
    public void setLocalizacao(String localizacao) { this.localizacao = localizacao; }
    public Modalidade getModalidade() { return modalidade; }
    public void setModalidade(Modalidade modalidade) { this.modalidade = modalidade; }
    public int getCargaHorariaSemanal() { return cargaHorariaSemanal; }
    public void setCargaHorariaSemanal(int cargaHorariaSemanal) { this.cargaHorariaSemanal = cargaHorariaSemanal; }
    public SituacaoVaga getSituacao() { return situacao; }
    public void setSituacao(SituacaoVaga situacao) { this.situacao = situacao; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Vaga outra)) return false;
        return id != null && Objects.equals(id, outra.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "#" + id + " " + titulo + " | " + empresa.getNomeEmpresa() + " | " + area.getNome()
                + " | " + localizacao + " | " + modalidade + " | " + cargaHorariaSemanal + "h/sem | " + situacao;
    }
}
