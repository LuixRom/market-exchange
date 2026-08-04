import axios from "axios";
import { FormEvent, useState } from "react";
import { useNavigate } from "react-router-dom";
import { FaEnvelope, FaKey } from "react-icons/fa";
import AuthFooter from "../components/AuthFooter";
import { Button } from "../components/ui/Button";
import { forgotPassword } from "../services/auth/session";

export default function ForgotPasswordPage() {
  const navigate = useNavigate();
  const [email, setEmail] = useState("");
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setLoading(true);
    setMessage(null);
    setError(null);

    try {
      const response = await forgotPassword(email);
      setMessage(response.data.message);
      window.setTimeout(() => {
        navigate("/login");
      }, 3000);
    } catch (submitError) {
      const backendMessage = axios.isAxiosError(submitError) && submitError.response?.data?.message;
      setError(backendMessage || "No se pudo solicitar la recuperacion de contrasena.");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="bg-cream flex flex-col min-h-[calc(100vh-80px)] justify-between">
      <div className="flex-grow flex items-center justify-center px-4 py-12">
        <div className="w-full max-w-lg bg-white rounded-3xl shadow-xl p-8 sm:p-10 border border-gray-100">
          <div className="w-14 h-14 rounded-2xl bg-primary/10 text-primary flex items-center justify-center mb-4">
            <FaKey size={24} />
          </div>
          <h1 className="text-2xl sm:text-3xl font-extrabold text-gray-900">Recuperar contrasena</h1>
          <p className="mt-2 text-sm text-gray-600">
            Ingresa tu correo y te enviaremos un enlace para restablecer tu acceso.
          </p>

          <form onSubmit={handleSubmit} className="mt-6 space-y-5">
            {message && <div className="p-3 bg-emerald-50 border border-emerald-200 text-emerald-700 text-sm rounded-xl font-semibold">{message}</div>}
            {error && <div className="p-3 bg-danger/10 border border-danger/20 text-danger text-sm rounded-xl font-semibold">{error}</div>}

            <div>
              <label htmlFor="email" className="block text-sm font-bold text-gray-700 mb-1.5">
                Correo electronico
              </label>
              <div className="relative">
                <FaEnvelope className="absolute left-3.5 top-1/2 -translate-y-1/2 text-gray-400 text-base pointer-events-none" />
                <input
                  id="email"
                  type="email"
                  value={email}
                  onChange={(event) => setEmail(event.target.value)}
                  className="w-full pl-10 pr-4 py-3 text-sm bg-white border border-gray-300 rounded-xl focus:ring-2 focus:ring-primary focus:border-transparent outline-none transition-all text-gray-900"
                  placeholder="ejemplo@correo.com"
                  required
                />
              </div>
            </div>

            <Button type="submit" className="w-full rounded-xl" disabled={loading}>
              {loading ? "Enviando..." : "Recuperar contrasena"}
            </Button>
          </form>
        </div>
      </div>
      <AuthFooter />
    </div>
  );
}
