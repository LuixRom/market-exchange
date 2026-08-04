import { createContext, useState, useEffect, useContext } from "react";
import type { AuthResponse } from "../interfaces/auth/AuthResponse";
import extractRoleFromToken from "../jwt/jwt";
import Api from "../apis/api";
import { logoutSession } from "../services/auth/session";

interface AuthProviderProps {
    children: React.ReactNode;
}

interface AuthContextType {
    isAuthenticated: boolean;
    role: string | null;
    emailVerified: boolean;
    getAccessToken: () => string | null;
    getRefreshToken: () => string | null;
    saveUser: (userData: AuthResponse) => void;
    logout: () => void;
}

const AuthContext = createContext<AuthContextType>({
    isAuthenticated: false,
    role: null,
    emailVerified: false,
    getAccessToken: () => null,
    getRefreshToken: () => null,
    saveUser: () => {},
    logout: () => {}
});

export function AuthProvider({ children }: AuthProviderProps) {
    const [isAuthenticated, setIsAuthenticated] = useState(false);
    const [accessToken, setAccessToken] = useState<string | null>(null);
    const [refreshToken, setRefreshToken] = useState<string | null>(null);
    const [role, setRole] = useState<string | null>(null);
    const [emailVerified, setEmailVerified] = useState(false);

    useEffect(() => {
        const token = localStorage.getItem("accessToken");
        const storedRefreshToken = localStorage.getItem("refreshToken");
        const storedEmailVerified = localStorage.getItem("emailVerified") === "true";
        if (token) {
            setAccessToken(token);
            setRefreshToken(storedRefreshToken);
            setIsAuthenticated(true);
            setRole(extractRoleFromToken(token) || "USER");
            setEmailVerified(storedEmailVerified);
        }
    }, []);

    function saveUser(userData: AuthResponse) {
        const token = userData.token;
        if (!token) {
            return;
        }
        setAccessToken(token);
        localStorage.setItem("accessToken", token);
        if (userData.refreshToken) {
            setRefreshToken(userData.refreshToken);
            localStorage.setItem("refreshToken", userData.refreshToken);
        }
        const verified = userData.emailVerified ?? true;
        setEmailVerified(verified);
        localStorage.setItem("emailVerified", String(verified));
        setIsAuthenticated(true);
        setRole(extractRoleFromToken(token) || "USER");
    }

    async function logout() {
        const tokenToRevoke = refreshToken || localStorage.getItem("refreshToken");
        if (tokenToRevoke) {
            try {
                await logoutSession(tokenToRevoke);
            } catch (error) {
                console.warn("No se pudo cerrar la sesion en backend:", error);
            }
        }
        setAccessToken(null);
        setRefreshToken(null);
        setRole(null);
        localStorage.removeItem("accessToken");
        localStorage.removeItem("refreshToken");
        localStorage.removeItem("emailVerified");
        setIsAuthenticated(false);
        setEmailVerified(false);
        Api.clearAuthorization();
    }

    function getAccessToken() {
        return accessToken;
    }

    function getRefreshToken() {
        return refreshToken;
    }

    return (
        <AuthContext.Provider value={{ isAuthenticated, role, emailVerified, getAccessToken, getRefreshToken, saveUser, logout }}>
            {children}
        </AuthContext.Provider>
    );
}

export const useAuth = () => useContext(AuthContext);
