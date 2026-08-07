import { ChangeEvent, FormEvent, useEffect, useState } from "react";
import { AnimatePresence, motion } from "framer-motion";
import { category } from "../services/category/category";
import { usuario } from "../services/user/user";
import { CategoryResponse } from "../interfaces/category/CategoryResponse";
import { ItemResponse } from "../interfaces/item/ItemResponse";
import { item } from "../services/item/item";
import { useNavigate } from "react-router-dom";
import { slideUp, fadeIn } from "../lib/motion";
import {
  FaTag,
  FaCommentAlt,
  FaFolder,
  FaShieldAlt,
  FaImage,
  FaCloudUploadAlt,
  FaCheckCircle,
  FaPaperPlane,
  FaPlus,
  FaCheck,
} from "react-icons/fa";

type ItemFormProps = {
  onSubmitSuccess: (response: ItemResponse) => void;
  onSubmitError: (error: unknown) => void;
};

export default function ItemForm({ onSubmitSuccess, onSubmitError }: Readonly<ItemFormProps>) {
  const navigate = useNavigate();
  const [successMessage, setSuccessMessage] = useState<string | null>(null);
  const [formData, setFormData] = useState({
    name: "",
    description: "",
    condition: "NEW",
  });

  const [images, setImages] = useState<File[]>([]);
  const [imagePreviews, setImagePreviews] = useState<string[]>([]);
  const [categories, setCategories] = useState<CategoryResponse[]>([]);
  const [selectedCategory, setSelectedCategory] = useState<number | null>(null);
  const [userId, setUserId] = useState<number | null>(null);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [loading, setLoading] = useState<boolean>(false);

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
        setUserId(userInfo.id);
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
    const selectedFiles = Array.from(e.target.files || []);

    if (selectedFiles.length > 4) {
      setErrorMessage("Puedes subir maximo 4 imagenes por item.");
      e.target.value = "";
      return;
    }

    setImages(selectedFiles);
    setImagePreviews([]);

    selectedFiles.forEach((file) => {
      const reader = new FileReader();
      reader.onloadend = () => {
        setImagePreviews((current) => [...current, reader.result as string]);
      };
      reader.readAsDataURL(file);
    });

    if (selectedFiles.length > 0) {
      setErrorMessage(null);
    }
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

    if (images.length === 0) {
      setErrorMessage("Por favor selecciona una imagen para tu ítem.");
      return;
    }

    if (images.length > 4) {
      setErrorMessage("Puedes subir maximo 4 imagenes por item.");
      return;
    }

    try {
      setLoading(true);
      const formDataToSend = new FormData();
      formDataToSend.append("name", formData.name);
      formDataToSend.append("description", formData.description);
      formDataToSend.append("condition", formData.condition);
      formDataToSend.append("categoryId", selectedCategory.toString());
      formDataToSend.append("userId", userId.toString());
      images.forEach((file) => formDataToSend.append("images", file));

      const response = await item.createItem(formDataToSend);
      onSubmitSuccess(response);

      setSuccessMessage("¡Ítem registrado exitosamente!");
      setTimeout(() => {
        navigate("/dashboard");
      }, 1500);
    } catch (error: unknown) {
      if (error instanceof Error) {
        setErrorMessage(`Error al registrar el ítem: ${error.message}`);
      } else {
        setErrorMessage("Error desconocido al registrar el ítem.");
      }
      onSubmitError(error);
    } finally {
      setLoading(false);
    }
  }

  const handleCancel = () => {
    navigate("/dashboard");
  };

  return (
    <motion.div
      className="w-full max-w-container mx-auto px-4 sm:px-6 py-8"
      variants={fadeIn}
      initial="hidden"
      animate="visible"
      transition={{ duration: 0.3 }}
    >
      {/* Encabezado Principal */}
      <div className="flex items-center gap-3 mb-6">
        <div className="w-10 h-10 rounded-2xl bg-primary/10 text-primary flex items-center justify-center flex-shrink-0 shadow-xs">
          <FaPlus size={18} />
        </div>
        <div>
          <h1 className="text-2xl sm:text-3xl font-extrabold text-gray-900 tracking-tight">
            Publicar nuevo ítem
          </h1>
          <p className="text-xs sm:text-sm text-gray-500 font-medium mt-0.5">
            Comparte un ítem con la comunidad y encuentra lo que necesitas intercambiando.
          </p>
        </div>
      </div>

      {/* Tarjeta Principal de Formulario */}
      <motion.div
        className="bg-white rounded-[32px] border border-gray-100 shadow-xl p-6 sm:p-8 md:p-10 grid lg:grid-cols-12 gap-8 lg:gap-10 relative overflow-hidden"
        variants={slideUp}
      >
        {/* Columna Izquierda: Tarjeta Ilustrativa */}
        <div className="lg:col-span-4 bg-[#faf6f0] border border-gray-200/70 rounded-3xl p-6 sm:p-8 flex flex-col justify-between items-center text-center relative overflow-hidden shadow-xs">
          <div className="z-10">
            <h2 className="text-2xl sm:text-3xl font-extrabold text-gray-900 leading-tight mb-3">
              Dale una nueva vida a lo que <span className="text-primary block">ya no usas</span>
            </h2>
            <p className="text-xs sm:text-sm text-gray-600 max-w-xs leading-relaxed font-medium">
              Publica tu ítem, intercambia y sé parte de un consumo más consciente.
            </p>
          </div>

          {/* Ilustración central */}
          <div className="relative flex justify-center items-center my-6 z-10 w-full">
            <div className="w-48 h-48 sm:w-56 sm:h-56 bg-primary/15 rounded-full flex items-center justify-center shadow-inner">
              <img
                src="/img/caja2.png"
                alt="Ilustración caja de intercambio"
                className="w-40 sm:w-48 h-auto object-contain drop-shadow-md"
              />
            </div>
          </div>
        </div>

        {/* Columna Derecha: Campos del Formulario */}
        <div className="lg:col-span-8">
          <AnimatePresence>
            {errorMessage && (
              <motion.div
                className="p-3.5 bg-danger/10 border border-danger/20 text-danger text-sm rounded-2xl font-semibold mb-6 text-center"
                initial={{ opacity: 0, height: 0 }}
                animate={{ opacity: 1, height: "auto" }}
                exit={{ opacity: 0, height: 0 }}
              >
                {errorMessage}
              </motion.div>
            )}
            {successMessage && (
              <motion.div
                className="p-3.5 bg-emerald-50 border border-emerald-200 text-emerald-700 text-sm rounded-2xl font-semibold mb-6 text-center"
                initial={{ opacity: 0, height: 0 }}
                animate={{ opacity: 1, height: "auto" }}
                exit={{ opacity: 0, height: 0 }}
              >
                {successMessage}
              </motion.div>
            )}
          </AnimatePresence>

          <form onSubmit={handleSubmit} className="space-y-6">
            {/* Nombre del ítem */}
            <div className="flex items-start gap-3">
              <div className="w-10 h-10 rounded-2xl bg-primary/10 text-primary flex items-center justify-center flex-shrink-0 mt-6 shadow-xs">
                <FaTag size={16} />
              </div>
              <div className="flex-1">
                <div className="flex items-center justify-between mb-1.5">
                  <label htmlFor="name" className="block text-sm font-bold text-gray-800">
                    Nombre del ítem <span className="text-danger">*</span>
                  </label>
                  <span className="text-[11px] text-gray-400 font-medium">
                    {formData.name.length}/80
                  </span>
                </div>
                <input
                  type="text"
                  id="name"
                  name="name"
                  maxLength={80}
                  value={formData.name}
                  onChange={handleInputChange}
                  placeholder="Ej: Silla de oficina ergonómica"
                  required
                  className="w-full px-4 py-3 text-sm bg-white border border-gray-300 rounded-xl focus:ring-2 focus:ring-primary focus:border-transparent outline-none transition-all text-gray-900 shadow-xs"
                />
              </div>
            </div>

            {/* Descripción del ítem */}
            <div className="flex items-start gap-3">
              <div className="w-10 h-10 rounded-2xl bg-primary/10 text-primary flex items-center justify-center flex-shrink-0 mt-6 shadow-xs">
                <FaCommentAlt size={16} />
              </div>
              <div className="flex-1">
                <div className="flex items-center justify-between mb-1.5">
                  <label htmlFor="description" className="block text-sm font-bold text-gray-800">
                    Descripción del ítem <span className="text-danger">*</span>
                  </label>
                  <span className="text-[11px] text-gray-400 font-medium">
                    {formData.description.length}/500
                  </span>
                </div>
                <textarea
                  id="description"
                  name="description"
                  maxLength={500}
                  value={formData.description}
                  onChange={handleInputChange}
                  placeholder="Describe tu ítem, su estado, características y cualquier detalle importante."
                  rows={3}
                  required
                  className="w-full p-4 text-sm bg-white border border-gray-300 rounded-xl focus:ring-2 focus:ring-primary focus:border-transparent outline-none transition-all text-gray-900 resize-none shadow-xs leading-relaxed"
                />
              </div>
            </div>

            {/* Categoría y Estado del ítem en 2 columnas */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              {/* Categoría */}
              <div className="flex items-start gap-3">
                <div className="w-10 h-10 rounded-2xl bg-primary/10 text-primary flex items-center justify-center flex-shrink-0 mt-6 shadow-xs">
                  <FaFolder size={16} />
                </div>
                <div className="flex-1">
                  <label htmlFor="category" className="block text-sm font-bold text-gray-800 mb-1.5">
                    Categoría <span className="text-danger">*</span>
                  </label>
                  <select
                    id="category"
                    name="category"
                    value={selectedCategory || ""}
                    onChange={handleCategoryChange}
                    required
                    className="w-full px-4 py-3 text-sm bg-white border border-gray-300 rounded-xl focus:ring-2 focus:ring-primary focus:border-transparent outline-none transition-all text-gray-900 shadow-xs cursor-pointer"
                  >
                    <option value="" disabled>
                      Selecciona una categoría
                    </option>
                    {categories.map((cat) => (
                      <option key={cat.id} value={cat.id}>
                        {cat.name}
                      </option>
                    ))}
                  </select>
                </div>
              </div>

              {/* Estado del ítem (Condition) */}
              <div className="flex items-start gap-3">
                <div className="w-10 h-10 rounded-2xl bg-primary/10 text-primary flex items-center justify-center flex-shrink-0 mt-6 shadow-xs">
                  <FaShieldAlt size={16} />
                </div>
                <div className="flex-1">
                  <label htmlFor="condition" className="block text-sm font-bold text-gray-800 mb-1.5">
                    Estado del ítem <span className="text-danger">*</span>
                  </label>
                  <select
                    id="condition"
                    name="condition"
                    value={formData.condition}
                    onChange={handleInputChange}
                    required
                    className="w-full px-4 py-3 text-sm bg-white border border-gray-300 rounded-xl focus:ring-2 focus:ring-primary focus:border-transparent outline-none transition-all text-gray-900 shadow-xs cursor-pointer"
                  >
                    <option value="NEW">Nuevo</option>
                    <option value="USED">Usado</option>
                  </select>
                </div>
              </div>
            </div>

            {/* Subir imágenes */}
            <div className="flex items-start gap-3">
              <div className="w-10 h-10 rounded-2xl bg-primary/10 text-primary flex items-center justify-center flex-shrink-0 mt-6 shadow-xs">
                <FaImage size={16} />
              </div>
              <div className="flex-1">
                <label htmlFor="images" className="block text-sm font-bold text-gray-800 mb-2">
                  Subir imágenes <span className="text-danger">*</span>
                </label>

                <div className="grid grid-cols-1 md:grid-cols-12 gap-4">
                  {/* Dropzone de arrastrar/seleccionar archivo */}
                  <div className="md:col-span-7 border-2 border-dashed border-primary/30 rounded-2xl p-5 text-center hover:bg-primary/5 transition-all relative flex flex-col items-center justify-center min-h-[140px] group cursor-pointer">
                    <input
                      id="images"
                      type="file"
                      accept="image/*"
                      multiple
                      onChange={handleImageChange}
                      required={images.length === 0}
                      className="absolute inset-0 opacity-0 cursor-pointer w-full h-full z-10"
                    />

                    {imagePreviews.length > 0 ? (
                      <div className="flex flex-col items-center gap-3 w-full">
                        <div className="grid grid-cols-2 sm:grid-cols-4 gap-2 w-full">
                          {imagePreviews.map((preview, index) => (
                            <img
                              key={`${images[index]?.name || "preview"}-${index}`}
                              src={preview}
                              alt={`Vista previa ${index + 1}`}
                              className="w-full aspect-square object-cover rounded-xl border border-gray-200 shadow-xs"
                            />
                          ))}
                        </div>
                        <span className="text-xs font-bold text-primary flex items-center gap-1">
                          <FaCheck size={12} /> {images.length} imagen{images.length === 1 ? "" : "es"} seleccionada{images.length === 1 ? "" : "s"}
                        </span>
                      </div>
                    ) : (
                      <>
                        <div className="w-10 h-10 rounded-full bg-primary/10 text-primary flex items-center justify-center mb-2 group-hover:scale-110 transition-transform">
                          <FaCloudUploadAlt size={20} />
                        </div>
                        <p className="text-xs font-bold text-gray-800">
                          Arrastra y suelta tus imágenes aquí
                        </p>
                        <p className="text-[11px] text-primary font-semibold mt-0.5">
                          o haz clic para seleccionar
                        </p>
                        <p className="text-[10px] text-gray-400 mt-2">
                          Formatos: JPG, PNG, WEBP. Máx 5MB por imagen.
                        </p>
                      </>
                    )}
                  </div>

                  {/* Cuadro de Consejo */}
                  <div className="md:col-span-5 bg-primary/5 border border-primary/10 rounded-2xl p-4 text-xs text-gray-600 flex flex-col justify-center">
                    <div className="flex items-center gap-2 font-bold text-gray-900 text-sm mb-1.5">
                      <FaCheckCircle className="text-primary" size={16} />
                      <span>Consejo</span>
                    </div>
                    <p className="leading-relaxed text-[11px]">
                      Las publicaciones con buenas imágenes reciben más intercambios. Te recomendamos subir fotos claras y bien iluminadas.
                    </p>
                  </div>
                </div>
              </div>
            </div>

            {/* Acciones de enviar y cancelar */}
            <div className="flex items-center justify-end gap-3 pt-6 border-t border-gray-100">
              <button
                type="button"
                onClick={handleCancel}
                className="px-6 py-2.5 rounded-xl border border-gray-200 text-gray-700 font-bold hover:bg-gray-50 transition-colors text-sm"
              >
                Cancelar
              </button>
              <button
                type="submit"
                disabled={loading}
                className="px-7 py-3 rounded-xl bg-primary hover:bg-primary-hover text-white font-bold transition-all shadow-md hover:shadow-lg text-sm flex items-center gap-2 disabled:opacity-50"
              >
                <FaPaperPlane size={13} />
                <span>{loading ? "Publicando..." : "Publicar ítem"}</span>
              </button>
            </div>
          </form>
        </div>
      </motion.div>
    </motion.div>
  );
}
