import { useState } from "react";
import { AnimatePresence, motion } from "framer-motion";
import UserSettings from "../components/UserSettings"; // Renderiza la configuración del usuario
import UserItems from "../components/UserItems"; // Renderiza los ítems publicados por el usuario
import UserTrades from "../components/UserTrades";
import UserTradesPending from "../components/UserTradesPending";
import { Card } from "../components/ui/Card";
import { fadeIn } from "../lib/motion";

export default function CuentaPage() {
    const [activeTab, setActiveTab] = useState<"info" | "items" | "tradeos" | "tradeosPending">("info"); // Estado para alternar entre pestañas

    const tabs: { key: typeof activeTab; label: string }[] = [
        { key: "info", label: "Información del Usuario" },
        { key: "items", label: "Mis Ítems Publicados" },
        { key: "tradeos", label: "Tradeos" },
        { key: "tradeosPending", label: "Tradeos por revisar" },
    ];

    return (
        <div className="min-h-screen flex flex-col items-center justify-center bg-muted py-10">
            <Card className="w-full max-w-4xl p-6">
                <h1 className="text-4xl font-bold text-center text-primary mb-6">Mi Cuenta</h1>

                <div className="flex">
                    {/* Menú lateral */}
                    <div className="w-1/4 pr-4 border-r border-border">
                        <ul className="space-y-2">
                            {tabs.map((tab) => (
                                <li
                                    key={tab.key}
                                    className={`cursor-pointer p-2 rounded-card transition-colors ${
                                        activeTab === tab.key
                                            ? "bg-primary/10 text-primary font-bold"
                                            : "text-gray-600 hover:bg-muted"
                                    }`}
                                    onClick={() => setActiveTab(tab.key)}
                                >
                                    {tab.label}
                                </li>
                            ))}
                        </ul>
                    </div>

                    {/* Contenido principal */}
                    <div className="w-3/4 pl-4">
                        <AnimatePresence mode="wait">
                            <motion.div
                                key={activeTab}
                                variants={fadeIn}
                                initial="hidden"
                                animate="visible"
                                exit="hidden"
                                transition={{ duration: 0.15 }}
                            >
                                {activeTab === "info" && (
                                    <>
                                        <h2 className="text-2xl font-bold text-gray-700 mb-4">Información del Usuario</h2>
                                        <UserSettings /> {/* Componente para editar/eliminar la cuenta */}
                                    </>
                                )}

                                {activeTab === "items" && <UserItems /> /* Componente para mostrar los ítems del usuario */}

                                {activeTab === "tradeos" && <UserTrades /> /* Componente para mostrar los ítems del usuario */}

                                {activeTab === "tradeosPending" && <UserTradesPending /> /* Componente para mostrar los ítems del usuario */}
                            </motion.div>
                        </AnimatePresence>
                    </div>
                </div>
            </Card>
        </div>
    );
}
