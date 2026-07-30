import LoginForm from "../components/LoginForm";
import AuthFooter from "../components/AuthFooter";
import { Navigate } from "react-router-dom";
import { useAuth } from "../context/AuthProvider";

export default function LoginPage() {
    const auth = useAuth();
    if (auth.isAuthenticated) {
        return <Navigate to="/dashboard" />;
    }
    return (
        <div className="bg-cream flex flex-col min-h-[calc(100vh-80px)] justify-between">
            <div className="flex-grow flex flex-col justify-center">
                <LoginForm />
            </div>
            <AuthFooter />
        </div>
    );
}