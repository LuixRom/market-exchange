import React from "react";
import { useAuth } from "../context/AuthProvider";
import UserTradesAccepted from "../components/UserTradesApproved"; // Asegúrate de importar correctamente el componente
import AllItems from "../components/AllItems";

export default function Dashboard() {
    const { role } = useAuth();

    return (
        <div className="flex flex-col lg:flex-row h-scren">
            {/* Sidebar Izquierdo */}
            <div className="flex-1 bg-purple-50 shadow-lg p-6 rounded-lg border ">
                {role === "ADMIN" ? (
                    <div>
                        <h2 className="text-xl font-semibold text-gray-800 mb-4">Publicaciones Pendientes</h2>
                        <AllItems /> {/* Renderiza el componente AllItems */}
                    </div>
                ) : (
                    <div>
                        <AllItems /> {/* Renderiza el componente AllItems */}
                    </div>
                )}
            </div>

            {/* Sidebar Derecho: Tradeos recientes (Solo visible para usuarios normales) */}
            {role !== "ADMIN" && (
                <div className="lg:w-1/4 bg-white shadow-lg p-6 rounded-lg border-l border-gray-300">
                    <h1 className="text-2xl font-bold text-green-800 mb-6">Tradeos Recientes</h1>
                    <UserTradesAccepted /> {/* Renderiza el componente de tradeos aprobados */}
                </div>
            )}
        </div>
    );
}