import axios from "axios";
import { getApiBaseUrl } from "../../apis/api";
import { ItemResponse } from "../../interfaces/item/ItemResponse";

// Caché en memoria para guardar las URLs de objetos Blob generadas
const imageBlobCache = new Map<string, string>();
// Deduplicador de peticiones en vuelo para evitar llamadas duplicadas simultáneas
const pendingImageRequests = new Map<string, Promise<string>>();

export async function fetchImage(imageUrl: string, token: string): Promise<string> {
  const cacheKey = `${token.slice(-10)}_${imageUrl}`;

  // 1. Si la imagen ya está en caché, la retornamos inmediatamente
  if (imageBlobCache.has(cacheKey)) {
    return imageBlobCache.get(cacheKey)!;
  }

  // 2. Si hay una petición en vuelo para la misma imagen, esperamos la misma promesa
  if (pendingImageRequests.has(cacheKey)) {
    return pendingImageRequests.get(cacheKey)!;
  }

  // 3. Crear nueva petición HTTP
  const requestPromise = (async () => {
    try {
      const response = await axios.get(imageUrl, {
        headers: {
          Authorization: `Bearer ${token}`,
        },
        responseType: "blob",
      });

      const objectUrl = URL.createObjectURL(response.data);
      imageBlobCache.set(cacheKey, objectUrl);
      return objectUrl;
    } finally {
      pendingImageRequests.delete(cacheKey);
    }
  })();

  pendingImageRequests.set(cacheKey, requestPromise);
  return requestPromise;
}

export async function fetchItemImage(item: ItemResponse, token: string): Promise<string> {
  const itemCacheKey = `item_${item.id}`;

  if (imageBlobCache.has(itemCacheKey)) {
    return imageBlobCache.get(itemCacheKey)!;
  }

  const imagePaths = [
    ...(item.imageUrls || []),
    item.imageUrl,
    `/item/${item.id}/image`,
  ].filter((path): path is string => Boolean(path));

  const uniqueImagePaths = Array.from(new Set(imagePaths));

  if (uniqueImagePaths.length === 0) {
    return "/default-placeholder.png";
  }

  let lastError: unknown;

  for (const imagePath of uniqueImagePaths) {
    try {
      const imageUrl = imagePath.startsWith("http") ? imagePath : `${getApiBaseUrl()}${imagePath}`;
      const url = await fetchImage(imageUrl, token);
      imageBlobCache.set(itemCacheKey, url);
      return url;
    } catch (error) {
      lastError = error;
    }
  }

  throw lastError;
}

