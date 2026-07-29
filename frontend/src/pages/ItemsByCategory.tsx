import React, { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { ItemResponse } from "../interfaces/item/ItemResponse";
import { item } from "../services/item/item"; // Servicio de ítems
import { fetchImage } from "../services/image/image"; // Nueva función
import { useAuth } from "../context/AuthProvider";
import { getApiBaseUrl } from "../apis/api";

export default function CategoryItemsPage() {
  const { role } = useAuth();
  const { id } = useParams<{ id: string }>();
  const [items, setItems] = useState<ItemResponse[]>([]);
  const [imageUrls, setImageUrls] = useState<{ [key: number]: string }>({});
  const [loading, setLoading] = useState<boolean>(true);
  const navigate = useNavigate(); // Hook para redirigir

  useEffect(() => {
    const fetchItems = async () => {
      try {
        const itemsData = await item.getItemsByCategory(Number(id));
        setItems(itemsData);

        // Cargar las imágenes asociadas a cada ítem
        const accessToken = localStorage.getItem('accessToken');
        if (!accessToken) {
          console.error("No se encontró un token de autenticación.");
          return;
        }

        const imagePromises = itemsData.map(async (item) => {
          try {
            const imageUrl = await fetchImage(`${getApiBaseUrl()}${item.imageUrl}`, accessToken);
            return { id: item.id, url: imageUrl };
          } catch {
            return { id: item.id, url: "/default-placeholder.png" };
          }
        });

        const images = await Promise.all(imagePromises);
        setImageUrls(images.reduce((acc, img) => ({ ...acc, [img.id]: img.url }), {}));
      } catch (error) {
        console.error("Error al obtener ítems:", error);
      } finally {
        setLoading(false);
      }
    };

    fetchItems();
  }, [id]);

  if (loading) return <div>Cargando ítems...</div>;

  if (!items.length) return <div>No hay ítems en esta categoría.</div>;

  const handleTrade = (itemId: number) => {
    // Redirige a la página de acuerdos pasando el ID del ítem como parámetro
    navigate(`/dashboard/agreements/${itemId}`);
};

  return (
    <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4 p-4">
      {items.map((item) => (
        <div key={item.id} className="bg-gray-200 p-4 rounded-lg shadow-md">
           <div>
                                <h3 className="text-lg font-bold text-blue-600">{item.name}</h3>
                                <img
                                    src={imageUrls[item.id] || "/default-placeholder.png"}
                                    alt={item.name}
                                    className="w-full h-auto mt-2"
                                />

                                <p className="text-gray-700">
                                    <strong></strong> {item.description}
                                    </p>
                                <p className="text-sm text-gray-500">
                                    <strong>Categoría:</strong> {item.categoryName}
                                </p>
                                <p className="text-sm text-gray-500">
                                    <strong>Condición:</strong> {item.condition}
                                </p>
                                <p className="text-sm text-gray-500">
                                    <strong>Publicado por:</strong> {item.userName}
                                </p>
                            </div>
                            {role === "USER" && (
                                <button
                                    onClick={() => handleTrade(item.id)}
                                    className="bg-blue-500 text-white px-4 py-2 rounded mt-2"
                                >
                                    Tradear
                                </button>
                            )}
        </div>
      ))}
    </div>
  );
}