import { motion } from "framer-motion";
import { useAuth } from "../context/AuthProvider";
import UserTradesAccepted from "../components/UserTradesApproved"; // Asegúrate de importar correctamente el componente
import AllItems from "../components/AllItems";
import { Card } from "../components/ui/Card";
import { fadeIn } from "../lib/motion";

export default function Dashboard() {
    const { role } = useAuth();

    return (
        <motion.div
            className="flex flex-col lg:flex-row gap-6 p-6"
            variants={fadeIn}
            initial="hidden"
            animate="visible"
            transition={{ duration: 0.3 }}
        >
            {/* Panel Izquierdo */}
            <Card className="flex-1 p-6">
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
            </Card>

            {/* Panel Derecho: Tradeos recientes (Solo visible para usuarios normales) */}
            {role !== "ADMIN" && (
                <Card className="lg:w-1/4 p-6">
                    <h1 className="text-2xl font-bold text-primary mb-6">Tradeos Recientes</h1>
                    <UserTradesAccepted /> {/* Renderiza el componente de tradeos aprobados */}
                </Card>
            )}
        </motion.div>
    );
}
