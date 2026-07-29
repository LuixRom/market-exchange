import { ChangeEvent, FormEvent, useState } from "react";
import { RegisterRequest } from "../interfaces/auth/RegisterRequest";
import { FaUser, FaEnvelope, FaLock, FaPhone, FaMapMarkerAlt } from "react-icons/fa";

interface RegisterFormProps {
    formData: RegisterRequest;
    setFormData: React.Dispatch<React.SetStateAction<RegisterRequest>>;
    onSubmit: (data: RegisterRequest) => Promise<void>;
}

export default function RegisterForm({ formData, setFormData, onSubmit }: RegisterFormProps) {
    const [errorMessage, setErrorMessage] = useState<string | null>(null);
    const [confirmPassword, setConfirmPassword] = useState<string>("");

    function handleChange(e: ChangeEvent<HTMLInputElement>) {
        const { name, value, type, checked } = e.target;
        setFormData((prevData) => ({
            ...prevData,
            [name]: type === "checkbox" ? checked : value,
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
        <section className="flex flex-col items-center justify-center min-h-screen bg-white-100">
            <form
                onSubmit={handleSubmit}
                className="w-full max-w-md bg-white shadow-md rounded px-8 py-6 border-4  border-purple-700"
                style={{ maxWidth: "24rem" }} // Define el tamaño máximo en rem para mayor consistencia
            >
                <h2 className="text-2xl font-bold mb-6 text-center">Registro</h2>

                {/* Espacio para el mensaje de error */}
                {errorMessage && (
                    <div className="text-red-600 text-sm mb-4 text-center">{errorMessage}</div>
                )}

                {/* Input para Nombre */}
                <div className="mb-4 relative flex items-center">
                    <FaUser className="absolute left-3 text-gray-500" size={20} />
                    <input
                        type="text"
                        name="firstName"
                        id="firstName"
                        value={formData.firstName}
                        onChange={handleChange}
                        className="shadow appearance-none border border-gray-300 rounded w-full py-2 pl-10 pr-3 text-gray-700 leading-tight transition-all duration-300 focus:outline-none focus:border-blue-600 focus:shadow-lg"
                        placeholder="Tu nombre"
                        required
                    />
                </div>

                {/* Input para Apellido */}
                <div className="mb-4 relative flex items-center">
                    <FaUser className="absolute left-3 text-gray-500" size={20} />
                    <input
                        type="text"
                        name="lastName"
                        id="lastName"
                        value={formData.lastName}
                        onChange={handleChange}
                        className="shadow appearance-none border border-gray-300 rounded w-full py-2 pl-10 pr-3 text-gray-700 leading-tight transition-all duration-300 focus:outline-none focus:border-blue-600 focus:shadow-lg"
                        placeholder="Tu apellido"
                        required
                    />
                </div>

                {/* Input para Email */}
                <div className="mb-4 relative flex items-center">
                    <FaEnvelope className="absolute left-3 text-gray-500" size={20} />
                    <input
                        type="email"
                        name="email"
                        id="email"
                        value={formData.email}
                        onChange={handleChange}
                        className="shadow appearance-none border border-gray-300 rounded w-full py-2 pl-10 pr-3 text-gray-700 leading-tight transition-all duration-300 focus:outline-none focus:border-blue-600 focus:shadow-lg"
                        placeholder="tuemail@ejemplo.com"
                        required
                    />
                </div>

                {/* Input para Contraseña */}
                <div className="mb-4 relative flex items-center">
                    <FaLock className="absolute left-3 text-gray-500" size={20} />
                    <input
                        type="password"
                        name="password"
                        id="password"
                        value={formData.password}
                        onChange={handleChange}
                        className="shadow appearance-none border border-gray-300 rounded w-full py-2 pl-10 pr-3 text-gray-700 leading-tight transition-all duration-300 focus:outline-none focus:border-blue-600 focus:shadow-lg"
                        placeholder="Contraseña"
                        required
                    />
                </div>

                {/* Input para Confirmar Contraseña */}
                <div className="mb-4 relative flex items-center">
                    <FaLock className="absolute left-3 text-gray-500" size={20} />
                    <input
                        type="password"
                        id="confirmPassword"
                        value={confirmPassword}
                        onChange={handleConfirmPasswordChange}
                        className="shadow appearance-none border border-gray-300 rounded w-full py-2 pl-10 pr-3 text-gray-700 leading-tight transition-all duration-300 focus:outline-none focus:border-blue-600 focus:shadow-lg"
                        placeholder="Confirmar contraseña"
                        required
                    />
                </div>

                {/* Input para Celular */}
                <div className="mb-4 relative flex items-center">
                    <FaPhone className="absolute left-3 text-gray-500" size={20} />
                    <input
                        type="text"
                        name="phone"
                        id="phone"
                        value={formData.phone}
                        onChange={handleChange}
                        className="shadow appearance-none border border-gray-300 rounded w-full py-2 pl-10 pr-3 text-gray-700 leading-tight transition-all duration-300 focus:outline-none focus:border-blue-600 focus:shadow-lg"
                        placeholder="Tu número de teléfono"
                        required
                    />
                </div>

                {/* Input para Dirección */}
                <div className="mb-4 relative flex items-center">
                    <FaMapMarkerAlt className="absolute left-3 text-gray-500" size={20} />
                    <input
                        type="text"
                        name="address"
                        id="address"
                        value={formData.address}
                        onChange={handleChange}
                        className="shadow appearance-none border border-gray-300 rounded w-full py-2 pl-10 pr-3 text-gray-700 leading-tight transition-all duration-300 focus:outline-none focus:border-blue-600 focus:shadow-lg"
                        placeholder="Tu dirección"
                        required
                    />
                </div>

                <button
                    type="submit"
                    className="bg-purple-600 hover:bg-purple-800 text-white font-bold py-2 px-4 rounded focus:outline-none focus:shadow-outline transform transition-transform duration-300 hover:scale-105"
                >
                    Registrarse
                </button>
            </form>
        </section>
    );
}
