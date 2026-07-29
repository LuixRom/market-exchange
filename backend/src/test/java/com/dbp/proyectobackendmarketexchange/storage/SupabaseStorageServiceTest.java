package com.dbp.proyectobackendmarketexchange.storage;

import com.dbp.proyectobackendmarketexchange.exception.InvalidStorageFileException;
import com.dbp.proyectobackendmarketexchange.exception.ResourceNotFoundException;
import com.dbp.proyectobackendmarketexchange.exception.StorageException;
import com.dbp.proyectobackendmarketexchange.storage.config.StorageProperties;
import com.dbp.proyectobackendmarketexchange.storage.domain.StorageObject;
import com.dbp.proyectobackendmarketexchange.storage.domain.StorageProvider;
import com.dbp.proyectobackendmarketexchange.storage.infrastructure.SupabaseStorageService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.io.IOException;

import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withNoContent;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * Ninguna request de esta clase sale a la red real ni abre ningún socket: se interceptan
 * con MockRestServiceServer, salvo el caso de ResourceAccessException, que usa un
 * ClientHttpRequestFactory de prueba que lanza IOException de forma determinista.
 * "test-secret" es un valor de prueba,
 * no una credencial real.
 */
class SupabaseStorageServiceTest {

    private static final byte[] JPEG_BYTES = {
            (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0,
            0x00, 0x10, 'J', 'F', 'I', 'F', 0x00, 0x01, 0x01, 0x00, 0x00, 0x01, 0x00, 0x01, 0x00, 0x00
    };

    private static final String BASE_URL = "https://example.supabase.co";

    private StorageProperties storageProperties;
    private RestClient.Builder restClientBuilder;
    private MockRestServiceServer mockServer;
    private SupabaseStorageService storageService;

    @BeforeEach
    void setUp() {
        storageProperties = validSupabaseProperties();

        restClientBuilder = RestClient.builder().baseUrl(BASE_URL);
        mockServer = MockRestServiceServer.bindTo(restClientBuilder).build();
        RestClient restClient = restClientBuilder.build();

        storageService = new SupabaseStorageService(storageProperties, restClient);
    }

    private static StorageProperties validSupabaseProperties() {
        StorageProperties properties = new StorageProperties();
        properties.setProvider(StorageProvider.SUPABASE);
        properties.getSupabase().setUrl(BASE_URL);
        properties.getSupabase().setSecretKey("test-secret");
        properties.getSupabase().setBucket("market-exchange-items");
        return properties;
    }

    @Test
    void testStore_ValidJpeg_UploadsBinaryBodyWithCorrectPathContentTypeAndHeaders() {
        mockServer.expect(requestTo(startsWith(BASE_URL + "/storage/v1/object/market-exchange-items/items/")))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("x-upsert", "false"))
                .andExpect(content().contentType(MediaType.IMAGE_JPEG))
                .andExpect(content().bytes(JPEG_BYTES))
                .andRespond(withSuccess());

        MockMultipartFile file = new MockMultipartFile("image", "foto.jpg", "image/jpeg", JPEG_BYTES);
        StorageObject result = storageService.store(file, "items");

        assertTrue(result.storageKey().startsWith("items/"));
        assertTrue(result.storageKey().endsWith(".jpg"));

        mockServer.verify();
    }

    @Test
    void testStore_ReturnsStorageObjectWithExpectedFields() {
        mockServer.expect(requestTo(startsWith(BASE_URL + "/storage/v1/object/")))
                .andRespond(withSuccess());

        MockMultipartFile file = new MockMultipartFile("image", "foto.jpg", "image/jpeg", JPEG_BYTES);
        StorageObject result = storageService.store(file, "items");

        assertEquals(StorageProvider.SUPABASE, result.provider());
        assertEquals("image/jpeg", result.contentType());
        assertEquals(JPEG_BYTES.length, result.sizeBytes());
        assertEquals("foto.jpg", result.originalFilename());
    }

    @Test
    void testStore_EmptyFile_ThrowsWithoutCallingSupabase() {
        MockMultipartFile file = new MockMultipartFile("image", "vacio.jpg", "image/jpeg", new byte[0]);

        assertThrows(InvalidStorageFileException.class, () -> storageService.store(file, "items"));
    }

