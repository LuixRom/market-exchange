import { useEffect, useState } from "react";
import { motion } from "framer-motion";
import { usuario } from "../services/user/user";
import { Agreement } from "../services/agreement/Agreement";
import { AgreementResponse } from "../interfaces/agreement/AgreementResponse";
import { useNavigate } from "react-router-dom";
import TradeCard from "./TradeCard";
import { staggerChildren, slideUp } from "../lib/motion";
import { FaExchangeAlt, FaCheckCircle } from "react-icons/fa";

export default function UserTradesAccepted() {
  const [trades, setTrades] = useState<AgreementResponse[]>([]);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [userId, setUserId] = useState<number | null>(null);
  const navigate = useNavigate();

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

  useEffect(() => {
    async function fetchUserTrades() {
      if (userId === null) return;

      try {
        const userTrades = await Agreement.getAllAgreements();
        const userSpecificTrades = userTrades
          .filter(
            (trade) =>
              (trade.id_Ini === userId || trade.id_Fin === userId) &&
              trade.state === "ACCEPTED"
          )
          .slice(-3);
        setTrades(userSpecificTrades);
      } catch {
        setErrorMessage("Error al obtener los tradeos del usuario.");
      }
    }

    fetchUserTrades();
  }, [userId]);

  const handleTradeClick = (tradeId: number) => {
    navigate(`/dashboard/agreements/${tradeId}`);
  };

  return (
    <div className="w-full">
      {/* Encabezado con Icono y Subtítulo */}
      <div className="flex items-center gap-3 mb-6">
        <div className="w-10 h-10 rounded-2xl bg-primary/10 text-primary flex items-center justify-center flex-shrink-0 shadow-xs">
          <FaExchangeAlt size={18} />
        </div>
        <div>
          <h2 className="text-xl font-extrabold text-gray-900 tracking-tight">Trades Recientes</h2>
          <p className="text-xs text-gray-500 font-medium mt-0.5">Revisa los intercambios que han sido aprobados.</p>
        </div>
      </div>

      {/* Mostrar errores */}
      {errorMessage && (
        <div className="text-danger text-center text-sm mb-4">{errorMessage}</div>
      )}

      {/* Lista de tradeos */}
      {trades.length > 0 ? (
        <motion.ul
          className="space-y-4"
          variants={staggerChildren}
          initial="hidden"
          animate="visible"
        >
          {trades.map((trade) => (
            <motion.li key={trade.id} variants={slideUp}>
              <TradeCard trade={trade} onClick={() => handleTradeClick(trade.id)} showState={false} />
            </motion.li>
          ))}
        </motion.ul>
      ) : (
        <div className="flex flex-col items-center justify-center text-center py-10 px-4">
          <div className="relative w-28 h-28 rounded-full bg-primary/10 flex items-center justify-center mb-4">
            <img src="/img/caja2.png" alt="Trades" className="w-20 h-auto object-contain drop-shadow-sm" />
            <div className="absolute top-1 right-1 w-7 h-7 rounded-full bg-emerald-500 text-white flex items-center justify-center shadow-md">
              <FaCheckCircle size={14} />
            </div>
          </div>
          <h3 className="text-base font-extrabold text-gray-900 mb-1">
            No tienes trades aprobados
          </h3>
          <p className="text-xs text-gray-500 max-w-xs mb-5">
            Cuando apruebes intercambios, los verás aquí.
          </p>
          <button
            onClick={() => navigate("/dashboard/cuenta")}
            className="px-5 py-2.5 rounded-xl border border-primary text-primary hover:bg-primary/5 text-xs font-bold transition-all shadow-xs"
          >
            Ver mis trades
          </button>
        </div>
      )}
    </div>
  );
}
