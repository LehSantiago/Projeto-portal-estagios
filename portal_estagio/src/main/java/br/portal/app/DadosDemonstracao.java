package br.portal.app;

import br.portal.model.Area;
import br.portal.model.Modalidade;
import br.portal.model.Usuario;

/** Dados fictícios para a identidade simulada. Futuramente virão do script SQL. */
public final class DadosDemonstracao {
    private DadosDemonstracao() {
    }

    public static void carregar(Contexto ctx) {
        Usuario admin = ctx.usuarioService.cadastrarAdministradorDemonstracao("Admin Demo", "admin@portal.com");

        Area tecnologia = ctx.areaService.cadastrar(admin, "Tecnologia");
        ctx.areaService.cadastrar(admin, "Administração");
        ctx.areaService.cadastrar(admin, "Design");

        Usuario techCorp = ctx.usuarioService.cadastrarEmpresa("Carla RH", "rh@techcorp.com", "TechCorp");
        ctx.usuarioService.cadastrarEmpresa("Paulo RH", "rh@designhub.com", "DesignHub");
        ctx.usuarioService.cadastrarEstudante("Ana Souza", "ana@aluno.com", "Ciência da Computação");
        ctx.usuarioService.cadastrarEstudante("Bruno Lima", "bruno@aluno.com", "Design Gráfico");

        ctx.vagaService.publicar(techCorp, "Estágio em Desenvolvimento Java",
                "Apoio ao desenvolvimento de APIs", tecnologia.getId(), "São Paulo", Modalidade.HIBRIDA, 30);
        ctx.vagaService.publicar(techCorp, "Estágio em Suporte",
                "Atendimento a usuários internos", tecnologia.getId(), "Remoto", Modalidade.REMOTA, 20);
    }
}
