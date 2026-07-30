import { useState, useEffect, useRef, ChangeEvent, FormEvent } from "react";
import axios from "axios";
import { AnimatePresence, motion } from "framer-motion";
import { useAuth } from "../context/AuthProvider";
import { useNavigate, Navigate, Link } from "react-router-dom";
import {
  FaEnvelope,
  FaLock,
  FaEye,
  FaEyeSlash,
  FaExchangeAlt,
  FaLeaf,
  FaUsers,
} from "react-icons/fa";
import idle1 from "../assets/img/idle/1.png";
import idle2 from "../assets/img/idle/2.png";
import idle3 from "../assets/img/idle/3.png";
import idle4 from "../assets/img/idle/4.png";
import idle5 from "../assets/img/idle/5.png";
import cover1 from "../assets/img/cover/1.png";
import cover2 from "../assets/img/cover/2.png";
import cover3 from "../assets/img/cover/3.png";
import cover4 from "../assets/img/cover/4.png";
import cover5 from "../assets/img/cover/5.png";
import cover6 from "../assets/img/cover/6.png";
import cover7 from "../assets/img/cover/7.png";
import cover8 from "../assets/img/cover/8.png";
import { LoginRequest } from "../interfaces/auth/LoginRequest";
import { login } from "../services/auth/login";
import { Button } from "./ui/Button";
import { slideUp } from "../lib/motion";

const idleImages: string[] = [idle1, idle2, idle3, idle4, idle5];
const coverImages: string[] = [cover1, cover2, cover3, cover4, cover5, cover6, cover7, cover8];
const mainLogo = "/img/logos_Mesa de trabajo 1 copia 3.png";

