import React, { useEffect, useState } from "react";
import { motion } from "framer-motion";
import { item } from "../services/item/item";
import { ItemResponse } from "../interfaces/item/ItemResponse";
import { fetchItemImage } from "../services/image/image";
import { Card } from "./ui/Card";
import { Input } from "./ui/Input";
import { staggerChildren, slideUp } from "../lib/motion";

export default function UserItems() {
    const [items, setItems] = useState<ItemResponse[]>([]);
    const [filteredItems, setFilteredItems] = useState<ItemResponse[]>([]);
    const [searchTerm, setSearchTerm] = useState<string>("");
    const [errorMessage, setErrorMessage] = useState<string | null>(null);
    const [imageUrls, setImageUrls] = useState<{ [key: number]: string }>({});

    useEffect(() => {
        async function fetchUserItems() {
            try {
                const userItems = await item.getMyItems();
                setItems(userItems);
                setFilteredItems(userItems);
            } catch {
                setErrorMessage("Error al obtener tus items.");
            }
        }

        fetchUserItems();
    }, []);

    useEffect(() => {
        const loadImages = async () => {
            const accessToken = localStorage.getItem("accessToken");
            if (!accessToken) return;

            const imagePromises = items.map(async (item) => {
                try {
                    const imageUrl = await fetchItemImage(item, accessToken);
                    return { id: item.id, url: imageUrl };
                } catch {
                    return { id: item.id, url: "/default-placeholder.png" };
                }
            });

            const images = await Promise.all(imagePromises);
            setImageUrls(images.reduce((acc, img) => ({ ...acc, [img.id]: img.url }), {}));
        };

        if (items.length > 0) {
            loadImages();
        }
    }, [items]);

    function handleSearchChange(event: React.ChangeEvent<HTMLInputElement>) {
        const term = event.target.value.toLowerCase();
        setSearchTerm(term);
        setFilteredItems(items.filter((item) => item.name.toLowerCase().includes(term)));
    }

    return (
        <div>
            <h2 className="text-2xl font-bold text-gray-900 mb-4">Mis items publicados</h2>

            <div className="mb-6">
                <label htmlFor="search" className="block text-sm font-medium text-gray-700 mb-2">
                    Buscar por nombre:
                </label>
                <Input
                    id="search"
                    type="text"
                    value={searchTerm}
                    onChange={handleSearchChange}
                    placeholder="Escribe aqui para buscar items..."
                />
            </div>

            {errorMessage && (
                <div className="text-danger text-center mb-4">{errorMessage}</div>
            )}

            {filteredItems.length > 0 ? (
                <motion.ul
                    className="space-y-4"
                    variants={staggerChildren}
                    initial="hidden"
                    animate="visible"
                >
                    {filteredItems.map((item) => (
                        <motion.li key={item.id} variants={slideUp}>
                            <Card className="p-4 flex gap-4">
                                <div className="w-24 h-24 bg-gray-50 rounded-card flex-shrink-0 flex items-center justify-center overflow-hidden border border-gray-100">
                                    <img
                                        src={imageUrls[item.id] || "/default-placeholder.png"}
                                        alt={item.name}
                                        className="w-full h-full object-contain p-1"
                                    />
                                </div>
                                <div>
                                    <h3 className="text-lg font-bold text-gray-900">{item.name}</h3>
                                    <p className="text-gray-600">{item.description}</p>
                                    <p className="text-sm text-gray-500">
                                        <strong>Categoria:</strong> {item.categoryName}
                                    </p>
                                    <p className="text-sm text-gray-500">
                                        <strong>Condicion:</strong> {item.condition === "NEW" ? "Nuevo" : "Usado"}
                                    </p>
                                    <p className="text-sm text-gray-500">
                                        <strong>Estado:</strong> {item.status}
                                    </p>
                                </div>
                            </Card>
                        </motion.li>
                    ))}
                </motion.ul>
            ) : (
                <p className="text-gray-500">
                    {searchTerm
                        ? "No se encontraron items que coincidan con la busqueda."
                        : "No tienes items publicados."}
                </p>
            )}
        </div>
    );
}
