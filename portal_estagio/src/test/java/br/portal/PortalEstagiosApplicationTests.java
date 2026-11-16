package br.portal;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/** Verifica que o contexto Spring sobe (perfil "memoria": não precisa de PostgreSQL). */
@SpringBootTest
@ActiveProfiles("memoria")
class PortalEstagiosApplicationTests {

	@Test
	void contextLoads() {
	}

}