    @Test
    void testStore_InvalidMimeType_ThrowsWithoutCallingSupabase() {
        MockMultipartFile file = new MockMultipartFile("image", "no-es-imagen.jpg", "image/jpeg",
                "esto no es una imagen, es texto plano".getBytes());

        assertThrows(InvalidStorageFileException.class, () -> storageService.store(file, "items"));
    }

    @Test
    void testStore_UnsafeOriginalFilename_NeverUsedForObjectKey() {
        mockServer.expect(requestTo(startsWith(BASE_URL + "/storage/v1/object/")))
                .andRespond(withSuccess());

        MockMultipartFile file = new MockMultipartFile("image", "../../../etc/passwd.jpg", "image/jpeg", JPEG_BYTES);
        StorageObject result = storageService.store(file, "items");

        assertFalseContains(result.storageKey(), "..");
        assertTrue(result.storageKey().startsWith("items/"));
    }

    @Test
    void testStore_GeneratesDifferentObjectKeysOnEachUpload() {
        mockServer.expect(requestTo(startsWith(BASE_URL + "/storage/v1/object/"))).andRespond(withSuccess());
        mockServer.expect(requestTo(startsWith(BASE_URL + "/storage/v1/object/"))).andRespond(withSuccess());

        MockMultipartFile file = new MockMultipartFile("image", "foto.jpg", "image/jpeg", JPEG_BYTES);
        StorageObject first = storageService.store(file, "items");
        StorageObject second = storageService.store(file, "items");

        assertNotEquals(first.storageKey(), second.storageKey());
    }

    @Test
    void testStore_MissingBucket_ThrowsConfigurationErrorWithoutCallingSupabase() {
        StorageProperties properties = validSupabaseProperties();
        properties.getSupabase().setBucket("");
        SupabaseStorageService service = new SupabaseStorageService(properties, restClientBuilder.build());

        MockMultipartFile file = new MockMultipartFile("image", "foto.jpg", "image/jpeg", JPEG_BYTES);

        assertThrows(StorageException.class, () -> service.store(file, "items"));
    }

    @Test
    void testStore_DangerousDirectory_ParentTraversal_Throws() {
        MockMultipartFile file = new MockMultipartFile("image", "foto.jpg", "image/jpeg", JPEG_BYTES);

        assertThrows(InvalidStorageFileException.class, () -> storageService.store(file, "../items"));
    }

    @Test
    void testStore_DangerousDirectory_LeadingSlash_Throws() {
        MockMultipartFile file = new MockMultipartFile("image", "foto.jpg", "image/jpeg", JPEG_BYTES);

        assertThrows(InvalidStorageFileException.class, () -> storageService.store(file, "/items"));
    }

    @Test
    void testStore_DangerousDirectory_TrailingBackslash_Throws() {
        MockMultipartFile file = new MockMultipartFile("image", "foto.jpg", "image/jpeg", JPEG_BYTES);

        assertThrows(InvalidStorageFileException.class, () -> storageService.store(file, "items\\"));
    }

    @Test
    void testStore_DangerousDirectory_DoubleSlash_Throws() {
        MockMultipartFile file = new MockMultipartFile("image", "foto.jpg", "image/jpeg", JPEG_BYTES);

        assertThrows(InvalidStorageFileException.class, () -> storageService.store(file, "items//images"));
    }

    @Test
    void testStore_ValidNestedDirectory_Succeeds() {
        mockServer.expect(requestTo(startsWith(BASE_URL + "/storage/v1/object/market-exchange-items/users/123/items/")))
                .andRespond(withSuccess());

        MockMultipartFile file = new MockMultipartFile("image", "foto.jpg", "image/jpeg", JPEG_BYTES);
        StorageObject result = storageService.store(file, "users/123/items");

        assertTrue(result.storageKey().startsWith("users/123/items/"));
    }

