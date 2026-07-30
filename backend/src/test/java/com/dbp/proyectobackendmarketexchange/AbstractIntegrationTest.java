package com.dbp.proyectobackendmarketexchange;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Bootstrap de Testcontainers sin @DataJpaTest, para usar con @SpringBootTest cuando
 * hace falta el contexto completo (servicios reales, no solo el slice JPA) y
 * transacciones reales por hilo -p.ej. tests de concurrencia-, algo que
 * AbstractContainerBaseTest no permite por estar anotada @DataJpaTest en la clase
 * abstracta misma.
 */
@Testcontainers(disabledWithoutDocker = true)
public abstract class AbstractIntegrationTest {

    protected static final PostgreSQLContainer<?> postgresqlContainer;

    static {
        postgresqlContainer = new PostgreSQLContainer<>("postgres:16-alpine")
                .withDatabaseName("e2eTestDb")
                .withUsername("e2e")
                .withPassword("e2e");
        postgresqlContainer.start();
    }

    @DynamicPropertySource
    static void overrideTestProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresqlContainer::getUsername);
        registry.add("spring.datasource.password", postgresqlContainer::getPassword);
    }
}
