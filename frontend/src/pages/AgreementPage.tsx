import React, { useEffect, useState } from "react";
import { motion } from "framer-motion";
import { useNavigate, useParams, Link } from "react-router-dom";
import { AgreementRequest } from "../interfaces/agreement/AgreementRequest";
import { Agreement } from "../services/agreement/Agreement";
import { ItemResponse } from "../interfaces/item/ItemResponse";
import { item } from "../services/item/item";
import { usuario } from "../services/user/user";
import { fetchItemImage } from "../services/image/image";
import { Button } from "../components/ui/Button";
import { useToast } from "../components/ui/Toast";
import { Spinner } from "../components/ui/Spinner";
import { slideUp } from "../lib/motion";
import AuthFooter from "../components/AuthFooter";
import {
  FaHandshake,
  FaShieldAlt,
  FaExchangeAlt,
  FaSearch,
  FaCheckCircle,
  FaChevronRight,
  FaInfoCircle,
  FaPaperPlane,
  FaLeaf,
  FaBoxOpen,
  FaBoxes,
} from "react-icons/fa";

export default function AgreementPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { toast } = useToast();
  const [items, setItems] = useState<ItemResponse[]>([]);
  const [filteredItems, setFilteredItems] = useState<ItemResponse[]>([]);
  const [searchTerm, setSearchTerm] = useState<string>("");
  const [initialMessage, setInitialMessage] = useState<string>("");
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [requestedItem, setRequestedItem] = useState<ItemResponse | null>(null);
  const [offeredItem, setOfferedItem] = useState<ItemResponse | null>(null);
  const [imageUrls, setImageUrls] = useState<{ [key: number]: string }>({});
  const [loading, setLoading] = useState<boolean>(true);
  const [submitting, setSubmitting] = useState<boolean>(false);
  const [isSelfItem, setIsSelfItem] = useState<boolean>(false);

  useEffect(() => {
    async function fetchData() {
      if (!id) return;

      try {
        setLoading(true);
        setErrorMessage(null);

        // Fetching independently with fallback error handlers so target item always renders!
        const targetItemPromise = item.getItemById(Number(id)).catch((err) => {
          console.error("Error al obtener item solicitado:", err);
          return null;
        });

        const myItemsPromise = item.getMyItems().catch((err) => {
          console.error("Error al obtener mis items:", err);
          return [];
        });

        const myAgreementsPromise = Agreement.getMyAgreements().catch((err) => {
          console.error("Error al obtener mis tradeos:", err);
          return [];
        });

        const userInfoPromise = usuario.getMyInfo().catch(() => null);

        const [targetItemData, myItemsData, myAgreementsData, userInfoData] = await Promise.all([
          targetItemPromise,
          myItemsPromise,
          myAgreementsPromise,
          userInfoPromise,
        ]);

        if (!targetItemData) {
          setErrorMessage("No se pudo encontrar la publicación solicitada.");
          return;
        }

        setRequestedItem(targetItemData);

        if (targetItemData.user_id === userInfoData?.id) {
          setIsSelfItem(true);
          setErrorMessage("Esta es tu propia publicación. No puedes proponer un intercambio sobre tu propio ítem.");
        }

        // Obtener IDs de ítems que ya están participando en tradeos activos o pendientes
        const activeTradeItemIds = new Set(
          (myAgreementsData || [])
            .filter((ag) => ag.status === "PENDING" || ag.status === "ACCEPTED" || ag.status === "COMPLETED")
            .flatMap((ag) => [ag.offeredItemId, ag.requestedItemId])
        );

        // Filtrar mis ítems aprobados excluyendo aquellos que ya están en un tradeo activo
        const approvedItems = myItemsData.filter(
          (candidate) =>
            candidate.status === "APPROVED" &&
            candidate.id !== targetItemData.id &&
            !activeTradeItemIds.has(candidate.id)
        );

        setItems(approvedItems);
        setFilteredItems(approvedItems);
      } catch (err) {
        console.error("Error al cargar la información para crear la propuesta:", err);
        setErrorMessage("No se pudo cargar la información para crear el tradeo.");
      } finally {
        setLoading(false);
      }
    }

    fetchData();
  }, [id]);

  useEffect(() => {
    async function loadImages() {
      const accessToken = sessionStorage.getItem("accessToken");
      if (!accessToken) return;

      const imagesToLoad = [requestedItem, ...items].filter(Boolean) as ItemResponse[];
      const loaded = await Promise.all(
        imagesToLoad.map(async (entry) => {
          try {
            const url = await fetchItemImage(entry, accessToken);
            return { id: entry.id, url };
          } catch {
            return { id: entry.id, url: "/default-placeholder.png" };
          }
        })
      );
      setImageUrls(loaded.reduce((acc, image) => ({ ...acc, [image.id]: image.url }), {}));
    }

    if (requestedItem || items.length > 0) {
      loadImages();
    }
  }, [requestedItem, items]);

  const handleSearchChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    const term = event.target.value.toLowerCase();
    setSearchTerm(term);
    setFilteredItems(items.filter((itm) => itm.name.toLowerCase().includes(term)));
  };

  const handleTrade = async () => {
    if (isSelfItem) {
      toast({ title: "No puedes comerciar con tu propia publicación.", variant: "danger" });
      return;
    }

    if (!requestedItem || !offeredItem) {
      toast({ title: "Debes seleccionar uno de tus ítems para ofrecer.", variant: "danger" });
      return;
    }

    const agreementRequest: AgreementRequest = {
      offeredItemId: offeredItem.id,
      requestedItemId: requestedItem.id,
      initialMessage: initialMessage.trim() || undefined,
    };

    try {
      setSubmitting(true);
      const created = await Agreement.createAgreement(agreementRequest);
      toast({ title: "Propuesta de intercambio enviada", variant: "success" });
      navigate(`/dashboard/agreements/${created.id}`);
    } catch (error) {
      console.error("Error al crear la propuesta:", error);
      toast({ title: "No se pudo enviar la propuesta de intercambio.", variant: "danger" });
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) {
    return (
      <div className="min-h-[calc(100vh-80px)] flex items-center justify-center">
        <Spinner label="Cargando información del intercambio..." />
      </div>
    );
  }

  return (
    <div className="min-h-[calc(100vh-80px)] bg-[#f8faf7] flex flex-col justify-between font-sans text-gray-800">
      <div className="flex-grow max-w-container mx-auto px-4 sm:px-6 py-8 w-full">
        {/* 1. Header Banner Superior */}
        <motion.div
          className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 mb-6"
          variants={slideUp}
          initial="hidden"
          animate="visible"
        >
          <div className="flex items-center gap-3.5">
            <div className="w-12 h-12 rounded-2xl bg-purple-100 text-purple-700 flex items-center justify-center font-bold text-2xl shadow-xs flex-shrink-0">
              <FaHandshake />
            </div>
            <div>
              <h1 className="text-2xl sm:text-3xl font-extrabold text-gray-900 tracking-tight">
                Crear propuesta de intercambio
              </h1>
              <p className="text-xs sm:text-sm text-gray-500 font-medium mt-0.5">
                Intercambia artículos de forma fácil, segura y sostenible.
              </p>
            </div>
          </div>

          {/* Indicador de paso */}
          <div className="bg-white border border-gray-200/80 px-4 py-2 rounded-2xl shadow-xs text-right hidden sm:block">
            <span className="text-xs font-extrabold text-gray-700 block">Paso 1 de 2</span>
            <div className="w-16 h-1.5 bg-purple-100 rounded-full mt-1 overflow-hidden">
              <div className="w-1/2 h-full bg-purple-600 rounded-full"></div>
            </div>
          </div>
        </motion.div>

        {/* Aviso en Banner Morado Suave */}
        <div className="bg-[#faf5ff] border border-[#f0e6fe] text-[#6b21a8] rounded-2xl p-4 mb-6 flex items-center gap-3 text-xs sm:text-sm font-medium shadow-xs">
          <FaShieldAlt className="text-purple-600 text-lg flex-shrink-0" />
          <span>El propietario del ítem solicitado recibirá tu propuesta y podrá aceptarla o rechazarla.</span>
        </div>

        {/* Mensaje de Error Si Aplica */}
        {errorMessage && (
          <div className="bg-red-50 border border-red-200 text-red-700 rounded-2xl p-4 mb-6 text-sm font-semibold text-center flex items-center justify-center gap-2">
            <span>⚠️</span>
            <span>{errorMessage}</span>
          </div>
        )}

        {/* 2. Layout Principal de 2 Columnas */}
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 sm:gap-8">
          
          {/* Columna Izquierda (Item Solicitado + Tu Propuesta + Mensaje) */}
          <div className="lg:col-span-7 space-y-6">
            
            {/* Box Swap: Ítem Solicitado vs Tu Propuesta */}
            <div className="bg-white border border-gray-200/70 rounded-3xl p-5 sm:p-6 shadow-sm">
              <div className="grid grid-cols-1 md:grid-cols-11 gap-4 items-center relative">
                
                {/* Ítem Solicitado (Izquierda) */}
                <div className="md:col-span-5 bg-gray-50/60 border border-gray-100 rounded-2xl p-4 text-left">
                  <div className="flex items-center gap-1.5 text-xs font-bold text-gray-700 mb-3">
                    <span>Item solicitado</span>
                    <FaInfoCircle className="text-gray-400 text-xs" />
                  </div>

                  {requestedItem ? (
                    <div>
                      <div className="w-full h-36 bg-white rounded-xl border border-gray-100 flex items-center justify-center overflow-hidden mb-3">
                        <img
                          src={imageUrls[requestedItem.id] || "/default-placeholder.png"}
                          alt={requestedItem.name}
                          className="w-full h-full object-contain p-2"
                        />
                      </div>
                      <h3 className="font-extrabold text-gray-900 text-base line-clamp-1">
                        {requestedItem.name}
                      </h3>
                      
                      <div className="flex flex-wrap items-center gap-2 mt-2">
                        <span className="bg-purple-100/80 text-purple-700 text-[11px] font-bold px-2.5 py-0.5 rounded-full">
                          {requestedItem.categoryName}
                        </span>
                        <span className="bg-emerald-100/80 text-emerald-800 text-[11px] font-bold px-2.5 py-0.5 rounded-full flex items-center gap-1">
                          <span>{requestedItem.condition === "NEW" ? "Nuevo" : "Excelente"}</span>
                          <FaCheckCircle className="text-[10px]" />
                        </span>
                      </div>

                      <div className="mt-3 pt-3 border-t border-gray-200/60 text-xs text-gray-500 space-y-1">
                        <p>Publicado por: <strong className="text-purple-700">{requestedItem.userName}</strong></p>
                        {requestedItem.createdAt && (
                          <p>Publicado el: <span className="text-gray-600 font-medium">{new Date(requestedItem.createdAt).toLocaleDateString()}</span></p>
                        )}
                      </div>
                    </div>
                  ) : (
                    <div className="text-center py-8 text-gray-400 text-sm">
                      No se encontró el ítem solicitado.
                    </div>
                  )}
                </div>

                {/* Swap Circle Centrado */}
                <div className="md:col-span-1 flex items-center justify-center py-2 md:py-0">
                  <div className="w-11 h-11 rounded-full bg-purple-100 text-purple-700 border-4 border-white shadow-md flex items-center justify-center text-base font-bold z-10">
                    <FaExchangeAlt />
                  </div>
                </div>

                {/* Tu Propuesta / Ítem Seleccionado (Derecha) */}
                <div className="md:col-span-5 bg-gray-50/60 border border-gray-100 rounded-2xl p-4 text-left">
                  <div className="text-xs font-bold text-gray-700 mb-3">
                    Tu propuesta (ofreces)
                  </div>

                  {offeredItem ? (
                    <div>
                      <div className="w-full h-36 bg-white rounded-xl border border-emerald-200 flex items-center justify-center overflow-hidden mb-3 relative">
                        <img
                          src={imageUrls[offeredItem.id] || "/default-placeholder.png"}
                          alt={offeredItem.name}
                          className="w-full h-full object-contain p-2"
                        />
                        <span className="absolute top-2 right-2 bg-emerald-500 text-white p-1 rounded-full text-xs shadow-xs">
                          <FaCheckCircle />
                        </span>
                      </div>
                      <h3 className="font-extrabold text-gray-900 text-base line-clamp-1">
                        {offeredItem.name}
                      </h3>
                      <p className="text-xs text-purple-700 font-bold mt-1">
                        {offeredItem.categoryName}
                      </p>
                      <button
                        type="button"
                        onClick={() => setOfferedItem(null)}
                        className="mt-3 text-xs text-purple-700 font-bold hover:underline block"
                      >
                        Cambiar ítem seleccionado
                      </button>
                    </div>
                  ) : (
                    <div className="border-2 border-dashed border-purple-200 bg-purple-50/30 rounded-xl p-5 text-center flex flex-col items-center justify-center min-h-[190px]">
                      <div className="w-10 h-10 rounded-full bg-purple-100 text-purple-600 flex items-center justify-center text-lg mb-2">
                        <FaBoxOpen />
                      </div>
                      <p className="font-bold text-gray-900 text-sm">Selecciona un item</p>
                      <p className="text-[11px] text-gray-500 mt-1 leading-tight">
                        Elige uno de tus artículos aprobados para ofrecer en el intercambio.
                      </p>
                      <button
                        type="button"
                        onClick={() => {
                          const element = document.getElementById("search-my-items");
                          element?.focus();
                        }}
                        className="mt-3 bg-[#6d28d9] hover:bg-[#5b21b6] text-white text-xs font-bold px-4 py-2 rounded-xl shadow-xs transition-all"
                      >
                        Seleccionar item
                      </button>
                    </div>
                  )}
                </div>

              </div>
            </div>

            {/* Box Mensaje para el Propietario */}
            <div className="bg-white border border-gray-200/70 rounded-3xl p-5 sm:p-6 shadow-sm text-left">
              <label htmlFor="initialMessage" className="block text-sm font-bold text-gray-900 mb-2 flex items-center justify-between">
                <div className="flex items-center gap-2">
                  <span className="text-purple-600 text-base">💬</span>
                  <span>Mensaje para el propietario (opcional)</span>
                </div>
                <span className="text-xs text-gray-400 font-mono">
                  {initialMessage.length}/500
                </span>
              </label>

              <textarea
                id="initialMessage"
                value={initialMessage}
                onChange={(e) => setInitialMessage(e.target.value)}
                maxLength={500}
                rows={3}
                placeholder="Ej: Hola, me interesa tu item. Te ofrezco este producto porque está en buen estado..."
                className="w-full p-4 text-sm bg-gray-50/50 border border-gray-200 rounded-2xl focus:ring-2 focus:ring-purple-600 focus:border-transparent outline-none transition-all text-gray-900 resize-none font-normal"
              />
            </div>

          </div>

          {/* Columna Derecha (Selecciona uno de tus items) */}
          <div className="lg:col-span-5">
            <div className="bg-white border border-gray-200/70 rounded-3xl p-5 sm:p-6 shadow-sm text-left h-full flex flex-col justify-between">
              <div>
                <h2 className="text-base sm:text-lg font-extrabold text-gray-900 mb-4">
                  Selecciona uno de tus items
                </h2>

                {/* Buscador */}
                <div className="relative mb-4">
                  <FaSearch className="absolute left-3.5 top-1/2 -translate-y-1/2 text-gray-400 text-sm pointer-events-none" />
                  <input
                    id="search-my-items"
                    type="text"
                    value={searchTerm}
                    onChange={handleSearchChange}
                    placeholder="Buscar entre mis publicaciones..."
                    className="w-full pl-10 pr-4 py-2.5 text-sm bg-gray-50/50 border border-gray-200 rounded-xl focus:ring-2 focus:ring-purple-600 focus:border-transparent outline-none transition-all text-gray-900"
                  />
                </div>

                {/* Lista de Publicaciones Aprobadas */}
                {filteredItems.length > 0 ? (
                  <div className="space-y-3 max-h-[380px] overflow-y-auto pr-1">
                    {filteredItems.map((candidate) => {
                      const isSelected = offeredItem?.id === candidate.id;
                      return (
                        <button
                          key={candidate.id}
                          type="button"
                          onClick={() => setOfferedItem(candidate)}
                          className={`w-full text-left p-3 rounded-2xl border transition-all cursor-pointer flex items-center justify-between gap-3 group ${
                            isSelected
                              ? "bg-emerald-50/50 border-emerald-300 shadow-xs"
                              : "bg-white border-gray-100 hover:border-gray-200 hover:shadow-xs"
                          }`}
                        >
                          <div className="flex items-center gap-3 min-w-0">
                            <div className="w-12 h-12 bg-gray-50 rounded-xl border border-gray-100 flex items-center justify-center overflow-hidden flex-shrink-0">
                              <img
                                src={imageUrls[candidate.id] || "/default-placeholder.png"}
                                alt={candidate.name}
                                className="w-full h-full object-contain p-1"
                              />
                            </div>
                            <div className="min-w-0">
                              <h4 className="font-bold text-gray-900 text-xs sm:text-sm truncate">
                                {candidate.name}
                              </h4>
                              <p className="text-xs text-gray-500 font-medium">
                                {candidate.categoryName}
                              </p>
                            </div>
                          </div>

                          <div className="flex-shrink-0">
                            {isSelected ? (
                              <div className="w-6 h-6 rounded-full bg-emerald-500 text-white flex items-center justify-center text-xs shadow-xs">
                                <FaCheckCircle />
                              </div>
                            ) : (
                              <div className="w-7 h-7 rounded-full bg-gray-50 text-gray-400 group-hover:bg-purple-100 group-hover:text-purple-700 flex items-center justify-center text-xs transition-all">
                                <FaChevronRight size={10} />
                              </div>
                            )}
                          </div>
                        </button>
                      );
                    })}
                  </div>
                ) : (
                  <div className="py-8 text-center bg-gray-50/50 rounded-2xl border border-dashed border-gray-200 p-4">
                    <p className="text-xs sm:text-sm text-gray-500 font-medium">
                      {searchTerm
                        ? "No se encontraron ítems con esa búsqueda."
                        : "No tienes publicaciones aprobadas aún para ofrecer."}
                    </p>
                    {!searchTerm && (
                      <Button
                        asChild
                        size="sm"
                        className="mt-3 rounded-full text-xs"
                      >
                        <Link to="/dashboard/item/create">+ Crear publicación</Link>
                      </Button>
                    )}
                  </div>
                )}
              </div>

              {/* Boton Inferior para ver todas mis publicaciones */}
              <div className="mt-4 pt-4 border-t border-gray-100">
                <button
                  type="button"
                  onClick={() => navigate("/dashboard/cuenta?tab=items")}
                  className="w-full py-3 bg-purple-50/70 hover:bg-purple-100/70 text-purple-700 font-bold text-xs rounded-xl transition-all flex items-center justify-center gap-2"
                >
                  <FaBoxes size={14} />
                  <span>Ver todas mis publicaciones</span>
                </button>
              </div>

            </div>
          </div>

        </div>

        {/* 3. Barra Inferior de Acción y Envio de Propuesta */}
        <div className="bg-white border border-gray-200/80 rounded-3xl p-5 mt-8 shadow-sm flex flex-col sm:flex-row items-center justify-between gap-4">
          <div className="flex items-center gap-3.5 text-left">
            <div className="w-11 h-11 rounded-2xl bg-emerald-100/70 text-emerald-700 flex items-center justify-center text-xl flex-shrink-0">
              <FaLeaf />
            </div>
            <div className="text-xs sm:text-sm text-gray-600 font-medium">
              <p>Un intercambio responsable construye <strong className="text-emerald-700 font-bold">una comunidad más consciente.</strong></p>
            </div>
          </div>

          <button
            type="button"
            onClick={handleTrade}
            disabled={!offeredItem || submitting || isSelfItem}
            className="w-full sm:w-auto bg-[#6d28d9] hover:bg-[#5b21b6] active:bg-[#4c1d95] text-white font-extrabold text-sm sm:text-base py-3.5 px-8 rounded-2xl shadow-lg shadow-purple-600/25 transition-all duration-200 hover:-translate-y-0.5 flex flex-col items-center justify-center gap-0.5 group cursor-pointer disabled:opacity-50 disabled:cursor-not-allowed"
          >
            <div className="flex items-center gap-2">
              <FaPaperPlane className="text-sm" />
              <span>{submitting ? "Enviando propuesta..." : "Enviar propuesta de intercambio"}</span>
            </div>
            <span className="text-[11px] font-normal text-purple-200">El propietario recibirá una notificación</span>
          </button>
        </div>

      </div>

      <AuthFooter />
    </div>
  );
}
