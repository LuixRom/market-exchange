import React, { useEffect, useState } from "react";
import { usuario } from "../services/user/user"; // Servicio de usuario
import { Agreement } from "../services/agreement/Agreement"; // Servicio de acuerdos
import { AgreementResponse } from "../interfaces/agreement/AgreementResponse"; // Interfaz de respuesta de acuerdo
import { useNavigate } from "react-router-dom";

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
    <div className="bg-white shadow-md rounded-lg p-4">
      <h2 className="text-xl font-bold text-blue-700 mb-4">
        Últimos Tradeos Aprobados
      </h2>
      {/* Mostrar errores */}
      {errorMessage && (
        <div className="text-red-500 text-center mb-4">{errorMessage}</div>
      )}
      {/* Lista de tradeos */}
      {trades.length > 0 ? (
        <ul className="space-y-4">
          {trades.map((trade) => (
            <li
              key={trade.id} 
               onClick={() => handleTradeClick(trade.id)} // Navegar al hacer clic
              className="border border-gray-300 p-4 rounded shadow-sm hover:shadow-md"
            >
              <h3 className="text-lg font-bold text-blue-600">
                Trade ID: {trade.id}
              </h3>
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
            </li>
          ))}
        </ul>
      ) : (
        <p className="text-gray-500">
          No tienes tradeos aprobados recientes.
        </p>
      )}
    </div>
  );
}
