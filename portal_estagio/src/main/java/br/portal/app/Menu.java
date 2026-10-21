package br.portal.app;

import br.portal.excecao.PersistenciaException;
import br.portal.excecao.PortalException;
import br.portal.excecao.ValidacaoException;
import br.portal.model.Area;
import br.portal.model.Candidatura;
import br.portal.model.Modalidade;
import br.portal.model.Usuario;
import br.portal.model.Vaga;
import br.portal.service.RelatorioService.Contagens;

import java.util.List;

/**
 * Camada de apresentação (console). Não contém regras de negócio nem SQL:
 * lê entradas, chama os serviços e imprime resultados ou mensagens de erro.
 */
public class Menu {
    private final Contexto ctx;
    private final Console console;
    private Usuario usuarioAtual; // identidade SIMULADA (não é autenticação)

    public Menu(Contexto ctx, Console console) {
        this.ctx = ctx;
        this.console = console;
    }

    public void executar() {
        console.escrever("=== Portal de Estágios ===");
        try {
            boolean continuar = true;
            while (continuar) {
                try {
                    continuar = usuarioAtual == null ? menuInicial() : menuDoPerfil();
                } catch (ValidacaoException e) {
                    console.escrever("[Entrada inválida] " + e.getMessage());
                } catch (PersistenciaException e) {
                    console.escrever("[Falha no armazenamento] Não foi possível concluir a operação. Tente novamente.");
                } catch (PortalException e) {
                    console.escrever("[Operação recusada] " + e.getMessage());
                } catch (Console.FimDaEntradaException e) {
                    throw e;
                } catch (RuntimeException e) {
                    // rede de segurança: a aplicação não deve encerrar de forma inesperada
                    console.escrever("[Erro inesperado] " + e.getClass().getSimpleName() + ". A operação foi cancelada.");
                }
            }
        } catch (Console.FimDaEntradaException e) {
            console.escrever("\nEntrada encerrada.");
        }
        console.escrever("Até logo!");
    }

    // ---------------------------------------------------------------- menu inicial
    private boolean menuInicial() {
        console.escrever("\n--- Menu inicial (sem usuário selecionado) ---");
        console.escrever("1. Selecionar usuário de demonstração");
        console.escrever("2. Cadastrar estudante");
        console.escrever("3. Cadastrar empresa");
        console.escrever("4. Consultar vagas abertas");
        console.escrever("0. Sair");
        Integer opcao = console.lerInteiro("Opção");
        if (opcao == null) {
            console.escrever("Opção inválida.");
            return true;
        }
        switch (opcao) {
            case 1 -> selecionarUsuario();
            case 2 -> cadastrarEstudante();
            case 3 -> cadastrarEmpresa();
            case 4 -> consultarVagas();
            case 0 -> { return false; }
            default -> console.escrever("Opção inválida.");
        }
        return true;
    }

    private void selecionarUsuario() {
        List<Usuario> usuarios = ctx.usuarioService.listarTodos();
        usuarios.forEach(u -> console.escrever("  " + u));
        long id = console.lerLong("Id do usuário");
        usuarioAtual = ctx.usuarioService.buscarPorId(id);
        console.escrever("Usuário selecionado: " + usuarioAtual.getNome() + " (" + usuarioAtual.getPerfil() + ")");
    }

    private void cadastrarEstudante() {
        Usuario u = ctx.usuarioService.cadastrarEstudante(
                console.lerTexto("Nome"), console.lerTexto("E-mail"), console.lerTexto("Curso"));
        console.escrever("Estudante cadastrado: " + u);
    }

    private void cadastrarEmpresa() {
        Usuario u = ctx.usuarioService.cadastrarEmpresa(
                console.lerTexto("Nome do responsável"), console.lerTexto("E-mail"), console.lerTexto("Nome da empresa"));
        console.escrever("Empresa cadastrada: " + u);
    }

    private void consultarVagas() {
        listarAreas();
        Long areaId = console.lerLongOpcional("Filtrar por id da área");
        Modalidade modalidade = lerModalidadeOpcional();
        String palavra = console.lerTexto("Palavra-chave (Enter para ignorar)");
        List<Vaga> vagas = ctx.vagaService.consultarAbertas(areaId, modalidade, palavra);
        imprimirLista(vagas, "Nenhuma vaga aberta encontrada.");
    }

    // ---------------------------------------------------------------- menu por perfil
    private boolean menuDoPerfil() {
        console.escrever("\n--- Logado como: " + usuarioAtual.getNome() + " (" + usuarioAtual.getPerfil() + ") ---");
        switch (usuarioAtual.getPerfil()) {
            case ESTUDANTE -> {
                console.escrever("1. Consultar vagas abertas");
                console.escrever("2. Candidatar-me a uma vaga");
                console.escrever("3. Minhas candidaturas");
            }
            case EMPRESA -> {
                console.escrever("1. Publicar vaga");
                console.escrever("2. Minhas vagas");
                console.escrever("3. Editar vaga");
                console.escrever("4. Encerrar vaga");
                console.escrever("5. Candidatos de uma vaga");
                console.escrever("6. Consultar vagas abertas (público)");
            }
            case ADMINISTRADOR -> {
                console.escrever("1. Listar áreas");
                console.escrever("2. Cadastrar área");
                console.escrever("3. Editar área");
                console.escrever("4. Excluir área");
                console.escrever("5. Contagens administrativas");
            }
        }
        console.escrever("9. Trocar de usuário");
        console.escrever("0. Sair");
        Integer opcao = console.lerInteiro("Opção");
        if (opcao == null) {
            console.escrever("Opção inválida.");
            return true;
        }
        if (opcao == 0) {
            return false;
        }
        if (opcao == 9) {
            usuarioAtual = null;
            return true;
        }
        switch (usuarioAtual.getPerfil()) {
            case ESTUDANTE -> opcaoEstudante(opcao);
            case EMPRESA -> opcaoEmpresa(opcao);
            case ADMINISTRADOR -> opcaoAdministrador(opcao);
        }
        return true;
    }

