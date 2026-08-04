import { useMemo, useState } from "react";
import { motion } from "framer-motion";
import { useSearchParams } from "react-router-dom";
import UserSettings from "../components/UserSettings";
import UserItems from "../components/UserItems";
import UserTrades from "../components/UserTrades";
import UserTradesPending from "../components/UserTradesPending";
import UserFavorites from "../components/UserFavorites";
import { fadeIn } from "../lib/motion";

type AccountTab = "profile" | "items" | "favorites" | "trades" | "pending";

const tabs: { id: AccountTab; label: string }[] = [
    { id: "profile", label: "Perfil" },
    { id: "items", label: "Mis items" },
    { id: "favorites", label: "Favoritos" },
    { id: "trades", label: "Historial" },
    { id: "pending", label: "Pendientes" },
];

export default function CuentaPage() {
    const [searchParams, setSearchParams] = useSearchParams();
    const initialTab = useMemo<AccountTab>(() => {
        const tab = searchParams.get("tab");
        return tabs.some((entry) => entry.id === tab) ? (tab as AccountTab) : "profile";
    }, [searchParams]);
    const [activeTab, setActiveTab] = useState<AccountTab>(initialTab);

    function handleTabChange(tab: AccountTab) {
        setActiveTab(tab);
        setSearchParams(tab === "profile" ? {} : { tab });
    }

    return (
        <motion.div
            className="w-full max-w-container mx-auto px-4 sm:px-6 py-8"
            variants={fadeIn}
            initial="hidden"
            animate="visible"
            transition={{ duration: 0.3 }}
        >
            <div className="mb-6">
                <h1 className="text-2xl sm:text-3xl font-extrabold text-gray-900 tracking-tight">
                    Mi Cuenta
                </h1>
                <p className="mt-1 text-sm text-gray-600">
                    Gestiona tu perfil, tus propuestas de intercambio y los tradeos pendientes.
                </p>
            </div>

            <div className="inline-flex rounded-2xl bg-white border border-gray-100 shadow-sm p-1 mb-6">
                {tabs.map((tab) => (
                    <button
                        key={tab.id}
                        type="button"
                        onClick={() => handleTabChange(tab.id)}
                        className={`px-4 py-2 rounded-xl text-sm font-bold transition-colors ${
                            activeTab === tab.id
                                ? "bg-primary text-white"
                                : "text-gray-600 hover:bg-primary/5 hover:text-primary"
                        }`}
                    >
                        {tab.label}
                    </button>
                ))}
            </div>

            {activeTab === "profile" && <UserSettings />}
            {activeTab === "items" && <UserItems />}
            {activeTab === "favorites" && <UserFavorites />}
            {activeTab === "trades" && <UserTrades />}
            {activeTab === "pending" && <UserTradesPending />}
        </motion.div>
    );
}
