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
            navigate("/login");
        } catch {
            alert("Error al registrar. Inténtalo de nuevo.");
        }
    }

    return (
        <div className="bg-cream flex flex-col min-h-[calc(100vh-80px)] justify-between">
            <div className="flex-grow">
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
