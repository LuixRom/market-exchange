package com.dbp.proyectobackendmarketexchange.storage.domain;

/**
 * Descriptor de un archivo ya almacenado. No es un envoltorio de éxito/fracaso -eso ya
 * lo comunican las excepciones (StorageException/InvalidStorageFileException)-, es el
 * valor que se obtiene cuando store() efectivamente tuvo éxito.
 */
public record StorageObject(
        String storageKey,
        StorageProvider provider,
        String contentType,
        String originalFilename,
        long sizeBytes
) {
}
