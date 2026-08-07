import React, { useEffect, useState } from "react";
import { motion } from "framer-motion";
import { category } from "../services/category/category";
import { CategoryResponse } from "../interfaces/category/CategoryResponse";
import { useNavigate, Link } from "react-router-dom";
import { useAuth } from "../context/AuthProvider";
import { Spinner } from "../components/ui/Spinner";
import { Button } from "../components/ui/Button";
import { useToast } from "../components/ui/Toast";
import { slideUp, fadeIn } from "../lib/motion";
import {
  FaSearch,
  FaFolderOpen,
  FaPlus,
  FaPencilAlt,
  FaTrash,
  FaCouch,
  FaLeaf,
  FaShoppingBag,
  FaBook,
  FaHeadphones,
  FaTag,
} from "react-icons/fa";

const iconList = [FaCouch, FaLeaf, FaShoppingBag, FaBook, FaHeadphones, FaTag];
const colorList = [
  "bg-emerald-100 text-emerald-600",
  "bg-purple-100 text-purple-600",
  "bg-amber-100 text-amber-600",
  "bg-sky-100 text-sky-600",
  "bg-indigo-100 text-indigo-600",
  "bg-rose-100 text-rose-600",
];

export default function CategoriesPage() {
  const [categories, setCategories] = useState<CategoryResponse[]>([]);
  const [filteredCategories, setFilteredCategories] = useState<CategoryResponse[]>([]);
  const [searchTerm, setSearchTerm] = useState<string>("");
  const [loading, setLoading] = useState<boolean>(true);
  const { role } = useAuth();
  const navigate = useNavigate();
  const { toast } = useToast();

  useEffect(() => {
    const fetchCategories = async () => {
      try {
        const allCategories = await category.getAllCategories();
        setCategories(allCategories);
        setFilteredCategories(allCategories);
      } catch (error) {
        console.error("Error al obtener las categorías:", error);
      } finally {
        setLoading(false);
      }
    };

    fetchCategories();
  }, []);

  const handleSearch = (e: React.ChangeEvent<HTMLInputElement>) => {
    const term = e.target.value;
    setSearchTerm(term);
    if (!term.trim()) {
      setFilteredCategories(categories);
    } else {
      setFilteredCategories(
        categories.filter(
          (cat) =>
            cat.name.toLowerCase().includes(term.toLowerCase()) ||
            cat.description?.toLowerCase().includes(term.toLowerCase())
        )
      );
    }
  };

  const handleDelete = async (e: React.MouseEvent, id: number, name: string) => {
    e.stopPropagation();
    if (window.confirm(`¿Estás seguro de que deseas eliminar la categoría "${name}"?`)) {
      try {
        await category.deleteCategory(id);
        const updated = categories.filter((cat) => cat.id !== id);
        setCategories(updated);
        setFilteredCategories(updated.filter((cat) => cat.name.toLowerCase().includes(searchTerm.toLowerCase())));
        toast({ title: "Categoría eliminada correctamente", variant: "success" });
      } catch (error) {
        console.error("Error al eliminar la categoría:", error);
        toast({ title: "No se pudo eliminar la categoría", variant: "danger" });
      }
    }
  };

  if (loading) {
    return <Spinner label="Cargando categorías..." />;
  }

  return (
    <motion.div
      className="w-full max-w-container mx-auto px-4 sm:px-6 py-8"
      variants={fadeIn}
      initial="hidden"
      animate="visible"
      transition={{ duration: 0.3 }}
    >
      {/* Encabezado Principal */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-6">
        <div>
          <h1 className="text-2xl sm:text-3xl font-extrabold text-gray-900 tracking-tight">
            Categorías
          </h1>
          <p className="mt-1 text-sm text-gray-600">
            Organiza y gestiona las categorías de tu marketplace.
          </p>
        </div>

        {role === "ADMIN" && (
          <Button
            asChild
            size="md"
            className="rounded-xl px-5 py-2.5 font-bold shadow-sm self-start sm:self-auto flex items-center gap-2"
          >
            <Link to="/dashboard/category/create">
              <FaPlus size={14} />
              <span>Nueva categoría</span>
            </Link>
          </Button>
        )}
      </div>

      {/* Banner Informativo Superior */}
      <motion.div
        className="bg-[#faf6f0] border border-gray-200/70 rounded-3xl p-6 sm:p-8 relative overflow-hidden flex flex-col md:flex-row items-center justify-between gap-6 mb-8 shadow-sm"
        variants={slideUp}
      >
        <div className="flex items-center gap-4 max-w-xl">
          <div className="w-12 h-12 rounded-2xl bg-emerald-100 text-emerald-700 flex items-center justify-center flex-shrink-0 shadow-sm">
            <FaFolderOpen size={22} />
          </div>
          <p className="text-sm sm:text-base text-gray-700 font-medium leading-relaxed">
            Las categorías ayudan a los usuarios a encontrar exactamente lo que necesitan para intercambiar.
          </p>
        </div>

        {/* Ilustración Derecha */}
        <div className="relative flex justify-center items-center flex-shrink-0">
          <div className="w-36 h-28 sm:w-44 sm:h-32 bg-primary/10 rounded-2xl flex items-center justify-center">
            <img
              src="/img/caja2.png"
              alt="Categorías MarketExchange"
              className="w-28 sm:w-36 h-auto object-contain drop-shadow-md"
            />
          </div>
        </div>
      </motion.div>

      {/* Barra de Búsqueda y Filtro */}
      <div className="flex flex-col sm:flex-row items-center justify-between gap-4 mb-6">
        <div className="relative w-full sm:w-80">
          <FaSearch className="absolute left-3.5 top-1/2 -translate-y-1/2 text-gray-400 text-sm pointer-events-none" />
          <input
            type="text"
            value={searchTerm}
            onChange={handleSearch}
            placeholder="Buscar categorías..."
            className="w-full pl-10 pr-4 py-2.5 text-sm bg-white border border-gray-300 rounded-xl focus:ring-2 focus:ring-primary focus:border-transparent outline-none transition-all text-gray-900 shadow-sm"
          />
        </div>

        <div className="text-xs sm:text-sm text-gray-500 font-medium self-end sm:self-auto">
          Total registrado: <span className="font-bold text-gray-900">{categories.length}</span> categorías
        </div>
      </div>

      {/* Tabla / Lista de Categorías */}
      <motion.div
        className="bg-white rounded-3xl border border-gray-100 shadow-xl overflow-hidden"
        variants={slideUp}
      >
        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="border-b border-gray-100 bg-gray-50/60 text-xs font-bold text-gray-500 uppercase tracking-wider">
                <th className="py-4 px-6">Categoría</th>
                <th className="py-4 px-6">Descripción</th>
                <th className="py-4 px-6 text-center">Estado</th>
                {role === "ADMIN" && <th className="py-4 px-6 text-right">Acciones</th>}
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100 text-sm">
              {filteredCategories.length > 0 ? (
                filteredCategories.map((cat, idx) => {
                  const IconComp = iconList[idx % iconList.length];
                  const colorClass = colorList[idx % colorList.length];

                  return (
                    <motion.tr
                      key={cat.id}
                      onClick={() => navigate(`/dashboard/category/${cat.id}/items`)}
                      onKeyDown={(e) => {
                        if (e.key === "Enter" || e.key === " ") {
                          e.preventDefault();
                          navigate(`/dashboard/category/${cat.id}/items`);
                        }
                      }}
                      role="button"
                      tabIndex={0}
                      className="hover:bg-primary/5 cursor-pointer transition-colors group"
                      whileHover={{ backgroundColor: "rgba(124, 58, 237, 0.03)" }}
                    >
                      {/* Categoría e Icono */}
                      <td className="py-4 px-6 font-bold text-gray-900 flex items-center gap-3">
                        <div className={`w-10 h-10 rounded-xl ${colorClass} flex items-center justify-center flex-shrink-0 shadow-sm group-hover:scale-105 transition-transform`}>
                          <IconComp size={18} />
                        </div>
                        <span className="group-hover:text-primary transition-colors">{cat.name}</span>
                      </td>

                      {/* Descripción */}
                      <td className="py-4 px-6 text-gray-600 max-w-md">
                        {cat.description || "Sin descripción disponible"}
                      </td>

                      {/* Estado */}
                      <td className="py-4 px-6 text-center">
                        <span className="inline-flex items-center gap-1.5 px-3 py-1 bg-emerald-50 text-emerald-700 border border-emerald-200/60 rounded-full text-xs font-bold">
                          <span className="w-2 h-2 rounded-full bg-emerald-500 animate-pulse"></span>{" "}
                          Activa
                        </span>
                      </td>

                      {/* Acciones para ADMIN */}
                      {role === "ADMIN" && (
                        <td className="py-4 px-6 text-right">
                          <div className="flex items-center justify-end gap-2">
                            <button
                              type="button"
                              onClick={(e) => {
                                e.stopPropagation();
                                navigate(`/dashboard/category/edit/${cat.id}`);
                              }}
                              className="p-2 text-gray-400 hover:text-primary hover:bg-primary/10 rounded-xl transition-all"
                              title="Editar categoría"
                              aria-label="Editar categoría"
                            >
                              <FaPencilAlt size={15} />
                            </button>
                            <button
                              type="button"
                              onClick={(e) => handleDelete(e, cat.id, cat.name)}
                              className="p-2 text-gray-400 hover:text-danger hover:bg-danger/10 rounded-xl transition-all"
                              title="Eliminar categoría"
                              aria-label="Eliminar categoría"
                            >
                              <FaTrash size={15} />
                            </button>
                          </div>
                        </td>
                      )}
                    </motion.tr>
                  );
                })
              ) : (
                <tr>
                  <td colSpan={role === "ADMIN" ? 4 : 3} className="py-12 px-6 text-center">
                    <div className="flex flex-col items-center justify-center text-gray-500">
                      <FaFolderOpen className="text-primary/30 text-5xl mb-3" />
                      <p className="font-bold text-gray-800 text-base">
                        {searchTerm ? "No se encontraron categorías" : "No hay categorías registradas"}
                      </p>
                      <p className="text-xs text-gray-500 mt-1 max-w-sm">
                        {searchTerm
                          ? "Intenta buscar con otros términos o limpia el filtro."
                          : "Las categorías creadas aparecerán aquí ordenadas y listas para gestionar."}
                      </p>

                      {role === "ADMIN" && !searchTerm && (
                        <Button
                          asChild
                          size="sm"
                          className="mt-4 rounded-xl px-5 py-2 font-bold"
                        >
                          <Link to="/dashboard/category/create">
                            + Crear primera categoría
                          </Link>
                        </Button>
                      )}
                    </div>
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>

        {/* Footer de Paginación */}
        <div className="py-4 px-6 bg-gray-50/50 border-t border-gray-100 flex flex-col sm:flex-row items-center justify-between gap-3 text-xs text-gray-500 font-medium">
          <span>
            Mostrando {filteredCategories.length} de {categories.length} categorías
          </span>
          <div className="flex items-center gap-1">
            <button
              type="button"
              disabled
              className="px-2.5 py-1 rounded-lg border border-gray-200 bg-white text-gray-400 cursor-not-allowed"
            >
              &lt;
            </button>
            <button type="button" className="px-3 py-1 rounded-lg bg-primary text-white font-bold shadow-sm">
              1
            </button>
            <button
              type="button"
              disabled
              className="px-2.5 py-1 rounded-lg border border-gray-200 bg-white text-gray-400 cursor-not-allowed"
            >
              &gt;
            </button>
          </div>
        </div>
      </motion.div>
    </motion.div>
  );
}
