import React, { useEffect, useState } from "react";
import { motion } from "framer-motion";
import { useNavigate } from "react-router-dom";
import { Agreement } from "../services/agreement/Agreement";
import { AgreementResponse } from "../interfaces/agreement/AgreementResponse";
import { Input } from "./ui/Input";
import TradeCard from "./TradeCard";
import { staggerChildren, slideUp } from "../lib/motion";

export default function UserTrades() {
    const [trades, setTrades] = useState<AgreementResponse[]>([]);
    const [filteredTrades, setFilteredTrades] = useState<AgreementResponse[]>([]);
    const [searchTerm, setSearchTerm] = useState<string>("");
    const [errorMessage, setErrorMessage] = useState<string | null>(null);
    const navigate = useNavigate();

    useEffect(() => {
        async function fetchUserTrades() {
            try {
                const userTrades = await Agreement.getMyAgreements();
                setTrades(userTrades);
                setFilteredTrades(userTrades);
            } catch {
                setErrorMessage("Error al obtener tus tradeos.");
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

    return (
        <div>
            <h2 className="text-2xl font-bold text-gray-900 mb-4">Mis tradeos</h2>

            <div className="mb-6">
                <label htmlFor="search" className="block text-sm font-medium text-gray-700 mb-2">
                    Buscar por item:
                </label>
                <Input
                    id="search"
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
                            <TradeCard trade={trade} onClick={() => navigate(`/dashboard/agreements/${trade.id}`)} />
                        </motion.li>
                    ))}
                </motion.ul>
            ) : (
                <p className="text-gray-500">
                    {searchTerm
                        ? "No se encontraron tradeos que coincidan con la busqueda."
                        : "No tienes tradeos registrados."}
                </p>
            )}
        </div>
    );
}
