import React, { useEffect, useState } from "react";
import { motion } from "framer-motion";
import { useNavigate } from "react-router-dom";
import { item } from "../services/item/item";
import { category } from "../services/category/category";
import { ItemResponse } from "../interfaces/item/ItemResponse";
import { CategoryResponse } from "../interfaces/category/CategoryResponse";
import { useAuth } from "../context/AuthProvider";
import {usuario} from "../services/user/user";
import { fetchImage } from "../services/image/image"; // Nueva función
import { Agreement } from "../services/agreement/Agreement";
import { getApiBaseUrl } from "../apis/api";
import { Card } from "./ui/Card";
import { Input } from "./ui/Input";
import { Button } from "./ui/Button";
import { Spinner } from "./ui/Spinner";
import { useToast } from "./ui/Toast";
import { staggerChildren, slideUp } from "../lib/motion";


export default function AllItems() {
    const auth = useAuth();
    const role = auth.role;
    const navigate = useNavigate(); // Hook para redirigir
    const { toast } = useToast();
    const [items, setItems] = useState<ItemResponse[]>([]);
    const [filteredItems, setFilteredItems] = useState<ItemResponse[]>([]);
    const [categories, setCategories] = useState<CategoryResponse[]>([]);
    const [searchTerm, setSearchTerm] = useState<string>("");
    const [selectedCategory, setSelectedCategory] = useState<number | null>(null);
    const [errorMessage, setErrorMessage] = useState<string | null>(null);
    const [loading, setLoading] = useState<boolean>(true);
    const [userId, setUserId] = useState<number | null>(null); // ID del usuario autenticado
    const [imageUrls, setImageUrls] = useState<{ [key: number]: string }>({});


    useEffect(() => {
        const fetchUserId = async () => {
            const userInfo = await usuario.getMyInfo(); // Obtiene la información del usuario autenticado
            setUserId(userInfo.id); // Establece el userId
        };

        fetchUserId();
    }, []);

    useEffect(() => {
        const fetchData = async () => {
            if (!userId) return;

            setLoading(true); // Activa el indicador de carga
            try {
                const allItems = await item.getAllItems();
                const filteredItems = role === "ADMIN"
                    ? allItems.filter((item) => item.status === "PENDING")
                    : allItems.filter((item) => item.status === "APPROVED" && item.user_id !== userId
                );
                const allCategories = await category.getAllCategories();
                setItems(filteredItems);
                setFilteredItems(filteredItems);
                setCategories(allCategories);

            } catch (error) {
                console.error("Error al obtener los datos:", error);
                setErrorMessage("Error al obtener los datos.");
            } finally {
                setLoading(false); // Desactiva el indicador de carga
            }
        };

        if (role) {
            fetchData();
        }
    }, [role, userId]);

    useEffect(() => {
        const loadImages = async () => {
            const accessToken = localStorage.getItem("accessToken");
            if (!accessToken) {
                console.error("No se encontró un token de autenticación.");
                return;
            }

            const imagePromises = items.map(async (item) => {
                try {
                    const imageUrl = await fetchImage(`${getApiBaseUrl()}${item.imageUrl}`, accessToken);
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
    }, [items]); // Ejecuta esto solo cuando items cambie

    const handleSearchChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        const term = event.target.value.toLowerCase();
        setSearchTerm(term);
        filterItems(term, selectedCategory);
    };

    const handleCategoryChange = (event: React.ChangeEvent<HTMLSelectElement>) => {
        const categoryId = event.target.value === "" ? null : Number(event.target.value);
        setSelectedCategory(categoryId);
        filterItems(searchTerm, categoryId);
    };

    const filterItems = (term: string, categoryId: number | null) => {
        let filtered = items;

        if (term) {
            filtered = filtered.filter((item) =>
                item.name.toLowerCase().includes(term)
            );
        }

        if (categoryId !== null) {
            filtered = filtered.filter((item) => item.categoryName === categories.find(cat => cat.id === categoryId)?.name);
        }

        setFilteredItems(filtered);
    };

    const handleApprove = async (itemId: number) => {
        try {
            await item.approveItem(itemId, true);
            setFilteredItems((prevItems) => prevItems.filter((item) => item.id !== itemId));
            setItems((prevItems) => prevItems.filter((item) => item.id !== itemId));
            toast({ title: "Ítem aprobado", variant: "success" });
        } catch (error) {
            console.error("Error al aprobar el ítem:", error);
            toast({ title: "No se pudo aprobar el ítem", variant: "danger" });
        }
    };

    const handleDeny = async (itemId: number) => {
        try {
            await item.approveItem(itemId, false);
            setFilteredItems((prevItems) => prevItems.filter((item) => item.id !== itemId));
            setItems((prevItems) => prevItems.filter((item) => item.id !== itemId));
            toast({ title: "Ítem denegado", variant: "success" });
        } catch (error) {
            console.error("Error al denegar el ítem:", error);
            toast({ title: "No se pudo denegar el ítem", variant: "danger" });
        }
    };

    const handleTrade = async (itemId: number) => {
        try {
            // Verificar si el usuario ya tiene un tradeo con este ítem
            const allAgreements = await Agreement.getAllAgreements(); // Asumiendo que tienes un método para obtener todos los acuerdos
            const existingAgreement = allAgreements.find(
                (agreement) =>
                    (agreement.id_itemFin === itemId || agreement.id_itemIni === itemId) &&
                    (agreement.id_Ini === userId || agreement.id_Fin === userId)
            );

            if (existingAgreement) {
                // Si ya existe un tradeo, redirige a la página del tradeo existente
                navigate(`/dashboard/agreements/${existingAgreement.id}`);
            } else {
                // Si no existe, redirige al flujo normal de crear un tradeo
                navigate(`/dashboard/agreements/item/${itemId}`);
            }
        } catch (error) {
            console.error("Error al verificar los tradeos existentes:", error);
            toast({ title: "Hubo un problema al procesar la solicitud.", variant: "danger" });
        }
    };


    if (loading) {
        return <Spinner label="Cargando ítems..." />;
    }

    return (
        <div className="w-full max-w-5xl mx-auto">
            <h2 className="text-2xl font-bold text-gray-900 mb-6">Publicaciones Disponibles</h2>

            {/* Buscador */}
            <div className="mb-4">
                <label htmlFor="search" className="block text-sm font-medium text-gray-700 mb-2">
                    Buscar por nombre:
                </label>
                <Input
                    id="search"
                    type="text"
                    value={searchTerm}
                    onChange={handleSearchChange}
                    placeholder="Escribe aquí para buscar ítems..."
                />
            </div>

            {/* Filtro de categorías */}
            <div className="mb-6">
                <label htmlFor="categories" className="block text-sm font-medium text-gray-700 mb-2">
                    Filtrar por categoría:
                </label>
                <select
                    id="categories"
                    value={selectedCategory ?? ""}
                    onChange={handleCategoryChange}
                    className="w-full rounded-card border border-border bg-surface px-4 py-2.5 text-gray-800 shadow-sm transition-colors duration-200 focus:outline-none focus:ring-2 focus:ring-primary focus:border-primary"
                >
                    <option value="">Todas las categorías</option>
                    {categories.map((cat) => (
                        <option key={cat.id} value={cat.id}>
                            {cat.name}
                        </option>
                    ))}
                </select>
            </div>

            {/* Mensaje de error */}
            {errorMessage && (
                <div className="text-danger text-center mb-4">{errorMessage}</div>
            )}

            {/* Lista de publicaciones */}
            {filteredItems.length > 0 ? (
                <motion.ul
                    className="grid grid-cols-1 md:grid-cols-2 gap-6"
                    variants={staggerChildren}
                    initial="hidden"
                    animate="visible"
                >
                    {filteredItems.map((item) => (
                        <motion.li key={item.id} variants={slideUp}>
                            <Card className="overflow-hidden h-full flex flex-col">
                                <img
                                    src={imageUrls[item.id] || "/default-placeholder.png"}
                                    alt={item.name}
                                    className="w-full h-48 object-cover"
                                />
                                <div className="p-4 flex flex-col flex-1">
                                    <h3 className="text-lg font-bold text-gray-900">{item.name}</h3>
                                    <p className="text-gray-600 mt-2 flex-1">{item.description}</p>
                                    <div className="flex flex-wrap gap-2 mt-3">
                                        <span className="inline-block bg-primary/10 text-primary text-xs font-semibold px-3 py-1 rounded-full">
                                            {item.categoryName}
                                        </span>
                                        <span className="inline-block bg-muted text-gray-600 text-xs font-semibold px-3 py-1 rounded-full">
                                            {item.condition}
                                        </span>
                                    </div>
                                    <p className="text-sm text-gray-500 mt-3">
                                        Publicado por: <span className="font-medium">{item.userName}</span>
                                    </p>

                                    {role === "ADMIN" && (
                                        <div className="flex gap-3 mt-4">
                                            <Button
                                                onClick={() => handleApprove(item.id)}
                                                variant="primary"
                                                className="w-full"
                                            >
                                                Aprobar
                                            </Button>
                                            <Button
                                                onClick={() => handleDeny(item.id)}
                                                variant="danger"
                                                className="w-full"
                                            >
                                                Denegar
                                            </Button>
                                        </div>
                                    )}
                                    {role === "USER" && (
                                        <Button
                                            onClick={() => handleTrade(item.id)}
                                            variant="secondary"
                                            className="w-full mt-4"
                                        >
                                            Tradear
                                        </Button>
                                    )}
                                </div>
                            </Card>
                        </motion.li>
                    ))}
                </motion.ul>
            ) : (
                <p className="text-gray-500">
                    {searchTerm || selectedCategory
                        ? "No se encontraron ítems que coincidan con los filtros aplicados."
                        : "No hay ítems publicados."}
                </p>
            )}
        </div>
    );

}
