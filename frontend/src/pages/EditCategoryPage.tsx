import React, { useEffect, useState } from "react";
import { category as categoryApi} from "../services/category/category";
import { CategoryRequest } from "../interfaces/category/CategoryRequest";
import { useParams, useNavigate } from "react-router-dom";
import CategoryForm from "../components/CategoryForm";

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

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMessage(null);
    setSuccessMessage(null);
    try {
      await categoryApi.updateCategory(Number(id), formData);
      setSuccessMessage("Categoría actualizada con éxito.");
      setTimeout(() => navigate("/dashboard"), 2000); // Redirige después de 2 segundos
    } catch {
      setErrorMessage("Hubo un error al actualizar la categoría.");
    }
  };

  return (
    <CategoryForm
      title="Editar Categoría"
      formData={formData}
      setFormData={setFormData}
      onSubmit={handleSubmit}
      submitLabel="Guardar Cambios"
      successMessage={successMessage}
      errorMessage={errorMessage}
    />
  );
}
