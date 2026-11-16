package br.portal.controller;

import br.portal.dao.UsuarioDao;
import br.portal.dto.ApiDtos.ApiMessage;
import br.portal.dto.ApiDtos.CadastroEmpresa;
import br.portal.dto.ApiDtos.CadastroEstudante;
import br.portal.dto.ApiDtos.CsrfResponse;
import br.portal.dto.ApiDtos.Login;
import br.portal.dto.ApiDtos.UsuarioResponse;
import br.portal.service.AutenticacaoService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Cadastro público (estudante e empresa), login por sessão e identificação do usuário logado. */
@RestController
@RequestMapping("/api/auth")
public class AuthController extends ControllerSupport {
    private final AutenticacaoService autenticacao;
    private final AuthenticationManager manager;
    private final SecurityContextRepository contextos = new HttpSessionSecurityContextRepository();

    public AuthController(UsuarioDao usuarios, AutenticacaoService autenticacao, AuthenticationManager manager) {
        super(usuarios);
        this.autenticacao = autenticacao;
        this.manager = manager;
    }

    /** Entrega o token CSRF (e o nome do cabeçalho) para a interface enviar nas requisições de escrita. */
    @GetMapping("/csrf")
    public CsrfResponse csrf(CsrfToken token) {
        return new CsrfResponse(token.getHeaderName(), token.getToken());
    }

    @PostMapping("/cadastro/estudante")
    public ResponseEntity<ApiMessage> cadastrarEstudante(@Valid @RequestBody CadastroEstudante r) {
        autenticacao.cadastrarEstudante(r.nome(), r.email(), r.curso(), r.senha());
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiMessage("Estudante cadastrado."));
    }

    @PostMapping("/cadastro/empresa")
    public ResponseEntity<ApiMessage> cadastrarEmpresa(@Valid @RequestBody CadastroEmpresa r) {
        autenticacao.cadastrarEmpresa(r.nome(), r.email(), r.nomeEmpresa(), r.senha());
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiMessage("Empresa cadastrada."));
    }

    /** Credenciais inválidas lançam AuthenticationException, que o ApiExceptionHandler converte em 401. */
    @PostMapping("/login")
    public UsuarioResponse login(@Valid @RequestBody Login r, HttpServletRequest req, HttpServletResponse res) {
        Authentication auth = manager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(r.email().trim().toLowerCase(), r.senha()));
        if (req.getSession(false) != null) {
            req.changeSessionId(); // evita fixação de sessão
        }
        SecurityContext contexto = SecurityContextHolder.createEmptyContext();
        contexto.setAuthentication(auth);
        SecurityContextHolder.setContext(contexto);
        contextos.saveContext(contexto, req, res); // grava na sessão HTTP
        return UsuarioResponse.de(ator(auth));
    }

    /** Quem sou eu? Usado pela interface para saber se há sessão e qual é o perfil. */
    @GetMapping("/me")
    public UsuarioResponse me(Authentication auth) {
        return UsuarioResponse.de(ator(auth));
    }

    // POST /api/auth/logout é tratado pelo Spring Security (ver SecurityConfig): responde 204.
}
