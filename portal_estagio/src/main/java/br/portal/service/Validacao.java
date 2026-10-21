package br.portal.service;

import br.portal.excecao.AcessoNegadoException;
import br.portal.excecao.ValidacaoException;
import br.portal.model.Perfil;
import br.portal.model.Usuario;

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

    static void exigirPerfil(Usuario ator, Perfil esperado, String acao) {
        if (ator == null || ator.getPerfil() != esperado) {
            throw new AcessoNegadoException("Somente " + esperado.name().toLowerCase()
                    + " pode " + acao + ".");
        }
    }
}
