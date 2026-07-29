import React, { useState } from "react";
import UserSettings from "../components/UserSettings"; // Renderiza la configuración del usuario
import UserItems from "../components/UserItems"; // Renderiza los ítems publicados por el usuario
import UserTrades from "../components/UserTrades";
import UserTradesPending from "../components/UserTradesPending";

export default function CuentaPage() {
    const [activeTab, setActiveTab] = useState<"info" | "items" | "tradeos" | "tradeosPending">("info"); // Estado para alternar entre pestañas

    return (
        <div
            className="min-h-screen flex flex-col items-center justify-center"
            style={{
                backgroundImage: "url('https://source.unsplash.com/random/1920x1080?business')",
                backgroundSize: "cover",
                backgroundPosition: "center",
            }}
        >
            <div className="bg-white bg-opacity-90 shadow-lg rounded-lg w-full max-w-4xl p-6">
                <h1 className="text-4xl font-bold text-center text-blue-700 mb-6">Mi Cuenta</h1>

                <div className="flex">
                    {/* Menú lateral */}
                    <div className="w-1/4 pr-4 border-r">
                        <ul className="space-y-4">
                            <li
                                className={`cursor-pointer p-2 rounded ${
                                    activeTab === "info" ? "bg-blue-100 text-blue-700 font-bold" : "text-gray-600"
                                }`}
                                onClick={() => setActiveTab("info")}
                            >
                                Información del Usuario
                            </li>
                            <li
                                className={`cursor-pointer p-2 rounded ${
                                    activeTab === "items" ? "bg-blue-100 text-blue-700 font-bold" : "text-gray-600"
                                }`}
                                onClick={() => setActiveTab("items")}
                            >
                                Mis Ítems Publicados
                            </li>
                            <li
                                className={`cursor-pointer p-2 rounded ${
                                    activeTab === "tradeos" ? "bg-blue-100 text-blue-700 font-bold" : "text-gray-600"
                                }`}
                                onClick={() => setActiveTab("tradeos")}
                            >
                                Tradeos
                            </li>

                            <li
                                className={`cursor-pointer p-2 rounded ${
                                    activeTab === "tradeosPending" ? "bg-blue-100 text-blue-700 font-bold" : "text-gray-600"
                                }`}
                                onClick={() => setActiveTab("tradeosPending")}
                            >
                                Tradeos por revisar
                            </li>
                        </ul>
                    </div>

                    {/* Contenido principal */}
                    <div className="w-3/4 pl-4">
                        {activeTab === "info" && (
                            <>
                                <h2 className="text-2xl font-bold text-gray-700 mb-4">Información del Usuario</h2>
                                <UserSettings /> {/* Componente para editar/eliminar la cuenta */}
                            </>
                        )}

                        {activeTab === "items" && (
                            <>
                                <UserItems /> {/* Componente para mostrar los ítems del usuario */}
                            </>
                        )}

                        {activeTab === "tradeos" && (
                            <>
                                <UserTrades /> {/* Componente para mostrar los ítems del usuario */}
                            </>
                        )}

                        {activeTab === "tradeosPending" && (
                            <>
                                <UserTradesPending /> {/* Componente para mostrar los ítems del usuario */}
                            </>
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
}
