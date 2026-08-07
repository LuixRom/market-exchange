import React, { useEffect, useState } from "react";
import { motion } from "framer-motion";
import { useNavigate } from "react-router-dom";
import { item } from "../services/item/item";
import { category } from "../services/category/category";
import { ItemResponse } from "../interfaces/item/ItemResponse";
import { CategoryResponse } from "../interfaces/category/CategoryResponse";
import { useAuth } from "../context/AuthProvider";
import { usuario } from "../services/user/user";
import { fetchItemImage } from "../services/image/image";
import { Agreement } from "../services/agreement/Agreement";
import { Card } from "./ui/Card";
import { Button } from "./ui/Button";
import { Spinner } from "./ui/Spinner";
import { useToast } from "./ui/Toast";
import { staggerChildren, slideUp } from "../lib/motion";
import { FaSearch, FaFolder, FaBoxOpen, FaLeaf } from "react-icons/fa";

export default function AllItems() {
    const auth = useAuth();
    const role = auth.role;
    const navigate = useNavigate();
    const { toast } = useToast();
    const [items, setItems] = useState<ItemResponse[]>([]);
    const [filteredItems, setFilteredItems] = useState<ItemResponse[]>([]);
    const [categories, setCategories] = useState<CategoryResponse[]>([]);
    const [searchTerm, setSearchTerm] = useState<string>("");
    const [selectedCategory, setSelectedCategory] = useState<number | null>(null);
    const [errorMessage, setErrorMessage] = useState<string | null>(null);
    const [loading, setLoading] = useState<boolean>(true);
    const [userId, setUserId] = useState<number | null>(null);
    const [imageUrls, setImageUrls] = useState<{ [key: number]: string }>({});

    useEffect(() => {
        const fetchUserId = async () => {
            try {
                const userInfo = await usuario.getMyInfo();
                setUserId(userInfo.id);
            } catch (err) {
                console.error("Error al obtener ID del usuario:", err);
            }
        };

        fetchUserId();
    }, []);

    useEffect(() => {
        const fetchData = async () => {
            if (!userId) return;

            setLoading(true);
            try {
                const [catalog, allCategories] = await Promise.all([
                    item.getCatalog({
                        status: role === "ADMIN" ? "PENDING_REVIEW" : "APPROVED",
                        page: 0,
                        size: 100,
                        sort: "createdAt,desc",
                    }),
                    category.getAllCategories(),
                ]);

                const filtered = role === "ADMIN"
                    ? catalog.content
                    : catalog.content.filter((item) => item.user_id !== userId);

                setItems(filtered);
                setFilteredItems(filtered);
                setCategories(allCategories);
            } catch (error) {
                console.error("Error al obtener los datos:", error);
                setErrorMessage("Error al obtener los datos.");
            } finally {
                setLoading(false);
            }
        };

        if (role) {
            fetchData();
        }
    }, [role, userId]);

    useEffect(() => {
        const accessToken = sessionStorage.getItem("accessToken");
        if (!accessToken || items.length === 0) return;

        items.forEach((item) => {
            fetchItemImage(item, accessToken)
                .then((url) => {
                    setImageUrls((prev) => ({ ...prev, [item.id]: url }));
                })
                .catch(() => {
                    setImageUrls((prev) => ({ ...prev, [item.id]: "/default-placeholder.png" }));
                });
        });
    }, [items]);

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
            const allAgreements = await Agreement.getMyAgreements();
            const existingAgreement = allAgreements.find(
                (agreement) =>
                    agreement.status === "PENDING" &&
                    (agreement.requestedItemId === itemId || agreement.offeredItemId === itemId)
            );

            if (existingAgreement) {
                navigate(`/dashboard/agreements/${existingAgreement.id}`);
            } else {
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
        <div className="w-full">
            {/* Encabezado con Icono y Subtítulo */}
            <div className="flex items-center gap-3 mb-6">
                <div className="w-10 h-10 rounded-2xl bg-primary/10 text-primary flex items-center justify-center flex-shrink-0 shadow-xs">
                    <FaBoxOpen size={18} />
                </div>
                <div>
                    <h2 className="text-xl font-extrabold text-gray-900 tracking-tight">
                        {role === "ADMIN" ? "Items pendientes de revision" : "Publicaciones disponibles"}
                    </h2>
                    <p className="text-xs text-gray-500 font-medium mt-0.5">
                        {role === "ADMIN"
                            ? "Aprueba o deniega las publicaciones antes de que aparezcan en el catalogo."
                            : "Encuentra items que otros usuarios estan ofreciendo para intercambiar."}
                    </p>
                </div>
            </div>

            {/* Campo Buscador Fino */}
            <div className="mb-4">
                <label htmlFor="search" className="block text-xs font-bold text-gray-700 mb-1.5">
                    Buscar por nombre
                </label>
                <div className="relative">
                    <FaSearch className="absolute left-3.5 top-1/2 -translate-y-1/2 text-primary/60 text-sm pointer-events-none" />
                    <input
                        id="search"
                        type="text"
                        value={searchTerm}
                        onChange={handleSearchChange}
                        placeholder="Escribe aquí para buscar ítems..."
                        className="w-full pl-10 pr-4 py-2.5 text-sm bg-white border border-primary/40 focus:ring-2 focus:ring-primary focus:border-transparent rounded-xl outline-none text-gray-900 shadow-xs transition-all"
                    />
                </div>
            </div>

            {/* Campo Filtro por Categoría Fino */}
            <div className="mb-6">
                <label htmlFor="categories" className="block text-xs font-bold text-gray-700 mb-1.5">
                    Filtrar por categoría
                </label>
                <div className="relative">
                    <FaFolder className="absolute left-3.5 top-1/2 -translate-y-1/2 text-gray-400 text-sm pointer-events-none" />
                    <select
                        id="categories"
                        value={selectedCategory ?? ""}
                        onChange={handleCategoryChange}
                        className="w-full pl-10 pr-8 py-2.5 text-sm bg-white border border-gray-200 focus:ring-2 focus:ring-primary focus:border-transparent rounded-xl outline-none text-gray-900 shadow-xs cursor-pointer transition-all appearance-none"
                    >
                        <option value="">Todas las categorías</option>
                        {categories.map((cat) => (
                            <option key={cat.id} value={cat.id}>
                                {cat.name}
                            </option>
                        ))}
                    </select>
                </div>
            </div>

            {/* Mensaje de error */}
            {errorMessage && (
                <div className="text-danger text-center text-sm mb-4">{errorMessage}</div>
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
                            <Card className="overflow-hidden h-full flex flex-col rounded-2xl border border-gray-100 shadow-md hover:shadow-lg transition-shadow">
                                <div className={`w-full h-56 bg-gray-50 border-b border-gray-100 flex items-center justify-center overflow-hidden ${!imageUrls[item.id] ? "animate-pulse" : ""}`}>
                                    <img
                                        src={imageUrls[item.id] || "/default-placeholder.png"}
                                        alt={item.name}
                                        className="w-full h-full object-contain p-3"
                                        loading="lazy"
                                    />
                                </div>
                                <div className="p-4 flex flex-col flex-1">
                                    <h3 className="text-lg font-bold text-gray-900">{item.name}</h3>
                                    <p className="text-gray-600 text-sm mt-2 flex-1">{item.description}</p>
                                    <div className="flex flex-wrap gap-2 mt-3">
                                        <span className="inline-block bg-primary/10 text-primary text-xs font-semibold px-3 py-1 rounded-full">
                                            {item.categoryName}
                                        </span>
                                        <span className="inline-block bg-muted text-gray-600 text-xs font-semibold px-3 py-1 rounded-full">
                                            {item.condition === "NEW" ? "Nuevo" : "Usado"}
                                        </span>
                                    </div>
                                    <p className="text-xs text-gray-500 mt-3">
                                        Publicado por: <span className="font-medium text-gray-800">{item.userName}</span>
                                    </p>

                                    {role === "ADMIN" && (
                                        <div className="flex gap-3 mt-4">
                                            <Button
                                                onClick={() => handleApprove(item.id)}
                                                variant="primary"
                                                className="w-full text-xs font-bold rounded-xl"
                                            >
                                                Aprobar
                                            </Button>
                                            <Button
                                                onClick={() => handleDeny(item.id)}
                                                variant="danger"
                                                className="w-full text-xs font-bold rounded-xl"
                                            >
                                                Denegar
                                            </Button>
                                        </div>
                                    )}
                                    {role === "USER" && (
                                        <div className="flex gap-3 mt-4">
                                            <Button
                                                onClick={() => navigate(`/dashboard/item/${item.id}`)}
                                                variant="primary"
                                                className="w-full text-xs font-bold rounded-xl"
                                            >
                                                Ver detalle
                                            </Button>
                                            <Button
                                                onClick={() => handleTrade(item.id)}
                                                variant="secondary"
                                                className="w-full text-xs font-bold rounded-xl"
                                            >
                                                Tradear
                                            </Button>
                                        </div>
                                    )}
                                </div>
                            </Card>
                        </motion.li>
                    ))}
                </motion.ul>
            ) : (
                <div className="flex flex-col items-center justify-center text-center py-10 px-4">
                    <div className="relative w-24 h-24 rounded-full bg-primary/10 flex items-center justify-center mb-4">
                        <FaSearch className="text-primary text-3xl" />
                        <FaLeaf className="absolute bottom-2 right-2 text-emerald-500 text-lg" />
                    </div>
                    <h3 className="text-base font-extrabold text-gray-900 mb-1">
                        No hay ítems publicados
                    </h3>
                    <p className="text-xs text-gray-500 max-w-xs mb-5">
                        {searchTerm || selectedCategory
                            ? "No se encontraron ítems que coincidan con los filtros aplicados."
                            : "Aún no hay publicaciones disponibles. ¡Sé el primero en publicar algo!"}
                    </p>
                    {role === "USER" && (
                        <button
                            type="button"
                            onClick={() => navigate("/dashboard/item/create")}
                            className="px-5 py-2.5 rounded-xl border border-primary text-primary hover:bg-primary/5 text-xs font-bold transition-all shadow-xs"
                        >
                            + Publicar mi primer ítem
                        </button>
                    )}
                </div>
            )}
        </div>
    );
}