export default function LoginForm() {
  const [formData, setFormData] = useState<LoginRequest>({ username: "", password: "" });
  const [monsterSrc, setMonsterSrc] = useState<string>(idleImages[0]);
  const [error, setError] = useState<string | null>(null);
  const [imagesLoaded, setImagesLoaded] = useState<boolean>(false);
  const [showPassword, setShowPassword] = useState<boolean>(false);
  const [rememberMe, setRememberMe] = useState<boolean>(false);
  const currentTimeout = useRef<number | null>(null);

  const auth = useAuth();
  const navigate = useNavigate();

  // Precargar imágenes al cargar el componente
  useEffect(() => {
    const preloadImages = (images: string[]) => {
      let loadedImages = 0;
      const totalImages = images.length;
      images.forEach((src) => {
        const img = new Image();
        img.src = src;
        img.onload = () => {
          loadedImages++;
          if (loadedImages === totalImages) {
            setImagesLoaded(true);
          }
        };
      });
    };

    preloadImages([...idleImages, ...coverImages]);
  }, []);

  // Función para recorrer imágenes suavemente
  const animateImages = (images: string[], delay: number, reverse = false) => {
    let index = reverse ? images.length - 1 : 0;

    const loop = () => {
      setMonsterSrc(images[index]);
      index = reverse ? index - 1 : index + 1;

      if ((reverse && index >= 0) || (!reverse && index < images.length)) {
        currentTimeout.current = window.setTimeout(loop, delay);
      }
    };

    loop();
  };

  // Manejo del foco en el input de contraseña
  const handleClaveFocus = () => {
    clearTimeouts();
    animateImages(coverImages, 40);
  };

  const handleClaveBlur = () => {
    clearTimeouts();
    animateImages(coverImages, 40, true);
  };

  const clearTimeouts = () => {
    if (currentTimeout.current !== null) {
      window.clearTimeout(currentTimeout.current);
      currentTimeout.current = null;
    }
  };

  useEffect(() => {
    return () => clearTimeouts();
  }, []);

  const handleChange = (e: ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData((prevFormData) => ({
      ...prevFormData,
      [name]: value,
    }));
  };

  const handleSubmit = async (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    try {
      const data = await login(formData);
      setError(null);

      if (data.data.token) {
        auth.saveUser(data.data);
        navigate("/dashboard");
      }
    } catch (error) {
      const message =
        axios.isAxiosError(error) && error.response?.data?.message
          ? error.response.data.message
          : "Credenciales incorrectas. Intenta nuevamente.";
      setError(message);
    }
  };

  if (auth.isAuthenticated) {
    return <Navigate to="/dashboard" />;
  }

  if (!imagesLoaded) {
    return (
      <div className="flex items-center justify-center min-h-screen bg-cream text-primary font-bold text-lg">
        Cargando...
      </div>
    );
  }

  return (
    <div className="w-full max-w-container mx-auto px-4 sm:px-8 py-8 flex-grow flex flex-col justify-center gap-10">
        <div className="grid lg:grid-cols-12 gap-8 lg:gap-12 items-center">
          {/* Columna Izquierda: Chico de la caja animado */}
          <div className="lg:col-span-6 relative flex justify-center items-center py-4">
            {/* Hojas decorativas de fondo */}
            <FaLeaf className="absolute top-2 left-8 text-primary/10 text-7xl -rotate-12 pointer-events-none" />
            <FaLeaf className="absolute bottom-2 left-12 text-primary/10 text-6xl rotate-45 pointer-events-none" />
            
            {/* Círculo morado suave de fondo */}
            <div className="relative w-80 h-80 sm:w-96 sm:h-96 md:w-[420px] md:h-[420px] bg-primary/15 rounded-full flex items-center justify-center">
              <img
                src={monsterSrc}
                alt="Ilustración usuario con caja"
                className="w-full max-w-[340px] sm:max-w-[400px] md:max-w-[440px] h-auto object-contain z-10"
              />
            </div>
          </div>

          {/* Columna Derecha: Tarjeta de Login */}
          <div className="lg:col-span-6 flex justify-center">
            <motion.div
              className="w-full max-w-lg bg-white rounded-3xl shadow-xl p-8 sm:p-10 border border-gray-100"
              variants={slideUp}
              initial="hidden"
              animate="visible"
              transition={{ duration: 0.3 }}
            >
              <div className="inline-block bg-primary/10 text-primary font-bold text-xs px-3.5 py-1 rounded-full mb-3">
                ¡Bienvenido de vuelta!
              </div>

              <h1 className="text-2xl sm:text-3xl font-extrabold text-gray-900 tracking-tight">
                Inicia sesión en tu cuenta
              </h1>
              <p className="mt-2 text-sm text-gray-600">
                Nos alegra verte de nuevo. Ingresa tus datos para continuar.
              </p>

              <form onSubmit={handleSubmit} className="mt-6 space-y-5">
                <AnimatePresence>
                  {error && (
                    <motion.div
                      className="p-3 bg-danger/10 border border-danger/20 text-danger text-sm rounded-xl font-semibold text-center"
                      initial={{ opacity: 0, height: 0 }}
                      animate={{ opacity: 1, height: "auto" }}
                      exit={{ opacity: 0, height: 0 }}
                    >
                      {error}
                    </motion.div>
                  )}
                </AnimatePresence>

                <div>
                  <label htmlFor="username" className="block text-sm font-bold text-gray-700 mb-1.5">
                    Correo electrónico
                  </label>
                  <div className="relative">
                    <FaEnvelope className="absolute left-3.5 top-1/2 -translate-y-1/2 text-gray-400 text-base pointer-events-none" />
                    <input
                      type="text"
                      name="username"
                      id="username"
                      value={formData.username}
                      onChange={handleChange}
                      placeholder="ejemplo@correo.com"
                      className="w-full pl-10 pr-4 py-3 text-sm bg-white border border-gray-300 rounded-xl focus:ring-2 focus:ring-primary focus:border-transparent outline-none transition-all text-gray-900"
                      required
                    />
                  </div>
                </div>

                <div>
                  <label htmlFor="password" className="block text-sm font-bold text-gray-700 mb-1.5">
                    Contraseña
                  </label>
                  <div className="relative">
                    <FaLock className="absolute left-3.5 top-1/2 -translate-y-1/2 text-gray-400 text-base pointer-events-none" />
                    <input
                      type={showPassword ? "text" : "password"}
                      name="password"
                      id="password"
                      value={formData.password}
                      onChange={handleChange}
                      onFocus={handleClaveFocus}
                      onBlur={handleClaveBlur}
                      placeholder="••••••••"
                      className="w-full pl-10 pr-12 py-3 text-sm bg-white border border-gray-300 rounded-xl focus:ring-2 focus:ring-primary focus:border-transparent outline-none transition-all text-gray-900"
                      required
                    />
                    <button
                      type="button"
                      onClick={() => setShowPassword(!showPassword)}
                      className="absolute right-3.5 top-1/2 -translate-y-1/2 text-gray-400 hover:text-primary transition-colors focus:outline-none p-1"
                      aria-label={showPassword ? "Ocultar contraseña" : "Mostrar contraseña"}
                    >
                      {showPassword ? <FaEyeSlash size={16} /> : <FaEye size={16} />}
                    </button>
                  </div>
                </div>

                <div className="flex items-center justify-between text-xs sm:text-sm pt-1">
                  <label className="flex items-center gap-2 cursor-pointer select-none text-gray-700 font-medium">
                    <input
                      type="checkbox"
                      checked={rememberMe}
                      onChange={(e) => setRememberMe(e.target.checked)}
                      className="w-4 h-4 rounded text-primary focus:ring-primary border-gray-300 accent-primary"
                    />
                    <span>Recordarme</span>
                  </label>
                  <a href="#" className="font-bold text-primary hover:underline">
                    ¿Olvidaste tu contraseña?
                  </a>
                </div>

                <Button type="submit" size="lg" className="w-full py-3.5 text-base font-bold rounded-xl mt-2">
                  Iniciar sesión
                </Button>
              </form>

              <p className="mt-6 text-center text-sm text-gray-600">
                ¿No tienes cuenta?{" "}
                <Link to="/register" className="font-bold text-primary hover:underline">
                  Regístrate
                </Link>
              </p>
            </motion.div>
          </div>
        </div>

        {/* Banner Inferior: Intercambia, Reutiliza, Revoluciona */}
        <div className="bg-white rounded-2xl shadow-md border border-gray-100 p-6 sm:p-8 grid grid-cols-1 md:grid-cols-3 gap-6">
          <div className="flex items-center gap-4">
            <div className="w-12 h-12 rounded-full bg-primary/10 flex items-center justify-center text-primary flex-shrink-0">
              <FaExchangeAlt size={20} />
            </div>
            <div>
              <h3 className="font-bold text-gray-900">Intercambia</h3>
              <p className="text-xs text-gray-600">Dale una nueva vida a lo que ya no usas.</p>
            </div>
          </div>

          <div className="flex items-center gap-4">
            <div className="w-12 h-12 rounded-full bg-primary/10 flex items-center justify-center text-primary flex-shrink-0">
              <FaLeaf size={20} />
            </div>
            <div>
              <h3 className="font-bold text-gray-900">Reutiliza</h3>
              <p className="text-xs text-gray-600">Reduce el desperdicio y cuida nuestro planeta.</p>
            </div>
          </div>

          <div className="flex items-center gap-4">
            <div className="w-12 h-12 rounded-full bg-primary/10 flex items-center justify-center text-primary flex-shrink-0">
              <FaUsers size={20} />
            </div>
            <div>
              <h3 className="font-bold text-gray-900">Revoluciona</h3>
              <p className="text-xs text-gray-600">Conecta con personas y transforma tu comunidad.</p>
            </div>
          </div>
        </div>
    </div>
  );
}
