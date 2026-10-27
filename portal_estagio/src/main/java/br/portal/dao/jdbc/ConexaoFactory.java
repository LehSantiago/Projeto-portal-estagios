package br.portal.dao.jdbc;

import br.portal.excecao.PersistenciaException;
import br.portal.excecao.PortalException;
import br.portal.excecao.RegraNegocioException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Fabrica conexões com o PostgreSQL. As credenciais NUNCA ficam no código.
 * Cada valor é procurado nesta ordem:
 *   1. variável de ambiente do sistema;
 *   2. arquivo .env na pasta de execução (a raiz do projeto), que NÃO vai para o Git;
 *   3. valor padrão (serve só para um PostgreSQL local de desenvolvimento).
 *
 *   PORTAL_DB_URL       (padrão: jdbc:postgresql://localhost:5432/portal_estagios)
 *   PORTAL_DB_USER      (padrão: postgres)
 *   PORTAL_DB_PASSWORD  (padrão: vazio)
 */
public class ConexaoFactory {
    private static final String SQLSTATE_UNICIDADE = "23505";
    private static final String SQLSTATE_CHAVE_ESTRANGEIRA = "23503";
    private static final String SQLSTATE_CHECK = "23514";

    private final String url;
    private final String usuario;
    private final String senha;

    public ConexaoFactory(String url, String usuario, String senha) {
        this.url = url;
        this.usuario = usuario;
        this.senha = senha;
    }

    public static ConexaoFactory doAmbiente() {
        Map<String, String> arquivo = lerArquivoEnv(Path.of(".env"));
        return new ConexaoFactory(
                valor("PORTAL_DB_URL", arquivo, "jdbc:postgresql://localhost:5432/portal_estagios"),
                valor("PORTAL_DB_USER", arquivo, "postgres"),
                valor("PORTAL_DB_PASSWORD", arquivo, ""));
    }

    private static String valor(String nome, Map<String, String> arquivo, String padrao) {
        String v = System.getenv(nome);
        if (v == null || v.isBlank()) {
            v = arquivo.get(nome);
        }
        return (v == null || v.isBlank()) ? padrao : v;
    }

    /**
     * Lê um arquivo no formato CHAVE=VALOR (um por linha). Ignora linhas vazias e comentários (#),
     * aceita o prefixo "export " e aspas simples ou duplas ao redor do valor.
     * Se o arquivo não existir ou não puder ser lido, devolve um mapa vazio.
     */
    static Map<String, String> lerArquivoEnv(Path caminho) {
        Map<String, String> valores = new HashMap<>();
        if (!Files.isRegularFile(caminho)) {
            return valores;
        }
        try {
            for (String linha : Files.readAllLines(caminho)) {
                String l = linha.trim();
                if (l.isEmpty() || l.startsWith("#")) {
                    continue;
                }
                if (l.startsWith("export ")) {
                    l = l.substring(7).trim();
                }
                int igual = l.indexOf('=');
                if (igual <= 0) {
                    continue;
                }
                String chave = l.substring(0, igual).trim();
                String valor = l.substring(igual + 1).trim();
                if (valor.length() >= 2
                        && (valor.startsWith("\"") && valor.endsWith("\"")
                        || valor.startsWith("'") && valor.endsWith("'"))) {
                    valor = valor.substring(1, valor.length() - 1);
                }
                valores.put(chave, valor);
            }
        } catch (IOException e) {
            // arquivo ilegível: segue só com variáveis de ambiente e padrões
        }
        return valores;
    }

    /** Abre uma nova conexão. Quem chama deve fechá-la (try-with-resources). */
    public Connection abrir() {
        try {
            return DriverManager.getConnection(url, usuario, senha);
        } catch (SQLException e) {
            throw new PersistenciaException("Não foi possível conectar ao banco de dados.", e);
        }
    }

    /** Verifica a conexão logo na inicialização. */
    public void testar() {
        try (Connection c = abrir()) {
            c.isValid(2);
        } catch (SQLException e) {
            throw new PersistenciaException("Falha ao testar a conexão.", e);
        }
    }

    public String getUrl() {
        return url;
    }

    /**
     * Converte SQLException na exceção do domínio. Violações de restrição do banco
     * (unicidade, chave estrangeira, check) viram RegraNegocioException: são a segunda
     * linha de defesa caso uma validação do serviço seja contornada. O resto é falha de persistência.
     */
    static PortalException traduzir(SQLException e, String operacao) {
        String estado = e.getSQLState();
        if (SQLSTATE_UNICIDADE.equals(estado)) {
            return new RegraNegocioException("Registro duplicado: " + operacao + ".");
        }
        if (SQLSTATE_CHAVE_ESTRANGEIRA.equals(estado)) {
            return new RegraNegocioException("Operação viola um vínculo existente: " + operacao + ".");
        }
        if (SQLSTATE_CHECK.equals(estado)) {
            return new RegraNegocioException("Dados rejeitados pelo banco: " + operacao + ".");
        }
        return new PersistenciaException("Falha ao acessar o banco (" + operacao + ").", e);
    }
}
