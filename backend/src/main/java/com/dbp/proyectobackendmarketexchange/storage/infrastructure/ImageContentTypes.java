package com.dbp.proyectobackendmarketexchange.storage.infrastructure;

import com.dbp.proyectobackendmarketexchange.exception.InvalidStorageFileException;

import org.apache.tika.Tika;

import java.util.Set;

/**
 * Whitelist de tipos de imagen aceptados y su extensión asociada, compartida por todas las
 * implementaciones de StorageService -si cada una mantuviera su propia copia, podrían
 * divergir silenciosamente sobre qué tipos de archivo aceptan-.
 */
final class ImageContentTypes {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/jpeg", "image/png", "image/webp");
    private static final Tika TIKA = new Tika();

    private ImageContentTypes() {
    }

    /**
     * Detecta el tipo real por contenido (no confía en el Content-Type del cliente ni en la
     * extensión del nombre original, que son falsificables) y lo valida contra la whitelist.
     */
    static String detectAndValidate(byte[] content) {
        String contentType = TIKA.detect(content);
        if (!ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new InvalidStorageFileException("Tipo de archivo no permitido: " + contentType);
        }
        return contentType;
    }

    static String extensionFor(String contentType) {
        return switch (contentType) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> "";
        };
    }
}
