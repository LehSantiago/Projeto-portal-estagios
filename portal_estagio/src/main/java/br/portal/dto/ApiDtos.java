package br.portal.dto;

import br.portal.model.Area;
import br.portal.model.Candidatura;
import br.portal.model.Modalidade;
import br.portal.model.SituacaoVaga;
import br.portal.model.Usuario;
import br.portal.model.Vaga;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * DTOs da API: o que entra (requests, com Bean Validation) e o que sai (responses).
 * As entidades do modelo nunca são devolvidas diretamente, e nenhum DTO de saída tem senha ou hash.
 * Obs.: não há campo "perfil" nos cadastros; o servidor decide o perfil pela rota usada.
 */
public class ApiDtos {
    private static final String SENHA_REGEX = "^(?=.*[A-Za-z])(?=.*\\d).{8,72}$";
    private static final String SENHA_MSG = "deve ter de 8 a 72 caracteres, com letras e números";

    // ---------- requisições ----------
    public record CadastroEstudante(
            @NotBlank @Size(max = 120) String nome,
            @NotBlank @Email @Size(max = 160) String email,
            @NotBlank @Size(max = 120) String curso,
            @NotBlank @Pattern(regexp = SENHA_REGEX, message = SENHA_MSG) String senha) {
    }

    public record CadastroEmpresa(
            @NotBlank @Size(max = 120) String nome,
            @NotBlank @Email @Size(max = 160) String email,
            @NotBlank @Size(max = 120) String nomeEmpresa,
            @NotBlank @Pattern(regexp = SENHA_REGEX, message = SENHA_MSG) String senha) {
    }

    public record Login(@NotBlank @Email String email, @NotBlank String senha) {
    }

    public record VagaRequest(
            @NotBlank @Size(max = 150) String titulo,
            @NotBlank String descricao,
            @NotNull @Positive Long areaId,
            @NotBlank @Size(max = 120) String localizacao,
            @NotNull Modalidade modalidade,
            @Positive int cargaHorariaSemanal) {
    }

    public record AreaRequest(@NotBlank @Size(max = 80) String nome) {
    }

    // ---------- respostas ----------
    public record ApiMessage(String mensagem) {
    }

    public record CsrfResponse(String header, String token) {
    }

    public record UsuarioResponse(String nome, String email, String perfil) {
        public static UsuarioResponse de(Usuario u) {
            return new UsuarioResponse(u.getNome(), u.getEmail(), u.getPerfil().name());
        }
    }

    public record AreaResponse(Long id, String nome) {
        public static AreaResponse de(Area a) {
            return new AreaResponse(a.getId(), a.getNome());
        }
    }

    public record VagaResponse(Long id, String titulo, String descricao, Long areaId, String area,
                               String empresa, String localizacao, Modalidade modalidade,
                               int cargaHorariaSemanal, SituacaoVaga situacao) {
        public static VagaResponse de(Vaga v) {
            return new VagaResponse(v.getId(), v.getTitulo(), v.getDescricao(), v.getArea().getId(),
                    v.getArea().getNome(), v.getEmpresa().getNomeEmpresa(), v.getLocalizacao(),
                    v.getModalidade(), v.getCargaHorariaSemanal(), v.getSituacao());
        }
    }

    public record CandidaturaResponse(Long id, Long vagaId, String vaga, String empresa,
                                      Modalidade modalidade, SituacaoVaga situacaoVaga,
                                      LocalDate dataInscricao) {
        public static CandidaturaResponse de(Candidatura c) {
            Vaga v = c.getVaga();
            return new CandidaturaResponse(c.getId(), v.getId(), v.getTitulo(),
                    v.getEmpresa().getNomeEmpresa(), v.getModalidade(), v.getSituacao(),
                    c.getDataInscricao());
        }
    }

    /** Candidato visto pela empresa dona da vaga: só nome, e-mail e curso. */
    public record CandidatoResponse(Long candidaturaId, String nome, String email, String curso,
                                    LocalDate dataInscricao) {
        public static CandidatoResponse de(Candidatura c) {
            Usuario e = c.getEstudante();
            return new CandidatoResponse(c.getId(), e.getNome(), e.getEmail(), e.getCurso(),
                    c.getDataInscricao());
        }
    }
}
