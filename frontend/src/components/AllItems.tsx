import React, { useEffect, useState } from "react";
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


export default function AllItems() {
    const auth = useAuth();
    const role = auth.role;
    const navigate = useNavigate(); // Hook para redirigir
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
        } catch (error) {
            console.error("Error al aprobar el ítem:", error);
        }
    };

    const handleDeny = async (itemId: number) => {
        try {
            await item.approveItem(itemId, false);
            setFilteredItems((prevItems) => prevItems.filter((item) => item.id !== itemId));
            setItems((prevItems) => prevItems.filter((item) => item.id !== itemId));
        } catch (error) {
            console.error("Error al denegar el ítem:", error);
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
            alert("Hubo un problema al procesar la solicitud.");
        }
    };
    

    if (loading) {
        return <div>Cargando ítems...</div>;
    }

    return (
        
        <div className="bg-white shadow-lg rounded-lg p-6 w-full max-w-5xl mx-auto mt-10">
            <h2 className="text-2xl font-bold text-black mb-6">Publicaciones Disponibles</h2>
    
            {/* Buscador */}
            <div className="mb-6">
                <label
                    htmlFor="search"
                    className="block text-sm font-medium text-white mb-2"
                >
                    Buscar por nombre:
                </label>
                <input
                    id="search"
                    type="text"
                    value={searchTerm}
                    onChange={handleSearchChange}
                    placeholder="Escribe aquí para buscar ítems..."
                    className="w-full p-3 bg-gray-50 text-gray-700 rounded-md shadow-md focus:outline-none focus:ring focus:ring-purple-300"
                />
            </div>
    
            {/* Filtro de categorías */}
            <div className="mb-6">
                <label
                    htmlFor="categories"
                    className="block text-sm font-medium text-white mb-2"
                >
                    Filtrar por categoría:
                </label>
                <select
                    id="categories"
                    value={selectedCategory ?? ""}
                    onChange={handleCategoryChange}
                    className="w-full p-3 bg-gray-50 text-gray-700 rounded-md shadow-md focus:outline-none focus:ring focus:ring-purple-300"
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
                <div className="text-red-500 text-center mb-4">{errorMessage}</div>
            )}
    
            {/* Lista de publicaciones */}
            {filteredItems.length > 0 ? (
                <ul className="grid grid-cols-1 md:grid-cols-2 gap-6">
                    {filteredItems.map((item) => (
                        <li
                            key={item.id}
                            className="bg-purple-600 border border-purple-400 p-4 rounded-lg shadow-lg hover:shadow-xl transition-shadow duration-300"
                        >
                            <div>
                                <h3 className="text-lg font-bold text-white">{item.name}</h3>
                                <img
                                    src={imageUrls[item.id] || "/default-placeholder.png"}
                                    alt={item.name}
                                    className="w-full h-auto mt-2 rounded-lg"
                                />
                                <p className="text-gray-200 mt-4">{item.description}</p>
                                <p className="text-lg font text-gray-300">
                                    <strong>Categoría:</strong> {item.categoryName}
                                </p>
                                <p className="text-lg font-sm text-gray-300">
                                    <strong>Condición:</strong> {item.condition}
                                </p>
                                <p className="text-lg font-sm text-gray-300">
                                    <strong>Publicado por:</strong> {item.userName}
                                </p>
                            </div>
                         
                            {role === "ADMIN" && (
                                <div className="flex gap-4">
                                    <button
                                        onClick={() => handleApprove(item.id)}
                                        className="bg-green-600 hover:bg-green-700 text-white font-bold py-2 px-4 rounded-md mt-4 w-full transition-colors duration-300"
                                    >
                                        Aprobar
                                    </button>
                                    <button
                                        onClick={() => handleDeny(item.id)}
                                         className="bg-red-600 hover:bg-red-700 text-white font-bold py-2 px-4 rounded-md mt-4 w-full transition-colors duration-300"
                                    >
                                        Denegar
                                    </button>
                                </div>
                            )}
                            {role === "USER" && (
                                <button
                                    onClick={() => handleTrade(item.id)}
                                    className="bg-white hover:bg-gray-200 text-purple-600 font-bold py-2 px-4 rounded-md mt-4 w-full transition-colors duration-300"
                                >
                                    Tradear
                                </button>
                            )}

                        </li>
                    ))}
                </ul>
            ) : (
                <p className="text-gray-200">
                    {searchTerm || selectedCategory
                        ? "No se encontraron ítems que coincidan con los filtros aplicados."
                        : "No hay ítems publicados."}
                </p>
            )}
        </div>
    );
    
}
