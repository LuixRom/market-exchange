package com.dbp.proyectobackendmarketexchange.storage.infrastructure;

import com.dbp.proyectobackendmarketexchange.exception.InvalidStorageFileException;
import com.dbp.proyectobackendmarketexchange.exception.ResourceNotFoundException;
import com.dbp.proyectobackendmarketexchange.exception.StorageException;
import com.dbp.proyectobackendmarketexchange.storage.config.StorageProperties;
import com.dbp.proyectobackendmarketexchange.storage.domain.StorageObject;
import com.dbp.proyectobackendmarketexchange.storage.domain.StorageProvider;
import com.dbp.proyectobackendmarketexchange.storage.domain.StorageService;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Implementación de Supabase Storage. store(), retrieve(), exists() y delete() están
 * implementados; getPublicUrl() sigue sin tocar red (fuera de alcance todavía) y lanza una
 * excepción clara en vez de fallar silenciosamente o con un NullPointerException. Se
 * registra siempre como bean (sin @ConditionalOnProperty) para que, si algún Item ya
 * tuviera imageProvider=SUPABASE en el futuro, StorageServiceRegistry pueda resolverlo
 * igual y falle con un mensaje explícito en vez de un bean inexistente.
 */
@Service
public class SupabaseStorageService implements StorageService {

    private static final String OBJECT_ENDPOINT = "/storage/v1/object/";
    private static final String PATH_DELIMITER = "/";

    private final StorageProperties storageProperties;
    private final RestClient supabaseRestClient;

    public SupabaseStorageService(StorageProperties storageProperties,
                                   @Qualifier("supabaseRestClient") RestClient supabaseRestClient) {
        this.storageProperties = storageProperties;
        this.supabaseRestClient = supabaseRestClient;
    }

    @Override
    public StorageProvider getProviderName() {
        return StorageProvider.SUPABASE;
    }

    @Override
    public StorageObject store(MultipartFile file, String directory) {
        if (file == null || file.isEmpty()) {
            throw new InvalidStorageFileException("El archivo no puede estar vacío");
        }
        StorageDirectories.validate(directory);

        StorageProperties.Supabase supabase = storageProperties.getSupabase();
        String bucket = supabase.getBucket();
        ensureConfigured(supabase, bucket);

        byte[] content;
        try {
            content = file.getBytes();
        } catch (IOException e) {
            throw new StorageException("No se pudo leer el archivo", e);
        }

        String contentType = ImageContentTypes.detectAndValidate(content);
        String objectKey = directory + "/" + UUID.randomUUID() + ImageContentTypes.extensionFor(contentType);

        // Se arma la ruta como texto ya resuelto (no como variable de plantilla) porque
        // objectKey contiene "/" a propósito -si se pasara como {objectKey} a uri(...), el
        // encoding por defecto de RestClient escaparía esas barras y rompería la ruta real
        // en Supabase-. Es seguro: bucket/directory/objectKey son siempre server-side.
        String path = buildObjectPath(bucket, objectKey);

        try {
            supabaseRestClient.post()
                    .uri(path)
                    .contentType(MediaType.parseMediaType(contentType))
                    .header("x-upsert", "false")
                    .body(content)
                    .retrieve()
                    .toBodilessEntity();
        } catch (HttpClientErrorException e) {
            throw new StorageException("Supabase Storage rechazó la carga del archivo", e);
        } catch (HttpServerErrorException e) {
            throw new StorageException("Supabase Storage no se encuentra disponible", e);
        } catch (ResourceAccessException e) {
            throw new StorageException("No fue posible conectarse con Supabase Storage", e);
        } catch (RestClientException e) {
            throw new StorageException("Ocurrió un error al comunicarse con Supabase Storage", e);
        }

        return new StorageObject(objectKey, StorageProvider.SUPABASE, contentType, file.getOriginalFilename(), content.length);
    }

