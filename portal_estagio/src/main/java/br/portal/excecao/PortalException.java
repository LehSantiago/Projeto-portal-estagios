package br.portal.excecao;

/** Base de todas as exceções do portal. Não verificada, para não poluir as assinaturas. */
public class PortalException extends RuntimeException {
    public PortalException(String mensagem) {
        super(mensagem);
    }

    public PortalException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
