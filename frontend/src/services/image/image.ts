import axios from "axios";
import { getApiBaseUrl } from "../../apis/api";
import { ItemResponse } from "../../interfaces/item/ItemResponse";

export async function fetchImage(imageUrl: string, token: string): Promise<string> {
  const response = await axios.get(imageUrl, {
    headers: {
      Authorization: `Bearer ${token}`,
    },
    responseType: "blob",
  });

  return URL.createObjectURL(response.data);
}

export async function fetchItemImage(item: ItemResponse, token: string): Promise<string> {
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
      return await fetchImage(imageUrl, token);
    } catch (error) {
      lastError = error;
    }
  }

  throw lastError;
}
