package br.portal.excecao;

/** Violação de regra de negócio: e-mail repetido, candidatura duplicada, vaga encerrada etc. */
public class RegraNegocioException extends PortalException {
    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
