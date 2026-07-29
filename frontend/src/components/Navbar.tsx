import React, { useEffect, useState } from "react";
import { useAuth } from "../context/AuthProvider";
import { FaRegUserCircle } from "react-icons/fa";
import { Link } from "react-router-dom";
import { usuario } from "../services/user/user"; // Servicio de usuario

export default function Navbar() {
    
    const auth = useAuth();
    const [isDropdownOpen, setIsDropdownOpen] = useState(false);
    const [userName, setUserName] = useState<string | null>(null);

    const toggleDropdown = () => setIsDropdownOpen((prev) => !prev);
    const closeDropdown = () => setIsDropdownOpen(false);

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
                                <Link to="/dashboard" className="hover:text-purple-500">
                                    MarketExchange
                                </Link>
                                <Link to="/dashboard/item/create" className="hover:text-purple-500">
                                    Publicar
                                </Link>
                                <Link to="/dashboard/category" className="hover:text-purple-500">
                                    Categorías
                                </Link>
                            </>
                        )}
                        {role === "ADMIN" && (
                            <>
                                <Link to="/dashboard" className="hover:text-purple-500">
                                    MarketExchange
                                </Link>
                             
                                <Link to="/dashboard/category" className="hover:text-purple-500">
                                    Categorías
                                </Link>
                            </>
                        )}
                    </>
                ) : (
                    <>
                        <Link to="/" className="hover:text-purple-500">
                            MarketExchange
                        </Link>
                    </>
                )}
            </div>
    
            {/* Sección derecha del navbar */}
            <div className="flex items-center space-x-4">
                {auth.isAuthenticated ? (
                    <div className="relative">
                        {/* Botón del dropdown */}
                        <button
                            className="flex items-center bg-purple-100 py-2 px-4 rounded-full hover:bg-purple-200 transition"
                            onClick={toggleDropdown}
                            aria-expanded={isDropdownOpen}
                            aria-label="Perfil"
                        >
                            <FaRegUserCircle className="mr-2" />
                            <p>{userName ? userName : "Perfil"}</p>
                            {/* Icono de flecha */}
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
    
                        {/* Dropdown */}
                        {isDropdownOpen && (
                            <div className="absolute right-0 mt-2 w-48 bg-white text-gray-800 rounded-lg shadow-lg z-10">
                                <Link
                                    to="/dashboard/cuenta"
                                    onClick={closeDropdown}
                                    className="block px-4 py-2 hover:bg-gray-100"
                                >
                                    Cuenta
                                </Link>
                                <button
                                    onClick={() => {
                                        closeDropdown();
                                        auth.logout();
                                    }}
                                    className="block w-full text-left px-4 py-2 hover:bg-gray-100"
                                >
                                    Cerrar sesión
                                </button>
                            </div>
                        )}
                    </div>
                ) : (
                    <div className="flex items-center space-x-4">
                    <Link to="/login" className="hover:text-purple-500 text-sm font-semibold">
                        Inicia sesión
                    </Link>
                    <Link to="/register" className="bg-purple-700 text-white py-2 px-4 rounded-full hover:bg-purple-800 text-sm font-semibold">
                        Regístrate
                    </Link>
                </div>
                
                )}
            </div>
        </nav>
    );
}