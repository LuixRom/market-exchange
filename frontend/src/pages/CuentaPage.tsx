import { useState } from "react";
import { AnimatePresence, motion } from "framer-motion";
import UserSettings from "../components/UserSettings";
import UserItems from "../components/UserItems";
import UserTrades from "../components/UserTrades";
import UserTradesPending from "../components/UserTradesPending";
import { fadeIn } from "../lib/motion";
import { FaUser, FaBoxes, FaExchangeAlt, FaHistory } from "react-icons/fa";

export default function CuentaPage() {
    const [activeTab, setActiveTab] = useState<"info" | "items" | "tradeos" | "tradeosPending">("info");

    const tabs: { key: typeof activeTab; label: string; icon: React.ElementType }[] = [
        { key: "info", label: "Información de la Cuenta", icon: FaUser },
        { key: "items", label: "Mis Ítems Publicados", icon: FaBoxes },
        { key: "tradeos", label: "Tradeos Realizados", icon: FaExchangeAlt },
        { key: "tradeosPending", label: "Tradeos Por Revisar", icon: FaHistory },
    ];

    return (
        <motion.div
            className="w-full max-w-container mx-auto px-4 sm:px-6 py-8"
            variants={fadeIn}
            initial="hidden"
            animate="visible"
            transition={{ duration: 0.3 }}
        >
            {/* Encabezado de la página */}
            <div className="mb-6">
                <h1 className="text-2xl sm:text-3xl font-extrabold text-gray-900 tracking-tight">
                    Mi Cuenta
                </h1>
                <p className="mt-1 text-sm text-gray-600">
                    Gestiona tu información personal y la configuración de tu cuenta.
                </p>
            </div>

            {/* Pestañas Horizontales Tipo Pills */}
            <div className="flex items-center gap-2 overflow-x-auto pb-3 mb-6 scrollbar-none border-b border-gray-100">
                {tabs.map((tab) => {
                    const IconComp = tab.icon;
                    const isActive = activeTab === tab.key;
                    return (
                        <button
                            key={tab.key}
                            onClick={() => setActiveTab(tab.key)}
                            className={`flex items-center gap-2 px-4 py-2.5 rounded-2xl text-xs sm:text-sm font-bold whitespace-nowrap transition-all ${
                                isActive
                                    ? "bg-primary text-white shadow-md"
                                    : "bg-white text-gray-600 hover:bg-gray-100 border border-gray-200/80"
                            }`}
                        >
                            <IconComp size={14} />
                            <span>{tab.label}</span>
                        </button>
                    );
                })}
            </div>

            {/* Contenido Principal por Pestaña */}
            <AnimatePresence mode="wait">
                <motion.div
                    key={activeTab}
                    variants={fadeIn}
                    initial="hidden"
                    animate="visible"
                    exit="hidden"
                    transition={{ duration: 0.15 }}
                >
                    {activeTab === "info" && <UserSettings />}
                    {activeTab === "items" && <UserItems />}
                    {activeTab === "tradeos" && <UserTrades />}
                    {activeTab === "tradeosPending" && <UserTradesPending />}
                </motion.div>
            </AnimatePresence>
        </motion.div>
    );
}
