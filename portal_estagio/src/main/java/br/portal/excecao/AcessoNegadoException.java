package br.portal.excecao;

/** O usuário não tem o perfil exigido ou não é dono do registro. */
public class AcessoNegadoException extends PortalException {
    public AcessoNegadoException(String mensagem) {
        super(mensagem);
    }
}
