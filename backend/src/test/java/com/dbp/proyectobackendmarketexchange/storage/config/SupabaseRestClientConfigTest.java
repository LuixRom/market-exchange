package com.dbp.proyectobackendmarketexchange.storage.config;

import com.dbp.proyectobackendmarketexchange.storage.domain.StorageProvider;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.headerDoesNotExist;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * Ejercita SupabaseRestClientConfig.buildSupabaseRestClientBuilder(...) directamente
 * (paquete compartido, método package-private) usando MockRestServiceServer -ninguna
 * request sale a la red real-. El valor "test-secret" usado en estos tests no es una
 * credencial real.
 */
class SupabaseRestClientConfigTest {

    @Test
    void testBuilder_WithCredentials_SendsAuthHeadersAndBaseUrl() {
        StorageProperties properties = new StorageProperties();
        properties.setProvider(StorageProvider.SUPABASE);
        properties.getSupabase().setUrl("https://example.supabase.co");
        properties.getSupabase().setSecretKey("test-secret");
        properties.getSupabase().setBucket("market-exchange-items");

        RestClient.Builder builder = SupabaseRestClientConfig.buildSupabaseRestClientBuilder(properties);
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        RestClient client = builder.build();

        server.expect(requestTo("https://example.supabase.co/storage/v1/object/some-file.jpg"))
                .andExpect(header("Authorization", "Bearer test-secret"))
                .andExpect(header("apikey", "test-secret"))
                .andRespond(withSuccess());

        client.get().uri("/storage/v1/object/some-file.jpg").retrieve().toBodilessEntity();

        server.verify();
    }

    @Test
    void testBuilder_BlankCredentials_DoesNotThrowAndSendsNoAuthHeader() {
        StorageProperties properties = new StorageProperties();
        properties.setProvider(StorageProvider.LOCAL);
        // supabase.url/secretKey/bucket quedan en blanco por defecto -escenario provider=local-.

        RestClient.Builder builder = assertDoesNotThrow(
                () -> SupabaseRestClientConfig.buildSupabaseRestClientBuilder(properties));
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        RestClient client = assertDoesNotThrow(builder::build);

        // Sin baseUrl configurado, se necesita una URI absoluta para la request de prueba.
        server.expect(requestTo("http://localhost:12345/ping"))
                .andExpect(headerDoesNotExist("Authorization"))
                .andExpect(headerDoesNotExist("apikey"))
                .andRespond(withSuccess());

        client.get().uri("http://localhost:12345/ping").retrieve().toBodilessEntity();

        server.verify();
    }

    @Test
    void testStorageProperties_SupabaseTimeouts_DefaultValues() {
        StorageProperties properties = new StorageProperties();

        assertThat(properties.getSupabase().getConnectTimeout()).isEqualTo(Duration.ofSeconds(5));
        assertThat(properties.getSupabase().getReadTimeout()).isEqualTo(Duration.ofSeconds(15));
    }

    @Test
    void testStorageProperties_SupabaseTimeouts_BindFromProperties() {
        new ApplicationContextRunner()
                .withUserConfiguration(PropertiesOnlyConfig.class)
                .withPropertyValues(
                        "app.storage.provider=local",
                        "app.storage.supabase.connect-timeout=2s",
                        "app.storage.supabase.read-timeout=30s")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    StorageProperties properties = context.getBean(StorageProperties.class);
                    assertThat(properties.getSupabase().getConnectTimeout()).isEqualTo(Duration.ofSeconds(2));
                    assertThat(properties.getSupabase().getReadTimeout()).isEqualTo(Duration.ofSeconds(30));
                });
    }

    @Test
    void testContext_LocalProviderWithBlankCredentials_SupabaseRestClientBeanExists() {
        new ApplicationContextRunner()
                .withUserConfiguration(PropertiesOnlyConfig.class, SupabaseRestClientConfig.class)
                .withPropertyValues("app.storage.provider=local")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context.containsBean("supabaseRestClient")).isTrue();
                    assertThat(context.getBean("supabaseRestClient")).isInstanceOf(RestClient.class);
                });
    }

    @Configuration
    @EnableConfigurationProperties(StorageProperties.class)
    static class PropertiesOnlyConfig {
    }
}
