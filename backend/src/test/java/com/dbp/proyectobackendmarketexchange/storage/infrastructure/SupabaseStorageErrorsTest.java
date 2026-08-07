package com.dbp.proyectobackendmarketexchange.storage.infrastructure;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Fixtures construidas con el mismo constructor que usa internamente RestClient
 * (verificado en el stacktrace real del smoke test del bloque 8), sin red ni mocks de HTTP.
 */
class SupabaseStorageErrorsTest {

    @Test
    void testIsObjectNotFound_Http404Direct_True() {
        HttpClientErrorException ex = errorWith(HttpStatus.NOT_FOUND, null);
        assertTrue(SupabaseStorageErrors.isObjectNotFound(ex));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{\"statusCode\":\"404\",\"error\":\"not_found\",\"message\":\"Object not found\"}",
            "{\"statusCode\":404,\"error\":\"not_found\",\"message\":\"Object not found\"}",
            "{\"statusCode\":\"404\",\"error\":\"not_found\",\"message\":\"algo completamente distinto\"}"
    })
    void testIsObjectNotFound_Http400WithStructuredNotFoundBody_True(String body) {
        HttpClientErrorException ex = errorWith(HttpStatus.BAD_REQUEST, body);
        assertTrue(SupabaseStorageErrors.isObjectNotFound(ex));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{\"statusCode\":",
            "",
            "{\"statusCode\":\"400\",\"error\":\"not_found\"}",
            "{\"statusCode\":\"404\",\"error\":\"invalid_request\"}",
            "algo salió mal"
    })
    void testIsObjectNotFound_Http400WithoutValidNotFoundBody_False(String body) {
        HttpClientErrorException ex = errorWith(HttpStatus.BAD_REQUEST, body);
        assertFalse(SupabaseStorageErrors.isObjectNotFound(ex));
    }

    @Test
    void testIsObjectNotFound_OtherStatusNeverClassifiedByBody_False() {
        // Aunque el cuerpo "parezca" indicar not_found, un status distinto de 400/404 nunca
        // se reinterpreta a partir del cuerpo -evita convertir un 401/403/409 real en un
        // falso "objeto inexistente"-.
        HttpClientErrorException forbidden = errorWith(HttpStatus.FORBIDDEN,
                "{\"statusCode\":\"404\",\"error\":\"not_found\"}");
        assertFalse(SupabaseStorageErrors.isObjectNotFound(forbidden));

        HttpClientErrorException conflict = errorWith(HttpStatus.CONFLICT,
                "{\"statusCode\":\"404\",\"error\":\"not_found\"}");
        assertFalse(SupabaseStorageErrors.isObjectNotFound(conflict));
    }

    private static HttpClientErrorException errorWith(HttpStatus status, String body) {
        byte[] bodyBytes = body == null ? new byte[0] : body.getBytes(StandardCharsets.UTF_8);
        return new HttpClientErrorException(status, status.getReasonPhrase(), bodyBytes, StandardCharsets.UTF_8);
    }
}
