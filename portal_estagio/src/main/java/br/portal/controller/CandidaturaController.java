package br.portal.controller;

import br.portal.dao.UsuarioDao;
import br.portal.dto.ApiDtos.CandidaturaResponse;
import br.portal.service.CandidaturaService;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Candidaturas. O estudante é sempre quem está logado: o id dele nunca vem do cliente. */
@RestController
@RequestMapping("/api")
public class CandidaturaController extends ControllerSupport {
    private final CandidaturaService service;

    public CandidaturaController(UsuarioDao usuarios, CandidaturaService service) {
        super(usuarios);
        this.service = service;
    }

    @PostMapping("/vagas/{id}/candidaturas")
    public ResponseEntity<CandidaturaResponse> candidatar(Authentication auth, @PathVariable @Positive long id) {
        var candidatura = service.candidatar(ator(auth), id);
        return ResponseEntity.status(HttpStatus.CREATED).body(CandidaturaResponse.de(candidatura));
    }

    @GetMapping("/candidaturas/minhas")
    public List<CandidaturaResponse> minhas(Authentication auth) {
        return service.listarMinhas(ator(auth)).stream().map(CandidaturaResponse::de).toList();
    }
}
