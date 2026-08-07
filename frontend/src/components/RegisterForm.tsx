import { ChangeEvent, FormEvent, useState } from "react";
import { AnimatePresence, motion } from "framer-motion";
import { RegisterRequest } from "../interfaces/auth/RegisterRequest";
import {
  FaUser,
  FaEnvelope,
  FaLock,
  FaPhone,
  FaMapMarkerAlt,
  FaEye,
  FaEyeSlash,
  FaExchangeAlt,
  FaLeaf,
  FaUsers,
} from "react-icons/fa";
import { Link } from "react-router-dom";
import { Button } from "./ui/Button";
import { slideUp } from "../lib/motion";

interface RegisterFormProps {
  formData: RegisterRequest;
  setFormData: React.Dispatch<React.SetStateAction<RegisterRequest>>;
  onSubmit: (data: RegisterRequest) => Promise<void>;
}

const caja2Img = "/img/caja2.png";

export default function RegisterForm({ formData, setFormData, onSubmit }: Readonly<RegisterFormProps>) {
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [confirmPassword, setConfirmPassword] = useState<string>("");
  const [showPassword, setShowPassword] = useState<boolean>(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState<boolean>(false);

  function handleChange(e: ChangeEvent<HTMLInputElement>) {
    const { name, value } = e.target;
    setFormData((prevData) => ({
      ...prevData,
      [name]: value,
    }));
  }

  function handleConfirmPasswordChange(e: ChangeEvent<HTMLInputElement>) {
    setConfirmPassword(e.target.value);
  }

  async function handleSubmit(e: FormEvent<HTMLFormElement>) {
    e.preventDefault();
    setErrorMessage(null);

    if (formData.password.length < 8) {
      setErrorMessage("La contraseña debe tener al menos 8 caracteres");
      return;
    }

    if (formData.password !== confirmPassword) {
      setErrorMessage("Las contraseñas no coinciden");
      return;
    }

    await onSubmit(formData);
  }

  return (
    <div className="w-full max-w-container mx-auto px-4 sm:px-8 py-8 flex-grow flex flex-col justify-center gap-10">
      <div className="grid lg:grid-cols-12 gap-8 lg:gap-12 items-center">
        {/* Columna Izquierda: Ilustración y título de bienvenida */}
        <div className="lg:col-span-5 flex flex-col items-center lg:items-start text-center lg:text-left py-4">
          <div className="mb-6">
            <h2 className="text-3xl sm:text-4xl font-extrabold text-gray-900 flex items-center justify-center lg:justify-start gap-2 tracking-tight">
              <FaLeaf className="text-primary text-2xl sm:text-3xl" aria-hidden="true" />
              <span>
                Únete al <span className="text-primary">cambio</span>
              </span>
            </h2>
            <p className="mt-3 text-sm sm:text-base text-gray-600 max-w-sm">
              Forma parte de nuestra comunidad y comienza a intercambiar.
            </p>
          </div>

          <div className="relative w-full max-w-[360px] sm:max-w-[420px] aspect-square flex items-center justify-center my-2">
            {/* Hojas decorativas de fondo */}
            <FaLeaf className="absolute -top-2 left-4 text-primary/10 text-7xl -rotate-12 pointer-events-none" />
            <FaLeaf className="absolute bottom-4 left-8 text-primary/10 text-6xl rotate-45 pointer-events-none" />

            {/* Círculo morado suave de fondo */}
            <div className="relative w-72 h-72 sm:w-88 sm:h-88 md:w-96 md:h-96 bg-primary/15 rounded-full flex items-center justify-center">
              <img
                src={caja2Img}
                alt="Caja de intercambio con plantas y objetos"
                className="w-full max-w-[300px] sm:max-w-[360px] md:max-w-[400px] h-auto object-contain z-10 drop-shadow-md"
              />
            </div>
          </div>
        </div>

        {/* Columna Derecha: Tarjeta de Formulario de Registro */}
        <div className="lg:col-span-7 flex justify-center">
          <motion.div
            className="w-full max-w-xl bg-white rounded-3xl shadow-xl p-8 sm:p-10 border border-gray-100"
            variants={slideUp}
            initial="hidden"
            animate="visible"
            transition={{ duration: 0.3 }}
          >
            <div className="inline-block bg-primary/10 text-primary font-bold text-xs px-3.5 py-1 rounded-full mb-3">
              ¡Crea tu cuenta!
            </div>

            <h1 className="text-2xl sm:text-3xl font-extrabold text-gray-900 tracking-tight">
              Crear una cuenta
            </h1>
            <p className="mt-2 text-sm text-gray-600 mb-6">
              Completa tus datos para unirte a MarketExchange y ser parte del cambio.
            </p>

            <form onSubmit={handleSubmit} className="space-y-4">
              <AnimatePresence>
                {errorMessage && (
                  <motion.div
                    className="p-3 bg-danger/10 border border-danger/20 text-danger text-sm rounded-xl font-semibold text-center"
                    initial={{ opacity: 0, height: 0 }}
                    animate={{ opacity: 1, height: "auto" }}
                    exit={{ opacity: 0, height: 0 }}
                  >
                    {errorMessage}
                  </motion.div>
                )}
              </AnimatePresence>

              {/* Nombre y Apellido en 2 columnas */}
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div>
                  <div className="relative">
                    <FaUser className="absolute left-3.5 top-1/2 -translate-y-1/2 text-gray-400 text-sm pointer-events-none" />
                    <input
                      type="text"
                      name="firstName"
                      id="firstName"
                      value={formData.firstName}
                      onChange={handleChange}
                      placeholder="Tu nombre"
                      className="w-full pl-10 pr-4 py-3 text-sm bg-white border border-gray-300 rounded-xl focus:ring-2 focus:ring-primary focus:border-transparent outline-none transition-all text-gray-900"
                      required
                    />
                  </div>
                </div>

                <div>
                  <div className="relative">
                    <FaUser className="absolute left-3.5 top-1/2 -translate-y-1/2 text-gray-400 text-sm pointer-events-none" />
                    <input
                      type="text"
                      name="lastName"
                      id="lastName"
                      value={formData.lastName}
                      onChange={handleChange}
                      placeholder="Tu apellido"
                      className="w-full pl-10 pr-4 py-3 text-sm bg-white border border-gray-300 rounded-xl focus:ring-2 focus:ring-primary focus:border-transparent outline-none transition-all text-gray-900"
                      required
                    />
                  </div>
                </div>
              </div>

              {/* Correo Electrónico */}
              <div>
                <div className="relative">
                  <FaEnvelope className="absolute left-3.5 top-1/2 -translate-y-1/2 text-gray-400 text-sm pointer-events-none" />
                  <input
                    type="email"
                    name="email"
                    id="email"
                    value={formData.email}
                    onChange={handleChange}
                    placeholder="tuemail@ejemplo.com"
                    className="w-full pl-10 pr-4 py-3 text-sm bg-white border border-gray-300 rounded-xl focus:ring-2 focus:ring-primary focus:border-transparent outline-none transition-all text-gray-900"
                    required
                  />
                </div>
              </div>

              {/* Contraseña */}
              <div>
                <div className="relative">
                  <FaLock className="absolute left-3.5 top-1/2 -translate-y-1/2 text-gray-400 text-sm pointer-events-none" />
                  <input
                    type={showPassword ? "text" : "password"}
                    name="password"
                    id="password"
                    value={formData.password}
                    onChange={handleChange}
                    placeholder="Contraseña"
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

              {/* Confirmar Contraseña */}
              <div>
                <div className="relative">
                  <FaLock className="absolute left-3.5 top-1/2 -translate-y-1/2 text-gray-400 text-sm pointer-events-none" />
                  <input
                    type={showConfirmPassword ? "text" : "password"}
                    id="confirmPassword"
                    value={confirmPassword}
                    onChange={handleConfirmPasswordChange}
                    placeholder="Confirmar contraseña"
                    className="w-full pl-10 pr-12 py-3 text-sm bg-white border border-gray-300 rounded-xl focus:ring-2 focus:ring-primary focus:border-transparent outline-none transition-all text-gray-900"
                    required
                  />
                  <button
                    type="button"
                    onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                    className="absolute right-3.5 top-1/2 -translate-y-1/2 text-gray-400 hover:text-primary transition-colors focus:outline-none p-1"
                    aria-label={showConfirmPassword ? "Ocultar contraseña" : "Mostrar contraseña"}
                  >
                    {showConfirmPassword ? <FaEyeSlash size={16} /> : <FaEye size={16} />}
                  </button>
                </div>
              </div>

              {/* Celular */}
              <div>
                <div className="relative">
                  <FaPhone className="absolute left-3.5 top-1/2 -translate-y-1/2 text-gray-400 text-sm pointer-events-none" />
                  <input
                    type="text"
                    name="phone"
                    id="phone"
                    value={formData.phone}
                    onChange={handleChange}
                    placeholder="Tu número de teléfono"
                    className="w-full pl-10 pr-4 py-3 text-sm bg-white border border-gray-300 rounded-xl focus:ring-2 focus:ring-primary focus:border-transparent outline-none transition-all text-gray-900"
                    required
                  />
                </div>
              </div>

              {/* Dirección */}
              <div>
                <div className="relative">
                  <FaMapMarkerAlt className="absolute left-3.5 top-1/2 -translate-y-1/2 text-gray-400 text-sm pointer-events-none" />
                  <input
                    type="text"
                    name="address"
                    id="address"
                    value={formData.address}
                    onChange={handleChange}
                    placeholder="Tu dirección"
                    className="w-full pl-10 pr-4 py-3 text-sm bg-white border border-gray-300 rounded-xl focus:ring-2 focus:ring-primary focus:border-transparent outline-none transition-all text-gray-900"
                    required
                  />
                </div>
              </div>

              <Button type="submit" size="lg" className="w-full py-3.5 text-base font-bold rounded-xl mt-3">
                Registrarse
              </Button>
            </form>

            <p className="mt-6 text-center text-sm text-gray-600">
              ¿Ya tienes cuenta?{" "}
              <Link to="/login" className="font-bold text-primary hover:underline">
                Inicia sesión
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
