package com.dbp.proyectobackendmarketexchange.storage.infrastructure;

import com.dbp.proyectobackendmarketexchange.exception.StorageException;
import com.dbp.proyectobackendmarketexchange.storage.domain.StorageObject;
import com.dbp.proyectobackendmarketexchange.storage.domain.StorageProvider;
import com.dbp.proyectobackendmarketexchange.storage.domain.StorageService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Preparación arquitectónica para Supabase Storage. NO implementa ninguna llamada real
 * todavía (fuera de alcance de esta fase) — cada método lanza una excepción clara en vez
 * de fallar silenciosamente o con un NullPointerException. Se registra siempre como bean
 * (sin @ConditionalOnProperty) para que, si algún Item ya tuviera imageProvider=SUPABASE
 * en el futuro, StorageServiceRegistry pueda resolverlo igual y falle con un mensaje
 * explícito en vez de un bean inexistente.
 *
 * Cuando se implemente de verdad: Supabase Storage es una API REST simple (PUT/GET/DELETE
 * a un bucket con un bearer token) — alcanza con el RestClient que ya trae
 * spring-boot-starter-web, sin necesidad de un SDK externo.
 */
@Service
public class SupabaseStorageService implements StorageService {

    @Value("${SUPABASE_URL:}")
    private String supabaseUrl;

    @Value("${SUPABASE_SERVICE_ROLE_KEY:}")
    private String supabaseServiceRoleKey;

    @Value("${SUPABASE_STORAGE_BUCKET:}")
    private String supabaseStorageBucket;

    @Override
    public StorageProvider getProviderName() {
        return StorageProvider.SUPABASE;
    }

    @Override
    public StorageObject store(MultipartFile file, String directory) {
        throw notImplemented();
    }

    @Override
    public byte[] retrieve(String storageKey) {
        throw notImplemented();
    }

    @Override
    public boolean exists(String storageKey) {
        throw notImplemented();
    }

    @Override
    public void delete(String storageKey) {
        throw notImplemented();
    }

    @Override
    public String getPublicUrl(String storageKey) {
        throw notImplemented();
    }

    private StorageException notImplemented() {
        return new StorageException("Supabase Storage no está implementado todavía en esta fase");
    }
}