    private void ensureConfigured(StorageProperties.Supabase supabase, String bucket) {
        List<String> missing = new ArrayList<>();
        if (isBlank(supabase.getUrl())) {
            missing.add("app.storage.supabase.url");
        }
        if (isBlank(supabase.getSecretKey())) {
            missing.add("app.storage.supabase.secret-key");
        }
        if (isBlank(bucket)) {
            missing.add("app.storage.supabase.bucket");
        }
        if (!missing.isEmpty()) {
            throw new StorageException(
                    "Configuración de Supabase incompleta: falta(n) " + String.join(", ", missing));
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static String buildObjectPath(String bucket, String key) {
        return OBJECT_ENDPOINT + bucket + PATH_DELIMITER + key;
    }

    @Override
    public byte[] retrieve(String storageKey) {
        StorageObjectKeys.validate(storageKey);

        StorageProperties.Supabase supabase = storageProperties.getSupabase();
        String bucket = supabase.getBucket();
        ensureConfigured(supabase, bucket);

        String path = buildObjectPath(bucket, storageKey);

        byte[] body;
        try {
            body = supabaseRestClient.get()
                    .uri(path)
                    .retrieve()
                    .body(byte[].class);
        } catch (HttpClientErrorException e) {
            if (SupabaseStorageErrors.isObjectNotFound(e)) {
                throw new ResourceNotFoundException("Archivo no encontrado");
            }
            throw new StorageException("Supabase Storage rechazó la descarga del archivo", e);
        } catch (HttpServerErrorException e) {
            throw new StorageException("Supabase Storage no se encuentra disponible", e);
        } catch (ResourceAccessException e) {
            throw new StorageException("No fue posible conectarse con Supabase Storage", e);
        } catch (RestClientException e) {
            throw new StorageException("Ocurrió un error al comunicarse con Supabase Storage", e);
        }

        // LocalStorageService.retrieve() nunca devuelve null: o lee bytes reales (incluido
        // un archivo de 0 bytes en disco, que sí se acepta ahí) o lanza ResourceNotFoundException/
        // StorageException. RestClient.body(byte[].class) no permite replicar esa distinción:
        // una respuesta 200 con cuerpo vacío y una 204 sin contenido llegan igual como null
        // (verificado; el extractor de Spring nunca invoca un HttpMessageConverter en ninguno
        // de los dos casos), así que no hay forma de saber si Supabase realmente devolvió un
        // archivo vacío o directamente no devolvió nada. Por eso null nunca es un resultado
        // silencioso acá: siempre se traduce a una excepción explícita.
        if (body == null) {
            throw new StorageException("Supabase Storage devolvió una respuesta sin contenido");
        }

        return body;
    }

    @Override
    public boolean exists(String storageKey) {
        StorageObjectKeys.validate(storageKey);

        StorageProperties.Supabase supabase = storageProperties.getSupabase();
        String bucket = supabase.getBucket();
        ensureConfigured(supabase, bucket);

        String path = buildObjectPath(bucket, storageKey);

        try {
            // GET + Range: bytes=0-0, no HEAD: el smoke test manual (bloque 8) confirmó
            // contra el proyecto real que HEAD responde 400 en esta ruta. Range pide solo el
            // primer byte -200/206 confirman existencia sin descargar el objeto completo-.
            supabaseRestClient.get()
                    .uri(path)
                    .header(HttpHeaders.RANGE, "bytes=0-0")
                    .retrieve()
                    .toBodilessEntity();
            return true;
        } catch (HttpClientErrorException e) {
            if (SupabaseStorageErrors.isObjectNotFound(e)) {
                return false;
            }
            throw new StorageException("Supabase Storage rechazó la verificación del archivo", e);
        } catch (HttpServerErrorException e) {
            throw new StorageException("Supabase Storage no se encuentra disponible", e);
        } catch (ResourceAccessException e) {
            throw new StorageException("No fue posible conectarse con Supabase Storage", e);
        } catch (RestClientException e) {
            throw new StorageException("Ocurrió un error al comunicarse con Supabase Storage", e);
        }
    }

    @Override
    public void delete(String storageKey) {
        StorageObjectKeys.validate(storageKey);

        StorageProperties.Supabase supabase = storageProperties.getSupabase();
        String bucket = supabase.getBucket();
        ensureConfigured(supabase, bucket);

        String path = buildObjectPath(bucket, storageKey);

        try {
            // Una sola request: sin exists() previo, un 404 acá ya significa "objetivo
            // cumplido" (idempotente, igual que LocalStorageService.delete()).
            supabaseRestClient.delete().uri(path).retrieve().toBodilessEntity();
        } catch (HttpClientErrorException e) {
            if (SupabaseStorageErrors.isObjectNotFound(e)) {
                return;
            }
            throw new StorageException("Supabase Storage rechazó la eliminación del archivo", e);
        } catch (HttpServerErrorException e) {
            throw new StorageException("Supabase Storage no se encuentra disponible", e);
        } catch (ResourceAccessException e) {
            throw new StorageException("No fue posible conectarse con Supabase Storage", e);
        } catch (RestClientException e) {
            throw new StorageException("Ocurrió un error al comunicarse con Supabase Storage", e);
        }
    }

    /**
     * No soportado por diseño, no por falta de tiempo: el bucket es privado y el único
     * acceso válido es el endpoint autenticado del backend (GET /item/{id}/image), que ya
     * usa retrieve()/exists() en vez de esto. Implementarlo de verdad exigiría hacer
     * público el bucket o generar una signed URL -ninguna de las dos es aceptable acá-.
     */
    @Override
    public String getPublicUrl(String storageKey) {
        throw new StorageException("Supabase Storage no admite URLs públicas para buckets privados");
    }
}
