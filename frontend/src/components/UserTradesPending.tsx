import React, { useEffect, useState } from "react";
import { usuario } from "../services/user/user"; // Servicio de usuario
import { Agreement } from "../services/agreement/Agreement"; // Servicio de acuerdos
import { AgreementResponse } from "../interfaces/agreement/AgreementResponse"; // Interfaz de respuesta de acuerdo
import { useNavigate } from "react-router-dom";

export default function UserTradesPending() {
    const [trades, setTrades] = useState<AgreementResponse[]>([]);
    const [filteredTrades, setFilteredTrades] = useState<AgreementResponse[]>([]);
    const [userId, setUserId] = useState<number | null>(null);
    const [searchTerm, setSearchTerm] = useState<string>("");
    const [errorMessage, setErrorMessage] = useState<string | null>(null);
    const navigate = useNavigate();
    // Obtener el ID del usuario autenticado al montar el componente
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

    // Obtener los acuerdos del usuario una vez que se tenga el ID
    useEffect(() => {
        async function fetchUserTrades() {
            if (userId === null) return;

            try {
                const userTrades = await Agreement.getAllAgreements();
                const userSpecificTrades = userTrades.filter(
                    (trade) =>
                        trade.id_Fin === userId && trade.state === "PENDING"
                );
                setTrades(userSpecificTrades);
                setFilteredTrades(userSpecificTrades);
            } catch {
                setErrorMessage("Error al obtener los tradeos del usuario.");
            }
        }

        fetchUserTrades();
    }, [userId]);

    // Manejar cambios en el término de búsqueda
    function handleSearchChange(event: React.ChangeEvent<HTMLInputElement>) {
        const term = event.target.value.toLowerCase();
        setSearchTerm(term);

        const filtered = trades.filter(
            (trade) =>
                trade.itemIniName.toLowerCase().includes(term) ||
                trade.itemFinName.toLowerCase().includes(term)
        );
        setFilteredTrades(filtered);
    }

    // Aprobar un tradeo
    const handleApprove = async (tradeId: number) => {
        try {
            await Agreement.acceptAgreement(tradeId);
            setTrades((prev) => prev.filter((trade) => trade.id !== tradeId));
            setFilteredTrades((prev) =>
                prev.filter((trade) => trade.id !== tradeId)
            );
        } catch (error) {
            console.error("Error al aprobar el tradeo:", error);
        }
    };

    // Denegar un tradeo
    const handleReject = async (tradeId: number) => {
        try {
            await Agreement.rejectAgreement(tradeId);
            setTrades((prev) => prev.filter((trade) => trade.id !== tradeId));
            setFilteredTrades((prev) =>
                prev.filter((trade) => trade.id !== tradeId)
            );
        } catch (error) {
            console.error("Error al denegar el tradeo:", error);
        }
    };

    const handleTradeClick = (tradeId: number) => {
        navigate(`/dashboard/agreements/${tradeId}`); // Navega a la ruta específica
    };

    return (
        <div className="bg-white shadow-md rounded-lg p-6 w-full max-w-3xl mx-auto mt-10">
            <h2 className="text-2xl font-bold text-blue-700 mb-4">Mis Tradeos Pendientes</h2>

            {/* Buscador */}
            <div className="mb-6">
                <label
                    htmlFor="search"
                    className="block text-sm font-medium text-gray-700 mb-2"
                >
                    Buscar por ítem:
                </label>
                <input
                    id="search"
                    type="text"
                    value={searchTerm}
                    onChange={handleSearchChange}
                    placeholder="Escribe aquí para buscar tradeos..."
                    className="w-full p-2 border border-gray-300 rounded shadow-sm focus:outline-none focus:ring focus:ring-blue-200"
                />
            </div>
            {/* Mostrar errores */}
            {errorMessage && (
                <div className="text-red-500 text-center mb-4">{errorMessage}</div>
            )}

            {/* Lista de tradeos */}
            {filteredTrades.length > 0 ? (
                <ul className="space-y-4">
                    {filteredTrades.map((trade) => (
                        <li
                            onClick={() => handleTradeClick(trade.id)} // Navegar al hacer clic
                            key={trade.id}
                            className="border border-gray-300 p-4 rounded shadow-sm hover:shadow-md"
                        >   
                            <h3 className="text-lg font-bold text-blue-600">Trade ID: {trade.id}</h3>
                            <p className="text-gray-700">
                                <strong>Ítem Ofrecido:</strong> {trade.itemIniName}
                            </p>
                            <p className="text-gray-700">
                                <strong>Ítem Recibido:</strong> {trade.itemFinName}
                            </p>
                            <p className="text-sm text-gray-500">
                                <strong>Iniciado por:</strong> {trade.iniUsername}
                            </p>
                            <p className="text-sm text-gray-500">
                                <strong>Recibido por:</strong> {trade.finUsername}
                            </p>
                            <p className="text-sm text-gray-500">
                                <strong>Estado:</strong> {trade.state}
                            </p>
                            <div className="flex justify-end gap-4 mt-4">
                                <button
                                    onClick={(event) => {
                                        event.stopPropagation(); // Detener la propagación del clic
                                        handleApprove(trade.id);
                                    }}
                                    className="bg-green-500 text-white px-4 py-2 rounded"
                                >
                                    Aprobar
                                </button>
                                <button
                                    onClick={(event) => {
                                        event.stopPropagation(); // Detener la propagación del clic
                                        handleReject(trade.id);
                                    }}
                                    className="bg-red-500 text-white px-4 py-2 rounded"
                                >
                                    Denegar
                                </button>
                            </div>

                        </li>
                    ))}
                </ul>
            ) : (
                <p className="text-gray-500">
                    {searchTerm
                        ? "No se encontraron tradeos que coincidan con la búsqueda."
                        : "No tienes tradeos pendientes."}
                </p>
            )}
        </div>
    );
}
