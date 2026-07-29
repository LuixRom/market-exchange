import { useEffect, useState } from "react";
import { useAuth } from "../context/AuthProvider";
import { FaRegUserCircle } from "react-icons/fa";
import { Link } from "react-router-dom";
import { usuario } from "../services/user/user"; // Servicio de usuario
import { DropdownMenu, DropdownMenuItem } from "./ui/DropdownMenu";
import { Button } from "./ui/Button";

export default function Navbar() {
    const auth = useAuth();
    const [userName, setUserName] = useState<string | null>(null);

    const role = auth.role;

    useEffect(() => {
        const fetchUserInfo = async () => {
            try {
                const userInfo = await usuario.getMyInfo();
                setUserName(`${userInfo.firstname}`); // Establece el nombre completo
            } catch (error) {
                console.error("Error al obtener información del usuario:", error);
            }
        };

        if (auth.isAuthenticated) {
            fetchUserInfo();
        }
    }, [auth.isAuthenticated]);

    return (
        <nav className="flex justify-between items-center bg-white text-gris-700 py-4 px-8 shadow">
            {/* Sección izquierda del navbar */}
            <div className="flex space-x-8 text-sm font-semibold">
                {auth.isAuthenticated ? (
                    <>
                        {role === "USER" && (
                            <>
                                <Link to="/dashboard" className="hover:text-primary transition-colors">
                                    MarketExchange
                                </Link>
                                <Link to="/dashboard/item/create" className="hover:text-primary transition-colors">
                                    Publicar
                                </Link>
                                <Link to="/dashboard/category" className="hover:text-primary transition-colors">
                                    Categorías
                                </Link>
                            </>
                        )}
                        {role === "ADMIN" && (
                            <>
                                <Link to="/dashboard" className="hover:text-primary transition-colors">
                                    MarketExchange
                                </Link>

                                <Link to="/dashboard/category" className="hover:text-primary transition-colors">
                                    Categorías
                                </Link>
                            </>
                        )}
                    </>
                ) : (
                    <>
                        <Link to="/" className="hover:text-primary transition-colors">
                            MarketExchange
                        </Link>
                    </>
                )}
            </div>

            {/* Sección derecha del navbar */}
            <div className="flex items-center space-x-4">
                {auth.isAuthenticated ? (
                    <DropdownMenu
                        trigger={
                            <button
                                className="flex items-center bg-primary/10 text-primary py-2 px-4 rounded-full hover:bg-primary/20 transition-colors"
                                aria-label="Perfil"
                            >
                                <FaRegUserCircle className="mr-2" />
                                <span>{userName ? userName : "Perfil"}</span>
                                <svg
                                    xmlns="http://www.w3.org/2000/svg"
                                    className="h-5 w-5 ml-2"
                                    viewBox="0 0 20 20"
                                    fill="currentColor"
                                >
                                    <path
                                        fillRule="evenodd"
                                        d="M5.293 9.293a1 1 0 011.414 0L10 12.586l3.293-3.293a1 1 0 011.414 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414z"
                                        clipRule="evenodd"
                                    />
                                </svg>
                            </button>
                        }
                    >
                        <DropdownMenuItem asChild>
                            <Link to="/dashboard/cuenta">Cuenta</Link>
                        </DropdownMenuItem>
                        <DropdownMenuItem
                            onSelect={() => auth.logout()}
                            className="text-danger hover:bg-danger/10"
                        >
                            Cerrar sesión
                        </DropdownMenuItem>
                    </DropdownMenu>
                ) : (
                    <div className="flex items-center space-x-4">
                        <Link to="/login" className="hover:text-primary text-sm font-semibold transition-colors">
                            Inicia sesión
                        </Link>
                        <Button asChild size="sm">
                            <Link to="/register">Regístrate</Link>
                        </Button>
                    </div>
                )}
            </div>
        </nav>
    );
}
