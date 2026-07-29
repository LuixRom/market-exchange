package com.dbp.proyectobackendmarketexchange.storage.infrastructure;

import com.dbp.proyectobackendmarketexchange.exception.InvalidStorageFileException;

import java.util.regex.Pattern;

/**
 * Valida el parámetro "directory" que cada llamador (p.ej. ItemService) le pasa a
 * StorageService.store(...). A diferencia de LocalStorageService -que además se protege a
 * sí mismo al resolver la ruta final contra el filesystem (normalize + startsWith)-, un
 * proveedor HTTP como Supabase arma la ruta como texto, así que necesita esta validación
 * explícita antes de construir el objectKey.
 *
 * Solo se permiten rutas lógicas relativas de segmentos alfanuméricos (guion y guion bajo
 * incluidos), sin ".", "..", barras invertidas, segmentos vacíos, ni slash inicial/final.
 */
final class StorageDirectories {

    private static final Pattern SEGMENT_PATTERN = Pattern.compile("^[A-Za-z0-9_-]+$");

    private StorageDirectories() {
    }

    static void validate(String directory) {
        if (directory == null || directory.isBlank()) {
            throw invalid();
        }
        if (directory.contains("\\")) {
            throw invalid();
        }
        if (directory.startsWith("/") || directory.endsWith("/")) {
            throw invalid();
        }
        for (String segment : directory.split("/", -1)) {
            if (segment.isEmpty() || segment.equals(".") || segment.equals("..")
                    || !SEGMENT_PATTERN.matcher(segment).matches()) {
                throw invalid();
            }
        }
    }

    private static InvalidStorageFileException invalid() {
        return new InvalidStorageFileException("Directorio de almacenamiento inválido");
    }
}
