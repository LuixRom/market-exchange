import { ChangeEvent, FormEvent, useEffect, useState } from "react";
import { AnimatePresence, motion } from "framer-motion";
import { category } from "../services/category/category";
import { usuario } from "../services/user/user"; // Importa el servicio de usuario
import { CategoryResponse } from "../interfaces/category/CategoryResponse";
import { ItemResponse } from "../interfaces/item/ItemResponse";
import { item } from "../services/item/item";
import { useNavigate } from "react-router-dom";
import { Card } from "./ui/Card";
import { Input } from "./ui/Input";
import { Button } from "./ui/Button";
import { slideUp } from "../lib/motion";

type ItemFormProps = {
    initialData: {
        name: string;
        description: string;
        condition: "NEW" | "USED";
    };
    onSubmitSuccess: (response: ItemResponse) => void;
    onSubmitError: (error: unknown) => void;
};

const selectClassName =
    "w-full rounded-card border border-border bg-surface px-4 py-2.5 text-gray-800 shadow-sm transition-colors duration-200 focus:outline-none focus:ring-2 focus:ring-primary focus:border-primary";

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
    <section className="flex flex-col items-center justify-center min-h-screen bg-white-100 py-10">
      <motion.div
        className="w-full max-w-md px-4"
        variants={slideUp}
        initial="hidden"
        animate="visible"
        transition={{ duration: 0.3 }}
      >
        <Card className="p-8 border-0 shadow-lg">
          <form onSubmit={handleSubmit}>
            <h2 className="text-2xl font-bold mb-6 text-center">Registrar Ítem</h2>

            <AnimatePresence>
              {errorMessage && (
                <motion.div
                  className="text-danger text-sm mb-4 text-center"
                  initial={{ opacity: 0, height: 0 }}
                  animate={{ opacity: 1, height: "auto" }}
                  exit={{ opacity: 0, height: 0 }}
                  transition={{ duration: 0.2 }}
                >
                  {errorMessage}
                </motion.div>
              )}
              {successMessage && (
                <motion.div
                  className="text-success text-sm mb-4 text-center"
                  initial={{ opacity: 0, height: 0 }}
                  animate={{ opacity: 1, height: "auto" }}
                  exit={{ opacity: 0, height: 0 }}
                  transition={{ duration: 0.2 }}
                >
                  {successMessage}
                </motion.div>
              )}
            </AnimatePresence>

            <div className="mb-4">
              <Input
                type="text"
                name="name"
                value={formData.name}
                onChange={handleInputChange}
                placeholder="Nombre del ítem"
                required
              />
            </div>

            <div className="mb-4">
              <textarea
                name="description"
                value={formData.description}
                onChange={handleInputChange}
                className={selectClassName}
                placeholder="Descripción del ítem"
                required
              />
            </div>

            <div className="mb-4">
              <select
                name="category"
                value={selectedCategory || ""}
                onChange={handleCategoryChange}
                className={selectClassName}
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
                className={selectClassName}
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
                className={`${selectClassName} file:mr-3 file:rounded-full file:border-0 file:bg-primary file:text-primary-foreground file:px-3 file:py-1.5 file:text-sm file:font-semibold`}
                required
              />
            </div>

            <Button type="submit" variant="primary" size="lg" className="w-full">
              Registrar Ítem
            </Button>
          </form>
        </Card>
      </motion.div>
    </section>
  );
}
