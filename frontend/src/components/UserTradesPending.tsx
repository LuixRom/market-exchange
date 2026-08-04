import React, { useEffect, useState } from "react";
import { motion } from "framer-motion";
import { Agreement } from "../services/agreement/Agreement";
import { AgreementResponse } from "../interfaces/agreement/AgreementResponse";
import { useNavigate } from "react-router-dom";
import { Input } from "./ui/Input";
import { Button } from "./ui/Button";
import { useToast } from "./ui/Toast";
import TradeCard from "./TradeCard";
import { staggerChildren, slideUp } from "../lib/motion";

export default function UserTradesPending() {
    const [trades, setTrades] = useState<AgreementResponse[]>([]);
    const [filteredTrades, setFilteredTrades] = useState<AgreementResponse[]>([]);
    const [searchTerm, setSearchTerm] = useState<string>("");
    const [errorMessage, setErrorMessage] = useState<string | null>(null);
    const navigate = useNavigate();
    const { toast } = useToast();

    useEffect(() => {
        async function fetchUserTrades() {
            try {
                const received = await Agreement.getReceivedAgreements();
                const pending = received.filter((trade) => trade.status === "PENDING");
                setTrades(pending);
                setFilteredTrades(pending);
            } catch {
                setErrorMessage("Error al obtener tus tradeos pendientes.");
            }
        }

        fetchUserTrades();
    }, []);

    function handleSearchChange(event: React.ChangeEvent<HTMLInputElement>) {
        const term = event.target.value.toLowerCase();
        setSearchTerm(term);
        setFilteredTrades(
            trades.filter(
                (trade) =>
                    trade.offeredItemName.toLowerCase().includes(term) ||
                    trade.requestedItemName.toLowerCase().includes(term)
            )
        );
    }

    const removeTrade = (tradeId: number) => {
        setTrades((prev) => prev.filter((trade) => trade.id !== tradeId));
        setFilteredTrades((prev) => prev.filter((trade) => trade.id !== tradeId));
    };

    const handleApprove = async (tradeId: number) => {
        try {
            await Agreement.acceptAgreement(tradeId);
            removeTrade(tradeId);
            toast({ title: "Tradeo aprobado", variant: "success" });
        } catch (error) {
            console.error("Error al aprobar el tradeo:", error);
            toast({ title: "No se pudo aprobar el tradeo", variant: "danger" });
        }
    };

    const handleReject = async (tradeId: number) => {
        try {
            await Agreement.rejectAgreement(tradeId);
            removeTrade(tradeId);
            toast({ title: "Tradeo rechazado", variant: "success" });
        } catch (error) {
            console.error("Error al rechazar el tradeo:", error);
            toast({ title: "No se pudo rechazar el tradeo", variant: "danger" });
        }
    };

    return (
        <div>
            <h2 className="text-2xl font-bold text-gray-900 mb-4">Tradeos pendientes recibidos</h2>

            <div className="mb-6">
                <label htmlFor="pending-search" className="block text-sm font-medium text-gray-700 mb-2">
                    Buscar por item:
                </label>
                <Input
                    id="pending-search"
                    type="text"
                    value={searchTerm}
                    onChange={handleSearchChange}
                    placeholder="Escribe aqui para buscar tradeos..."
                />
            </div>

            {errorMessage && (
                <div className="text-danger text-center mb-4">{errorMessage}</div>
            )}

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
                                onClick={() => navigate(`/dashboard/agreements/${trade.id}`)}
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
                                            Aceptar
                                        </Button>
                                        <Button
                                            variant="danger"
                                            size="sm"
                                            onClick={(event) => {
                                                event.stopPropagation();
                                                handleReject(trade.id);
                                            }}
                                        >
                                            Rechazar
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
                        ? "No se encontraron tradeos que coincidan con la busqueda."
                        : "No tienes tradeos pendientes recibidos."}
                </p>
            )}
        </div>
    );
}
