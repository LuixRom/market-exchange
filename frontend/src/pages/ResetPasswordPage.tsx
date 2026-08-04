import axios from "axios";
import { FormEvent, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { FaLock } from "react-icons/fa";
import AuthFooter from "../components/AuthFooter";
import { Button } from "../components/ui/Button";
import { resetPassword } from "../services/auth/session";

export default function ResetPasswordPage() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  const token = searchParams.get("token") || "";

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setMessage(null);
    setError(null);

    if (!token) {
      setError("El enlace de recuperacion no tiene token.");
      return;
    }

    if (password !== confirmPassword) {
      setError("Las contrasenas no coinciden.");
      return;
    }

    if (password.length < 8) {
      setError("La contrasena debe tener al menos 8 caracteres.");
      return;
    }

    setLoading(true);
    try {
      const response = await resetPassword(token, password);
      setMessage(response.data.message);
      setPassword("");
      setConfirmPassword("");
      window.setTimeout(() => {
        navigate("/login");
      }, 3000);
    } catch (submitError) {
      const backendMessage = axios.isAxiosError(submitError) && submitError.response?.data?.message;
      setError(backendMessage || "No se pudo restablecer la contrasena.");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="bg-cream flex flex-col min-h-[calc(100vh-80px)] justify-between">
      <div className="flex-grow flex items-center justify-center px-4 py-12">
        <div className="w-full max-w-lg bg-white rounded-3xl shadow-xl p-8 sm:p-10 border border-gray-100">
          <div className="w-14 h-14 rounded-2xl bg-primary/10 text-primary flex items-center justify-center mb-4">
            <FaLock size={24} />
          </div>
          <h1 className="text-2xl sm:text-3xl font-extrabold text-gray-900">Nueva contrasena</h1>
          <p className="mt-2 text-sm text-gray-600">
            Elige una nueva contrasena para recuperar el acceso a tu cuenta.
          </p>

          <form onSubmit={handleSubmit} className="mt-6 space-y-5">
            {message && <div className="p-3 bg-emerald-50 border border-emerald-200 text-emerald-700 text-sm rounded-xl font-semibold">{message}</div>}
            {error && <div className="p-3 bg-danger/10 border border-danger/20 text-danger text-sm rounded-xl font-semibold">{error}</div>}

            <div>
              <label htmlFor="password" className="block text-sm font-bold text-gray-700 mb-1.5">
                Nueva contrasena
              </label>
              <input
                id="password"
                type="password"
                value={password}
                onChange={(event) => setPassword(event.target.value)}
                className="w-full px-4 py-3 text-sm bg-white border border-gray-300 rounded-xl focus:ring-2 focus:ring-primary focus:border-transparent outline-none transition-all text-gray-900"
                required
              />
            </div>

            <div>
              <label htmlFor="confirmPassword" className="block text-sm font-bold text-gray-700 mb-1.5">
                Confirmar contrasena
              </label>
              <input
                id="confirmPassword"
                type="password"
                value={confirmPassword}
                onChange={(event) => setConfirmPassword(event.target.value)}
                className="w-full px-4 py-3 text-sm bg-white border border-gray-300 rounded-xl focus:ring-2 focus:ring-primary focus:border-transparent outline-none transition-all text-gray-900"
                required
              />
            </div>

            <Button type="submit" className="w-full rounded-xl" disabled={loading}>
              {loading ? "Guardando..." : "Restablecer contrasena"}
            </Button>
          </form>
        </div>
      </div>
      <AuthFooter />
    </div>
  );
}
