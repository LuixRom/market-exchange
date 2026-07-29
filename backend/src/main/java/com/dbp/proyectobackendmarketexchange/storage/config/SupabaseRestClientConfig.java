package com.dbp.proyectobackendmarketexchange.storage.config;

import org.springframework.boot.web.client.ClientHttpRequestFactories;
import org.springframework.boot.web.client.ClientHttpRequestFactorySettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * Cliente HTTP dedicado a Supabase Storage. Se expone un único bean, ya construido
 * ("supabaseRestClient"), en vez de un RestClient.Builder -un builder configurado con
 * headers de autenticación no debería quedar disponible como bean del contexto para que
 * otro componente lo mute o lo reutilice por error-.
 *
 * Cuando provider=LOCAL (o cuando faltan credenciales), el bean igual se construye: sin
 * baseUrl ni headers de auth, para no romper el arranque -StorageProperties.validate() ya
 * garantiza que, si provider=SUPABASE, url/secretKey/bucket están presentes antes de que
 * este bean llegue a usarse de verdad-.
 */
@Configuration
public class SupabaseRestClientConfig {

    @Bean("supabaseRestClient")
    public RestClient supabaseRestClient(StorageProperties storageProperties) {
        return buildSupabaseRestClientBuilder(storageProperties).build();
    }

    /**
     * Package-private (no es un @Bean) para que los tests puedan ejercitar la misma
     * construcción con MockRestServiceServer.bindTo(RestClient.Builder) sin que un builder
     * ya configurado con credenciales quede expuesto como bean del contexto.
     */
    static RestClient.Builder buildSupabaseRestClientBuilder(StorageProperties storageProperties) {
        StorageProperties.Supabase supabase = storageProperties.getSupabase();

        ClientHttpRequestFactorySettings settings = ClientHttpRequestFactorySettings.DEFAULTS
                .withConnectTimeout(supabase.getConnectTimeout())
                .withReadTimeout(supabase.getReadTimeout());
        ClientHttpRequestFactory requestFactory = ClientHttpRequestFactories.get(settings);

        RestClient.Builder builder = RestClient.builder().requestFactory(requestFactory);

        String baseUrl = supabase.getUrl();
        if (baseUrl != null && !baseUrl.isBlank()) {
            builder.baseUrl(baseUrl);
        }

        String secretKey = supabase.getSecretKey();
        if (secretKey != null && !secretKey.isBlank()) {
            builder.defaultHeader("Authorization", "Bearer " + secretKey)
                    .defaultHeader("apikey", secretKey);
        }

        return builder;
    }
}
