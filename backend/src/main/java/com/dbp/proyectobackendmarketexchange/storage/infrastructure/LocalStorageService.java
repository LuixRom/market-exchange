package com.dbp.proyectobackendmarketexchange.storage.infrastructure;

import com.dbp.proyectobackendmarketexchange.exception.InvalidStorageFileException;
import com.dbp.proyectobackendmarketexchange.exception.ResourceNotFoundException;
import com.dbp.proyectobackendmarketexchange.exception.StorageException;
import com.dbp.proyectobackendmarketexchange.storage.config.StorageProperties;
import com.dbp.proyectobackendmarketexchange.storage.domain.StorageObject;
import com.dbp.proyectobackendmarketexchange.storage.domain.StorageProvider;
import com.dbp.proyectobackendmarketexchange.storage.domain.StorageService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * Implementación local del storage: encapsula exactamente lo que ItemService/ItemController
 * hacían directamente con java.nio.file.* antes de esta fase. Ni el controller ni ItemService
 * vuelven a tocar Path/Files fuera de esta clase.
 */
@Service
public class LocalStorageService implements StorageService {

    private final Path baseDirectory;
    private final DataSize maxFileSize;

    public LocalStorageService(
            StorageProperties storageProperties,
            @Value("${spring.servlet.multipart.max-file-size:1MB}") DataSize maxFileSize) {
        this.baseDirectory = Paths.get(storageProperties.getLocal().getBaseDirectory()).normalize();
        this.maxFileSize = maxFileSize;
    }

    @Override
    public StorageProvider getProviderName() {
        return StorageProvider.LOCAL;
    }

    @Override
    public StorageObject store(MultipartFile file, String directory) {
        if (file == null || file.isEmpty()) {
            throw new InvalidStorageFileException("El archivo no puede estar vacío");
        }
        if (maxFileSize != null && maxFileSize.toBytes() > 0 && file.getSize() > maxFileSize.toBytes()) {
            throw new InvalidStorageFileException("El archivo supera el tamaño máximo permitido");
        }

        byte[] content;
        try {
            content = file.getBytes();
        } catch (IOException e) {
            throw new StorageException("No se pudo leer el archivo", e);
        }

        String contentType = ImageContentTypes.detectAndValidate(content);

        String key = directory + "/" + UUID.randomUUID() + ImageContentTypes.extensionFor(contentType);
        Path targetPath = resolve(key);

        try {
            Files.createDirectories(targetPath.getParent());
            Files.write(targetPath, content);
        } catch (IOException e) {
            throw new StorageException("No se pudo guardar el archivo", e);
        }

        return new StorageObject(key, StorageProvider.LOCAL, contentType, file.getOriginalFilename(), content.length);
    }

    @Override
    public byte[] retrieve(String storageKey) {
        Path path = resolve(storageKey);
        if (!Files.exists(path)) {
            throw new ResourceNotFoundException("Archivo no encontrado");
        }
        try {
            return Files.readAllBytes(path);
        } catch (IOException e) {
            throw new StorageException("No se pudo leer el archivo", e);
        }
    }

    @Override
    public boolean exists(String storageKey) {
        return Files.exists(resolve(storageKey));
    }

    @Override
    public void delete(String storageKey) {
        try {
            Files.deleteIfExists(resolve(storageKey));
        } catch (IOException e) {
            throw new StorageException("No se pudo eliminar el archivo", e);
        }
    }

    @Override
    public String getPublicUrl(String storageKey) {
        return "/" + baseDirectory.getFileName() + "/" + storageKey;
    }

    private Path resolve(String storageKey) {
        if (storageKey == null || storageKey.isBlank()) {
            throw new InvalidStorageFileException("Referencia de almacenamiento inválida");
        }
        Path resolved = baseDirectory.resolve(storageKey).normalize();
        if (!resolved.startsWith(baseDirectory)) {
            // Defensa adicional: aunque las keys siempre se generan server-side, nunca se
            // resuelve una ruta que escape del directorio base.
            throw new InvalidStorageFileException("Referencia de almacenamiento inválida");
        }
        return resolved;
    }
}
