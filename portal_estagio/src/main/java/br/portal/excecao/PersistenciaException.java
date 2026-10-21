package br.portal.excecao;

/**
 * Falha ao acessar o armazenamento. A implementação JDBC deve capturar SQLException
 * e relançar como PersistenciaException, para que a camada de serviço não dependa de java.sql.
 */
public class PersistenciaException extends PortalException {
    public PersistenciaException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
