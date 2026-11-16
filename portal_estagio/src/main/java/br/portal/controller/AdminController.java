package br.portal.controller;

import br.portal.dao.UsuarioDao;
import br.portal.service.RelatorioService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Relatório administrativo: contagens de estudantes, empresas, vagas e candidaturas. */
@RestController
@RequestMapping("/api/admin")
public class AdminController extends ControllerSupport {
    private final RelatorioService service;

    public AdminController(UsuarioDao usuarios, RelatorioService service) {
        super(usuarios);
        this.service = service;
    }

    @GetMapping("/relatorios")
    public RelatorioService.Contagens relatorio(Authentication auth) {
        return service.contagens(ator(auth));
    }
}
