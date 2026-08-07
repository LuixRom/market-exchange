package com.dbp.proyectobackendmarketexchange;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "app.seed.enabled=false",
        "jwt.secret=test-only-secret-not-real"
})
class ProyectoBackendMarketexchangeApplicationTests extends AbstractIntegrationTest {

    @Test
    void contextLoads() {
    }

}
