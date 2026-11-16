package br.portal.controller;

import br.portal.dao.UsuarioDao;
import br.portal.dto.ApiDtos.CandidatoResponse;
import br.portal.dto.ApiDtos.VagaRequest;
import br.portal.dto.ApiDtos.VagaResponse;
import br.portal.model.Modalidade;
import br.portal.service.CandidaturaService;
import br.portal.service.VagaService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Vagas. As regras (perfil, propriedade, validações) ficam no VagaService/CandidaturaService. */
@RestController
@RequestMapping("/api/vagas")
public class VagaController extends ControllerSupport {
    private final VagaService vagas;
    private final CandidaturaService candidaturas;

    public VagaController(UsuarioDao usuarios, VagaService vagas, CandidaturaService candidaturas) {
        super(usuarios);
        this.vagas = vagas;
        this.candidaturas = candidaturas;
    }

    /** Consulta pública das vagas ABERTAS, com filtros opcionais: ?areaId=1&modalidade=REMOTA&q=java */
    @GetMapping
    public List<VagaResponse> abertas(@RequestParam(required = false) Long areaId,
                                      @RequestParam(required = false) Modalidade modalidade,
                                      @RequestParam(required = false) String q) {
        return vagas.consultarAbertas(areaId, modalidade, q).stream().map(VagaResponse::de).toList();
    }

    @GetMapping("/{id}")
    public VagaResponse buscar(@PathVariable @Positive long id) {
        return VagaResponse.de(vagas.buscarPorId(id));
    }

    @GetMapping("/minhas")
    public List<VagaResponse> minhas(Authentication auth) {
        return vagas.listarDaEmpresa(ator(auth)).stream().map(VagaResponse::de).toList();
    }

    @PostMapping
    public ResponseEntity<VagaResponse> publicar(Authentication auth, @Valid @RequestBody VagaRequest r) {
        var vaga = vagas.publicar(ator(auth), r.titulo(), r.descricao(), r.areaId(), r.localizacao(),
                r.modalidade(), r.cargaHorariaSemanal());
        return ResponseEntity.status(HttpStatus.CREATED).body(VagaResponse.de(vaga));
    }

    @PutMapping("/{id}")
    public VagaResponse editar(Authentication auth, @PathVariable @Positive long id,
                               @Valid @RequestBody VagaRequest r) {
        return VagaResponse.de(vagas.editar(ator(auth), id, r.titulo(), r.descricao(), r.areaId(),
                r.localizacao(), r.modalidade(), r.cargaHorariaSemanal()));
    }

    @PatchMapping("/{id}/encerrar")
    public VagaResponse encerrar(Authentication auth, @PathVariable @Positive long id) {
        return VagaResponse.de(vagas.encerrar(ator(auth), id));
    }

    @GetMapping("/{id}/candidatos")
    public List<CandidatoResponse> candidatos(Authentication auth, @PathVariable @Positive long id) {
        return candidaturas.listarCandidatos(ator(auth), id).stream().map(CandidatoResponse::de).toList();
    }
}