    private void opcaoEstudante(int opcao) {
        switch (opcao) {
            case 1 -> consultarVagas();
            case 2 -> {
                consultarVagas();
                Candidatura c = ctx.candidaturaService.candidatar(usuarioAtual, console.lerLong("Id da vaga"));
                console.escrever("Candidatura registrada: " + c);
            }
            case 3 -> imprimirLista(ctx.candidaturaService.listarMinhas(usuarioAtual), "Você ainda não tem candidaturas.");
            default -> console.escrever("Opção inválida.");
        }
    }

    private void opcaoEmpresa(int opcao) {
        switch (opcao) {
            case 1 -> {
                listarAreas();
                Vaga v = ctx.vagaService.publicar(usuarioAtual,
                        console.lerTexto("Título"), console.lerTexto("Descrição"), console.lerLong("Id da área"),
                        console.lerTexto("Localização"), lerModalidade(), lerCarga());
                console.escrever("Vaga publicada: " + v);
            }
            case 2 -> imprimirLista(ctx.vagaService.listarDaEmpresa(usuarioAtual), "Você ainda não publicou vagas.");
            case 3 -> {
                imprimirLista(ctx.vagaService.listarDaEmpresa(usuarioAtual), "Você ainda não publicou vagas.");
                long id = console.lerLong("Id da vaga a editar");
                listarAreas();
                Vaga v = ctx.vagaService.editar(usuarioAtual, id,
                        console.lerTexto("Novo título"), console.lerTexto("Nova descrição"), console.lerLong("Id da área"),
                        console.lerTexto("Nova localização"), lerModalidade(), lerCarga());
                console.escrever("Vaga atualizada: " + v);
            }
            case 4 -> {
                imprimirLista(ctx.vagaService.listarDaEmpresa(usuarioAtual), "Você ainda não publicou vagas.");
                Vaga v = ctx.vagaService.encerrar(usuarioAtual, console.lerLong("Id da vaga a encerrar"));
                console.escrever("Vaga encerrada: " + v);
            }
            case 5 -> imprimirLista(
                    ctx.candidaturaService.listarCandidatos(usuarioAtual, console.lerLong("Id da vaga")),
                    "Nenhum candidato para esta vaga.");
            case 6 -> consultarVagas();
            default -> console.escrever("Opção inválida.");
        }
    }

    private void opcaoAdministrador(int opcao) {
        switch (opcao) {
            case 1 -> listarAreas();
            case 2 -> {
                Area a = ctx.areaService.cadastrar(usuarioAtual, console.lerTexto("Nome da nova área"));
                console.escrever("Área cadastrada: " + a);
            }
            case 3 -> {
                listarAreas();
                Area a = ctx.areaService.editar(usuarioAtual, console.lerLong("Id da área"), console.lerTexto("Novo nome"));
                console.escrever("Área atualizada: " + a);
            }
            case 4 -> {
                listarAreas();
                ctx.areaService.excluir(usuarioAtual, console.lerLong("Id da área a excluir"));
                console.escrever("Área excluída.");
            }
            case 5 -> {
                Contagens c = ctx.relatorioService.contagens(usuarioAtual);
                console.escrever("Estudantes: " + c.estudantes());
                console.escrever("Empresas: " + c.empresas());
                console.escrever("Vagas abertas: " + c.vagasAbertas());
                console.escrever("Vagas encerradas: " + c.vagasEncerradas());
                console.escrever("Candidaturas: " + c.candidaturas());
            }
            default -> console.escrever("Opção inválida.");
        }
    }

    // ---------------------------------------------------------------- auxiliares
    private void listarAreas() {
        console.escrever("Áreas:");
        imprimirLista(ctx.areaService.listar(), "Nenhuma área cadastrada.");
    }

    private <T> void imprimirLista(List<T> itens, String mensagemVazia) {
        if (itens.isEmpty()) {
            console.escrever("  " + mensagemVazia);
        } else {
            itens.forEach(i -> console.escrever("  " + i));
        }
    }

    private Modalidade lerModalidade() {
        Modalidade m = lerModalidadeOpcional();
        if (m == null) {
            throw new ValidacaoException("A modalidade é obrigatória.");
        }
        return m;
    }

    private Modalidade lerModalidadeOpcional() {
        String texto = console.lerTexto("Modalidade [PRESENCIAL/REMOTA/HIBRIDA] (Enter para ignorar)");
        if (texto.isEmpty()) {
            return null;
        }
        try {
            return Modalidade.valueOf(texto.toUpperCase().replace('Í', 'I'));
        } catch (IllegalArgumentException e) {
            throw new ValidacaoException("Modalidade inválida: " + texto);
        }
    }

    private int lerCarga() {
        Integer carga = console.lerInteiro("Carga horária semanal (horas)");
        if (carga == null) {
            throw new ValidacaoException("A carga horária deve ser um número inteiro.");
        }
        return carga;
    }
}
