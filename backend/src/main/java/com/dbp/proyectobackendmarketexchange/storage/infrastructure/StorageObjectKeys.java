package com.dbp.proyectobackendmarketexchange.storage.infrastructure;

import com.dbp.proyectobackendmarketexchange.exception.InvalidStorageFileException;

import java.util.regex.Pattern;

/**
 * Valida un storageKey/objectKey completo (p.ej. "items/&lt;uuid&gt;.jpg") antes de
 * usarlo para armar una ruta HTTP hacia Supabase. Deliberadamente distinto de
 * StorageDirectories: un objectKey real siempre termina en un segmento con un punto (la
 * extensión), que el patrón de segmento de directorio (sin puntos) rechazaría.
 */
final class StorageObjectKeys {

    private static final Pattern DIRECTORY_SEGMENT = Pattern.compile("^[A-Za-z0-9_-]+$");
    private static final Pattern FILENAME_SEGMENT = Pattern.compile("^[A-Za-z0-9_-]+(\\.[A-Za-z0-9]+)?$");

    private StorageObjectKeys() {
    }

    static void validate(String objectKey) {
        if (objectKey == null || objectKey.isBlank()) {
            throw invalid();
        }
        if (objectKey.contains("\\")) {
            throw invalid();
        }
        if (objectKey.startsWith("/") || objectKey.endsWith("/")) {
            throw invalid();
        }

        String[] segments = objectKey.split("/", -1);
        for (int i = 0; i < segments.length; i++) {
            String segment = segments[i];
            if (segment.isEmpty() || segment.equals(".") || segment.equals("..")) {
                throw invalid();
            }
            boolean isLastSegment = i == segments.length - 1;
            Pattern pattern = isLastSegment ? FILENAME_SEGMENT : DIRECTORY_SEGMENT;
            if (!pattern.matcher(segment).matches()) {
                throw invalid();
            }
        }
    }

    private static InvalidStorageFileException invalid() {
        return new InvalidStorageFileException("Referencia de almacenamiento inválida");
    }
}
