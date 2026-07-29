import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthProvider"; // Importa useAuth para acceder al contexto
import { RegisterRequest } from "../interfaces/auth/RegisterRequest";
import RegisterForm from "../components/RegisterForm";
import { register } from "../services/auth/register";

export default function RegisterPage() {
    const navigate = useNavigate();
    const auth = useAuth(); 
    const [formData, setFormData] = useState<RegisterRequest>({
        firstName: "",
        lastName: "",
        email: "",
        password: "",
        phone: "",
        address: "",
    });

    // Verifica si el usuario ya está autenticado y redirige al Dashboard
    useEffect(() => {
        if (auth.isAuthenticated) {
            navigate("/dashboard", { replace: true }); 
        }
    }, [auth.isAuthenticated, navigate]);

    async function handleRegisterSubmit(data: RegisterRequest) {
        try {
            await register(data);
            navigate("/login");
        } catch {
            alert("Error al registrar. Inténtalo de nuevo.");
        }
    }

    return (
        <RegisterForm
            formData={formData}
            setFormData={setFormData}
            onSubmit={handleRegisterSubmit}
        />
    );
}
