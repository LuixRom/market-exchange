import React, { useEffect, useState } from "react";
import { category } from "../services/category/category"; // Ruta donde tienes tu servicio
import { CategoryResponse } from "../interfaces/category/CategoryResponse";
import { useNavigate } from "react-router-dom";
import CategoryCard from "./CategoryCard";
import { useAuth } from "../context/AuthProvider";

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
    return <div>Cargando categorías...</div>; // Muestra un mensaje de carga
  }

  return (
    <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4 p-4 bg-transparent">
      {categories.map((cat) => (
        <div key={cat.id}>
          <CategoryCard id={cat.id} name={cat.name} description={cat.description} />
          {role === "ADMIN" && (
            <button
              onClick={() => navigate(`/dashboard/category/edit/${cat.id}`)}
              className="mt-2 w-full bg-white text-purple-600 py-2 px-4 rounded-lg border border-purple-600 hover:bg-purple-600 hover:text-white transition"
            >
              Editar
            </button>
          )}
        </div>
      ))}

      {/* Tarjeta para crear nueva categoría */}
      {role === "ADMIN" && (
        <div
          onClick={() => navigate("/dashboard/category/create")}
          className="flex justify-center items-center bg-purple-300 hover:bg-purple-400 text-white text-4xl font-bold rounded-lg shadow-md cursor-pointer"
        >
          +
        </div>
      )}
    </div>
  );
}
