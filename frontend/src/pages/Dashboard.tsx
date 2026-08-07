import { ReactNode, useEffect, useState } from "react";
import { motion } from "framer-motion";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthProvider";
import { usuario } from "../services/user/user";
import { item } from "../services/item/item";
import { Agreement } from "../services/agreement/Agreement";
import { ItemResponse } from "../interfaces/item/ItemResponse";
import { AgreementResponse } from "../interfaces/agreement/AgreementResponse";
import { fetchItemImage } from "../services/image/image";
import { Button } from "../components/ui/Button";
import { fadeIn, slideUp } from "../lib/motion";
import {
  FaBoxes,
  FaExchangeAlt,
  FaHourglassHalf,
  FaChevronRight,
  FaEllipsisV,
  FaCheckCircle,
  FaPlus,
  FaClock,
  FaTimesCircle,
} from "react-icons/fa";

interface ActivityItem {
  id: string;
  type: "trade_completed" | "trade_pending" | "trade_rejected" | "item_created";
  title: string;
  subtitle: string;
  date: string;
}

export default function Dashboard() {
  const { role } = useAuth();
  const navigate = useNavigate();
  const [userName, setUserName] = useState<string>("");
  const [userItems, setUserItems] = useState<ItemResponse[]>([]);
  const [userTrades, setUserTrades] = useState<AgreementResponse[]>([]);
  const [imageUrls, setImageUrls] = useState<{ [key: number]: string }>({});
  const [pendingItemsCount, setPendingItemsCount] = useState<number>(0);
  const [loading, setLoading] = useState<boolean>(true);

  useEffect(() => {
    const fetchUserData = async () => {
      try {
        const [info, items, myAgreements, pendingCatalog] = await Promise.all([
          usuario.getMyInfo().catch(() => ({ firstname: "" })),
          item.getMyItems().catch(() => []),
          Agreement.getMyAgreements().catch(() => []),
          role === "ADMIN"
            ? item.getCatalog({ status: "PENDING_REVIEW", page: 0, size: 1 }).catch(() => ({ totalElements: 0 }))
            : Promise.resolve(null),
        ]);

        if (info.firstname) setUserName(info.firstname);
        setUserItems(items);
        setUserTrades(myAgreements);

        if (pendingCatalog && "totalElements" in pendingCatalog) {
          setPendingItemsCount(pendingCatalog.totalElements);
        }
      } catch (err) {
        console.error("Error al cargar información del dashboard:", err);
      } finally {
        setLoading(false);
      }
    };

    fetchUserData();
  }, [role]);

  // Cargar miniaturas de las publicaciones de manera progresiva
  useEffect(() => {
    const accessToken = sessionStorage.getItem("accessToken");
    if (!accessToken || userItems.length === 0) return;

    userItems.forEach((itm) => {
      fetchItemImage(itm, accessToken)
        .then((url) => {
          setImageUrls((prev) => ({ ...prev, [itm.id]: url }));
        })
        .catch(() => {
          setImageUrls((prev) => ({ ...prev, [itm.id]: "/default-placeholder.png" }));
        });
    });
  }, [userItems]);

  // Conteos calculados para las métricas
  const publishedCount = role === "ADMIN" ? pendingItemsCount : userItems.length;
  const completedTradesCount = userTrades.filter(
    (t) => t.status === "ACCEPTED" || t.status === "COMPLETED"
  ).length;
  const pendingTradesCount = userTrades.filter(
    (t) => t.status === "PENDING"
  ).length;

  // Construcción de lista de actividad reciente
  const activities: ActivityItem[] = [];

  userTrades.forEach((t) => {
    if (t.status === "ACCEPTED" || t.status === "COMPLETED") {
      activities.push({
        id: `trade-accepted-${t.id}`,
        type: "trade_completed",
        title: "Completaste un intercambio",
        subtitle: `${t.offeredItemName} -> ${t.requestedItemName}`,
        date: "Reciente",
      });
    } else if (t.status === "PENDING") {
      activities.push({
        id: `trade-pending-${t.id}`,
        type: "trade_pending",
        title: "Tienes un intercambio pendiente",
        subtitle: `${t.offeredItemName} -> ${t.requestedItemName}`,
        date: "Pendiente",
      });
    } else if (t.status === "REJECTED" || t.status === "CANCELLED") {
      activities.push({
        id: `trade-rejected-${t.id}`,
        type: "trade_rejected",
        title: "Intercambio no concretado",
        subtitle: `${t.offeredItemName} -> ${t.requestedItemName}`,
        date: "Finalizado",
      });
    }
  });

  userItems.slice(0, 3).forEach((itm) => {
    activities.push({
      id: `item-created-${itm.id}`,
      type: "item_created",
      title: "Publicación creada",
      subtitle: itm.name,
      date: "Reciente",
    });
  });

  const getStatusBadge = (status: string) => {
    if (status === "APPROVED" || status === "ACTIVO") {
      return (
        <span className="px-3 py-1 text-xs font-bold rounded-full bg-emerald-100/80 text-emerald-700">
          Activo
        </span>
      );
    }
    if (status === "PENDING_REVIEW" || status === "PENDING" || status === "EN REVISIÓN") {
      return (
        <span className="px-3 py-1 text-xs font-bold rounded-full bg-amber-100/80 text-amber-700">
          En revisión
        </span>
      );
    }
    return (
      <span className="px-3 py-1 text-xs font-bold rounded-full bg-rose-100/80 text-rose-700">
        Pausado
      </span>
    );
  };

  let myItemsContent: ReactNode;
  if (loading) {
    myItemsContent = (
      <div className="py-12 text-center text-gray-400 text-sm font-medium">
        Cargando publicaciones...
      </div>
    );
  } else if (userItems.length > 0) {
    myItemsContent = (
      <div className="divide-y divide-gray-50">
        {userItems.slice(0, 4).map((itm) => (
          <div
            key={itm.id}
            className="py-3.5 flex items-center justify-between gap-4 hover:bg-gray-50/60 px-2 rounded-2xl transition-colors"
          >
            <div className="flex items-center gap-3.5 overflow-hidden">
              <div className={`w-12 h-12 sm:w-14 sm:h-14 rounded-2xl overflow-hidden bg-gray-100 border border-gray-100 flex-shrink-0 relative ${!imageUrls[itm.id] ? "animate-pulse" : ""}`}>
                <img
                  src={imageUrls[itm.id] || "/default-placeholder.png"}
                  alt={itm.name}
                  className="w-full h-full object-cover transition-opacity duration-300"
                  loading="lazy"
                />
              </div>
              <div className="overflow-hidden">
                <h4 className="text-sm font-bold text-gray-900 truncate">
                  {itm.name}
                </h4>
                <p className="text-xs text-gray-400 font-medium truncate">
                  {itm.categoryName || "General"}
                </p>
              </div>
            </div>

            <div className="flex items-center gap-3 flex-shrink-0">
              {getStatusBadge(itm.status)}
              <button
                type="button"
                aria-label="Opciones"
                className="text-gray-400 hover:text-gray-600 p-1.5 rounded-lg transition-colors"
              >
                <FaEllipsisV size={13} />
              </button>
            </div>
          </div>
        ))}
      </div>
    );
  } else {
    myItemsContent = (
      <div className="py-10 text-center text-gray-500 space-y-3">
        <p className="text-sm">No tienes publicaciones activas aún.</p>
        <Button asChild size="sm" className="rounded-full">
          <Link to="/dashboard/item/create">+ Crear primera publicación</Link>
        </Button>
      </div>
    );
  }

  let activityContent: ReactNode;
  if (loading) {
    activityContent = (
      <div className="py-12 text-center text-gray-400 text-sm font-medium">
        Cargando actividad...
      </div>
    );
  } else if (activities.length > 0) {
    activityContent = (
      <div className="space-y-3">
        {activities.slice(0, 4).map((act) => (
          <div
            key={act.id}
            className="p-3 rounded-2xl bg-gray-50/50 border border-gray-100/80 flex items-center justify-between gap-3 hover:bg-gray-50 transition-colors"
          >
            <div className="flex items-center gap-3 overflow-hidden">
              {act.type === "trade_completed" && (
                <div className="w-9 h-9 rounded-full bg-emerald-100 text-emerald-600 flex items-center justify-center flex-shrink-0">
                  <FaCheckCircle size={15} />
                </div>
              )}
              {act.type === "trade_pending" && (
                <div className="w-9 h-9 rounded-full bg-amber-100 text-amber-600 flex items-center justify-center flex-shrink-0">
                  <FaClock size={15} />
                </div>
              )}
              {act.type === "trade_rejected" && (
                <div className="w-9 h-9 rounded-full bg-rose-100 text-rose-600 flex items-center justify-center flex-shrink-0">
                  <FaTimesCircle size={15} />
                </div>
              )}
              {act.type === "item_created" && (
                <div className="w-9 h-9 rounded-full bg-purple-100 text-primary flex items-center justify-center flex-shrink-0">
                  <FaPlus size={13} />
                </div>
              )}

              <div className="overflow-hidden">
                <h4 className="text-xs sm:text-sm font-bold text-gray-900 truncate">
                  {act.title}
                </h4>
                <p className="text-[11px] sm:text-xs text-gray-500 font-medium truncate">
                  {act.subtitle}
                </p>
              </div>
            </div>

            <span className="text-[11px] font-semibold text-gray-400 whitespace-nowrap flex-shrink-0">
              {act.date}
            </span>
          </div>
        ))}
      </div>
    );
  } else {
    activityContent = (
      <div className="py-10 text-center text-gray-500 text-sm font-medium">
        No hay actividad reciente registrada.
      </div>
    );
  }

  return (
    <motion.div
      className="w-full max-w-container mx-auto px-4 sm:px-6 py-8 space-y-8"
      variants={fadeIn}
      initial="hidden"
      animate="visible"
      transition={{ duration: 0.3 }}
    >
      {/* 1. Banner de Bienvenida superior estilo Hero */}
      <motion.div
        className="bg-[#faf6f0] rounded-3xl p-6 sm:p-8 md:p-10 border border-gray-200/70 shadow-sm relative overflow-hidden flex flex-col md:flex-row items-center justify-between gap-6"
        variants={slideUp}
      >
        <div className="max-w-xl text-center md:text-left z-10">
          <h1 className="text-2xl sm:text-3xl md:text-4xl font-extrabold text-gray-900 tracking-tight flex items-center justify-center md:justify-start gap-2">
            ¡Hola, {userName || "Cliente"}! 👋
          </h1>
          <p className="mt-2 text-sm sm:text-base font-semibold text-primary">
            Bienvenido de vuelta a MarketExchange.
          </p>
          <p className="mt-2 text-xs sm:text-sm text-gray-600 leading-relaxed">
            Gestiona tus publicaciones, revisa tus intercambios y continúa contribuyendo a un consumo más consciente.
          </p>

          <div className="mt-6 flex flex-wrap items-center justify-center md:justify-start gap-3">
            {role === "USER" && (
              <Button asChild size="md" className="rounded-full px-6 shadow-sm font-bold">
                <Link to="/dashboard/item/create">
                  + Nueva publicación
                </Link>
              </Button>
            )}
            <Button asChild variant="secondary" size="md" className="rounded-full px-6 shadow-sm font-bold border-primary/30 text-primary hover:bg-primary/5">
              <Link to="/dashboard/explore">
                🔍 Explorar marketplace
              </Link>
            </Button>
            <Button asChild variant="secondary" size="md" className="rounded-full px-6 shadow-sm font-bold text-gray-700 hover:bg-gray-100">
              <Link to="/dashboard/category">
                Ver categorías
              </Link>
            </Button>
          </div>
        </div>

        {/* Ilustración de Caja de Reciclaje / Eco Box */}
        <div className="relative flex justify-center items-center flex-shrink-0 z-10">
          <div className="relative w-48 h-48 sm:w-56 sm:h-56 bg-purple-100/40 rounded-full flex items-center justify-center overflow-hidden">
            <img
              src="/img/logos_Mesa de trabajo 1 copia 3.png"
              alt="Market Exchange Eco"
              className="w-36 h-36 sm:w-44 sm:h-44 object-contain drop-shadow-md"
            />
          </div>
        </div>
      </motion.div>

      {/* 2. Tarjetas de Resumen de Métricas (3 Columnas) */}
      <motion.div
        className="grid grid-cols-1 sm:grid-cols-3 gap-4 sm:gap-6"
        variants={slideUp}
      >
        {/* Card 1: Mis Ítems Publicados */}
        <button
          type="button"
          onClick={() => navigate(role === "ADMIN" ? "/dashboard/admin/items" : "/dashboard/cuenta?tab=items")}
          className="w-full text-left bg-white rounded-3xl p-5 border border-gray-100 shadow-sm hover:shadow-md transition-all flex items-center justify-between group cursor-pointer"
        >
          <div className="flex items-center gap-4">
            <div className="w-12 h-12 bg-purple-100/70 text-primary rounded-2xl flex items-center justify-center font-bold text-xl flex-shrink-0">
              <FaBoxes />
            </div>
            <div>
              <span className="block text-2xl font-extrabold text-gray-900 leading-none">
                {publishedCount}
              </span>
              <span className="block text-xs font-bold text-gray-900 mt-1">
                {role === "ADMIN" ? "Items en revision" : "Mis items publicados"}
              </span>
              <span className="block text-[11px] text-gray-400 font-medium">
                {role === "ADMIN" ? "Pendientes de aprobar" : "Activos en el marketplace"}
              </span>
            </div>
          </div>
          <div className="w-8 h-8 rounded-full bg-gray-50 flex items-center justify-center text-gray-400 group-hover:bg-primary group-hover:text-white transition-all flex-shrink-0">
            <FaChevronRight size={12} />
          </div>
        </button>

        {/* Card 2: Tradeos Realizados */}
        <button
          type="button"
          onClick={() => navigate("/dashboard/cuenta?tab=trades")}
          className="w-full text-left bg-white rounded-3xl p-5 border border-gray-100 shadow-sm hover:shadow-md transition-all flex items-center justify-between group cursor-pointer"
        >
          <div className="flex items-center gap-4">
            <div className="w-12 h-12 bg-emerald-100/70 text-emerald-600 rounded-2xl flex items-center justify-center font-bold text-xl flex-shrink-0">
              <FaExchangeAlt />
            </div>
            <div>
              <span className="block text-2xl font-extrabold text-gray-900 leading-none">
                {completedTradesCount}
              </span>
              <span className="block text-xs font-bold text-gray-900 mt-1">
                Tradeos realizados
              </span>
              <span className="block text-[11px] text-gray-400 font-medium">
                Intercambios completados
              </span>
            </div>
          </div>
          <div className="w-8 h-8 rounded-full bg-gray-50 flex items-center justify-center text-gray-400 group-hover:bg-emerald-600 group-hover:text-white transition-all flex-shrink-0">
            <FaChevronRight size={12} />
          </div>
        </button>

        {/* Card 3: Tradeos Por Realizar */}
        <button
          type="button"
          onClick={() => navigate("/dashboard/cuenta?tab=pending")}
          className="w-full text-left bg-white rounded-3xl p-5 border border-gray-100 shadow-sm hover:shadow-md transition-all flex items-center justify-between group cursor-pointer"
        >
          <div className="flex items-center gap-4">
            <div className="w-12 h-12 bg-amber-100/70 text-amber-600 rounded-2xl flex items-center justify-center font-bold text-xl flex-shrink-0">
              <FaHourglassHalf />
            </div>
            <div>
              <span className="block text-2xl font-extrabold text-gray-900 leading-none">
                {pendingTradesCount}
              </span>
              <span className="block text-xs font-bold text-gray-900 mt-1">
                Tradeos por realizar
              </span>
              <span className="block text-[11px] text-gray-400 font-medium">
                Pendientes de completar
              </span>
            </div>
          </div>
          <div className="w-8 h-8 rounded-full bg-gray-50 flex items-center justify-center text-gray-400 group-hover:bg-amber-600 group-hover:text-white transition-all flex-shrink-0">
            <FaChevronRight size={12} />
          </div>
        </button>
      </motion.div>

      {/* 3. Grid Principal de 2 Columnas */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 sm:gap-8">
        {/* Columna Izquierda: Mis ítems publicados */}
        <motion.div
          className="bg-white rounded-3xl border border-gray-100 shadow-lg p-6 flex flex-col justify-between"
          variants={slideUp}
        >
          <div>
            <div className="flex items-center justify-between pb-4 border-b border-gray-100 mb-4">
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 rounded-2xl bg-primary/10 text-primary flex items-center justify-center flex-shrink-0">
                  <FaBoxes size={18} />
                </div>
                <div>
                  <h3 className="text-base font-extrabold text-gray-900">
                    Mis ítems publicados
                  </h3>
                  <p className="text-xs text-gray-500 font-medium">
                    Administra y revisa tus publicaciones recientes.
                  </p>
                </div>
              </div>

              <Link
                to={role === "ADMIN" ? "/dashboard/admin/items" : "/dashboard/cuenta?tab=items"}
                className="text-xs font-bold text-primary hover:underline flex items-center gap-1"
              >
                Ver todos <FaChevronRight size={10} />
              </Link>
            </div>

            {/* Lista de ítems */}
            {myItemsContent}
          </div>

          <div className="pt-4 border-t border-gray-100 text-center mt-4">
            <Link
                to={role === "ADMIN" ? "/dashboard/admin/items" : "/dashboard/cuenta?tab=items"}
              className="text-xs font-bold text-primary hover:text-primary-hover transition-colors inline-flex items-center gap-1.5"
            >
              Ver todas mis publicaciones <FaChevronRight size={10} />
            </Link>
          </div>
        </motion.div>

        {/* Columna Derecha: Actividad reciente */}
        <motion.div
          className="bg-white rounded-3xl border border-gray-100 shadow-lg p-6 flex flex-col justify-between"
          variants={slideUp}
        >
          <div>
            <div className="flex items-center justify-between pb-4 border-b border-gray-100 mb-4">
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 rounded-2xl bg-primary/10 text-primary flex items-center justify-center flex-shrink-0">
                  <FaExchangeAlt size={18} />
                </div>
                <div>
                  <h3 className="text-base font-extrabold text-gray-900">
                    Actividad reciente
                  </h3>
                  <p className="text-xs text-gray-500 font-medium">
                    Resumen de tu actividad en MarketExchange.
                  </p>
                </div>
              </div>
            </div>

            {/* Lista de actividades */}
            {activityContent}
          </div>

          <div className="pt-4 border-t border-gray-100 text-center mt-4">
            <Link
              to="/dashboard/cuenta?tab=trades"
              className="text-xs font-bold text-primary hover:text-primary-hover transition-colors inline-flex items-center gap-1.5"
            >
              Ver toda la actividad <FaChevronRight size={10} />
            </Link>
          </div>
        </motion.div>
      </div>
    </motion.div>
  );
}
