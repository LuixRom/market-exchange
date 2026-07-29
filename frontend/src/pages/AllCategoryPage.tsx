import React, { useEffect, useState } from "react";
import { motion } from "framer-motion";
import { category } from "../services/category/category"; // Ruta donde tienes tu servicio
import { CategoryResponse } from "../interfaces/category/CategoryResponse";
import { useNavigate } from "react-router-dom";
import CategoryCard from "./CategoryCard";
import { useAuth } from "../context/AuthProvider";
import { Spinner } from "../components/ui/Spinner";
import { staggerChildren, slideUp } from "../lib/motion";

export default function CategoriesPage() {
  const [categories, setCategories] = useState<CategoryResponse[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const { role } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    const fetchCategories = async () => {
      try {
        const allCategories = await category.getAllCategories();
        setCategories(allCategories);
      } catch (error) {
        console.error("Error al obtener las categorías:", error);
      } finally {
        setLoading(false);
      }
    };

    fetchCategories();
  }, []);

  if (loading) {
    return <Spinner label="Cargando categorías..." />;
  }

  return (
    <motion.div
      className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4 p-4 bg-transparent"
      variants={staggerChildren}
      initial="hidden"
      animate="visible"
    >
      {categories.map((cat) => (
        <motion.div key={cat.id} variants={slideUp}>
          <CategoryCard id={cat.id} name={cat.name} description={cat.description} />
          {role === "ADMIN" && (
            <button
              onClick={() => navigate(`/dashboard/category/edit/${cat.id}`)}
              className="mt-2 w-full bg-white text-primary py-2 px-4 rounded-card border border-primary hover:bg-primary hover:text-white transition-colors"
            >
              Editar
            </button>
          )}
        </motion.div>
      ))}

      {/* Tarjeta para crear nueva categoría */}
      {role === "ADMIN" && (
        <motion.div
          variants={slideUp}
          whileHover={{ scale: 1.03 }}
          onClick={() => navigate("/dashboard/category/create")}
          className="flex justify-center items-center bg-primary/10 hover:bg-primary/20 text-primary text-4xl font-bold rounded-card shadow-sm cursor-pointer"
        >
          +
        </motion.div>
      )}
    </motion.div>
  );
}
