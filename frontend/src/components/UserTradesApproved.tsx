import { useEffect, useState } from "react";
import { motion } from "framer-motion";
import { Agreement } from "../services/agreement/Agreement";
import { AgreementResponse } from "../interfaces/agreement/AgreementResponse";
import { useNavigate } from "react-router-dom";
import TradeCard from "./TradeCard";
import { staggerChildren, slideUp } from "../lib/motion";
import { FaExchangeAlt, FaCheckCircle } from "react-icons/fa";

export default function UserTradesAccepted() {
  const [trades, setTrades] = useState<AgreementResponse[]>([]);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const navigate = useNavigate();

  useEffect(() => {
    async function fetchUserTrades() {
      try {
        const userTrades = await Agreement.getMyAgreements();
        const acceptedTrades = userTrades
          .filter((trade) => trade.status === "ACCEPTED" || trade.status === "COMPLETED")
          .slice(0, 3);
        setTrades(acceptedTrades);
      } catch {
        setErrorMessage("Error al obtener tus tradeos recientes.");
      }
    }

    fetchUserTrades();
  }, []);

  return (
    <div className="w-full">
      <div className="flex items-center gap-3 mb-6">
        <div className="w-10 h-10 rounded-2xl bg-primary/10 text-primary flex items-center justify-center flex-shrink-0 shadow-xs">
          <FaExchangeAlt size={18} />
        </div>
        <div>
          <h2 className="text-xl font-extrabold text-gray-900 tracking-tight">Trades recientes</h2>
          <p className="text-xs text-gray-500 font-medium mt-0.5">Revisa los intercambios aceptados o completados.</p>
        </div>
      </div>

      {errorMessage && (
        <div className="text-danger text-center text-sm mb-4">{errorMessage}</div>
      )}

      {trades.length > 0 ? (
        <motion.ul
          className="space-y-4"
          variants={staggerChildren}
          initial="hidden"
          animate="visible"
        >
          {trades.map((trade) => (
            <motion.li key={trade.id} variants={slideUp}>
              <TradeCard trade={trade} onClick={() => navigate(`/dashboard/agreements/${trade.id}`)} showState={false} />
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
            No tienes trades aceptados
          </h3>
          <p className="text-xs text-gray-500 max-w-xs mb-5">
            Cuando aceptes intercambios, los veras aqui.
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
