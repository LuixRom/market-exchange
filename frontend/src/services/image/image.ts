import axios from "axios";

/**
 * Realiza una solicitud para obtener una imagen protegida desde el backend.
 * @param imageUrl - La URL del endpoint de la imagen en el backend.
 * @param token - El token JWT para autenticación.
 * @returns - Una URL temporal (blob URL) que se puede usar para mostrar la imagen.
 */
export async function fetchImage(imageUrl: string, token: string): Promise<string> {
  try {
    const response = await axios.get(imageUrl, {
      headers: {
        Authorization: `Bearer ${token}`, // Autenticación con el token JWT
      },
      responseType: "blob", // Especifica que la respuesta es un blob (archivo binario)
    });

    // Generar una URL temporal para el blob
    return URL.createObjectURL(response.data);
  } catch (error) {
    console.error("Error al cargar la imagen:", error);
    throw error; // Lanza el error para que el componente lo maneje
  }
}