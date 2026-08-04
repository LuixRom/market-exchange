import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthProvider";
import { RegisterRequest } from "../interfaces/auth/RegisterRequest";
import RegisterForm from "../components/RegisterForm";
import AuthFooter from "../components/AuthFooter";
import { register } from "../services/auth/register";

export default function RegisterPage() {
    const navigate = useNavigate();
    const auth = useAuth();
    const [successMessage, setSuccessMessage] = useState<string | null>(null);
    const [formData, setFormData] = useState<RegisterRequest>({
        firstName: "",
        lastName: "",
        email: "",
        password: "",
        phone: "",
        address: "",
    });

    useEffect(() => {
        if (auth.isAuthenticated) {
            navigate("/dashboard", { replace: true });
        }
    }, [auth.isAuthenticated, navigate]);

    async function handleRegisterSubmit(data: RegisterRequest) {
        try {
            await register(data);
            setSuccessMessage("Cuenta creada. Revisa tu correo para verificarla antes de iniciar sesion.");
        } catch {
            alert("Error al registrar. Intentalo de nuevo.");
        }
    }

    return (
        <div className="bg-cream flex flex-col min-h-[calc(100vh-80px)] justify-between">
            <div className="flex-grow">
                {successMessage && (
                    <div className="max-w-xl mx-auto mt-8 px-4">
                        <div className="bg-emerald-50 border border-emerald-200 text-emerald-800 rounded-2xl p-4 text-sm font-semibold">
                            <p>{successMessage}</p>
                            <button
                                type="button"
                                onClick={() => navigate("/login")}
                                className="mt-3 text-primary font-bold hover:underline"
                            >
                                Ir al login
                            </button>
                        </div>
                    </div>
                )}
                <RegisterForm
                    formData={formData}
                    setFormData={setFormData}
                    onSubmit={handleRegisterSubmit}
                />
            </div>
            <AuthFooter />
        </div>
    );
}
