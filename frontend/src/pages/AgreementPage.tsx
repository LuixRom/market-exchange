import React, { useEffect, useState } from "react";
import { useParams } from "react-router-dom"; // Para capturar el ID desde la URL
import { AgreementRequest } from "../interfaces/agreement/AgreementRequest";
import { Agreement } from "../services/agreement/Agreement";
import { ItemResponse } from "../interfaces/item/ItemResponse";
import { item } from "../services/item/item";
import { usuario } from "../services/user/user";

export default function AgreementPage() {
    const { id } = useParams<{ id: string }>(); // Capturar el ID del ítem de la URL
    const [items, setItems] = useState<ItemResponse[]>([]); // Ítems del usuario autenticado
    const [filteredItems, setFilteredItems] = useState<ItemResponse[]>([]); // Ítems filtrados
    const [userId, setUserId] = useState<number | null>(null); // ID del usuario autenticado
    const [searchTerm, setSearchTerm] = useState<string>(""); // Término de búsqueda
    const [errorMessage, setErrorMessage] = useState<string | null>(null); // Errores
    const [selectedItem, setSelectedItem] = useState<ItemResponse | null>(null); // Ítem de la izquierda (desde URL)
    const [offeredItem, setOfferedItem] = useState<ItemResponse | null>(null); // Ítem seleccionado para ofrecer

    // Obtener el ID del usuario autenticado
    useEffect(() => {
        async function fetchUserId() {
            try {
                const userInfo = await usuario.getMyInfo();
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
            if (!userId) return;
            try {
                const userItems = await item.getItemsByUser(userId);
                const userItemsFilter = userItems
          .filter(
            (item) =>
              item.status == "APPROVED"
          )
                setItems(userItemsFilter);
                setFilteredItems(userItemsFilter);
            } catch {
                setErrorMessage("Error al obtener los ítems del usuario.");
            }
        }
        fetchUserItems();
    }, [userId]);

    // Obtener el ítem desde la URL
    useEffect(() => {
        async function fetchSelectedItem() {
            if (!id) return;
            try {
                const fetchedItem = await item.getItemById(Number(id));
                setSelectedItem(fetchedItem);
            } catch (error) {
                console.error("Error al obtener el ítem seleccionado:", error);
            }
        }
        fetchSelectedItem();
    }, [id]);

    // Manejar cambios en el término de búsqueda
    const handleSearchChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        const term = event.target.value.toLowerCase();
        setSearchTerm(term);
        setFilteredItems(items.filter((item) => item.name.toLowerCase().includes(term)));
    };

    // Manejar selección del ítem para ofrecer
    const handleItemSelection = (item: ItemResponse) => {
        setOfferedItem(item); // Establecer el ítem seleccionado
    };

    // Crear acuerdo
    const handleTrade = async () => {
        if (!selectedItem || !offeredItem || !userId) {
            alert("Debes seleccionar un ítem y asegurarte de que ambos ítems estén definidos.");
            return;
        }

        const agreementRequest: AgreementRequest = {
            itemIniId: offeredItem.id, // Ítem que se ofrece
            itemFinId: selectedItem.id, // Ítem de la URL
            usuarioIniId: userId, // Usuario autenticado
            usuarioFinId: selectedItem.user_id, // Usuario del ítem seleccionado
        };

        try {
            const agreement = await Agreement.createAgreement(agreementRequest);
            alert("Tradeo creado con éxito: " + JSON.stringify(agreement));
        } catch (error) {
            console.error("Error al crear el tradeo:", error);
            alert("Error al crear el tradeo.");
        }
    };

    return (
        <div className="bg-gray-100 min-h-screen p-6">
            <div className="max-w-7xl mx-auto flex gap-4">
                {/* Contenedor para el ítem seleccionado */}
                <div className="w-1/2 p-4 bg-white shadow-md rounded-lg">
                    <h2 className="text-xl font-bold text-blue-700 mb-4">Ítem Seleccionado</h2>
                    {selectedItem ? (
                        <div className="border p-4 rounded shadow-md">
                            <h3 className="text-lg font-bold">{selectedItem.name}</h3>
                            <p>{selectedItem.description}</p>
                            <p className="text-sm text-gray-500">
                                <strong>Publicado por:</strong> {selectedItem.userName}
                            </p>
                            <p className="text-sm text-gray-500">
                                <strong>Categoría:</strong> {selectedItem.categoryName}
                            </p>
                            <p className="text-sm text-gray-500">
                                <strong>Estado:</strong> {selectedItem.condition}
                            </p>
                        </div>
                    ) : (
                        <p className="text-gray-500">Cargando ítem seleccionado...</p>
                    )}
                </div>

                {/* Botón Tradear */}
                <div className="flex items-center">
                    <button
                        onClick={handleTrade}
                        className="bg-blue-500 text-white px-6 py-3 rounded-lg shadow hover:bg-blue-600"
                        disabled={!offeredItem}
                    >
                        Tradear
                    </button>
                </div>

                {/* Lista de ítems enmarcada */}
                <div className="w-1/2 bg-white shadow-md rounded-lg p-4 border border-gray-300">
                    <h2 className="text-xl font-bold text-blue-700 mb-4 text-center">
                        Ofrezco
                    </h2>
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
                                    className={`border p-4 rounded shadow-sm hover:shadow-md ${
                                        offeredItem?.id === item.id ? "border-blue-500 bg-blue-100" : ""
                                    }`}
                                    onClick={() => handleItemSelection(item)}
                                >
                                    <h3 className="text-lg font-bold text-blue-600">{item.name}</h3>
                                    <p className="text-gray-700">{item.description}</p>
                                    <p className="text-sm text-gray-500">
                                        <strong>Publicado por:</strong> {item.userName}
                                    </p>
                                    <p className="text-sm text-gray-500">
                                        <strong>Categoría:</strong> {item.categoryName}
                                    </p>
                                    <p className="text-sm text-gray-500">
                                        <strong>Estado:</strong> {item.condition}
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
            </div>
        </div>
    );
}
