import { ChangeEvent, FormEvent, useState } from "react";
import { AnimatePresence, motion } from "framer-motion";
import { RegisterRequest } from "../interfaces/auth/RegisterRequest";
import { FaUser, FaEnvelope, FaLock, FaPhone, FaMapMarkerAlt } from "react-icons/fa";
import { Card } from "./ui/Card";
import { Input } from "./ui/Input";
import { Button } from "./ui/Button";
import { slideUp } from "../lib/motion";

interface RegisterFormProps {
    formData: RegisterRequest;
    setFormData: React.Dispatch<React.SetStateAction<RegisterRequest>>;
    onSubmit: (data: RegisterRequest) => Promise<void>;
}

export default function RegisterForm({ formData, setFormData, onSubmit }: RegisterFormProps) {
    const [errorMessage, setErrorMessage] = useState<string | null>(null);
    const [confirmPassword, setConfirmPassword] = useState<string>("");

    function handleChange(e: ChangeEvent<HTMLInputElement>) {
        const { name, value } = e.target;
        setFormData((prevData) => ({
            ...prevData,
            [name]: value,
        }));
    }

    function handleConfirmPasswordChange(e: ChangeEvent<HTMLInputElement>) {
        setConfirmPassword(e.target.value);
    }

    async function handleSubmit(e: FormEvent<HTMLFormElement>) {
        e.preventDefault();
        setErrorMessage(null);

        if (formData.password.length < 8) {
            setErrorMessage("La contraseña debe tener al menos 8 caracteres");
            return;
        }

        if (formData.password !== confirmPassword) {
            setErrorMessage("Las contraseñas no coinciden");
            return;
        }

        await onSubmit(formData);
    }

    return (
        <section className="flex flex-col items-center justify-center min-h-screen bg-white-100 py-10">
            <motion.div
                className="w-full max-w-md px-4"
                variants={slideUp}
                initial="hidden"
                animate="visible"
                transition={{ duration: 0.3 }}
                style={{ maxWidth: "24rem" }}
            >
                <Card className="p-8 border-0 shadow-lg">
                    <form onSubmit={handleSubmit}>
                        <h2 className="text-2xl font-bold mb-6 text-center">Registro</h2>

                        <AnimatePresence>
                            {errorMessage && (
                                <motion.div
                                    className="text-danger text-sm mb-4 text-center"
                                    initial={{ opacity: 0, height: 0 }}
                                    animate={{ opacity: 1, height: "auto" }}
                                    exit={{ opacity: 0, height: 0 }}
                                    transition={{ duration: 0.2 }}
                                >
                                    {errorMessage}
                                </motion.div>
                            )}
                        </AnimatePresence>

                        {/* Input para Nombre */}
                        <div className="mb-4 relative flex items-center">
                            <FaUser className="absolute left-3 text-gray-500 z-10" size={20} />
                            <Input
                                type="text"
                                name="firstName"
                                id="firstName"
                                value={formData.firstName}
                                onChange={handleChange}
                                className="pl-10"
                                placeholder="Tu nombre"
                                required
                            />
                        </div>

                        {/* Input para Apellido */}
                        <div className="mb-4 relative flex items-center">
                            <FaUser className="absolute left-3 text-gray-500 z-10" size={20} />
                            <Input
                                type="text"
                                name="lastName"
                                id="lastName"
                                value={formData.lastName}
                                onChange={handleChange}
                                className="pl-10"
                                placeholder="Tu apellido"
                                required
                            />
                        </div>

                        {/* Input para Email */}
                        <div className="mb-4 relative flex items-center">
                            <FaEnvelope className="absolute left-3 text-gray-500 z-10" size={20} />
                            <Input
                                type="email"
                                name="email"
                                id="email"
                                value={formData.email}
                                onChange={handleChange}
                                className="pl-10"
                                placeholder="tuemail@ejemplo.com"
                                required
                            />
                        </div>

                        {/* Input para Contraseña */}
                        <div className="mb-4 relative flex items-center">
                            <FaLock className="absolute left-3 text-gray-500 z-10" size={20} />
                            <Input
                                type="password"
                                name="password"
                                id="password"
                                value={formData.password}
                                onChange={handleChange}
                                className="pl-10"
                                placeholder="Contraseña"
                                required
                            />
                        </div>

                        {/* Input para Confirmar Contraseña */}
                        <div className="mb-4 relative flex items-center">
                            <FaLock className="absolute left-3 text-gray-500 z-10" size={20} />
                            <Input
                                type="password"
                                id="confirmPassword"
                                value={confirmPassword}
                                onChange={handleConfirmPasswordChange}
                                className="pl-10"
                                placeholder="Confirmar contraseña"
                                required
                            />
                        </div>

                        {/* Input para Celular */}
                        <div className="mb-4 relative flex items-center">
                            <FaPhone className="absolute left-3 text-gray-500 z-10" size={20} />
                            <Input
                                type="text"
                                name="phone"
                                id="phone"
                                value={formData.phone}
                                onChange={handleChange}
                                className="pl-10"
                                placeholder="Tu número de teléfono"
                                required
                            />
                        </div>

                        {/* Input para Dirección */}
                        <div className="mb-6 relative flex items-center">
                            <FaMapMarkerAlt className="absolute left-3 text-gray-500 z-10" size={20} />
                            <Input
                                type="text"
                                name="address"
                                id="address"
                                value={formData.address}
                                onChange={handleChange}
                                className="pl-10"
                                placeholder="Tu dirección"
                                required
                            />
                        </div>

                        <Button type="submit" variant="primary" size="lg" className="w-full">
                            Registrarse
                        </Button>
                    </form>
                </Card>
            </motion.div>
        </section>
    );
}
