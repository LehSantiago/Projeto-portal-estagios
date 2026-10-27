package br.portal.app;

import br.portal.dao.jdbc.ConexaoFactory;
import br.portal.excecao.PersistenciaException;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        boolean semBanco = args.length > 0 && args[0].equals("--memoria");
        Contexto ctx;

        if (semBanco) {
            System.out.println("[Modo --memoria] Sem banco de dados: os dados serão perdidos ao sair.");
            ctx = Contexto.criarEmMemoria();
            DadosDemonstracao.carregar(ctx);
        } else {
            ConexaoFactory conexoes = ConexaoFactory.doAmbiente();
            try {
                conexoes.testar();
            } catch (PersistenciaException e) {
                System.err.println("Não foi possível conectar ao PostgreSQL em " + conexoes.getUrl());
                System.err.println("Verifique se o banco está no ar, se os scripts em sql/ foram executados e se as variáveis");
                System.err.println("PORTAL_DB_URL, PORTAL_DB_USER e PORTAL_DB_PASSWORD estão corretas (veja o README).");
                System.exit(1);
                return;
            }
            ctx = Contexto.criarComJdbc(conexoes);
        }

        new Menu(ctx, new Console(new Scanner(System.in), System.out)).executar();
    }
}
