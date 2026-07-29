import { useEffect, useState } from "react";
import { motion } from "framer-motion";
import { usuario } from "../services/user/user"; // Servicio de usuario
import { Agreement } from "../services/agreement/Agreement"; // Servicio de acuerdos
import { AgreementResponse } from "../interfaces/agreement/AgreementResponse"; // Interfaz de respuesta de acuerdo
import { useNavigate } from "react-router-dom";
import TradeCard from "./TradeCard";
import { staggerChildren, slideUp } from "../lib/motion";

export default function UserTradesAccepted() {
  const [trades, setTrades] = useState<AgreementResponse[]>([]); // Estado para almacenar los tradeos
  const [errorMessage, setErrorMessage] = useState<string | null>(null); // Estado para errores
  const [userId, setUserId] = useState<number | null>(null); // ID del usuario autenticado
  const navigate = useNavigate();
  // Obtener el ID del usuario autenticado al montar el componente
  useEffect(() => {
    async function fetchUserId() {
      try {
        const userInfo = await usuario.getMyInfo(); // Obtiene la información del usuario autenticado
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
        const userTrades = await Agreement.getAllAgreements(); // Obtiene todos los acuerdos
        const userSpecificTrades = userTrades
          .filter(
            (trade) =>
              (trade.id_Ini === userId || trade.id_Fin === userId) &&
              trade.state === "ACCEPTED"
          )
          .slice(-3); // Toma solo los últimos 3 tradeos
        setTrades(userSpecificTrades);
      } catch {
        setErrorMessage("Error al obtener los tradeos del usuario.");
      }
    }

    fetchUserTrades();
  }, [userId]);

  const handleTradeClick = (tradeId: number) => {
    navigate(`/dashboard/agreements/${tradeId}`); // Navega a la ruta específica
};

  return (
    <div>
      {/* Mostrar errores */}
      {errorMessage && (
        <div className="text-danger text-center mb-4">{errorMessage}</div>
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
        <p className="text-gray-500">
          No tienes tradeos aprobados recientes.
        </p>
      )}
    </div>
  );
}
