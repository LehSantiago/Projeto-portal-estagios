package br.portal.controller;

import br.portal.dao.UsuarioDao;
import br.portal.dto.ApiDtos.AreaRequest;
import br.portal.dto.ApiDtos.AreaResponse;
import br.portal.service.AreaService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Áreas: listagem pública (para os filtros) e CRUD restrito ao administrador. */
@RestController
@RequestMapping("/api")
public class AreaController extends ControllerSupport {
    private final AreaService service;

    public AreaController(UsuarioDao usuarios, AreaService service) {
        super(usuarios);
        this.service = service;
    }

    @GetMapping("/areas")
    public List<AreaResponse> listarPublico() {
        return service.listar().stream().map(AreaResponse::de).toList();
    }

    @GetMapping("/admin/areas")
    public List<AreaResponse> listarAdmin() {
        return service.listar().stream().map(AreaResponse::de).toList();
    }

    @PostMapping("/admin/areas")
    public ResponseEntity<AreaResponse> criar(Authentication auth, @Valid @RequestBody AreaRequest r) {
        var area = service.cadastrar(ator(auth), r.nome());
        return ResponseEntity.status(HttpStatus.CREATED).body(AreaResponse.de(area));
    }

    @PutMapping("/admin/areas/{id}")
    public AreaResponse editar(Authentication auth, @PathVariable @Positive long id,
                               @Valid @RequestBody AreaRequest r) {
        return AreaResponse.de(service.editar(ator(auth), id, r.nome()));
    }

    @DeleteMapping("/admin/areas/{id}")
    public ResponseEntity<Void> excluir(Authentication auth, @PathVariable @Positive long id) {
        service.excluir(ator(auth), id);
        return ResponseEntity.noContent().build();
    }
}
