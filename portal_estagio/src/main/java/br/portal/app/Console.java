package br.portal.app;

import java.io.PrintStream;
import java.util.NoSuchElementException;
import java.util.Scanner;

/** Leitura segura de entradas: nunca lança NumberFormatException para o menu. */
public class Console {
    private final Scanner entrada;
    private final PrintStream saida;

    public Console(Scanner entrada, PrintStream saida) {
        this.entrada = entrada;
        this.saida = saida;
    }

    public void escrever(String texto) {
        saida.println(texto);
    }

    /** Lê uma linha. Se a entrada acabar (Ctrl+D), sinaliza para encerrar o programa. */
    public String lerTexto(String rotulo) {
        saida.print(rotulo + ": ");
        try {
            return entrada.nextLine().trim();
        } catch (NoSuchElementException e) {
            throw new FimDaEntradaException();
        }
    }

    /** Retorna o número lido ou null se a entrada não for um inteiro válido. */
    public Integer lerInteiro(String rotulo) {
        String texto = lerTexto(rotulo);
        try {
            return Integer.parseInt(texto);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** Como lerInteiro, mas aceita vazio (retorna null) — útil para filtros opcionais. */
    public Long lerLongOpcional(String rotulo) {
        String texto = lerTexto(rotulo + " (Enter para ignorar)");
        if (texto.isEmpty()) {
            return null;
        }
        try {
            return Long.parseLong(texto);
        } catch (NumberFormatException e) {
            throw new br.portal.excecao.ValidacaoException("Número inválido: " + texto);
        }
    }

    public long lerLong(String rotulo) {
        String texto = lerTexto(rotulo);
        try {
            return Long.parseLong(texto);
        } catch (NumberFormatException e) {
            throw new br.portal.excecao.ValidacaoException("Número inválido: " + texto);
        }
    }

    public static class FimDaEntradaException extends RuntimeException {
    }
}
