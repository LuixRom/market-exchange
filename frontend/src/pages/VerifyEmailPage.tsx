import axios from "axios";
import { useEffect, useState } from "react";
import { Link, useSearchParams } from "react-router-dom";
import { FaCheckCircle, FaEnvelope, FaExclamationCircle } from "react-icons/fa";
import { verifyEmail } from "../services/auth/session";
import { Button } from "../components/ui/Button";
import AuthFooter from "../components/AuthFooter";

type VerifyState = "loading" | "success" | "error";

export default function VerifyEmailPage() {
  const [searchParams] = useSearchParams();
  const [state, setState] = useState<VerifyState>("loading");
  const [message, setMessage] = useState("Verificando tu correo...");

  useEffect(() => {
    const token = searchParams.get("token");
    if (!token) {
      setState("error");
      setMessage("El enlace de verificacion no tiene token.");
      return;
    }

    verifyEmail(token)
      .then((response) => {
        setState("success");
        setMessage(response.data.message || "Correo verificado correctamente.");
      })
      .catch((error) => {
        const backendMessage = axios.isAxiosError(error) && error.response?.data?.message;
        setState("error");
        setMessage(backendMessage || "No se pudo verificar el correo. El enlace puede haber expirado.");
      });
  }, [searchParams]);

  let Icon = FaEnvelope;
  let heading = "Verificando correo";
  if (state === "success") {
    Icon = FaCheckCircle;
    heading = "Correo verificado";
  } else if (state === "error") {
    Icon = FaExclamationCircle;
    heading = "No se pudo verificar";
  }

  return (
    <div className="bg-cream flex flex-col min-h-[calc(100vh-80px)] justify-between">
      <div className="flex-grow flex items-center justify-center px-4 py-12">
        <div className="w-full max-w-md bg-white border border-gray-100 shadow-xl rounded-3xl p-8 text-center">
          <div className="mx-auto w-16 h-16 rounded-full bg-primary/10 text-primary flex items-center justify-center mb-5">
            <Icon size={28} />
          </div>
          <h1 className="text-2xl font-extrabold text-gray-900">
            {heading}
          </h1>
          <p className="mt-3 text-sm text-gray-600">{message}</p>

          <Button asChild className="w-full mt-6 rounded-xl">
            <Link to="/login">Ir a iniciar sesion</Link>
          </Button>
        </div>
      </div>
      <AuthFooter />
    </div>
  );
}
