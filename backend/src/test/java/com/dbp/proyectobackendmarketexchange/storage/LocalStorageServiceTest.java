package com.dbp.proyectobackendmarketexchange.storage;

import com.dbp.proyectobackendmarketexchange.exception.InvalidStorageFileException;
import com.dbp.proyectobackendmarketexchange.exception.ResourceNotFoundException;
import com.dbp.proyectobackendmarketexchange.storage.config.StorageProperties;
import com.dbp.proyectobackendmarketexchange.storage.domain.StorageObject;
import com.dbp.proyectobackendmarketexchange.storage.domain.StorageProvider;
import com.dbp.proyectobackendmarketexchange.storage.infrastructure.LocalStorageService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.unit.DataSize;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class LocalStorageServiceTest {

    // Cabecera JPEG mínima (SOI + APP0) — suficiente para que Tika detecte image/jpeg
    // por contenido real, no por extensión/Content-Type declarado por el cliente.
    private static final byte[] JPEG_BYTES = {
            (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0,
            0x00, 0x10, 'J', 'F', 'I', 'F', 0x00, 0x01, 0x01, 0x00, 0x00, 0x01, 0x00, 0x01, 0x00, 0x00
    };

    @TempDir
    Path tempDir;

    private LocalStorageService storageService;

    @BeforeEach
    void setUp() {
        StorageProperties storageProperties = new StorageProperties();
        storageProperties.getLocal().setBaseDirectory(tempDir.toString());
        storageService = new LocalStorageService(storageProperties, DataSize.ofMegabytes(10));
    }

    @Test
    void testStore_ValidJpeg_Success() {
        MockMultipartFile file = new MockMultipartFile("image", "foto.jpg", "image/jpeg", JPEG_BYTES);

        StorageObject result = storageService.store(file, "items");

        assertNotNull(result);
        assertTrue(result.storageKey().startsWith("items/"));
        assertTrue(result.storageKey().endsWith(".jpg"));
        assertEquals(StorageProvider.LOCAL, result.provider());
        assertEquals("image/jpeg", result.contentType());
        assertTrue(storageService.exists(result.storageKey()));
    }

    @Test
    void testStore_EmptyFile_Throws() {
        MockMultipartFile file = new MockMultipartFile("image", "vacio.jpg", "image/jpeg", new byte[0]);

        assertThrows(InvalidStorageFileException.class, () -> storageService.store(file, "items"));
    }

    @Test
    void testStore_InvalidMimeType_Throws() {
        MockMultipartFile file = new MockMultipartFile("image", "no-es-imagen.jpg", "image/jpeg",
                "esto no es una imagen, es texto plano".getBytes());

        // El nombre y el Content-Type dicen "imagen", pero el contenido real es texto -
        // la detección es por contenido (Tika), no confía en ninguno de los dos.
        assertThrows(InvalidStorageFileException.class, () -> storageService.store(file, "items"));
    }

    @Test
    void testStore_UnsafeOriginalFilename_NeverUsedForKey() {
        MockMultipartFile file = new MockMultipartFile("image", "../../../etc/passwd.jpg", "image/jpeg", JPEG_BYTES);

        StorageObject result = storageService.store(file, "items");

        assertFalse(result.storageKey().contains(".."));
        assertTrue(result.storageKey().startsWith("items/"));
    }

    @Test
    void testStore_GeneratesUniqueKeysForSameFile() {
        MockMultipartFile file = new MockMultipartFile("image", "foto.jpg", "image/jpeg", JPEG_BYTES);

        StorageObject first = storageService.store(file, "items");
        StorageObject second = storageService.store(file, "items");

        assertNotEquals(first.storageKey(), second.storageKey());
    }

    @Test
    void testRetrieve_NonExistentKey_ThrowsResourceNotFoundException() {
        assertThrows(ResourceNotFoundException.class, () -> storageService.retrieve("items/no-existe.jpg"));
    }

    @Test
    void testExists_FalseThenTrueAfterStore() {
        assertFalse(storageService.exists("items/no-existe.jpg"));

        MockMultipartFile file = new MockMultipartFile("image", "foto.jpg", "image/jpeg", JPEG_BYTES);
        StorageObject result = storageService.store(file, "items");

        assertTrue(storageService.exists(result.storageKey()));
    }

    @Test
    void testDelete_RemovesFile() {
        MockMultipartFile file = new MockMultipartFile("image", "foto.jpg", "image/jpeg", JPEG_BYTES);
        StorageObject result = storageService.store(file, "items");

        storageService.delete(result.storageKey());

        assertFalse(storageService.exists(result.storageKey()));
    }

    @Test
    void testDelete_NonExistentKey_IsNoOp() {
        assertDoesNotThrow(() -> storageService.delete("items/no-existe.jpg"));
    }

    @Test
    void testRetrieve_PathTraversalAttempt_Rejected() {
        assertThrows(InvalidStorageFileException.class, () -> storageService.retrieve("../../../../etc/passwd"));
    }
}
