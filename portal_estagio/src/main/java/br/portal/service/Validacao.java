package br.portal.service;

import br.portal.excecao.AcessoNegadoException;
import br.portal.excecao.ValidacaoException;
import br.portal.model.Perfil;
import br.portal.model.Usuario;

import java.nio.charset.StandardCharsets;

/** Validações reutilizadas pelos serviços. */
final class Validacao {
    private Validacao() {
    }

    static String textoObrigatorio(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new ValidacaoException("O campo '" + campo + "' é obrigatório.");
        }
        return valor.trim();
    }

    static String emailValido(String email) {
        String e = textoObrigatorio(email, "e-mail");
        if (!e.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new ValidacaoException("E-mail em formato inválido: " + e);
        }
        return e.toLowerCase();
    }

    /**
     * Política mínima de senha: 8 a 72 bytes (limite do BCrypt), com pelo menos uma letra e um número.
     * A mesma regra aparece em @Pattern nos DTOs para devolver 400 com mensagem por campo.
     */
    static String senhaForte(String senha) {
        if (senha == null || senha.length() < 8
                || senha.getBytes(StandardCharsets.UTF_8).length > 72
                || !senha.matches(".*[A-Za-z].*") || !senha.matches(".*\\d.*")) {
            throw new ValidacaoException("A senha deve ter de 8 a 72 caracteres, com letras e números.");
        }
        return senha;
    }

    static void exigirPerfil(Usuario ator, Perfil esperado, String acao) {
        if (ator == null || ator.getPerfil() != esperado) {
            throw new AcessoNegadoException("Somente " + esperado.name().toLowerCase()
                    + " pode " + acao + ".");
        }
    }
}
