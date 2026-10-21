package br.portal.excecao;

/** Entrada inválida: campo ausente, formato incorreto, carga horária não positiva etc. */
public class ValidacaoException extends PortalException {
    public ValidacaoException(String mensagem) {
        super(mensagem);
    }
}
