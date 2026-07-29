import React, { useEffect, useState } from "react";
import { motion } from "framer-motion";
import { usuario } from "../services/user/user"; // Servicio de usuario
import { Agreement } from "../services/agreement/Agreement"; // Servicio de acuerdos
import { AgreementResponse } from "../interfaces/agreement/AgreementResponse"; // Interfaz de respuesta de acuerdo
import { useNavigate } from "react-router-dom";
import { Input } from "./ui/Input";
import { Button } from "./ui/Button";
import { useToast } from "./ui/Toast";
import TradeCard from "./TradeCard";
import { staggerChildren, slideUp } from "../lib/motion";

export default function UserTradesPending() {
    const [trades, setTrades] = useState<AgreementResponse[]>([]);
    const [filteredTrades, setFilteredTrades] = useState<AgreementResponse[]>([]);
    const [userId, setUserId] = useState<number | null>(null);
    const [searchTerm, setSearchTerm] = useState<string>("");
    const [errorMessage, setErrorMessage] = useState<string | null>(null);
    const navigate = useNavigate();
    const { toast } = useToast();
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
            toast({ title: "Tradeo aprobado", variant: "success" });
        } catch (error) {
            console.error("Error al aprobar el tradeo:", error);
            toast({ title: "No se pudo aprobar el tradeo", variant: "danger" });
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
            toast({ title: "Tradeo denegado", variant: "success" });
        } catch (error) {
            console.error("Error al denegar el tradeo:", error);
            toast({ title: "No se pudo denegar el tradeo", variant: "danger" });
        }
    };

    const handleTradeClick = (tradeId: number) => {
        navigate(`/dashboard/agreements/${tradeId}`); // Navega a la ruta específica
    };

    return (
        <div>
            <h2 className="text-2xl font-bold text-gray-900 mb-4">Mis Tradeos Pendientes</h2>

            {/* Buscador */}
            <div className="mb-6">
                <label htmlFor="search" className="block text-sm font-medium text-gray-700 mb-2">
                    Buscar por ítem:
                </label>
                <Input
                    id="search"
                    type="text"
                    value={searchTerm}
                    onChange={handleSearchChange}
                    placeholder="Escribe aquí para buscar tradeos..."
                />
            </div>
            {/* Mostrar errores */}
            {errorMessage && (
                <div className="text-danger text-center mb-4">{errorMessage}</div>
            )}

            {/* Lista de tradeos */}
            {filteredTrades.length > 0 ? (
                <motion.ul
                    className="space-y-4"
                    variants={staggerChildren}
                    initial="hidden"
                    animate="visible"
                >
                    {filteredTrades.map((trade) => (
                        <motion.li key={trade.id} variants={slideUp}>
                            <TradeCard
                                trade={trade}
                                onClick={() => handleTradeClick(trade.id)}
                                actions={
                                    <>
                                        <Button
                                            variant="primary"
                                            size="sm"
                                            onClick={(event) => {
                                                event.stopPropagation();
                                                handleApprove(trade.id);
                                            }}
                                        >
                                            Aprobar
                                        </Button>
                                        <Button
                                            variant="danger"
                                            size="sm"
                                            onClick={(event) => {
                                                event.stopPropagation();
                                                handleReject(trade.id);
                                            }}
                                        >
                                            Denegar
                                        </Button>
                                    </>
                                }
                            />
                        </motion.li>
                    ))}
                </motion.ul>
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
