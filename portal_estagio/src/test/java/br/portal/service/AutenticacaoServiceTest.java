package br.portal.service;

import br.portal.dao.UsuarioDao;
import br.portal.dao.memoria.UsuarioDaoMemoria;
import br.portal.excecao.RegraNegocioException;
import br.portal.excecao.ValidacaoException;
import br.portal.model.Perfil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Cadastro com senha: política de senha, hash BCrypt e e-mail único (sem Spring, só Services + DAO em memória). */
class AutenticacaoServiceTest {
    private UsuarioDao dao;
    private BCryptPasswordEncoder encoder;
    private AutenticacaoService auth;

    @BeforeEach
    void preparar() {
        dao = new UsuarioDaoMemoria();
        encoder = new BCryptPasswordEncoder();
        auth = new AutenticacaoService(new UsuarioService(dao), encoder);
    }

    @Test
    void cadastroValidoGuardaSomenteOHashBCrypt() {
        auth.cadastrarEstudante("Ana", "ana@t.com", "Computação", "Senha1234");

        UsuarioDao.Credencial c = dao.buscarCredencialPorEmail("ana@t.com").orElseThrow();
        assertEquals(Perfil.ESTUDANTE, c.perfil());
        assertFalse(c.senhaHash().contains("Senha1234"));
        assertTrue(c.senhaHash().startsWith("$2"));
        assertTrue(encoder.matches("Senha1234", c.senhaHash()));
    }

    @Test
    void senhaFracaEhRejeitada() {
        assertThrows(ValidacaoException.class, () -> auth.cadastrarEstudante("Ana", "ana@t.com", "BCC", "curta1"));
        assertThrows(ValidacaoException.class, () -> auth.cadastrarEstudante("Ana", "ana@t.com", "BCC", "somenteletras"));
        assertThrows(ValidacaoException.class, () -> auth.cadastrarEmpresa("RH", "rh@t.com", "Empresa", "12345678"));
        assertTrue(dao.buscarPorEmail("ana@t.com").isEmpty());
    }

    @Test
    void emailDuplicadoEhRejeitadoIgnorandoMaiusculas() {
        auth.cadastrarEmpresa("RH", "rh@t.com", "Empresa", "Senha1234");
        assertThrows(RegraNegocioException.class,
                () -> auth.cadastrarEstudante("Outro", "RH@T.COM", "Design", "Senha1234"));
    }
}
