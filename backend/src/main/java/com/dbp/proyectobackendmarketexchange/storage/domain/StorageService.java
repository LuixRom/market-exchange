package com.dbp.proyectobackendmarketexchange.storage.domain;

import org.springframework.web.multipart.MultipartFile;

/**
 * Abstracción de almacenamiento de archivos, no acoplada a Item: cualquier dominio que
 * necesite guardar/leer/borrar un archivo puede reusar esta interfaz.
 *
 * storageKey es siempre una ruta lógica generada server-side (p.ej. "items/&lt;uuid&gt;.jpg"),
 * nunca derivada del nombre de archivo que mandó el cliente (previene path traversal).
 */
public interface StorageService {

    StorageProvider getProviderName();

    /**
     * Valida el archivo (no vacío, MIME real por contenido, tamaño) y lo almacena bajo un
     * nombre único dentro de {@code directory}. Lanza {@link com.dbp.proyectobackendmarketexchange.exception.InvalidStorageFileException}
     * si el archivo no es válido, o {@link com.dbp.proyectobackendmarketexchange.exception.StorageException}
     * si falla la escritura.
     */
    StorageObject store(MultipartFile file, String directory);

    /**
     * Devuelve el contenido del archivo. Lanza {@link com.dbp.proyectobackendmarketexchange.exception.ResourceNotFoundException}
     * si la key no existe, o {@link com.dbp.proyectobackendmarketexchange.exception.StorageException}
     * ante un fallo real del proveedor.
     */
    byte[] retrieve(String storageKey);

    boolean exists(String storageKey);

    /**
     * Idempotente: borrar una key que no existe no lanza excepción.
     */
    void delete(String storageKey);

    /**
     * URL pública (si el proveedor la tiene). Hoy no la usa el controller de Item -que
     * prefiere retrieve() para preservar el gate de autenticación existente-, se deja
     * preparada para un escenario futuro de URL directa (p.ej. bucket público de Supabase).
     */
    String getPublicUrl(String storageKey);
}
