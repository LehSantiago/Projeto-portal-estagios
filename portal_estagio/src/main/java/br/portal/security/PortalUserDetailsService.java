package br.portal.security;

import br.portal.dao.UsuarioDao;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Ensina o Spring Security a buscar e-mail, hash e perfil no banco (via DAO).
 * O perfil vira a authority ROLE_ESTUDANTE, ROLE_EMPRESA ou ROLE_ADMINISTRADOR.
 */
@Service
public class PortalUserDetailsService implements UserDetailsService {
    private final UsuarioDao usuarios;

    public PortalUserDetailsService(UsuarioDao usuarios) {
        this.usuarios = usuarios;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UsuarioDao.Credencial c = usuarios.buscarCredencialPorEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
        if (c.senhaHash() == null) {
            // conta da Entrega 1, sem senha: tratada como inexistente (não consegue entrar)
            throw new UsernameNotFoundException("Conta sem senha");
        }
        return User.withUsername(c.email()).password(c.senhaHash()).roles(c.perfil().name()).build();
    }
}
