package br.portal.controller;

import br.portal.dao.UsuarioDao;
import br.portal.model.Usuario;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;

/**
 * Base dos controllers: descobre QUEM está fazendo a requisição pela autenticação do Spring
 * Security (sessão). Nenhum id de usuário é aceito vindo do cliente.
 */
abstract class ControllerSupport {
    protected final UsuarioDao usuarios;

    ControllerSupport(UsuarioDao usuarios) {
        this.usuarios = usuarios;
    }

    protected Usuario ator(Authentication autenticacao) {
        if (autenticacao == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        return usuarios.buscarPorEmail(autenticacao.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }
}
