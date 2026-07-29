package com.dbp.proyectobackendmarketexchange.storage.config;

import com.dbp.proyectobackendmarketexchange.storage.domain.StorageProvider;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Configuración tipada de app.storage.*. No usa @Data/@ToString (Lombok) para no arriesgar
 * exponer supabase.secretKey en un toString() generado automáticamente -si en el futuro se
 * necesita un toString(), debe escribirse a mano excluyendo ese campo explícitamente-.
 */
@Component
@ConfigurationProperties(prefix = "app.storage")
@Getter
@Setter
public class StorageProperties {

    private StorageProvider provider = StorageProvider.LOCAL;
    private Local local = new Local();
    private Supabase supabase = new Supabase();

    /**
     * Falla rápido al arrancar si provider=SUPABASE y falta alguna credencial. El mensaje
     * solo nombra las propiedades faltantes, nunca sus valores.
     */
    @PostConstruct
    public void validate() {
        if (provider != StorageProvider.SUPABASE) {
            return;
        }
        List<String> missing = new ArrayList<>();
        if (isBlank(supabase.getUrl())) {
            missing.add("app.storage.supabase.url");
        }
        if (isBlank(supabase.getSecretKey())) {
            missing.add("app.storage.supabase.secret-key");
        }
        if (isBlank(supabase.getBucket())) {
            missing.add("app.storage.supabase.bucket");
        }
        if (!missing.isEmpty()) {
            throw new IllegalStateException(
                    "Configuración de Supabase incompleta: falta(n) " + String.join(", ", missing)
                            + ". Verifica las variables de entorno correspondientes.");
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    @Getter
    @Setter
    public static class Local {
        private String baseDirectory = "uploads";
    }

    @Getter
    @Setter
    public static class Supabase {
        private String url;
        private String secretKey;
        private String bucket;
        private Duration connectTimeout = Duration.ofSeconds(5);
        private Duration readTimeout = Duration.ofSeconds(15);
    }
}
