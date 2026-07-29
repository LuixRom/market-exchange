import { ChangeEvent, FormEvent, useEffect, useState } from "react";
import { category } from "../services/category/category";
import { usuario } from "../services/user/user"; // Importa el servicio de usuario
import { CategoryResponse } from "../interfaces/category/CategoryResponse";
import { ItemResponse } from "../interfaces/item/ItemResponse";
import { item } from "../services/item/item";
import { useNavigate } from "react-router-dom";

type ItemFormProps = {
    initialData: {
        name: string;
        description: string;
        condition: "NEW" | "USED";
    };
    onSubmitSuccess: (response: ItemResponse) => void;
    onSubmitError: (error: unknown) => void;
};

export default function ItemForm({ onSubmitSuccess, onSubmitError }: ItemFormProps) {
  const navigate = useNavigate();
  const [successMessage, setSuccessMessage] = useState<string | null>(null);
  const [formData, setFormData] = useState({
    name: "",
    description: "",
    condition: "NEW",
  });


  const [image, setImage] = useState<File | null>(null); // Estado para la imagen
  const [categories, setCategories] = useState<CategoryResponse[]>([]);
  const [selectedCategory, setSelectedCategory] = useState<number | null>(null);
  const [userId, setUserId] = useState<number | null>(null); // Estado para almacenar el ID del usuario
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  // Cargar categorías al montar el componente
  useEffect(() => {
    async function fetchCategories() {
      try {
        const categoryList = await category.getAllCategories();
        setCategories(categoryList);
      } catch {
        setErrorMessage("Error al cargar categorías.");
      }
    }

    fetchCategories().catch(console.error);
  }, []);

  // Obtener el ID del usuario al montar el componente
  useEffect(() => {
    async function fetchUserId() {
      try {
        const userInfo = await usuario.getMyInfo();
        setUserId(userInfo.id); // Establece el ID del usuario en el estado
      } catch {
        setErrorMessage("Error al obtener información del usuario.");
      }
    }

    fetchUserId().catch(console.error);
  }, []);

  function handleInputChange(e: ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) {
    const { name, value } = e.target;
    setFormData((prevData) => ({
      ...prevData,
      [name]: value,
    }));
  }

  function handleCategoryChange(e: ChangeEvent<HTMLSelectElement>) {
    setSelectedCategory(Number(e.target.value));
  }

  function handleImageChange(e: ChangeEvent<HTMLInputElement>) {
    const file = e.target.files?.[0] || null;
    setImage(file); // Establecer la imagen seleccionada en el estado
  }

  async function handleSubmit(e: FormEvent<HTMLFormElement>) {
    e.preventDefault();
    setErrorMessage(null);
    setSuccessMessage(null);

    if (selectedCategory === null) {
      setErrorMessage("Por favor selecciona una categoría.");
      return;
    }

    if (!userId) {
      setErrorMessage("No se pudo obtener el ID del usuario. Intenta iniciar sesión nuevamente.");
      return;
    }

    if (!image) {
      setErrorMessage("Por favor selecciona una imagen.");
      return;
    }

    try {
      setErrorMessage(null);
        const formDataToSend = new FormData();
        formDataToSend.append("name", formData.name);
        formDataToSend.append("description", formData.description);
        formDataToSend.append("condition", formData.condition);
        formDataToSend.append("category_id", selectedCategory.toString());
        formDataToSend.append("user_id", userId.toString());
        formDataToSend.append("image", image);

        const response = await item.createItem(formDataToSend); // Cambiar a FormData
        onSubmitSuccess(response);

      setErrorMessage(null);
      setSuccessMessage("Ítem registrado exitosamente.");
      setTimeout(() => {
        navigate("/");
      }, 3000);


    } catch (error: unknown) {
        if (error instanceof Error) {
            setErrorMessage(`Error al registrar el ítem: ${error.message}`);
        } else {
            setErrorMessage("Error desconocido al registrar el ítem.");
        }
        onSubmitError(error);
    }
  }

  return (
    <section className="flex flex-col items-center justify-center min-h-screen bg-white-100">
      <form
        onSubmit={handleSubmit}
        className="w-full max-w-md bg-white shadow-md rounded px-8 py-6 border-4 border-blue-700"
      >
        <h2 className="text-2xl font-bold mb-6 text-center">Registrar Ítem</h2>

        {errorMessage && <div className="text-red-600 text-sm mb-4 text-center">{errorMessage}</div>}
        {successMessage && <div className="text-green-600 text-sm mb-4 text-center">{successMessage}</div>}


        <div className="mb-4">
          <input
            type="text"
            name="name"
            value={formData.name}
            onChange={handleInputChange}
            className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700"
            placeholder="Nombre del ítem"
            required
          />
        </div>

        <div className="mb-4">
          <textarea
            name="description"
            value={formData.description}
            onChange={handleInputChange}
            className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700"
            placeholder="Descripción del ítem"
            required
          />
        </div>

        <div className="mb-4">
          <select
            name="category"
            value={selectedCategory || ""}
            onChange={handleCategoryChange}
            className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700"
            required
          >
            <option value="" disabled>
              Selecciona una categoría
            </option>
            {categories.map((category) => (
              <option key={category.id} value={category.id}>
                {category.name}
              </option>
            ))}
          </select>
        </div>

        <div className="mb-4">
          <select
            name="condition"
            value={formData.condition}
            onChange={handleInputChange}
            className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700"
            required
          >
            <option value="NEW">Nuevo</option>
            <option value="USED">Usado</option>
          </select>
        </div>

        <div className="mb-4">
          <input
            type="file"
            accept="image/*"
            onChange={handleImageChange}
            className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700"
            required
          />
        </div>

        <button
          type="submit"
          className="bg-blue-600 hover:bg-blue-800 text-white font-bold py-2 px-4 rounded focus:outline-none"
        >
          Registrar Ítem
        </button>
      </form>
    </section>
  );
}
