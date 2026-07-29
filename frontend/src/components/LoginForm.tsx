import { useState, useEffect, useRef, ChangeEvent, FormEvent } from "react";
import axios from "axios";
import { AnimatePresence, motion } from "framer-motion";
import { useAuth } from "../context/AuthProvider";
import { useNavigate, Navigate } from "react-router-dom";
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
import { Card } from "./ui/Card";
import { Input } from "./ui/Input";
import { Button } from "./ui/Button";
import { slideUp } from "../lib/motion";

const idleImages: string[] = [idle1, idle2, idle3, idle4, idle5];
const coverImages: string[] = [cover1, cover2, cover3, cover4, cover5, cover6, cover7, cover8];

export default function LoginForm() {
    const [formData, setFormData] = useState<LoginRequest>({ username: "", password: "" });
    const [monsterSrc, setMonsterSrc] = useState<string>(idleImages[0]);
    const [error, setError] = useState<string | null>(null);
    const [imagesLoaded, setImagesLoaded] = useState<boolean>(false); // Estado para controlar precarga de imágenes
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
                        setImagesLoaded(true); // Todas las imágenes han sido cargadas
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

        loop(); // Iniciar la animación
    };

    // Manejo del foco en el input de contraseña
    const handleClaveFocus = () => {
        clearTimeouts();
        animateImages(coverImages, 40); // Animación fluida al enfocar
    };

    const handleClaveBlur = () => {
        clearTimeouts();
        animateImages(coverImages, 40, true); // Animación inversa al desenfocar
    };

    // Limpiar timeouts al desmontar el componente o cancelar animación
    const clearTimeouts = () => {
        if (currentTimeout.current !== null) {
            window.clearTimeout(currentTimeout.current);
            currentTimeout.current = null;
        }
    };

    useEffect(() => {
        return () => clearTimeouts(); // Limpiar timeouts al desmontar
    }, []);

    // Manejar cambios en los inputs
    const handleChange = (e: ChangeEvent<HTMLInputElement>) => {
        const { name, value } = e.target;
        setFormData((prevFormData) => ({
            ...prevFormData,
            [name]: value,
        }));
    };

    // Manejar envío del formulario
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
            const message = axios.isAxiosError(error) && error.response?.data?.message
                ? error.response.data.message
                : "Credenciales incorrectas. Intenta nuevamente.";
            setError(message);
        }
    };

    if (auth.isAuthenticated) {
        return <Navigate to="/dashboard" />;
    }

    // Mostrar un loader mientras se precargan las imágenes
    if (!imagesLoaded) {
        return <div className="flex items-center justify-center h-screen">Cargando...</div>;
    }

    return (
        <div className="flex flex-col items-center justify-center relative py-10">
            <img src={monsterSrc} alt="Monster" className="w-64 h-64 mb-8" />
            <motion.div
                className="w-full max-w-md px-4"
                variants={slideUp}
                initial="hidden"
                animate="visible"
                transition={{ duration: 0.3 }}
            >
                <Card className="p-8 border-0 shadow-lg">
                    <form onSubmit={handleSubmit}>
                        <h2 className="text-2xl font-bold mb-6 text-center">Login</h2>
                        <AnimatePresence>
                            {error && (
                                <motion.p
                                    className="text-danger mb-4 text-sm text-center"
                                    initial={{ opacity: 0, height: 0 }}
                                    animate={{ opacity: 1, height: "auto" }}
                                    exit={{ opacity: 0, height: 0 }}
                                    transition={{ duration: 0.2 }}
                                >
                                    {error}
                                </motion.p>
                            )}
                        </AnimatePresence>
                        <div className="mb-4">
                            <label htmlFor="username" className="block text-gray-700 text-sm font-bold mb-2">
                                Usuario
                            </label>
                            <Input
                                type="text"
                                name="username"
                                id="username"
                                value={formData.username}
                                onChange={handleChange}
                                placeholder="mounstrito.gracioso@gmail.com"
                                required
                            />
                        </div>
                        <div className="mb-6">
                            <label htmlFor="password" className="block text-gray-700 text-sm font-bold mb-2">
                                Contraseña
                            </label>
                            <Input
                                type="password"
                                name="password"
                                id="password"
                                value={formData.password}
                                onChange={handleChange}
                                onFocus={handleClaveFocus}
                                onBlur={handleClaveBlur}
                                placeholder="***"
                                required
                            />
                        </div>
                        <Button type="submit" variant="primary" size="lg" className="w-full">
                            Login
                        </Button>
                    </form>
                </Card>
            </motion.div>
        </div>
    );
}
