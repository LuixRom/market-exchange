import { createContext, useState, useEffect, useContext } from "react";
import type { AuthResponse } from "../interfaces/auth/AuthResponse";
import extractRoleFromToken from "../jwt/jwt";
import Api from "../apis/api";

interface AuthProviderProps {
    children: React.ReactNode;
}

interface AuthContextType {
    isAuthenticated: boolean;
    role: string | null;
    getAccessToken: () => string | null;
    saveUser: (userData: AuthResponse) => void;
    logout: () => void;
}

const AuthContext = createContext<AuthContextType>({
    isAuthenticated: false,
    role: null,
    getAccessToken: () => null,
    saveUser: () => {},
    logout: () => {}
});

export function AuthProvider({ children }: AuthProviderProps) {
    const [isAuthenticated, setIsAuthenticated] = useState(false);
    const [accessToken, setAccessToken] = useState<string | null>(null);
    const [role, setRole] = useState<string | null>(null);

    useEffect(() => {
        const token = localStorage.getItem("accessToken");
        if (token) {
            setAccessToken(token);
            setIsAuthenticated(true);
            setRole(extractRoleFromToken(token) || "USER");
        }
    }, []);

    function saveUser(userData: AuthResponse) {
        const token = userData.token;
        setAccessToken(token);
        localStorage.setItem("accessToken", token);
        setIsAuthenticated(true);
        setRole(extractRoleFromToken(token) || "USER");
    }

    function logout() {
        setAccessToken(null);
        setRole(null);
        localStorage.removeItem("accessToken");
        setIsAuthenticated(false);
        Api.clearAuthorization();
    }

    function getAccessToken() {
        return accessToken;
    }

    return (
        <AuthContext.Provider value={{ isAuthenticated, role, getAccessToken, saveUser, logout }}>
            {children}
        </AuthContext.Provider>
    );
}

export const useAuth = () => useContext(AuthContext);