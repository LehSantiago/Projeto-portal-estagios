package br.portal.service;

import br.portal.model.Usuario;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Cadastro com senha (Entrega 2). Valida a política de senha, gera o hash BCrypt e delega o resto
 * (campos obrigatórios, e-mail válido e único) ao UsuarioService, que já tinha essas regras.
 * Só existem cadastros de ESTUDANTE e EMPRESA: o perfil nunca vem do cliente.
 */
public class AutenticacaoService {
    private final UsuarioService usuarioService;
    private final PasswordEncoder encoder;

    public AutenticacaoService(UsuarioService usuarioService, PasswordEncoder encoder) {
        this.usuarioService = usuarioService;
        this.encoder = encoder;
    }

    public Usuario cadastrarEstudante(String nome, String email, String curso, String senha) {
        Validacao.senhaForte(senha);
        return usuarioService.cadastrarEstudante(nome, email, curso, encoder.encode(senha));
    }

    public Usuario cadastrarEmpresa(String nome, String email, String nomeEmpresa, String senha) {
        Validacao.senhaForte(senha);
        return usuarioService.cadastrarEmpresa(nome, email, nomeEmpresa, encoder.encode(senha));
    }

    /** Usado somente pelo AdminInitializer (inicialização controlada). */
    public Usuario cadastrarAdministradorDemonstracao(String nome, String email, String senha) {
        Validacao.senhaForte(senha);
        return usuarioService.cadastrarAdministradorDemonstracao(nome, email, encoder.encode(senha));
    }
}
