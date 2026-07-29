import React, { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { Agreement } from "../services/agreement/Agreement";
import { AgreementResponse } from "../interfaces/agreement/AgreementResponse";
import { item } from "../services/item/item";
import { ItemResponse } from "../interfaces/item/ItemResponse";
import { fetchImage } from "../services/image/image"; // Función para cargar imágenes
import { getApiBaseUrl } from "../apis/api";
import { Card } from "../components/ui/Card";
import { Spinner } from "../components/ui/Spinner";

export default function AgreementByIdPage() {
    const { id } = useParams<{ id: string }>();
    const [trade, setTrade] = useState<AgreementResponse | null>(null);
    const [receptorItem, setReceptorItem] = useState<ItemResponse | null>(null);
    const [iniciadorItem, setIniciadorItem] = useState<ItemResponse | null>(null);
    const [imageUrls, setImageUrls] = useState<{ [key: number]: string }>({});
    const [errorMessage, setErrorMessage] = useState<string | null>(null);

    // Cargar el trade y los ítems asociados
    useEffect(() => {
        async function fetchTradeData() {
            try {
                if (!id) return;

                // Obtener los detalles del trade
                const fetchedTrade = await Agreement.getAgreementById(Number(id));
                setTrade(fetchedTrade);

                // Obtener los ítems del receptor e iniciador
                if (fetchedTrade.id_itemFin) {
                    const receptorFetchedItem = await item.getItemById(fetchedTrade.id_itemFin);
                    setReceptorItem(receptorFetchedItem);
                }

                if (fetchedTrade.id_itemIni) {
                    const iniciadorFetchedItem = await item.getItemById(fetchedTrade.id_itemIni);
                    setIniciadorItem(iniciadorFetchedItem);
                }
            } catch (error) {
                console.error("Error al obtener los datos del tradeo:", error);
                setErrorMessage("Error al obtener los datos del tradeo.");
            }
        }
        fetchTradeData();
    }, [id]);

    // Cargar las imágenes de los ítems
    useEffect(() => {
        const loadImages = async () => {
            const accessToken = localStorage.getItem("accessToken");
            if (!accessToken) {
                console.error("No se encontró un token de autenticación.");
                return;
            }

            const promises = [
                receptorItem &&
                    fetchImage(`${getApiBaseUrl()}${receptorItem.imageUrl}`, accessToken).then((url) => ({
                        id: receptorItem.id,
                        url,
                    })),
                iniciadorItem &&
                    fetchImage(`${getApiBaseUrl()}${iniciadorItem.imageUrl}`, accessToken).then((url) => ({
                        id: iniciadorItem.id,
                        url,
                    })),
            ].filter(Boolean) as Promise<{ id: number; url: string }>[];

            const results = await Promise.all(promises);
            const urls = results.reduce((acc, { id, url }) => ({ ...acc, [id]: url }), {});
            setImageUrls(urls);
        };


        if (receptorItem || iniciadorItem) {
            loadImages();
        }
    }, [receptorItem, iniciadorItem]);

    if (errorMessage) {
        return <div className="text-danger text-center py-10">{errorMessage}</div>;
    }

    if (!trade) {
        return <Spinner label="Cargando información del tradeo..." />;
    }

    return (
        <div className="bg-muted min-h-screen p-6">
            <Card className="max-w-5xl mx-auto p-6">
                <h1 className="text-2xl font-bold text-gray-900 mb-4">
                    Tradeo {trade.id} - Estado: {trade.state}
                </h1>
                <div className="grid grid-cols-2 gap-4">
                    {/* Ítem del receptor */}
                    <div>
                        <h2 className="text-lg font-semibold text-gray-700 mb-2">Receptor</h2>
                        {receptorItem ? (
                            <div className="border border-border p-4 rounded-card bg-muted">
                                <h3 className="text-lg font-bold text-gray-900">{receptorItem.name}</h3>
                                <img
                                    src={imageUrls[receptorItem.id] || "/default-placeholder.png"}
                                    alt={receptorItem.name}
                                    className="w-full h-auto mt-2 rounded-card"
                                />
                                <p>{receptorItem.description}</p>
                                <p className="text-sm text-gray-500">
                                    <strong>Publicado por:</strong> {receptorItem.userName}
                                </p>
                                <p className="text-sm text-gray-500">
                                    <strong>Categoría:</strong> {receptorItem.categoryName}
                                </p>
                                <p className="text-sm text-gray-500">
                                    <strong>Condición:</strong> {receptorItem.condition}
                                </p>
                            </div>
                        ) : (
                            <p>Cargando ítem del receptor...</p>
                        )}
                    </div>
                    {/* Ítem del iniciador */}
                    <div>
                        <h2 className="text-lg font-semibold text-gray-700 mb-2">Iniciador</h2>
                        {iniciadorItem ? (
                            <div className="border border-border p-4 rounded-card bg-muted">
                                <h3 className="text-lg font-bold text-gray-900">{iniciadorItem.name}</h3>
                                <img
                                    src={imageUrls[iniciadorItem.id] || "/default-placeholder.png"}
                                    alt={iniciadorItem.name}
                                    className="w-full h-auto mt-2 rounded-card"
                                />
                                <p>{iniciadorItem.description}</p>
                                <p className="text-sm text-gray-500">
                                    <strong>Publicado por:</strong> {iniciadorItem.userName}
                                </p>
                                <p className="text-sm text-gray-500">
                                    <strong>Categoría:</strong> {iniciadorItem.categoryName}
                                </p>
                                <p className="text-sm text-gray-500">
                                    <strong>Condición:</strong> {iniciadorItem.condition}
                                </p>
                            </div>
                        ) : (
                            <p>Cargando ítem del iniciador...</p>
                        )}
                    </div>
                </div>
            </Card>
        </div>
    );
}
