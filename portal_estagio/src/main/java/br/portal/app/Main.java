package br.portal.app;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Contexto ctx = Contexto.criarEmMemoria();
        DadosDemonstracao.carregar(ctx);
        new Menu(ctx, new Console(new Scanner(System.in), System.out)).executar();
    }
}
