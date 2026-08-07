import { useEffect, useMemo, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { FaArrowLeft, FaExchangeAlt, FaHeart, FaRegHeart } from "react-icons/fa";
import { item as itemService } from "../services/item/item";
import { usuario } from "../services/user/user";
import { ItemResponse } from "../interfaces/item/ItemResponse";
import { ItemImageResponse } from "../interfaces/item/ItemImageResponse";
import { fetchImage, fetchItemImage } from "../services/image/image";
import { getApiBaseUrl } from "../apis/api";
import { Button } from "../components/ui/Button";
import { Card } from "../components/ui/Card";
import { Spinner } from "../components/ui/Spinner";
import { useToast } from "../components/ui/Toast";
import ReportButton from "../components/ReportButton";

export default function ItemDetailPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { toast } = useToast();
  const itemId = Number(id);
  const [item, setItem] = useState<ItemResponse | null>(null);
  const [images, setImages] = useState<ItemImageResponse[]>([]);
  const [imageUrls, setImageUrls] = useState<string[]>([]);
  const [activeImage, setActiveImage] = useState<string>("/default-placeholder.png");
  const [currentUserId, setCurrentUserId] = useState<number | null>(null);
  const [favorite, setFavorite] = useState(false);
  const [loading, setLoading] = useState(true);
  const [savingFavorite, setSavingFavorite] = useState(false);

  const isOwner = useMemo(
    () => currentUserId !== null && item?.user_id === currentUserId,
    [currentUserId, item?.user_id]
  );

  useEffect(() => {
    async function loadItem() {
      if (!itemId) return;

      try {
        setLoading(true);
        const [itemData, itemImages, userInfo] = await Promise.all([
          itemService.getItemById(itemId),
          itemService.getImages(itemId).catch(() => []),
          usuario.getMyInfo().catch(() => null),
        ]);

        setItem(itemData);
        setFavorite(Boolean(itemData.favorite));
        setImages(itemImages);
        setCurrentUserId(userInfo?.id ?? null);
      } catch {
        toast({ title: "No se pudo cargar el item", variant: "danger" });
      } finally {
        setLoading(false);
      }
    }

    loadItem();
  }, [itemId, toast]);

  useEffect(() => {
    async function loadImages() {
      if (!item) return;

      const accessToken = sessionStorage.getItem("accessToken");
      if (!accessToken) return;

      try {
        const sortedImages = [...images].sort((a, b) => Number(b.primary) - Number(a.primary) || a.sortOrder - b.sortOrder);
        const urls = sortedImages.length > 0
          ? await Promise.all(sortedImages.map((image) => fetchImage(`${getApiBaseUrl()}${image.imageUrl}`, accessToken)))
          : [await fetchItemImage(item, accessToken)];

        setImageUrls(urls);
        setActiveImage(urls[0] || "/default-placeholder.png");
      } catch {
        setImageUrls(["/default-placeholder.png"]);
        setActiveImage("/default-placeholder.png");
      }
    }

    loadImages();
  }, [images, item]);

  async function handleToggleFavorite() {
    if (!item || savingFavorite) return;

    try {
      setSavingFavorite(true);
      if (favorite) {
        await itemService.removeFavorite(item.id);
      } else {
        await itemService.addFavorite(item.id);
      }

      setFavorite((value) => !value);
    } catch {
      toast({ title: "No se pudo actualizar favoritos", variant: "danger" });
    } finally {
      setSavingFavorite(false);
    }
  }

  if (loading) {
    return <Spinner label="Cargando item..." />;
  }

  if (!item) {
    return <p className="text-center text-gray-500 py-10">No se encontro el item.</p>;
  }

  return (
    <div className="max-w-container mx-auto px-4 sm:px-6 py-8">
      <button
        type="button"
        onClick={() => navigate(-1)}
        className="inline-flex items-center gap-2 text-sm font-bold text-primary hover:text-primary-hover mb-6"
      >
        <FaArrowLeft size={13} />
        Volver
      </button>

      <div className="grid grid-cols-1 lg:grid-cols-12 gap-8">
        <section className="lg:col-span-7">
          <Card className="overflow-hidden bg-white">
            <img
              src={activeImage}
              alt={item.name}
              className="w-full h-[520px] max-h-[65vh] object-contain bg-gray-50 p-4"
            />
          </Card>

          {imageUrls.length > 1 && (
            <div className="grid grid-cols-4 gap-3 mt-4">
              {imageUrls.map((url, index) => (
                <button
                  key={`${url}-${index}`}
                  type="button"
                  onClick={() => setActiveImage(url)}
                  className={`rounded-card border-2 overflow-hidden bg-gray-50 ${activeImage === url ? "border-primary" : "border-transparent"}`}
                >
                  <img src={url} alt={`Imagen ${index + 1}`} className="w-full aspect-square object-contain p-1" />
                </button>
              ))}
            </div>
          )}
        </section>

        <section className="lg:col-span-5">
          <Card className="p-6">
            <div className="flex items-start justify-between gap-4">
              <div>
                <p className="text-xs font-bold text-primary uppercase">{item.categoryName}</p>
                <h1 className="text-3xl font-extrabold text-gray-900 mt-2">{item.name}</h1>
              </div>
              {!isOwner && (
                <button
                  type="button"
                  onClick={handleToggleFavorite}
                  disabled={savingFavorite}
                  className="w-11 h-11 rounded-full bg-primary/10 text-primary flex items-center justify-center disabled:opacity-50"
                  aria-label={favorite ? "Quitar de favoritos" : "Agregar a favoritos"}
                >
                  {favorite ? <FaHeart /> : <FaRegHeart />}
                </button>
              )}
            </div>

            <p className="text-gray-700 mt-5 leading-relaxed">{item.description}</p>

            <div className="flex flex-wrap gap-2 mt-5">
              <span className="inline-block bg-primary/10 text-primary text-xs font-semibold px-3 py-1 rounded-full">
                {item.condition === "NEW" ? "Nuevo" : "Usado"}
              </span>
              <span className="inline-block bg-muted text-gray-600 text-xs font-semibold px-3 py-1 rounded-full">
                {item.status}
              </span>
            </div>

            <div className="mt-6 border-t border-gray-100 pt-5 text-sm text-gray-600 space-y-2">
              <p>
                Publicado por: <span className="font-bold text-gray-900">{item.userName}</span>
              </p>
              {item.createdAt && (
                <p>Fecha: {new Date(item.createdAt).toLocaleDateString()}</p>
              )}
            </div>

            {!isOwner && item.status === "APPROVED" && (
              <div className="mt-6 flex flex-col gap-3">
                <Button
                  onClick={() => navigate(`/dashboard/agreements/item/${item.id}`)}
                  className="w-full"
                >
                  <FaExchangeAlt />
                  Tradear
                </Button>
                <ReportButton targetType="ITEM" targetId={item.id} label="Reportar item" />
              </div>
            )}
          </Card>
        </section>
      </div>
    </div>
  );
}
