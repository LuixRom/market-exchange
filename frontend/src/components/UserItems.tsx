import React, { useEffect, useState } from "react";
import { item } from "../services/item/item"; // Servicio de ítems
import { usuario } from "../services/user/user"; // Servicio de usuario
import { ItemResponse } from "../interfaces/item/ItemResponse";
import { fetchImage } from "../services/image/image";
import { getApiBaseUrl } from "../apis/api";

export default function UserItems() {
    const [items, setItems] = useState<ItemResponse[]>([]); // Estado para almacenar los ítems del usuario
    const [filteredItems, setFilteredItems] = useState<ItemResponse[]>([]); // Estado para los ítems filtrados
    const [userId, setUserId] = useState<number | null>(null); // ID del usuario autenticado
    const [searchTerm, setSearchTerm] = useState<string>(""); // Término de búsqueda
    const [errorMessage, setErrorMessage] = useState<string | null>(null); // Estado para errores
    const [imageUrls, setImageUrls] = useState<{ [key: number]: string }>({});
    // Obtener el ID del usuario autenticado al montar el componente
    useEffect(() => {
        async function fetchUserId() {
            try {
                const userInfo = await usuario.getMyInfo(); // Obtiene la información del usuario autenticado
                setUserId(userInfo.id);
            } catch {
                setErrorMessage("Error al obtener la información del usuario.");
            }
        }

        fetchUserId();
    }, []);

    // Obtener los ítems del usuario una vez que se tenga el ID
    useEffect(() => {
        async function fetchUserItems() {
            if (userId === null) return;

            try {
                const userItems = await item.getItemsByUser(userId); // Obtiene los ítems por ID del usuario
                setItems(userItems);
                setFilteredItems(userItems); // Inicializa los ítems filtrados con todos los ítems
            } catch {
                setErrorMessage("Error al obtener los ítems del usuario.");
            }
        }

        fetchUserItems();
    }, [userId]);


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

    
    // Manejar cambios en el término de búsqueda
    function handleSearchChange(event: React.ChangeEvent<HTMLInputElement>) {
        const term = event.target.value.toLowerCase(); // Convierte el término a minúsculas
        setSearchTerm(term);

        // Filtra los ítems según el término de búsqueda
        const filtered = items.filter((item) =>
            item.name.toLowerCase().includes(term)
        );
        setFilteredItems(filtered);
    }

    return (
        <div className="bg-white shadow-md rounded-lg p-6 w-full max-w-3xl mx-auto mt-10">
            <h2 className="text-2xl font-bold text-blue-700 mb-4">Mis Ítems Publicados</h2>

            {/* Buscador */}
            <div className="mb-6">
                <label
                    htmlFor="search"
                    className="block text-sm font-medium text-gray-700 mb-2"
                >
                    Buscar por nombre:
                </label>
                <input
                    id="search"
                    type="text"
                    value={searchTerm}
                    onChange={handleSearchChange}
                    placeholder="Escribe aquí para buscar ítems..."
                    className="w-full p-2 border border-gray-300 rounded shadow-sm focus:outline-none focus:ring focus:ring-blue-200"
                />
            </div>

            {/* Mostrar errores */}
            {errorMessage && (
                <div className="text-red-500 text-center mb-4">{errorMessage}</div>
            )}

            {/* Lista de ítems */}
            {filteredItems.length > 0 ? (
                <ul className="space-y-4">
                    {filteredItems.map((item) => (
                        <li
                            key={item.id}
                            className="border border-gray-300 p-4 rounded shadow-sm hover:shadow-md"
                        >
                            <h3 className="text-lg font-bold text-blue-600">{item.name}</h3>
                            <img
                                    src={imageUrls[item.id] || "/default-placeholder.png"}
                                    alt={item.name}
                                    className="w-full h-auto mt-2"
                                />
                            <p className="text-gray-700">{item.description}</p>
                            <p className="text-sm text-gray-500">
                                <strong>Categoría:</strong> {item.categoryName}
                            </p>
                            <p className="text-sm text-gray-500">
                                <strong>Condicion:</strong> {item.condition}
                            </p>
                            <p className="text-sm text-gray-500">
                                <strong>Estado:</strong> {item.status}
                            </p>
                        </li>
                    ))}
                </ul>
            ) : (
                <p className="text-gray-500">
                    {searchTerm
                        ? "No se encontraron ítems que coincidan con la búsqueda."
                        : "No tienes ítems publicados."}
                </p>
            )}  
        </div>
    );
}
