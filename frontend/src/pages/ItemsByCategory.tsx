import { Dispatch, SetStateAction, useEffect, useState } from "react";
import { motion } from "framer-motion";
import { useNavigate, useParams } from "react-router-dom";
import { ItemResponse } from "../interfaces/item/ItemResponse";
import { item } from "../services/item/item";
import { usuario } from "../services/user/user";
import { fetchItemImage } from "../services/image/image";
import { useAuth } from "../context/AuthProvider";
import { Card } from "../components/ui/Card";
import { Button } from "../components/ui/Button";
import { Spinner } from "../components/ui/Spinner";
import { staggerChildren, slideUp } from "../lib/motion";

async function loadItemImage(
  itemData: ItemResponse,
  accessToken: string,
  setImageUrls: Dispatch<SetStateAction<{ [key: number]: string }>>
) {
  try {
    const url = await fetchItemImage(itemData, accessToken);
    setImageUrls((prev) => ({ ...prev, [itemData.id]: url }));
  } catch {
    setImageUrls((prev) => ({ ...prev, [itemData.id]: "/default-placeholder.png" }));
  }
}

export default function CategoryItemsPage() {
  const { role } = useAuth();
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [items, setItems] = useState<ItemResponse[]>([]);
  const [currentUserId, setCurrentUserId] = useState<number | null>(null);
  const [imageUrls, setImageUrls] = useState<{ [key: number]: string }>({});
  const [loading, setLoading] = useState<boolean>(true);

  useEffect(() => {
    usuario.getMyInfo().then((info) => setCurrentUserId(info.id)).catch(() => {});
  }, []);

  useEffect(() => {
    const fetchItems = async () => {
      setLoading(true);
      try {
        const itemsData = await item.getCatalog({
          categoryId: Number(id),
          status: "APPROVED",
          page: 0,
          size: 100,
          sort: "createdAt,desc",
        });
        setItems(itemsData.content);

        const accessToken = sessionStorage.getItem("accessToken");
        if (accessToken && itemsData.content.length > 0) {
          itemsData.content.forEach((itemData) => loadItemImage(itemData, accessToken, setImageUrls));
        }
      } catch (error) {
        console.error("Error al obtener items:", error);
      } finally {
        setLoading(false);
      }
    };

    fetchItems();
  }, [id]);

  if (loading) return <Spinner label="Cargando items..." />;

  if (!items.length) return <p className="text-center text-gray-500 py-10">No hay items en esta categoria.</p>;

  return (
    <motion.div
      className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4 p-4"
      variants={staggerChildren}
      initial="hidden"
      animate="visible"
    >
      {items.map((itemData) => (
        <motion.div key={itemData.id} variants={slideUp}>
          <Card className="overflow-hidden h-full flex flex-col">
            <div className={`w-full h-48 bg-gray-50 border-b border-gray-100 flex items-center justify-center overflow-hidden ${!imageUrls[itemData.id] ? "animate-pulse" : ""}`}>
              <img
                src={imageUrls[itemData.id] || "/default-placeholder.png"}
                alt={itemData.name}
                className="w-full h-full object-contain p-3"
                loading="lazy"
              />
            </div>
            <div className="p-4 flex flex-col flex-1">
              <h3 className="text-lg font-bold text-gray-900">{itemData.name}</h3>
              <p className="text-gray-600 mt-1 flex-1">{itemData.description}</p>
              <p className="text-sm text-gray-500 mt-2">
                <strong>Categoria:</strong> {itemData.categoryName}
              </p>
              <p className="text-sm text-gray-500">
                <strong>Condicion:</strong> {itemData.condition === "NEW" ? "Nuevo" : "Usado"}
              </p>
              <p className="text-sm text-gray-500">
                <strong>Publicado por:</strong> {itemData.userName}
              </p>
              <div className="flex gap-3 mt-4">
                <Button
                  onClick={() => navigate(`/dashboard/item/${itemData.id}`)}
                  variant="secondary"
                  className="w-full text-xs"
                >
                  Ver detalle
                </Button>
                {role === "USER" && (
                  itemData.user_id === currentUserId ? (
                    <span className="w-full py-2 px-3 text-center text-xs font-bold text-gray-500 bg-gray-100 rounded-xl flex items-center justify-center border border-gray-200">
                      Tu publicación
                    </span>
                  ) : (
                    <Button
                      onClick={() => navigate(`/dashboard/agreements/item/${itemData.id}`)}
                      className="w-full text-xs"
                    >
                      Tradear
                    </Button>
                  )
                )}
              </div>
            </div>
          </Card>
        </motion.div>
      ))}
    </motion.div>
  );
}
