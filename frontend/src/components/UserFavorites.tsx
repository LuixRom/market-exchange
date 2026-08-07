import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { FaHeart } from "react-icons/fa";
import { item } from "../services/item/item";
import { ItemResponse } from "../interfaces/item/ItemResponse";
import { fetchItemImage } from "../services/image/image";
import { Card } from "./ui/Card";
import { Spinner } from "./ui/Spinner";

export default function UserFavorites() {
    const navigate = useNavigate();
    const [items, setItems] = useState<ItemResponse[]>([]);
    const [imageUrls, setImageUrls] = useState<{ [key: number]: string }>({});
    const [loading, setLoading] = useState(true);
    const [errorMessage, setErrorMessage] = useState<string | null>(null);

    useEffect(() => {
        async function loadFavorites() {
            try {
                setLoading(true);
                const favorites = await item.getFavorites();
                setItems(favorites);
            } catch {
                setErrorMessage("No se pudieron cargar tus favoritos.");
            } finally {
                setLoading(false);
            }
        }

        loadFavorites();
    }, []);

    useEffect(() => {
        async function loadImages() {
            const accessToken = sessionStorage.getItem("accessToken");
            if (!accessToken || items.length === 0) return;

            const results = await Promise.all(
                items.map(async (entry) => {
                    try {
                        return { id: entry.id, url: await fetchItemImage(entry, accessToken) };
                    } catch {
                        return { id: entry.id, url: "/default-placeholder.png" };
                    }
                })
            );
            setImageUrls(results.reduce((acc, entry) => ({ ...acc, [entry.id]: entry.url }), {}));
        }

        loadImages();
    }, [items]);

    if (loading) {
        return <Spinner label="Cargando favoritos..." />;
    }

    return (
        <div>
            <div className="mb-4 flex items-center gap-3">
                <div className="flex h-10 w-10 items-center justify-center rounded-2xl bg-primary/10 text-primary">
                    <FaHeart size={17} />
                </div>
                <div>
                    <h2 className="text-2xl font-bold text-gray-900">Favoritos</h2>
                    <p className="text-sm text-gray-500">Items guardados para revisar despues.</p>
                </div>
            </div>

            {errorMessage && <div className="mb-4 text-center text-danger">{errorMessage}</div>}

            {items.length === 0 ? (
                <p className="text-gray-500">Todavia no tienes favoritos.</p>
            ) : (
                <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
                    {items.map((entry) => (
                        <Card
                            key={entry.id}
                            onClick={() => navigate(`/dashboard/item/${entry.id}`)}
                            className="flex cursor-pointer gap-4 p-4 transition-shadow hover:shadow-lg"
                        >
                            <div className="flex h-24 w-24 flex-shrink-0 items-center justify-center overflow-hidden rounded-card border border-gray-100 bg-gray-50">
                                <img
                                    src={imageUrls[entry.id] || "/default-placeholder.png"}
                                    alt={entry.name}
                                    className="h-full w-full object-contain p-1"
                                />
                            </div>
                            <div className="min-w-0">
                                <h3 className="truncate text-lg font-bold text-gray-900">{entry.name}</h3>
                                <p className="line-clamp-2 text-sm text-gray-600">{entry.description}</p>
                                <p className="mt-2 text-sm text-gray-500">
                                    <strong>Categoria:</strong> {entry.categoryName}
                                </p>
                                <p className="text-sm text-gray-500">
                                    <strong>Estado:</strong> {entry.status}
                                </p>
                            </div>
                        </Card>
                    ))}
                </div>
            )}
        </div>
    );
}
