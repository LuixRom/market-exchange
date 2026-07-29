import React, { useEffect, useState } from "react";
import { category as categoryApi} from "../services/category/category";
import { CategoryRequest } from "../interfaces/category/CategoryRequest";
import { useParams, useNavigate } from "react-router-dom";

export default function EditCategoryPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();

  const [formData, setFormData] = useState<CategoryRequest>({
    name: "",
    description: "",
  });

  const [successMessage, setSuccessMessage] = useState<string | null>(null);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  // Cargar datos de la categoría existente
  useEffect(() => {
    const fetchCategory = async () => {
      try {
        const category = await categoryApi.getCategoryById(Number(id));
        setFormData({ name: category.name, description: category.description });
      } catch {
        setErrorMessage("No se pudo cargar la categoría.");
      }
    };

    fetchCategory();
  }, [id]);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value,
    });
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await categoryApi.updateCategory(Number(id), formData);
      setSuccessMessage("Categoría actualizada con éxito.");
      setTimeout(() => navigate("/dashboard"), 2000); // Redirige después de 2 segundos
    } catch {
      setErrorMessage("Hubo un error al actualizar la categoría.");
    }
  };

  return (
    <div className="flex justify-center items-center min-h-screen bg-purple-50">
      <div className="w-full max-w-md bg-white shadow-md rounded-lg p-8 border-4 border-purple-600">
        <h1 className="text-2xl font-bold mb-6 text-center text-gray-800">
          Editar Categoría
        </h1>
        {successMessage && (
          <p className="text-green-500 mb-4 text-center">{successMessage}</p>
        )}
        {errorMessage && (
          <p className="text-red-500 mb-4 text-center">{errorMessage}</p>
        )}
  
        <form onSubmit={handleSubmit} className="space-y-6">
          {/* Nombre */}
          <div>
            <label
              htmlFor="name"
              className="block text-gray-700 font-semibold mb-2"
            >
              Nombre
            </label>
            <input
              type="text"
              id="name"
              name="name"
              value={formData.name}
              onChange={handleChange}
              placeholder="Nombre de la categoría"
              className="w-full border border-gray-300 p-3 rounded-lg shadow-sm focus:outline-none focus:ring focus:ring-purple-200"
              required
            />
          </div>
  
          {/* Descripción */}
          <div>
            <label
              htmlFor="description"
              className="block text-gray-700 font-semibold mb-2"
            >
              Descripción
            </label>
            <textarea
              id="description"
              name="description"
              value={formData.description}
              onChange={handleChange}
              placeholder="Descripción de la categoría"
              className="w-full border border-gray-300 p-3 rounded-lg shadow-sm focus:outline-none focus:ring focus:ring-purple-200"
              rows={4}
              required
            />
          </div>
  
          {/* Botón de envío */}
          <div>
            <button
              type="submit"
              className="w-full bg-purple-600 text-white py-3 rounded-lg font-bold hover:bg-purple-700 transition-colors"
            >
              Guardar Cambios
            </button>
          </div>
        </form>
      </div>
    </div>
  );
  
}
