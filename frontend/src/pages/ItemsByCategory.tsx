import React, { useEffect, useState } from "react";
import { motion } from "framer-motion";
import { useNavigate, useParams } from "react-router-dom";
import { ItemResponse } from "../interfaces/item/ItemResponse";
import { item } from "../services/item/item"; // Servicio de ítems
import { fetchImage } from "../services/image/image"; // Nueva función
import { useAuth } from "../context/AuthProvider";
import { getApiBaseUrl } from "../apis/api";
import { Card } from "../components/ui/Card";
import { Button } from "../components/ui/Button";
import { Spinner } from "../components/ui/Spinner";
import { staggerChildren, slideUp } from "../lib/motion";

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

  if (loading) return <Spinner label="Cargando ítems..." />;

  if (!items.length) return <p className="text-center text-gray-500 py-10">No hay ítems en esta categoría.</p>;

  const handleTrade = (itemId: number) => {
    // Redirige a la página de acuerdos pasando el ID del ítem como parámetro
    navigate(`/dashboard/agreements/${itemId}`);
};

  return (
    <motion.div
      className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4 p-4"
      variants={staggerChildren}
      initial="hidden"
      animate="visible"
    >
      {items.map((item) => (
        <motion.div key={item.id} variants={slideUp}>
          <Card className="overflow-hidden h-full flex flex-col">
            <img
              src={imageUrls[item.id] || "/default-placeholder.png"}
              alt={item.name}
              className="w-full h-40 object-cover"
            />
            <div className="p-4 flex flex-col flex-1">
              <h3 className="text-lg font-bold text-gray-900">{item.name}</h3>
              <p className="text-gray-600 mt-1 flex-1">{item.description}</p>
              <p className="text-sm text-gray-500 mt-2">
                <strong>Categoría:</strong> {item.categoryName}
              </p>
              <p className="text-sm text-gray-500">
                <strong>Condición:</strong> {item.condition}
              </p>
              <p className="text-sm text-gray-500">
                <strong>Publicado por:</strong> {item.userName}
              </p>
              {role === "USER" && (
                <Button onClick={() => handleTrade(item.id)} className="w-full mt-4">
                  Tradear
                </Button>
              )}
            </div>
          </Card>
        </motion.div>
      ))}
    </motion.div>
  );
}
