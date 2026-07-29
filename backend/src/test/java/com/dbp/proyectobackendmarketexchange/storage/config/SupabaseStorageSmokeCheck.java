package com.dbp.proyectobackendmarketexchange.storage.config;

import com.dbp.proyectobackendmarketexchange.storage.domain.StorageObject;
import com.dbp.proyectobackendmarketexchange.storage.domain.StorageProvider;
import com.dbp.proyectobackendmarketexchange.storage.infrastructure.SupabaseStorageService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.client.RestClient;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Smoke test FINAL de ciclo completo contra el proyecto REAL de Supabase Storage. Prueba
 * exclusivamente el contrato público de StorageService -store/exists/retrieve/delete-, sin
 * tocar RestClient directamente (a diferencia de las investigaciones temporales del bloque
 * 8, ya retiradas: HEAD quedó descartado por un 400 real, y ese mismo 400 con cuerpo
 * {"statusCode":"404","error":"not_found"} ya está reconocido por
 * SupabaseStorageErrors.isObjectNotFound(), que exists()/retrieve()/delete() usan en
 * producción).
 *
 * Dos barreras contra ejecución accidental: el nombre de la clase no matchea los patrones
 * por defecto de Surefire (Test*.java, *Test.java, *Tests.java, *TestCase.java) así que
 * "mvn test"/CI ni la descubre, y además está deshabilitada salvo que se exporte
 * explícitamente RUN_SUPABASE_SMOKE_TEST=true.
 *
 * Requiere SUPABASE_URL, SUPABASE_SECRET_KEY y SUPABASE_STORAGE_BUCKET ya exportadas en el
 * entorno. Nunca imprime la URL del proyecto, la secret key, el apikey, el header
 * Authorization ni ningún cuerpo de error -solo los pasos saneados de la lista de abajo-.
 * Sube/lee/borra un único objeto pequeño y determinista bajo "smoke-tests/" -nunca bajo
 * "items/", el directorio real de Item- y lo limpia incondicionalmente al terminar.
 *
 * Ejecutar (PowerShell), desde backend/:
 *   $env:RUN_SUPABASE_SMOKE_TEST = "true"
 *   .\mvnw.cmd test "-Dtest=SupabaseStorageSmokeCheck#verifyFullStorageServiceLifecycle"
 */
@EnabledIfEnvironmentVariable(named = "RUN_SUPABASE_SMOKE_TEST", matches = "true")
class SupabaseStorageSmokeCheck {

    private static final byte[] SMOKE_BYTES = {
            (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0,
            0x00, 0x10, 'J', 'F', 'I', 'F', 0x00, 0x01, 0x01, 0x00, 0x00, 0x01, 0x00, 0x01, 0x00, 0x00
    };

    private static final String SMOKE_TEST_DIRECTORY = "smoke-tests";

    @Test
    void verifyFullStorageServiceLifecycle() throws Throwable {
        StorageProperties properties = new StorageProperties();
        properties.setProvider(StorageProvider.SUPABASE);
        properties.getSupabase().setUrl(requireEnv("SUPABASE_URL"));
        properties.getSupabase().setSecretKey(requireEnv("SUPABASE_SECRET_KEY"));
        properties.getSupabase().setBucket(requireEnv("SUPABASE_STORAGE_BUCKET"));

        RestClient restClient = SupabaseRestClientConfig.buildSupabaseRestClientBuilder(properties).build();
        SupabaseStorageService service = new SupabaseStorageService(properties, restClient);

        // 1. Contenido conocido.
        MockMultipartFile file = new MockMultipartFile("image", "smoke.jpg", "image/jpeg", SMOKE_BYTES);

        // 2. Confirmar exists(key) == false antes de almacenar -key de sondeo propia, ya
        // que store() genera su propia key server-side y todavía no la conocemos-.
        String probeKey = SMOKE_TEST_DIRECTORY + "/" + UUID.randomUUID() + ".jpg";
        assertFalse(service.exists(probeKey), "La key de sondeo no debería existir todavía");
        System.out.println("[SMOKE] initial exists=false");

        StorageObject stored = null;
        Throwable primaryFailure = null;
        try {
            // 3. store() y validar la key devuelta.
            stored = service.store(file, SMOKE_TEST_DIRECTORY);
            String objectKey = stored.storageKey();
            assertTrue(objectKey != null && !objectKey.isBlank(), "objectKey no debe ser null ni blank");
            assertTrue(objectKey.startsWith(SMOKE_TEST_DIRECTORY + "/"),
                    "objectKey debe vivir bajo " + SMOKE_TEST_DIRECTORY + "/");
            System.out.println("[SMOKE] store=ok");

            // 4. exists(storedKey) == true.
            assertTrue(service.exists(objectKey), "El objeto recién subido debería existir");
            System.out.println("[SMOKE] exists after store=true");

            // 5. retrieve(storedKey): igualdad exacta de bytes.
            byte[] downloaded = service.retrieve(objectKey);
            assertArrayEquals(SMOKE_BYTES, downloaded, "Los bytes descargados deben coincidir exactamente");
            System.out.println("[SMOKE] retrieve bytesMatch=true");

            // 6. delete(storedKey).
            service.delete(objectKey);
            System.out.println("[SMOKE] delete=ok");

            // 7. exists(storedKey) == false.
            assertFalse(service.exists(objectKey), "El objeto debería haber desaparecido tras delete()");
            System.out.println("[SMOKE] exists after delete=false");

            // 8. delete(storedKey) por segunda vez: no debe lanzar (idempotente).
            assertDoesNotThrow(() -> service.delete(objectKey), "Un segundo delete() debe ser idempotente");
            System.out.println("[SMOKE] idempotent delete=ok");

            // 9. Confirmar nuevamente exists(storedKey) == false.
            assertFalse(service.exists(objectKey), "El objeto no debe seguir en el bucket");
            System.out.println("[SMOKE] final exists=false");
        } catch (Throwable failure) {
            primaryFailure = failure;
            throw failure;
        } finally {
            // Limpieza incondicional: en el camino feliz el objeto ya fue borrado en el
            // paso 6 (delete() es idempotente, así que repetirlo acá es inofensivo); si
            // alguna aserción intermedia falló antes de llegar a ese paso, esto es lo único
            // que evita dejar basura real en el bucket.
            if (stored != null) {
                try {
                    service.delete(stored.storageKey());
                } catch (RuntimeException cleanupFailure) {
                    if (primaryFailure != null) {
                        // Ya hubo un fallo principal: se conserva como suppressed para no
                        // perder ninguno de los dos diagnósticos.
                        primaryFailure.addSuppressed(cleanupFailure);
                    } else {
                        // No hubo fallo principal: un fallo del propio cleanup debe hacer
                        // fallar el test, no ignorarse en silencio.
                        throw cleanupFailure;
                    }
                }
            }
        }
    }

    private static String requireEnv(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Falta la variable de entorno " + name
                    + " -este smoke test requiere credenciales reales de Supabase ya exportadas-");
        }
        return value;
    }
}