    @Test
    void testStore_SupabaseReturns400_TranslatesToStorageExceptionWithClientErrorCause() {
        mockServer.expect(requestTo(startsWith(BASE_URL + "/storage/v1/object/")))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST).body("{\"error\":\"Duplicate\"}"));

        MockMultipartFile file = new MockMultipartFile("image", "foto.jpg", "image/jpeg", JPEG_BYTES);

        StorageException ex = assertThrows(StorageException.class, () -> storageService.store(file, "items"));
        assertTrue(ex.getCause() instanceof HttpClientErrorException);
        assertEquals("Supabase Storage rechazó la carga del archivo", ex.getMessage());
    }

    @Test
    void testStore_SupabaseReturns500_TranslatesToStorageExceptionWithServerErrorCause() {
        mockServer.expect(requestTo(startsWith(BASE_URL + "/storage/v1/object/")))
                .andRespond(withServerError());

        MockMultipartFile file = new MockMultipartFile("image", "foto.jpg", "image/jpeg", JPEG_BYTES);

        StorageException ex = assertThrows(StorageException.class, () -> storageService.store(file, "items"));
        assertTrue(ex.getCause() instanceof HttpServerErrorException);
        assertEquals("Supabase Storage no se encuentra disponible", ex.getMessage());
    }

    @Test
    void testStore_ConnectionFailure_TranslatesToStorageExceptionWithResourceAccessExceptionCause() {
        // ClientHttpRequestFactory de prueba que lanza IOException de forma determinista al
        // crear la request -sin abrir ningún socket ni depender del estado de la red local-.
        // RestClient envuelve ese IOException en ResourceAccessException, igual que lo haría
        // ante un fallo de conexión real.
        ClientHttpRequestFactory throwingRequestFactory = (uri, httpMethod) -> {
            throw new IOException("Fallo de E/S simulado, sin red real");
        };
        RestClient client = RestClient.builder()
                .baseUrl(BASE_URL)
                .requestFactory(throwingRequestFactory)
                .defaultHeader("Authorization", "Bearer test-secret")
                .build();
        SupabaseStorageService service = new SupabaseStorageService(validSupabaseProperties(), client);

        MockMultipartFile file = new MockMultipartFile("image", "foto.jpg", "image/jpeg", JPEG_BYTES);

        StorageException ex = assertThrows(StorageException.class, () -> service.store(file, "items"));
        assertTrue(ex.getCause() instanceof ResourceAccessException);
        assertEquals("No fue posible conectarse con Supabase Storage", ex.getMessage());
    }

    @Test
    void testRetrieve_ValidObjectKey_DownloadsBytes() {
        mockServer.expect(requestTo(BASE_URL + "/storage/v1/object/market-exchange-items/items/foto.jpg"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(JPEG_BYTES, MediaType.IMAGE_JPEG));

        byte[] result = storageService.retrieve("items/foto.jpg");

        assertArrayEquals(JPEG_BYTES, result);
        mockServer.verify();
    }

    @Test
    void testRetrieve_NestedObjectKey_Succeeds() {
        mockServer.expect(requestTo(BASE_URL + "/storage/v1/object/market-exchange-items/users/123/items/foto.png"))
                .andRespond(withSuccess(JPEG_BYTES, MediaType.IMAGE_PNG));

        byte[] result = storageService.retrieve("users/123/items/foto.png");

        assertArrayEquals(JPEG_BYTES, result);
    }

    @Test
    void testRetrieve_404_ThrowsResourceNotFoundExceptionWithFixedMessage() {
        mockServer.expect(requestTo(startsWith(BASE_URL + "/storage/v1/object/")))
                .andRespond(withStatus(HttpStatus.NOT_FOUND).body("{\"error\":\"Not found\",\"details\":\"secret internal detail\"}"));

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> storageService.retrieve("items/no-existe.jpg"));
        assertEquals("Archivo no encontrado", ex.getMessage());
    }

    @Test
    void testRetrieve_Http400WithSupabaseNotFoundBody_ThrowsResourceNotFoundException() {
        // Forma real que confirmó el smoke test manual (bloque 8): Supabase responde 400,
        // no 404, para un objeto ausente.
        mockServer.expect(requestTo(startsWith(BASE_URL + "/storage/v1/object/")))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"statusCode\":\"404\",\"error\":\"not_found\",\"message\":\"Object not found\"}"));

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> storageService.retrieve("items/no-existe.jpg"));
        assertEquals("Archivo no encontrado", ex.getMessage());
    }

    @Test
    void testRetrieve_Other4xx_ThrowsStorageExceptionWithRejectedMessage() {
        mockServer.expect(requestTo(startsWith(BASE_URL + "/storage/v1/object/")))
                .andRespond(withStatus(HttpStatus.FORBIDDEN));

        StorageException ex = assertThrows(StorageException.class, () -> storageService.retrieve("items/foto.jpg"));
        assertTrue(ex.getCause() instanceof HttpClientErrorException);
        assertEquals("Supabase Storage rechazó la descarga del archivo", ex.getMessage());
    }

    @Test
    void testRetrieve_Http400WithoutNotFoundBody_ThrowsStorageException() {
        mockServer.expect(requestTo(startsWith(BASE_URL + "/storage/v1/object/")))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST).body("{\"statusCode\":\"400\",\"error\":\"invalid\"}"));

        StorageException ex = assertThrows(StorageException.class, () -> storageService.retrieve("items/foto.jpg"));
        assertTrue(ex.getCause() instanceof HttpClientErrorException);
        assertEquals("Supabase Storage rechazó la descarga del archivo", ex.getMessage());
    }

    @Test
    void testRetrieve_5xx_ThrowsStorageExceptionWithUnavailableMessage() {
        mockServer.expect(requestTo(startsWith(BASE_URL + "/storage/v1/object/")))
                .andRespond(withServerError());

        StorageException ex = assertThrows(StorageException.class, () -> storageService.retrieve("items/foto.jpg"));
        assertTrue(ex.getCause() instanceof HttpServerErrorException);
        assertEquals("Supabase Storage no se encuentra disponible", ex.getMessage());
    }

    @Test
    void testRetrieve_ConnectionFailure_TranslatesToStorageExceptionWithResourceAccessExceptionCause() {
        ClientHttpRequestFactory throwingRequestFactory = (uri, httpMethod) -> {
            throw new IOException("Fallo de E/S simulado, sin red real");
        };
        RestClient client = RestClient.builder()
                .baseUrl(BASE_URL)
                .requestFactory(throwingRequestFactory)
                .defaultHeader("Authorization", "Bearer test-secret")
                .build();
        SupabaseStorageService service = new SupabaseStorageService(validSupabaseProperties(), client);

        StorageException ex = assertThrows(StorageException.class, () -> service.retrieve("items/foto.jpg"));
        assertTrue(ex.getCause() instanceof ResourceAccessException);
        assertEquals("No fue posible conectarse con Supabase Storage", ex.getMessage());
    }

    @Test
    void testRetrieve_MissingBucket_ThrowsConfigurationErrorWithoutCallingSupabase() {
        StorageProperties properties = validSupabaseProperties();
        properties.getSupabase().setBucket("");
        SupabaseStorageService service = new SupabaseStorageService(properties, restClientBuilder.build());

        assertThrows(StorageException.class, () -> service.retrieve("items/foto.jpg"));
    }

    @Test
    void testRetrieve_NullObjectKey_ThrowsWithoutCallingSupabase() {
        assertThrows(InvalidStorageFileException.class, () -> storageService.retrieve(null));
    }

    @Test
    void testRetrieve_BlankObjectKey_ThrowsWithoutCallingSupabase() {
        assertThrows(InvalidStorageFileException.class, () -> storageService.retrieve("   "));
    }

    @Test
    void testRetrieve_LeadingSlash_ThrowsWithoutCallingSupabase() {
        assertThrows(InvalidStorageFileException.class, () -> storageService.retrieve("/items/foto.jpg"));
    }

    @Test
    void testRetrieve_TrailingSlash_ThrowsWithoutCallingSupabase() {
        assertThrows(InvalidStorageFileException.class, () -> storageService.retrieve("items/foto.jpg/"));
    }

    @Test
    void testRetrieve_Backslash_ThrowsWithoutCallingSupabase() {
        assertThrows(InvalidStorageFileException.class, () -> storageService.retrieve("items\\foto.jpg"));
    }

    @Test
    void testRetrieve_EmptySegment_ThrowsWithoutCallingSupabase() {
        assertThrows(InvalidStorageFileException.class, () -> storageService.retrieve("items//foto.jpg"));
    }

    @Test
    void testRetrieve_DotSegment_ThrowsWithoutCallingSupabase() {
        assertThrows(InvalidStorageFileException.class, () -> storageService.retrieve("items/./foto.jpg"));
    }

    @Test
    void testRetrieve_DotDotSegment_ThrowsWithoutCallingSupabase() {
        assertThrows(InvalidStorageFileException.class, () -> storageService.retrieve("items/../foto.jpg"));
    }

    @Test
    void testRetrieve_DisallowedCharacter_ThrowsWithoutCallingSupabase() {
        assertThrows(InvalidStorageFileException.class, () -> storageService.retrieve("items/foto archivo.jpg"));
    }

    @Test
    void testRetrieve_EmptyBody_ThrowsStorageExceptionInsteadOfReturningNull() {
        // RestClient.body(byte[].class) no distingue "200 con cuerpo vacío" de "sin
        // contenido": en ambos casos el extractor de Spring devuelve null antes de llegar a
        // cualquier HttpMessageConverter (verificado empíricamente). No hay forma de que
        // este código reciba un byte[] vacío-pero-real desde Supabase para tratarlo como
        // LocalStorageService trataría un archivo de 0 bytes en disco -por eso null (venga
        // de un 200 vacío o de un 204) se trata siempre como una respuesta inválida, nunca
        // como "archivo vacío válido".
        mockServer.expect(requestTo(startsWith(BASE_URL + "/storage/v1/object/")))
                .andRespond(withSuccess(new byte[0], MediaType.APPLICATION_OCTET_STREAM));

        StorageException ex = assertThrows(StorageException.class, () -> storageService.retrieve("items/vacio.bin"));
        assertEquals("Supabase Storage devolvió una respuesta sin contenido", ex.getMessage());
    }

    @Test
    void testRetrieve_NoContentResponse_ThrowsStorageExceptionInsteadOfReturningNull() {
        mockServer.expect(requestTo(startsWith(BASE_URL + "/storage/v1/object/")))
                .andRespond(withNoContent());

        StorageException ex = assertThrows(StorageException.class, () -> storageService.retrieve("items/foto.jpg"));
        assertEquals("Supabase Storage devolvió una respuesta sin contenido", ex.getMessage());
    }

    @Test
    void testExists_ObjectExists_ReturnsTrue() {
        mockServer.expect(requestTo(BASE_URL + "/storage/v1/object/market-exchange-items/items/foto.jpg"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("Range", "bytes=0-0"))
                .andRespond(withSuccess());

        assertTrue(storageService.exists("items/foto.jpg"));
        mockServer.verify();
    }

    @Test
    void testExists_206PartialContent_ReturnsTrue() {
        // Respuesta real de Supabase para un objeto existente con Range: bytes=0-0.
        mockServer.expect(requestTo(startsWith(BASE_URL + "/storage/v1/object/")))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("Range", "bytes=0-0"))
                .andRespond(withStatus(HttpStatus.PARTIAL_CONTENT));

        assertTrue(storageService.exists("items/foto.jpg"));
    }

    @Test
    void testExists_204NoContent_StillReturnsTrue() {
        // Cualquier 2xx significa "existe"; nunca se intenta leer un cuerpo de respuesta.
        mockServer.expect(requestTo(startsWith(BASE_URL + "/storage/v1/object/")))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("Range", "bytes=0-0"))
                .andRespond(withNoContent());

        assertTrue(storageService.exists("items/foto.jpg"));
    }

    @Test
    void testExists_404_ReturnsFalseWithoutThrowing() {
        mockServer.expect(requestTo(startsWith(BASE_URL + "/storage/v1/object/")))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("Range", "bytes=0-0"))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertFalse(storageService.exists("items/no-existe.jpg"));
    }

    @Test
    void testExists_Http400WithSupabaseNotFoundBody_ReturnsFalse() {
        // Forma real que confirmó el smoke test manual (bloque 8): Supabase responde 400,
        // no 404, para un objeto ausente.
        mockServer.expect(requestTo(startsWith(BASE_URL + "/storage/v1/object/")))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"statusCode\":\"404\",\"error\":\"not_found\",\"message\":\"Object not found\"}"));

        assertFalse(storageService.exists("items/no-existe.jpg"));
    }

    @Test
    void testExists_Http400WithoutNotFoundBody_ThrowsStorageException() {
        mockServer.expect(requestTo(startsWith(BASE_URL + "/storage/v1/object/")))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST).body("{\"statusCode\":\"400\",\"error\":\"invalid\"}"));

        StorageException ex = assertThrows(StorageException.class, () -> storageService.exists("items/foto.jpg"));
        assertTrue(ex.getCause() instanceof HttpClientErrorException);
        assertEquals("Supabase Storage rechazó la verificación del archivo", ex.getMessage());
    }

    @Test
    void testExists_Other4xx_ThrowsStorageExceptionWithRejectedMessage() {
        mockServer.expect(requestTo(startsWith(BASE_URL + "/storage/v1/object/")))
                .andRespond(withStatus(HttpStatus.FORBIDDEN));

        StorageException ex = assertThrows(StorageException.class, () -> storageService.exists("items/foto.jpg"));
        assertTrue(ex.getCause() instanceof HttpClientErrorException);
        assertEquals("Supabase Storage rechazó la verificación del archivo", ex.getMessage());
    }

    @Test
    void testExists_5xx_ThrowsStorageExceptionWithUnavailableMessage() {
        mockServer.expect(requestTo(startsWith(BASE_URL + "/storage/v1/object/")))
                .andRespond(withServerError());

        StorageException ex = assertThrows(StorageException.class, () -> storageService.exists("items/foto.jpg"));
        assertTrue(ex.getCause() instanceof HttpServerErrorException);
        assertEquals("Supabase Storage no se encuentra disponible", ex.getMessage());
    }

    @Test
    void testExists_ConnectionFailure_TranslatesToStorageExceptionWithResourceAccessExceptionCause() {
        ClientHttpRequestFactory throwingRequestFactory = (uri, httpMethod) -> {
            throw new IOException("Fallo de E/S simulado, sin red real");
        };
        RestClient client = RestClient.builder()
                .baseUrl(BASE_URL)
                .requestFactory(throwingRequestFactory)
                .defaultHeader("Authorization", "Bearer test-secret")
                .build();
        SupabaseStorageService service = new SupabaseStorageService(validSupabaseProperties(), client);

        StorageException ex = assertThrows(StorageException.class, () -> service.exists("items/foto.jpg"));
        assertTrue(ex.getCause() instanceof ResourceAccessException);
        assertEquals("No fue posible conectarse con Supabase Storage", ex.getMessage());
    }

    @Test
    void testExists_InvalidObjectKey_ThrowsWithoutCallingSupabase() {
        assertThrows(InvalidStorageFileException.class, () -> storageService.exists(null));
    }

    @Test
    void testExists_LeadingSlashObjectKey_ThrowsWithoutCallingSupabase() {
        assertThrows(InvalidStorageFileException.class, () -> storageService.exists("/items/foto.jpg"));
    }

    @Test
    void testExists_MissingBucket_ThrowsConfigurationErrorWithoutCallingSupabase() {
        StorageProperties properties = validSupabaseProperties();
        properties.getSupabase().setBucket("");
        SupabaseStorageService service = new SupabaseStorageService(properties, restClientBuilder.build());

        assertThrows(StorageException.class, () -> service.exists("items/foto.jpg"));
    }

    @Test
    void testDelete_ObjectExists_CompletesNormallyWithSingleRequest() {
        mockServer.expect(requestTo(BASE_URL + "/storage/v1/object/market-exchange-items/items/foto.jpg"))
                .andExpect(method(HttpMethod.DELETE))
                .andRespond(withSuccess());

        assertDoesNotThrow(() -> storageService.delete("items/foto.jpg"));

        // Una sola expectativa declarada: si delete() hiciera una request adicional (p.ej.
        // un exists() previo), MockRestServiceServer fallaría por request no esperada.
        mockServer.verify();
    }

    @Test
    void testDelete_204NoContent_CompletesNormally() {
        mockServer.expect(requestTo(startsWith(BASE_URL + "/storage/v1/object/")))
                .andExpect(method(HttpMethod.DELETE))
                .andRespond(withNoContent());

        assertDoesNotThrow(() -> storageService.delete("items/foto.jpg"));
        mockServer.verify();
    }

    @Test
    void testDelete_404_CompletesNormallyBecauseAlreadyGone() {
        // Idempotente, igual que LocalStorageService.delete(): el objetivo final (que el
        // objeto no exista) ya se cumplió, así que un 404 no es un error.
        mockServer.expect(requestTo(startsWith(BASE_URL + "/storage/v1/object/")))
                .andExpect(method(HttpMethod.DELETE))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertDoesNotThrow(() -> storageService.delete("items/no-existe.jpg"));
        mockServer.verify();
    }

    @Test
    void testDelete_Http400WithSupabaseNotFoundBody_CompletesNormally() {
        // Forma real que confirmó el smoke test manual (bloque 8): Supabase responde 400,
        // no 404, para un objeto ausente -incluso en un segundo delete()-.
        mockServer.expect(requestTo(startsWith(BASE_URL + "/storage/v1/object/")))
                .andExpect(method(HttpMethod.DELETE))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"statusCode\":\"404\",\"error\":\"not_found\",\"message\":\"Object not found\"}"));

        assertDoesNotThrow(() -> storageService.delete("items/no-existe.jpg"));
        mockServer.verify();
    }

    @Test
    void testDelete_Other4xx_ThrowsStorageExceptionWithRejectedMessage() {
        mockServer.expect(requestTo(startsWith(BASE_URL + "/storage/v1/object/")))
                .andRespond(withStatus(HttpStatus.FORBIDDEN));

        StorageException ex = assertThrows(StorageException.class, () -> storageService.delete("items/foto.jpg"));
        assertTrue(ex.getCause() instanceof HttpClientErrorException);
        assertEquals("Supabase Storage rechazó la eliminación del archivo", ex.getMessage());
    }

    @Test
    void testDelete_Http400WithoutNotFoundBody_ThrowsStorageException() {
        mockServer.expect(requestTo(startsWith(BASE_URL + "/storage/v1/object/")))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST).body("{\"statusCode\":\"400\",\"error\":\"invalid\"}"));

        StorageException ex = assertThrows(StorageException.class, () -> storageService.delete("items/foto.jpg"));
        assertTrue(ex.getCause() instanceof HttpClientErrorException);
        assertEquals("Supabase Storage rechazó la eliminación del archivo", ex.getMessage());
    }

    @Test
    void testDelete_5xx_ThrowsStorageExceptionWithUnavailableMessage() {
        mockServer.expect(requestTo(startsWith(BASE_URL + "/storage/v1/object/")))
                .andRespond(withServerError());

        StorageException ex = assertThrows(StorageException.class, () -> storageService.delete("items/foto.jpg"));
        assertTrue(ex.getCause() instanceof HttpServerErrorException);
        assertEquals("Supabase Storage no se encuentra disponible", ex.getMessage());
    }

    @Test
    void testDelete_ConnectionFailure_TranslatesToStorageExceptionWithResourceAccessExceptionCause() {
        ClientHttpRequestFactory throwingRequestFactory = (uri, httpMethod) -> {
            throw new IOException("Fallo de E/S simulado, sin red real");
        };
        RestClient client = RestClient.builder()
                .baseUrl(BASE_URL)
                .requestFactory(throwingRequestFactory)
                .defaultHeader("Authorization", "Bearer test-secret")
                .build();
        SupabaseStorageService service = new SupabaseStorageService(validSupabaseProperties(), client);

        StorageException ex = assertThrows(StorageException.class, () -> service.delete("items/foto.jpg"));
        assertTrue(ex.getCause() instanceof ResourceAccessException);
        assertEquals("No fue posible conectarse con Supabase Storage", ex.getMessage());
    }

    @Test
    void testDelete_InvalidObjectKey_ThrowsWithoutCallingSupabase() {
        assertThrows(InvalidStorageFileException.class, () -> storageService.delete(null));
    }

    @Test
    void testDelete_LeadingSlashObjectKey_ThrowsWithoutCallingSupabase() {
        assertThrows(InvalidStorageFileException.class, () -> storageService.delete("/items/foto.jpg"));
    }

    @Test
    void testDelete_MissingBucket_ThrowsConfigurationErrorWithoutCallingSupabase() {
        StorageProperties properties = validSupabaseProperties();
        properties.getSupabase().setBucket("");
        SupabaseStorageService service = new SupabaseStorageService(properties, restClientBuilder.build());

        assertThrows(StorageException.class, () -> service.delete("items/foto.jpg"));
    }

    @Test
    void testGetPublicUrl_UnsupportedForPrivateBucket() {
        StorageException ex = assertThrows(StorageException.class, () -> storageService.getPublicUrl("items/foto.jpg"));
        assertEquals("Supabase Storage no admite URLs públicas para buckets privados", ex.getMessage());
    }

    private static void assertFalseContains(String value, String forbidden) {
        assertTrue(!value.contains(forbidden), "No debería contener \"" + forbidden + "\": " + value);
    }
}
