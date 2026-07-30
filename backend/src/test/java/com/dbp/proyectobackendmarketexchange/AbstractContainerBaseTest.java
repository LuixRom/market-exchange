package com.dbp.proyectobackendmarketexchange;

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.testcontainers.junit.jupiter.Testcontainers;

@DataJpaTest
@Testcontainers(disabledWithoutDocker = true)
public abstract class AbstractContainerBaseTest extends AbstractIntegrationTest {
}
