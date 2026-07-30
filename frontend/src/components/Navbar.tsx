import { useEffect, useState } from "react";
import { useAuth } from "../context/AuthProvider";
import {
  FaRegUserCircle,
  FaHome,
  FaPlusCircle,
  FaThLarge,
} from "react-icons/fa";
import { Link, useLocation } from "react-router-dom";
import { usuario } from "../services/user/user";
import { DropdownMenu, DropdownMenuItem } from "./ui/DropdownMenu";
import { Button } from "./ui/Button";

const marketingLinkClass =
  "hover:text-primary transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2 rounded-card px-1";

const mainimage = "/img/logos_Mesa de trabajo 1 copia 3.png";

export default function Navbar() {
  const auth = useAuth();
  const location = useLocation();
  const [userName, setUserName] = useState<string | null>(null);

  const role = auth.role;

  useEffect(() => {
    const fetchUserInfo = async () => {
      try {
        const userInfo = await usuario.getMyInfo();
        setUserName(`${userInfo.firstname}`);
      } catch (error) {
        console.error("Error al obtener información del usuario:", error);
      }
    };

    if (auth.isAuthenticated) {
      fetchUserInfo();
    }
  }, [auth.isAuthenticated]);

  // 1. Variante de marketing: solo en "/" sin sesión iniciada.
  if (!auth.isAuthenticated && location.pathname === "/") {
    return (
      <nav className="sticky top-0 z-40 flex justify-between items-center bg-cream/95 backdrop-blur-md py-3 px-4 sm:px-8 shadow-sm transition-all">
        <Link to="/" className={`flex items-center gap-2 ${marketingLinkClass}`}>
          <img
            src={mainimage}
            alt="Market Exchange"
            className="h-14 w-auto object-contain flex-shrink-0"
          />
          <span className="leading-tight">
            <span className="block font-bold text-gray-900">market exchange</span>
            <span className="block text-caption text-gray-500">
              Intercambia. Reutiliza. Revoluciona.
            </span>
          </span>
        </Link>

        <div className="hidden md:flex items-center gap-8 text-sm font-semibold text-gray-700">
          <a href="/" className={`text-primary border-b-2 border-primary pb-1 ${marketingLinkClass}`}>
            Inicio
          </a>
          <a href="#nosotros" className={marketingLinkClass}>Sobre nosotros</a>
          <a href="#productos" className={marketingLinkClass}>Catálogo</a>
          <a href="#porque" className={marketingLinkClass}>¿Por qué elegirnos?</a>
          <a href="#contacto" className={marketingLinkClass}>Contacto</a>
        </div>

        <div className="flex items-center gap-4">
          <Link to="/login" className={`text-sm font-semibold ${marketingLinkClass}`}>
            Inicia sesión
          </Link>
          <Button asChild size="sm">
            <Link to="/register">Regístrate</Link>
          </Button>
        </div>
      </nav>
    );
  }

  // 2. Variante de autenticación (Login y Register)
  if (!auth.isAuthenticated && (location.pathname === "/login" || location.pathname === "/register")) {
    const isLogin = location.pathname === "/login";
    return (
      <nav className="sticky top-0 z-40 flex justify-between items-center bg-white py-3.5 px-4 sm:px-8 shadow-sm transition-all">
        <Link to="/" className="flex items-center gap-2 hover:opacity-95 transition-opacity">
          <img
            src={mainimage}
            alt="Market Exchange"
            className="h-12 sm:h-14 w-auto object-contain flex-shrink-0"
          />
          <span className="leading-tight">
            <span className="block font-bold text-gray-900 text-base sm:text-lg">market exchange</span>
            <span className="block text-caption text-gray-500">
              Intercambia. Reutiliza. Revoluciona.
            </span>
          </span>
        </Link>

        <div className="flex items-center gap-3 sm:gap-4">
          <span className="hidden sm:inline text-sm text-gray-700 font-semibold">
            {isLogin ? "¿No tienes cuenta?" : "¿Ya tienes cuenta?"}
          </span>
          <Button asChild size="md" className="rounded-full px-6">
            <Link to={isLogin ? "/register" : "/login"}>
              {isLogin ? "Regístrate" : "Inicia sesión"}
            </Link>
          </Button>
        </div>
      </nav>
    );
  }

  // 3. Navbar cuando el usuario está Autenticado / en Dashboard
  return (
    <nav className="sticky top-0 z-40 bg-white border-b border-gray-200/80 shadow-sm py-3 px-4 sm:px-8 flex items-center justify-between transition-all">
      {/* Sección Izquierda: Logo */}
      <Link to="/dashboard" className="flex items-center gap-2.5 hover:opacity-95 transition-opacity">
        <img
          src={mainimage}
          alt="Market Exchange"
          className="h-10 sm:h-11 w-auto object-contain flex-shrink-0"
        />
        <span className="leading-tight">
          <span className="block font-extrabold text-gray-900 text-base tracking-tight">market exchange</span>
          <span className="block text-[11px] text-gray-500 font-medium">Intercambia. Reutiliza. Revoluciona.</span>
        </span>
      </Link>

      {/* Sección Central: Links de navegación tipo pills */}
      <div className="hidden md:flex items-center gap-2 text-sm font-semibold">
        <Link
          to="/dashboard"
          className={`flex items-center gap-2 px-4 py-2 rounded-full transition-all ${
            location.pathname === "/dashboard"
              ? "bg-primary/10 text-primary font-bold"
              : "text-gray-600 hover:text-primary hover:bg-gray-100"
          }`}
        >
          <FaHome size={15} />
          <span>Inicio</span>
        </Link>

        {role === "USER" && (
          <Link
            to="/dashboard/item/create"
            className={`flex items-center gap-2 px-4 py-2 rounded-full transition-all ${
              location.pathname === "/dashboard/item/create"
                ? "bg-primary/10 text-primary font-bold"
                : "text-gray-600 hover:text-primary hover:bg-gray-100"
            }`}
          >
            <FaPlusCircle size={15} />
            <span>Publicar</span>
          </Link>
        )}

        <Link
          to="/dashboard/category"
          className={`flex items-center gap-2 px-4 py-2 rounded-full transition-all ${
            location.pathname.startsWith("/dashboard/category")
              ? "bg-primary/10 text-primary font-bold"
              : "text-gray-600 hover:text-primary hover:bg-gray-100"
          }`}
        >
          <FaThLarge size={15} />
          <span>Categorías</span>
        </Link>
      </div>

      {/* Sección Derecha: Usuario y Dropdown */}
      <div className="flex items-center gap-3">
        {auth.isAuthenticated ? (
          <DropdownMenu
            trigger={
              <button
                className="flex items-center gap-2.5 bg-primary/10 hover:bg-primary/20 text-primary font-bold py-1.5 px-3.5 rounded-full transition-all border border-primary/20"
                aria-label="Perfil de usuario"
              >
                <div className="w-8 h-8 rounded-full bg-primary text-white flex items-center justify-center text-sm font-bold shadow-sm">
                  {userName ? userName.charAt(0).toUpperCase() : <FaRegUserCircle />}
                </div>
                <div className="text-left leading-tight hidden sm:block">
                  <span className="block text-sm text-gray-900 font-bold">{userName || "Usuario"}</span>
                  <span className="block text-[10px] text-gray-500 font-semibold uppercase tracking-wider">
                    {role === "ADMIN" ? "Administrador" : "Usuario"}
                  </span>
                </div>
                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  className="h-4 w-4 text-primary ml-0.5"
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
              <Link to="/dashboard/cuenta" className="font-semibold">Cuenta</Link>
            </DropdownMenuItem>
            <DropdownMenuItem
              onSelect={() => auth.logout()}
              className="text-danger hover:bg-danger/10 font-semibold"
            >
              Cerrar sesión
            </DropdownMenuItem>
          </DropdownMenu>
        ) : (
          <div className="flex items-center gap-3">
            <Link to="/login" className="text-sm font-semibold text-gray-700 hover:text-primary transition-colors">
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
