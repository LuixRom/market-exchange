package com.dbp.proyectobackendmarketexchange.storage.infrastructure;

import com.dbp.proyectobackendmarketexchange.exception.StorageException;
import com.dbp.proyectobackendmarketexchange.storage.config.StorageProperties;
import com.dbp.proyectobackendmarketexchange.storage.domain.StorageProvider;
import com.dbp.proyectobackendmarketexchange.storage.domain.StorageService;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Resuelve qué StorageService usar. Existen dos necesidades distintas:
 *  - al escribir un archivo NUEVO: usar el proveedor activo (app.storage.provider).
 *  - al leer/borrar un archivo EXISTENTE: usar el proveedor con el que ESE archivo fue
 *    escrito (Item.imageProvider), no necesariamente el activo hoy -si en el futuro se
 *    cambia app.storage.provider a supabase, las imágenes viejas subidas en local deben
 *    seguir resolviendo por el adaptador local, o se rompen para siempre-.
 *
 * Por eso ambos StorageService (Local/Supabase) se registran siempre como beans
 * normales, sin @ConditionalOnProperty: si se condicionara el registro del bean local a
 * app.storage.provider=local, el día que se cambie la propiedad ese bean desaparecería
 * del contexto y ya no habría forma de servir/borrar ninguna imagen vieja.
 */
@Component
public class StorageServiceRegistry {

    private final Map<StorageProvider, StorageService> byProvider;
    private final StorageProvider defaultProvider;

    public StorageServiceRegistry(List<StorageService> storageServices, StorageProperties storageProperties) {
        this.byProvider = storageServices.stream()
                .collect(Collectors.toMap(StorageService::getProviderName, Function.identity()));
        this.defaultProvider = storageProperties.getProvider();
    }

    public StorageService getDefault() {
        return forProvider(defaultProvider);
    }

    public StorageService forProvider(StorageProvider provider) {
        StorageService service = byProvider.get(provider);
        if (service == null) {
            throw new StorageException("No hay un StorageService registrado para el proveedor " + provider);
        }
        return service;
    }
}
