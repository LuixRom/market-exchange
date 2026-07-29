import React, { useEffect, useState } from "react";
import { motion } from "framer-motion";
import { useParams } from "react-router-dom"; // Para capturar el ID desde la URL
import { AgreementRequest } from "../interfaces/agreement/AgreementRequest";
import { Agreement } from "../services/agreement/Agreement";
import { ItemResponse } from "../interfaces/item/ItemResponse";
import { item } from "../services/item/item";
import { usuario } from "../services/user/user";
import { Card } from "../components/ui/Card";
import { Input } from "../components/ui/Input";
import { Button } from "../components/ui/Button";
import { useToast } from "../components/ui/Toast";
import { staggerChildren, slideUp } from "../lib/motion";

export default function AgreementPage() {
    const { id } = useParams<{ id: string }>(); // Capturar el ID del ítem de la URL
    const { toast } = useToast();
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
            toast({ title: "Debes seleccionar un ítem para ofrecer.", variant: "danger" });
            return;
        }

        const agreementRequest: AgreementRequest = {
            itemIniId: offeredItem.id, // Ítem que se ofrece
            itemFinId: selectedItem.id, // Ítem de la URL
            usuarioIniId: userId, // Usuario autenticado
            usuarioFinId: selectedItem.user_id, // Usuario del ítem seleccionado
        };

        try {
            await Agreement.createAgreement(agreementRequest);
            toast({ title: "Tradeo creado con éxito", variant: "success" });
        } catch (error) {
            console.error("Error al crear el tradeo:", error);
            toast({ title: "Error al crear el tradeo.", variant: "danger" });
        }
    };

    return (
        <div className="bg-muted min-h-screen p-6">
            <div className="max-w-7xl mx-auto flex gap-4">
                {/* Contenedor para el ítem seleccionado */}
                <Card className="w-1/2 p-4">
                    <h2 className="text-xl font-bold text-gray-900 mb-4">Ítem Seleccionado</h2>
                    {selectedItem ? (
                        <div className="border border-border p-4 rounded-card">
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
                </Card>

                {/* Botón Tradear */}
                <div className="flex items-center">
                    <Button onClick={handleTrade} disabled={!offeredItem}>
                        Tradear
                    </Button>
                </div>

                {/* Lista de ítems enmarcada */}
                <Card className="w-1/2 p-4">
                    <h2 className="text-xl font-bold text-gray-900 mb-4 text-center">
                        Ofrezco
                    </h2>
                    {/* Buscador */}
                    <div className="mb-6">
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
                    {/* Mostrar errores */}
                    {errorMessage && (
                        <div className="text-danger text-center mb-4">{errorMessage}</div>
                    )}
                    {/* Lista de ítems */}
                    {filteredItems.length > 0 ? (
                        <motion.ul
                            className="space-y-4"
                            variants={staggerChildren}
                            initial="hidden"
                            animate="visible"
                        >
                            {filteredItems.map((item) => (
                                <motion.li
                                    key={item.id}
                                    variants={slideUp}
                                    className={`border p-4 rounded-card cursor-pointer hover:shadow-md transition-shadow ${
                                        offeredItem?.id === item.id ? "border-primary bg-primary/5" : "border-border"
                                    }`}
                                    onClick={() => handleItemSelection(item)}
                                >
                                    <h3 className="text-lg font-bold text-gray-900">{item.name}</h3>
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
                                </motion.li>
                            ))}
                        </motion.ul>
                    ) : (
                        <p className="text-gray-500">
                            {searchTerm
                                ? "No se encontraron ítems que coincidan con la búsqueda."
                                : "No tienes ítems publicados."}
                        </p>
                    )}
                </Card>
            </div>
        </div>
    );
}
