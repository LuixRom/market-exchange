import React, { useState } from "react";
import { category } from "../services/category/category";
import { CategoryRequest } from "../interfaces/category/CategoryRequest";
import CategoryForm from "../components/CategoryForm";

export default function CreateCategoryPage() {
  const [formData, setFormData] = useState<CategoryRequest>({
    name: "",
    description: "",
  });

  const [successMessage, setSuccessMessage] = useState<string | null>(null);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMessage(null);
    setSuccessMessage(null);
    try {
      await category.createCategory(formData);
      setSuccessMessage("Categoría creada con éxito.");
      setFormData({ name: "", description: "" });
    } catch {
      setErrorMessage("Hubo un error al crear la categoría.");
    }
  };

  return (
    <CategoryForm
      title="Crear Categoría"
      formData={formData}
      setFormData={setFormData}
      onSubmit={handleSubmit}
      submitLabel="Crear"
      successMessage={successMessage}
      errorMessage={errorMessage}
    />
  );
}
