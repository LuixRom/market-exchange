import { ChangeEvent, FormEvent } from "react";
import { AnimatePresence, motion } from "framer-motion";
import { useNavigate } from "react-router-dom";
import { CategoryRequest } from "../interfaces/category/CategoryRequest";
import { FaTag, FaLeaf, FaTimes, FaFolderOpen } from "react-icons/fa";
import { slideUp } from "../lib/motion";

interface CategoryFormProps {
  title: string;
  formData: CategoryRequest;
  setFormData: React.Dispatch<React.SetStateAction<CategoryRequest>>;
  onSubmit: (e: FormEvent<HTMLFormElement>) => void;
  submitLabel: string;
  successMessage?: string | null;
  errorMessage?: string | null;
}

export default function CategoryForm({
  title,
  formData,
  setFormData,
  onSubmit,
  submitLabel,
  successMessage,
  errorMessage,
}: Readonly<CategoryFormProps>) {
  const navigate = useNavigate();

  function handleChange(e: ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value,
    });
  }

  const handleCancel = () => {
    navigate("/dashboard/category");
  };

  return (
    <div className="flex justify-center items-center min-h-[calc(100vh-120px)] py-8 px-4">
      <motion.div
        className="w-full max-w-lg bg-white rounded-[32px] shadow-2xl p-6 sm:p-8 border border-gray-100 relative overflow-hidden"
        variants={slideUp}
        initial="hidden"
        animate="visible"
        transition={{ duration: 0.3 }}
      >
        {/* Botón de cerrar superior derecho */}
        <button
          type="button"
          onClick={handleCancel}
          className="absolute top-6 right-6 text-primary bg-primary/5 hover:bg-primary/10 w-9 h-9 rounded-full flex items-center justify-center font-bold transition-all focus:outline-none"
          aria-label="Cerrar"
        >
          <FaTimes size={16} />
        </button>

        {/* Encabezado con icono y subtítulo */}
        <div className="flex items-center gap-4 mb-5 pr-10">
          <div className="w-14 h-14 rounded-2xl bg-primary/10 text-primary flex items-center justify-center flex-shrink-0 shadow-xs relative">
            <FaFolderOpen size={26} />
            <FaLeaf size={12} className="absolute top-2.5 right-2.5 text-primary" />
          </div>
          <div>
            <h1 className="text-2xl font-extrabold text-gray-900 tracking-tight leading-tight">
              {title}
            </h1>
            <p className="text-xs text-gray-500 mt-1 font-medium">
              Completa la información para {title.toLowerCase().includes("crear") ? "crear una nueva" : "actualizar la"} categoría.
            </p>
          </div>
        </div>

        <div className="border-b border-gray-100 mb-6"></div>

        {/* Mensajes de éxito y error */}
        <AnimatePresence>
          {successMessage && (
            <motion.div
              className="p-3 bg-emerald-50 border border-emerald-200 text-emerald-700 text-sm rounded-xl font-semibold mb-5 text-center"
              initial={{ opacity: 0, height: 0 }}
              animate={{ opacity: 1, height: "auto" }}
              exit={{ opacity: 0, height: 0 }}
            >
              {successMessage}
            </motion.div>
          )}
          {errorMessage && (
            <motion.div
              className="p-3 bg-danger/10 border border-danger/20 text-danger text-sm rounded-xl font-semibold mb-5 text-center"
              initial={{ opacity: 0, height: 0 }}
              animate={{ opacity: 1, height: "auto" }}
              exit={{ opacity: 0, height: 0 }}
            >
              {errorMessage}
            </motion.div>
          )}
        </AnimatePresence>

        {/* Formulario */}
        <form onSubmit={onSubmit} className="space-y-5">
          {/* Nombre de la categoría */}
          <div>
            <label htmlFor="name" className="block text-sm font-bold text-gray-800 mb-2">
              Nombre de la categoría
            </label>
            <div className="relative">
              <FaTag className="absolute left-3.5 top-1/2 -translate-y-1/2 text-primary/60 text-sm pointer-events-none" />
              <input
                type="text"
                id="name"
                name="name"
                value={formData.name}
                onChange={handleChange}
                placeholder="Ej: Muebles, Hogar, Electrónicos..."
                required
                className="w-full pl-10 pr-4 py-3 text-sm bg-white border border-gray-300 rounded-xl focus:ring-2 focus:ring-primary focus:border-transparent outline-none transition-all text-gray-900 shadow-xs"
              />
            </div>
          </div>

          {/* Descripción (opcional) */}
          <div>
            <label htmlFor="description" className="block text-sm font-bold text-gray-800 mb-2">
              Descripción (opcional)
            </label>
            <textarea
              id="description"
              name="description"
              value={formData.description}
              onChange={handleChange}
              placeholder="Describe qué tipo de publicaciones incluirá esta categoría. Esto ayudará a los usuarios a entender mejor su propósito."
              rows={4}
              className="w-full p-4 text-sm bg-white border border-gray-300 rounded-xl focus:ring-2 focus:ring-primary focus:border-transparent outline-none transition-all text-gray-900 resize-none shadow-xs leading-relaxed"
            />
          </div>

          {/* Bloque Informativo Inferior con Ilustración */}
          <div className="relative pt-2 pb-2">
            <div className="bg-primary/5 border border-primary/10 rounded-2xl p-3.5 flex items-start gap-3 text-xs text-gray-600 max-w-[78%] relative z-10">
              <FaLeaf className="text-primary text-base flex-shrink-0 mt-0.5" />
              <span className="leading-tight">
                Las categorías bien definidas ayudan a los usuarios a encontrar exactamente lo que necesitan.
              </span>
            </div>

            {/* Hoja decorativa de fondo en esquina derecha */}
            <FaLeaf className="absolute -bottom-2 -right-2 text-emerald-500/25 text-7xl pointer-events-none rotate-12" />
          </div>

          {/* Acciones */}
          <div className="flex items-center justify-end gap-3 pt-4 border-t border-gray-100">
            <button
              type="button"
              onClick={handleCancel}
              className="px-5 py-2.5 rounded-xl border border-gray-200 text-gray-700 font-bold hover:bg-gray-50 transition-colors text-sm"
            >
              Cancelar
            </button>
            <button
              type="submit"
              className="px-6 py-2.5 rounded-xl bg-primary hover:bg-primary-hover text-white font-bold transition-all shadow-md hover:shadow-lg text-sm"
            >
              {submitLabel}
            </button>
          </div>
        </form>
      </motion.div>
    </div>
  );
}
