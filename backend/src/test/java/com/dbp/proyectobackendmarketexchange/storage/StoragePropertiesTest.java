package com.dbp.proyectobackendmarketexchange.storage;

import com.dbp.proyectobackendmarketexchange.storage.config.StorageProperties;
import com.dbp.proyectobackendmarketexchange.storage.domain.StorageProvider;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StoragePropertiesTest {

    @Test
    void testValidate_LocalProvider_BlankSupabaseFields_NoException() {
        StorageProperties properties = new StorageProperties();
        properties.setProvider(StorageProvider.LOCAL);

        assertDoesNotThrow(properties::validate);
    }

    @Test
    void testValidate_SupabaseProvider_AllFieldsPresent_NoException() {
        StorageProperties properties = new StorageProperties();
        properties.setProvider(StorageProvider.SUPABASE);
        properties.getSupabase().setUrl("https://example.supabase.co");
        properties.getSupabase().setSecretKey("sb_secret_xxx");
        properties.getSupabase().setBucket("market-exchange-items");

        assertDoesNotThrow(properties::validate);
    }

    @Test
    void testValidate_SupabaseProvider_MissingUrl_Throws() {
        StorageProperties properties = new StorageProperties();
        properties.setProvider(StorageProvider.SUPABASE);
        properties.getSupabase().setSecretKey("secret");
        properties.getSupabase().setBucket("bucket");

        IllegalStateException ex = assertThrows(IllegalStateException.class, properties::validate);
        assertTrue(ex.getMessage().contains("app.storage.supabase.url"));
    }

    @Test
    void testValidate_SupabaseProvider_MissingSecretKey_Throws() {
        StorageProperties properties = new StorageProperties();
        properties.setProvider(StorageProvider.SUPABASE);
        properties.getSupabase().setUrl("https://example.supabase.co");
        properties.getSupabase().setBucket("bucket");

        IllegalStateException ex = assertThrows(IllegalStateException.class, properties::validate);
        assertTrue(ex.getMessage().contains("app.storage.supabase.secret-key"));
    }

    @Test
    void testValidate_SupabaseProvider_MissingBucket_Throws() {
        StorageProperties properties = new StorageProperties();
        properties.setProvider(StorageProvider.SUPABASE);
        properties.getSupabase().setUrl("https://example.supabase.co");
        properties.getSupabase().setSecretKey("secret");

        IllegalStateException ex = assertThrows(IllegalStateException.class, properties::validate);
        assertTrue(ex.getMessage().contains("app.storage.supabase.bucket"));
    }

    @Test
    void testValidate_SupabaseProvider_AllFieldsMissing_ListsAllInMessage() {
        StorageProperties properties = new StorageProperties();
        properties.setProvider(StorageProvider.SUPABASE);

        IllegalStateException ex = assertThrows(IllegalStateException.class, properties::validate);
        assertTrue(ex.getMessage().contains("app.storage.supabase.url"));
        assertTrue(ex.getMessage().contains("app.storage.supabase.secret-key"));
        assertTrue(ex.getMessage().contains("app.storage.supabase.bucket"));
    }

    @Test
    void testValidate_MessageNeverContainsSecretValue() {
        StorageProperties properties = new StorageProperties();
        properties.setProvider(StorageProvider.SUPABASE);
        properties.getSupabase().setUrl("https://example.supabase.co");
        properties.getSupabase().setSecretKey("");
        properties.getSupabase().setBucket("bucket-real-no-deberia-aparecer");

        // El mensaje debe nombrar la propiedad faltante (secret-key) pero jamás el valor
        // real de una propiedad presente (aquí, el nombre del bucket).
        IllegalStateException ex = assertThrows(IllegalStateException.class, properties::validate);
        assertTrue(ex.getMessage().contains("app.storage.supabase.secret-key"));
        assertFalse(ex.getMessage().contains("bucket-real-no-deberia-aparecer"));
    }

    @Test
    void testContextFailsToRefresh_WhenSupabaseProviderMissingCredentials() {
        new ApplicationContextRunner()
                .withUserConfiguration(TestConfig.class)
                .withPropertyValues(
                        "app.storage.provider=supabase",
                        "app.storage.supabase.url=",
                        "app.storage.supabase.secret-key=",
                        "app.storage.supabase.bucket=")
                .run(context -> assertThat(context).hasFailed());
    }

    @Test
    void testContextRefreshesFine_WhenLocalProviderWithBlankSupabaseFields() {
        new ApplicationContextRunner()
                .withUserConfiguration(TestConfig.class)
                .withPropertyValues("app.storage.provider=local")
                .run(context -> assertThat(context).hasNotFailed());
    }

    @Test
    void testContextRefreshesFine_WhenSupabaseProviderWithAllCredentials() {
        new ApplicationContextRunner()
                .withUserConfiguration(TestConfig.class)
                .withPropertyValues(
                        "app.storage.provider=supabase",
                        "app.storage.supabase.url=https://example.supabase.co",
                        "app.storage.supabase.secret-key=sb_secret_xxx",
                        "app.storage.supabase.bucket=market-exchange-items")
                .run(context -> assertThat(context).hasNotFailed());
    }

    @Configuration
    @EnableConfigurationProperties(StorageProperties.class)
    static class TestConfig {
    }
}
