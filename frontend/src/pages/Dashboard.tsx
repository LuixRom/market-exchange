import { useEffect, useState } from "react";
import { motion } from "framer-motion";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthProvider";
import { usuario } from "../services/user/user";
import { item } from "../services/item/item";
import { Agreement } from "../services/agreement/Agreement";
import { ItemResponse } from "../interfaces/item/ItemResponse";
import { AgreementResponse } from "../interfaces/agreement/AgreementResponse";
import { fetchImage } from "../services/image/image";
import { getApiBaseUrl } from "../apis/api";
import { Button } from "../components/ui/Button";
import { fadeIn, slideUp } from "../lib/motion";
import {
  FaBoxes,
  FaExchangeAlt,
  FaHourglassHalf,
  FaChartLine,
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
  const [loading, setLoading] = useState<boolean>(true);

  useEffect(() => {
    const fetchUserData = async () => {
      try {
        const info = await usuario.getMyInfo();
        setUserName(info.firstname);

        // Obtener publicaciones del usuario
        const items = await item.getItemsByUser(info.id);
        setUserItems(items);

        // Obtener acuerdos del usuario
        const allAgreements = await Agreement.getAllAgreements();
        const myAgreements = allAgreements.filter(
          (t) => t.id_Ini === info.id || t.id_Fin === info.id
        );
        setUserTrades(myAgreements);
      } catch (err) {
        console.error("Error al cargar información del dashboard:", err);
      } finally {
        setLoading(false);
      }
    };

    fetchUserData();
  }, []);

  // Cargar miniaturas de las publicaciones
  useEffect(() => {
    const loadImages = async () => {
      const accessToken = localStorage.getItem("accessToken");
      if (!accessToken || userItems.length === 0) return;

      const promises = userItems.map(async (itm) => {
        try {
          const url = await fetchImage(
            `${getApiBaseUrl()}${itm.imageUrl}`,
            accessToken
          );
          return { id: itm.id, url };
        } catch {
          return { id: itm.id, url: "/default-placeholder.png" };
        }
      });

      const results = await Promise.all(promises);
      const map: { [key: number]: string } = {};
      results.forEach((r) => {
        map[r.id] = r.url;
      });
      setImageUrls(map);
    };

    loadImages();
  }, [userItems]);

  // Conteos calculados para las métricas
  const publishedCount = userItems.length;
  const completedTradesCount = userTrades.filter(
    (t) => t.state === "ACCEPTED"
  ).length;
  const pendingTradesCount = userTrades.filter(
    (t) => t.state === "PENDING"
  ).length;
  const impactPoints = (publishedCount * 5) + (completedTradesCount * 10) + 3;

  // Construcción de lista de actividad reciente
  const activities: ActivityItem[] = [];

  userTrades.forEach((t) => {
    if (t.state === "ACCEPTED") {
      activities.push({
        id: `trade-accepted-${t.id}`,
        type: "trade_completed",
        title: "Completaste un intercambio",
        subtitle: `${t.itemIniName} ↔ ${t.itemFinName}`,
        date: "Reciente",
      });
    } else if (t.state === "PENDING") {
      activities.push({
        id: `trade-pending-${t.id}`,
        type: "trade_pending",
        title: "Tienes un intercambio pendiente",
        subtitle: `${t.itemIniName} ↔ ${t.itemFinName}`,
        date: "Pendiente",
      });
    } else if (t.state === "REJECTED") {
      activities.push({
        id: `trade-rejected-${t.id}`,
        type: "trade_rejected",
        title: "Intercambio no concretado",
        subtitle: `${t.itemIniName} ↔ ${t.itemFinName}`,
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
    if (status === "PENDING" || status === "EN REVISIÓN") {
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

      {/* 2. Tarjetas de Resumen de Métricas (4 Columnas) */}
      <motion.div
        className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 sm:gap-6"
        variants={slideUp}
      >
        {/* Card 1: Mis Ítems Publicados */}
        <div
          onClick={() => navigate("/dashboard/category")}
          className="bg-white rounded-3xl p-5 border border-gray-100 shadow-sm hover:shadow-md transition-all flex items-center justify-between group cursor-pointer"
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
                Mis ítems publicados
              </span>
              <span className="block text-[11px] text-gray-400 font-medium">
                Activos en el marketplace
              </span>
            </div>
          </div>
          <div className="w-8 h-8 rounded-full bg-gray-50 flex items-center justify-center text-gray-400 group-hover:bg-primary group-hover:text-white transition-all flex-shrink-0">
            <FaChevronRight size={12} />
          </div>
        </div>

        {/* Card 2: Tradeos Realizados */}
        <div
          onClick={() => navigate("/dashboard/cuenta")}
          className="bg-white rounded-3xl p-5 border border-gray-100 shadow-sm hover:shadow-md transition-all flex items-center justify-between group cursor-pointer"
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
        </div>

        {/* Card 3: Tradeos Por Realizar */}
        <div
          onClick={() => navigate("/dashboard/cuenta")}
          className="bg-white rounded-3xl p-5 border border-gray-100 shadow-sm hover:shadow-md transition-all flex items-center justify-between group cursor-pointer"
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
        </div>

        {/* Card 4: Puntos de Impacto */}
        <div className="bg-white rounded-3xl p-5 border border-gray-100 shadow-sm hover:shadow-md transition-all flex items-center justify-between group cursor-pointer">
          <div className="flex items-center gap-4">
            <div className="w-12 h-12 bg-purple-100/70 text-primary rounded-2xl flex items-center justify-center font-bold text-xl flex-shrink-0">
              <FaChartLine />
            </div>
            <div>
              <span className="block text-2xl font-extrabold text-gray-900 leading-none">
                {impactPoints}
              </span>
              <span className="block text-xs font-bold text-gray-900 mt-1">
                Puntos de impacto
              </span>
              <span className="block text-[11px] text-gray-400 font-medium">
                Gracias por hacer la diferencia
              </span>
            </div>
          </div>
          <div className="w-8 h-8 rounded-full bg-gray-50 flex items-center justify-center text-gray-400 group-hover:bg-primary group-hover:text-white transition-all flex-shrink-0">
            <FaChevronRight size={12} />
          </div>
        </div>
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
                to="/dashboard/category"
                className="text-xs font-bold text-primary hover:underline flex items-center gap-1"
              >
                Ver todos <FaChevronRight size={10} />
              </Link>
            </div>

            {/* Lista de ítems */}
            {loading ? (
              <div className="py-12 text-center text-gray-400 text-sm font-medium">
                Cargando publicaciones...
              </div>
            ) : userItems.length > 0 ? (
              <div className="divide-y divide-gray-50">
                {userItems.slice(0, 4).map((itm) => (
                  <div
                    key={itm.id}
                    className="py-3.5 flex items-center justify-between gap-4 hover:bg-gray-50/60 px-2 rounded-2xl transition-colors"
                  >
                    <div className="flex items-center gap-3.5 overflow-hidden">
                      <img
                        src={imageUrls[itm.id] || "/default-placeholder.png"}
                        alt={itm.name}
                        className="w-12 h-12 sm:w-14 sm:h-14 rounded-2xl object-cover bg-gray-100 border border-gray-100 flex-shrink-0"
                      />
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
            ) : (
              <div className="py-10 text-center text-gray-500 space-y-3">
                <p className="text-sm">No tienes publicaciones activas aún.</p>
                <Button asChild size="sm" className="rounded-full">
                  <Link to="/dashboard/item/create">+ Crear primera publicación</Link>
                </Button>
              </div>
            )}
          </div>

          <div className="pt-4 border-t border-gray-100 text-center mt-4">
            <Link
              to="/dashboard/category"
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
            {loading ? (
              <div className="py-12 text-center text-gray-400 text-sm font-medium">
                Cargando actividad...
              </div>
            ) : activities.length > 0 ? (
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
            ) : (
              <div className="py-10 text-center text-gray-500 text-sm font-medium">
                No hay actividad reciente registrada.
              </div>
            )}
          </div>

          <div className="pt-4 border-t border-gray-100 text-center mt-4">
            <Link
              to="/dashboard/category"
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
