import { useEffect, useState } from "react";
import { motion } from "framer-motion";
import { Link } from "react-router-dom";
import { useAuth } from "../context/AuthProvider";
import { usuario } from "../services/user/user";
import UserTradesAccepted from "../components/UserTradesApproved";
import AllItems from "../components/AllItems";
import { Card } from "../components/ui/Card";
import { Button } from "../components/ui/Button";
import { fadeIn, slideUp } from "../lib/motion";

export default function Dashboard() {
    const { role } = useAuth();
    const [userName, setUserName] = useState<string>("");

    useEffect(() => {
        const fetchUser = async () => {
            try {
                const info = await usuario.getMyInfo();
                setUserName(info.firstname);
            } catch (err) {
                console.error("Error al cargar información del usuario:", err);
            }
        };

        fetchUser();
    }, []);

    return (
        <motion.div
            className="w-full max-w-container mx-auto px-4 sm:px-6 py-8"
            variants={fadeIn}
            initial="hidden"
            animate="visible"
            transition={{ duration: 0.3 }}
        >
            {/* Banner de Bienvenida superior */}
            <motion.div
                className="bg-[#faf6f0] rounded-3xl p-6 sm:p-8 md:p-10 border border-gray-200/70 shadow-sm relative overflow-hidden flex flex-col md:flex-row items-center justify-between gap-6 mb-8"
                variants={slideUp}
            >
                <div className="max-w-xl text-center md:text-left z-10">
                    <h1 className="text-2xl sm:text-3xl md:text-4xl font-extrabold text-gray-900 tracking-tight flex items-center justify-center md:justify-start gap-2">
                        ¡Hola, {userName || "comunidad"}! 👋
                    </h1>
                    <p className="mt-2 text-sm sm:text-base font-semibold text-primary">
                        Bienvenido de vuelta a MarketExchange.
                    </p>
                    <p className="mt-2 text-xs sm:text-sm text-gray-600 leading-relaxed">
                        Aquí puedes gestionar tus publicaciones, categorías y solicitudes de intercambio de manera fácil y eficiente.
                    </p>

                    <div className="mt-6 flex flex-wrap items-center justify-center md:justify-start gap-3">
                        {role === "USER" && (
                            <Button asChild size="md" className="rounded-full px-6 shadow-sm">
                                <Link to="/dashboard/item/create">
                                    + Nueva publicación
                                </Link>
                            </Button>
                        )}
                        <Button asChild variant="secondary" size="md" className="rounded-full px-6 shadow-sm">
                            <Link to="/dashboard/category">
                                Ver categorías
                            </Link>
                        </Button>
                    </div>
                </div>

                {/* Ilustración institucional derecha */}
                <div className="relative flex justify-center items-center flex-shrink-0 z-10">
                    <div className="w-44 h-44 sm:w-52 sm:h-52 bg-primary/10 rounded-full flex items-center justify-center shadow-inner">
                        <img
                            src="/img/logos_Mesa de trabajo 1 copia 3.png"
                            alt="Market Exchange"
                            className="w-32 h-32 sm:w-40 sm:h-40 object-contain"
                        />
                    </div>
                </div>
            </motion.div>

            {/* Contenido principal alimentado por el backend */}
            <div className="flex flex-col lg:flex-row gap-6">
                {/* Panel Izquierdo: AllItems alimentado por backend */}
                <Card className="flex-1 p-6 border border-gray-100 shadow-md rounded-2xl">
                    {role === "ADMIN" ? (
                        <div>
                            <h2 className="text-xl font-bold text-gray-900 mb-4">Publicaciones Pendientes</h2>
                            <AllItems />
                        </div>
                    ) : (
                        <div>
                            <AllItems />
                        </div>
                    )}
                </Card>

                {/* Panel Derecho: Tradeos Recientes reales del backend (solo para usuarios) */}
                {role !== "ADMIN" && (
                    <Card className="lg:w-1/3 p-6 border border-gray-100 shadow-md rounded-2xl">
                        <h2 className="text-xl font-bold text-primary mb-4">Tradeos Recientes</h2>
                        <UserTradesAccepted />
                    </Card>
                )}
            </div>
        </motion.div>
    );
}
