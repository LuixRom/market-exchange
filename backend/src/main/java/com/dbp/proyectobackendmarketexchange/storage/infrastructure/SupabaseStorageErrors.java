package com.dbp.proyectobackendmarketexchange.storage.infrastructure;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

/**
 * Determina si un HttpClientErrorException real de Supabase Storage representa "el objeto
 * no existe", más allá de un 404 directo. El smoke test manual del bloque 8 confirmó contra
 * el proyecto real que Supabase responde HTTP 400 (no 404) para un objeto ausente, con un
 * cuerpo JSON {"statusCode":"404","error":"not_found","message":"..."} -por eso no alcanza
 * con mirar solo el status HTTP-.
 *
 * Deliberadamente NO usa un ObjectMapper inyectado desde Spring: SupabaseStorageService se
 * construye directamente (sin Spring) tanto en los tests como en SupabaseStorageSmokeCheck
 * (bloque 8, investigación manual que no se debe tocar en este bloque) -agregar un tercer
 * parámetro al constructor rompería la compilación de ese archivo y obligaría a tocar los
 * ~9 call-sites de test que no tienen relación con esta funcionalidad-. Un ObjectMapper
 * privado acá es barato de crear (uso puntual, sin configuración custom real) y mantiene el
 * cambio acotado a lo que este bloque necesita.
 */
final class SupabaseStorageErrors {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private SupabaseStorageErrors() {
    }

    /**
     * Solo clasifica por cuerpo estructurado cuando el status es exactamente 400 -cualquier
     * otro status (401/403/409/etc.) nunca se reinterpreta como "no encontrado" a partir del
     * cuerpo, para no convertir un error real en un falso negativo silencioso-.
     */
    static boolean isObjectNotFound(HttpClientErrorException exception) {
        if (exception.getStatusCode() == HttpStatus.NOT_FOUND) {
            return true;
        }
        if (exception.getStatusCode() != HttpStatus.BAD_REQUEST) {
            return false;
        }
        return hasNotFoundBody(exception);
    }

    private static boolean hasNotFoundBody(HttpClientErrorException exception) {
        String body = exception.getResponseBodyAsString();
        if (body == null || body.isBlank()) {
            return false;
        }
        JsonNode json;
        try {
            json = OBJECT_MAPPER.readTree(body);
        } catch (Exception e) {
            return false;
        }
        if (json == null || !json.isObject()) {
            return false;
        }
        return isStatusCode404(json.get("statusCode")) && isNotFoundError(json.get("error"));
    }

    private static boolean isStatusCode404(JsonNode statusCodeNode) {
        if (statusCodeNode == null) {
            return false;
        }
        if (statusCodeNode.isNumber()) {
            return statusCodeNode.asInt() == 404;
        }
        if (statusCodeNode.isTextual()) {
            return "404".equals(statusCodeNode.asText());
        }
        return false;
    }

    private static boolean isNotFoundError(JsonNode errorNode) {
        return errorNode != null && errorNode.isTextual() && "not_found".equals(errorNode.asText());
    }
}
