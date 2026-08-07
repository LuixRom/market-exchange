import React, { useEffect, useState } from "react";
import { motion } from "framer-motion";
import { Agreement } from "../services/agreement/Agreement";
import { AgreementResponse } from "../interfaces/agreement/AgreementResponse";
import { usuario } from "../services/user/user";
import { useNavigate } from "react-router-dom";
import { Input } from "./ui/Input";
import { Button } from "./ui/Button";
import { useToast } from "./ui/Toast";
import TradeCard from "./TradeCard";
import { staggerChildren, slideUp } from "../lib/motion";
import { FaInbox, FaPaperPlane } from "react-icons/fa";

export default function UserTradesPending() {
  const [receivedTrades, setReceivedTrades] = useState<AgreementResponse[]>([]);
  const [sentTrades, setSentTrades] = useState<AgreementResponse[]>([]);
  const [searchTerm, setSearchTerm] = useState<string>("");
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const navigate = useNavigate();
  const { toast } = useToast();

  useEffect(() => {
    async function fetchUserTrades() {
      try {
        setLoading(true);
        const [allMyTrades, userInfo] = await Promise.all([
          Agreement.getMyAgreements().catch(() => []),
          usuario.getMyInfo().catch(() => null),
        ]);

        const pendingTrades = allMyTrades.filter((trade) => trade.status === "PENDING");

        if (userInfo) {
          const received = pendingTrades.filter(
            (t) => t.receiverId === userInfo.id || t.receiverEmail === userInfo.email
          );
          const sent = pendingTrades.filter(
            (t) => t.proposerId === userInfo.id || t.proposerEmail === userInfo.email
          );
          setReceivedTrades(received);
          setSentTrades(sent);
        } else {
          setReceivedTrades(pendingTrades);
        }
      } catch {
        setErrorMessage("Error al obtener tus tradeos pendientes.");
      } finally {
        setLoading(false);
      }
    }

    fetchUserTrades();
  }, []);

  const handleSearchChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    setSearchTerm(event.target.value.toLowerCase());
  };

  const removeReceivedTrade = (tradeId: number) => {
    setReceivedTrades((prev) => prev.filter((trade) => trade.id !== tradeId));
  };

  const handleApprove = async (tradeId: number) => {
    try {
      await Agreement.acceptAgreement(tradeId);
      removeReceivedTrade(tradeId);
      toast({ title: "Tradeo aprobado con éxito", variant: "success" });
    } catch (error) {
      console.error("Error al aprobar el tradeo:", error);
      toast({ title: "No se pudo aprobar el tradeo.", variant: "danger" });
    }
  };

  const handleReject = async (tradeId: number) => {
    try {
      await Agreement.rejectAgreement(tradeId);
      removeReceivedTrade(tradeId);
      toast({ title: "Tradeo rechazado", variant: "success" });
    } catch (error) {
      console.error("Error al rechazar el tradeo:", error);
      toast({ title: "No se pudo rechazar el tradeo.", variant: "danger" });
    }
  };

  const filteredReceived = receivedTrades.filter(
    (trade) =>
      trade.offeredItemName.toLowerCase().includes(searchTerm) ||
      trade.requestedItemName.toLowerCase().includes(searchTerm)
  );

  const filteredSent = sentTrades.filter(
    (trade) =>
      trade.offeredItemName.toLowerCase().includes(searchTerm) ||
      trade.requestedItemName.toLowerCase().includes(searchTerm)
  );

  if (loading) {
    return <p className="text-center text-gray-500 py-8">Cargando tradeos pendientes...</p>;
  }

  return (
    <div className="space-y-8 text-left">
      <div>
        <h2 className="text-2xl font-extrabold text-gray-900">Tradeos pendientes</h2>
        <p className="text-xs sm:text-sm text-gray-500 mt-1">
          Gestiona las propuestas que has recibido de otros usuarios y las que has enviado.
        </p>
      </div>

      <div className="mb-6">
        <label htmlFor="pending-search" className="block text-xs font-bold text-gray-700 mb-2">
          Buscar por ítem:
        </label>
        <Input
          id="pending-search"
          type="text"
          value={searchTerm}
          onChange={handleSearchChange}
          placeholder="Escribe aquí para buscar tradeos..."
        />
      </div>

      {errorMessage && <div className="text-red-600 font-semibold text-center mb-4">{errorMessage}</div>}

      {/* Sección 1: Propuestas Recibidas */}
      <div className="space-y-4">
        <h3 className="text-lg font-bold text-gray-900 flex items-center gap-2 border-b border-gray-100 pb-2">
          <FaInbox className="text-purple-600" />
          <span>Solicitudes recibidas ({filteredReceived.length})</span>
        </h3>

        {filteredReceived.length > 0 ? (
          <motion.ul className="space-y-4" variants={staggerChildren} initial="hidden" animate="visible">
            {filteredReceived.map((trade) => (
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
          <p className="text-xs sm:text-sm text-gray-500 py-3 italic">
            {searchTerm
              ? "No se encontraron solicitudes recibidas con esa búsqueda."
              : "No tienes solicitudes pendientes recibidas por otros usuarios."}
          </p>
        )}
      </div>

      {/* Sección 2: Propuestas Enviadas */}
      <div className="space-y-4 pt-4">
        <h3 className="text-lg font-bold text-gray-900 flex items-center gap-2 border-b border-gray-100 pb-2">
          <FaPaperPlane className="text-purple-600" />
          <span>Propuestas enviadas ({filteredSent.length})</span>
        </h3>

        {filteredSent.length > 0 ? (
          <motion.ul className="space-y-4" variants={staggerChildren} initial="hidden" animate="visible">
            {filteredSent.map((trade) => (
              <motion.li key={trade.id} variants={slideUp}>
                <TradeCard
                  trade={trade}
                  onClick={() => navigate(`/dashboard/agreements/${trade.id}`)}
                  actions={
                    <Button
                      variant="secondary"
                      size="sm"
                      onClick={(event) => {
                        event.stopPropagation();
                        navigate(`/dashboard/agreements/${trade.id}`);
                      }}
                    >
                      Ver detalle
                    </Button>
                  }
                />
              </motion.li>
            ))}
          </motion.ul>
        ) : (
          <p className="text-xs sm:text-sm text-gray-500 py-3 italic">
            {searchTerm
              ? "No se encontraron propuestas enviadas con esa búsqueda."
              : "No tienes propuestas enviadas en espera de respuesta."}
          </p>
        )}
      </div>
    </div>
  );
}
