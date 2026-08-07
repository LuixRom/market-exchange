import { createContext, useCallback, useContext, useEffect, useMemo, useState } from "react";
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

export function AuthProvider({ children }: Readonly<AuthProviderProps>) {
    const [isAuthenticated, setIsAuthenticated] = useState(false);
    const [accessToken, setAccessToken] = useState<string | null>(null);
    const [refreshToken, setRefreshToken] = useState<string | null>(null);
    const [role, setRole] = useState<string | null>(null);
    const [emailVerified, setEmailVerified] = useState(false);

    useEffect(() => {
        const token = sessionStorage.getItem("accessToken");
        const storedRefreshToken = sessionStorage.getItem("refreshToken");
        const storedEmailVerified = sessionStorage.getItem("emailVerified") === "true";
        if (token) {
            setAccessToken(token);
            setRefreshToken(storedRefreshToken);
            setIsAuthenticated(true);
            setRole(extractRoleFromToken(token) || "USER");
            setEmailVerified(storedEmailVerified);
        }
    }, []);

    const saveUser = useCallback((userData: AuthResponse) => {
        const token = userData.token;
        if (!token) {
            return;
        }
        setAccessToken(token);
        sessionStorage.setItem("accessToken", token);
        if (userData.refreshToken) {
            setRefreshToken(userData.refreshToken);
            sessionStorage.setItem("refreshToken", userData.refreshToken);
        }
        const verified = userData.emailVerified ?? true;
        setEmailVerified(verified);
        sessionStorage.setItem("emailVerified", String(verified));
        setIsAuthenticated(true);
        setRole(extractRoleFromToken(token) || "USER");
    }, []);

    const logout = useCallback(async () => {
        const tokenToRevoke = refreshToken || sessionStorage.getItem("refreshToken");
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
        sessionStorage.removeItem("accessToken");
        sessionStorage.removeItem("refreshToken");
        sessionStorage.removeItem("emailVerified");
        setIsAuthenticated(false);
        setEmailVerified(false);
        Api.clearAuthorization();
    }, [refreshToken]);

    const getAccessToken = useCallback(() => accessToken, [accessToken]);

    const getRefreshToken = useCallback(() => refreshToken, [refreshToken]);

    const contextValue = useMemo(
        () => ({ isAuthenticated, role, emailVerified, getAccessToken, getRefreshToken, saveUser, logout }),
        [isAuthenticated, role, emailVerified, getAccessToken, getRefreshToken, saveUser, logout]
    );

    return (
        <AuthContext.Provider value={contextValue}>
            {children}
        </AuthContext.Provider>
    );
}

export const useAuth = () => useContext(AuthContext);
