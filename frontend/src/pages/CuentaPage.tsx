import { motion } from "framer-motion";
import UserSettings from "../components/UserSettings";
import { fadeIn } from "../lib/motion";

export default function CuentaPage() {
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

            {/* Contenido Principal: Configuración e Información del Usuario */}
            <UserSettings />
        </motion.div>
    );
}
