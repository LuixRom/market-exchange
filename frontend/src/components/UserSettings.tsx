import React, { useEffect, useState } from "react";
import { AnimatePresence, motion } from "framer-motion";
import { usuario } from "../services/user/user";
import { UsuarioResponseDto } from "../interfaces/usuario/UsuarioResponseDto";
import { ProfileUpdateRequest, UsuarioRequestDto } from "../interfaces/usuario/UsuarioRequestDto";
import { useAuth } from "../context/AuthProvider";
import { useNavigate } from "react-router-dom";
import { rating } from "../services/rating/rating";
import { RatingReputation } from "../interfaces/rating/Rating";
import { Input } from "./ui/Input";
import { Button } from "./ui/Button";
import { Dialog } from "./ui/Dialog";
import { useToast } from "./ui/Toast";
import {
  FaUser,
  FaEnvelope,
  FaPhone,
  FaMapMarkerAlt,
  FaShieldAlt,
  FaCalendarAlt,
  FaLock,
  FaKey,
  FaDesktop,
  FaCheckCircle,
  FaTrash,
  FaPencilAlt,
  FaCamera,
  FaChevronRight,
  FaStar,
} from "react-icons/fa";
import { slideUp } from "../lib/motion";

export default function UserSettings() {
  const { role } = useAuth();
  const { toast } = useToast();
  const [userInfo, setUserInfo] = useState<UsuarioResponseDto | null>(null);
  const [editMode, setEditMode] = useState<boolean>(false);
  const [formData, setFormData] = useState<UsuarioRequestDto | null>(null);
  const [profileData, setProfileData] = useState<ProfileUpdateRequest>({});
  const [reputation, setReputation] = useState<RatingReputation | null>(null);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [deleteConfirmation, setDeleteConfirmation] = useState("");
  const navigate = useNavigate();

  useEffect(() => {
    async function fetchUserInfo() {
      try {
        const userData = await usuario.getMyInfo();
        setUserInfo(userData);
        setFormData({
          firstname: userData.firstname,
          lastname: userData.lastname,
          email: userData.email,
          phone: userData.phone,
          address: userData.address,
          password: "",
          role: role || "USER",
        });
        setProfileData({
          bio: userData.bio || "",
          avatarUrl: userData.avatarUrl || "",
          location: userData.location || "",
        });
        setReputation(await rating.getReputation(userData.id).catch(() => null));
      } catch (error: unknown) {
        if (error instanceof Error) {
          setErrorMessage(`Error al obtener la información del usuario: ${error.message}`);
        } else {
          setErrorMessage("Error desconocido al obtener la información del usuario.");
        }
      }
    }

    fetchUserInfo().catch(console.error);
  }, [role]);

  function handleInputChange(event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) {
    const { name, value } = event.target;
    setFormData((prev) => (prev ? { ...prev, [name]: value } : null));
  }

  function handleProfileInputChange(event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) {
    const { name, value } = event.target;
    setProfileData((prev) => ({ ...prev, [name]: value }));
  }

  function validateForm(data: UsuarioRequestDto | null): string | null {
    if (!data) return "Los datos del formulario no son válidos.";
    if (!data.firstname) return "El nombre es obligatorio.";
    if (!data.lastname) return "El apellido es obligatorio.";
    if (!data.email) return "El correo electrónico es obligatorio.";
    if (!data.phone) return "El teléfono es obligatorio.";
    if (!data.address) return "La dirección es obligatoria.";
    return null;
  }

  async function handleDeleteUser() {
    if (!userInfo) return;

    try {
      await usuario.eliminarUsuario(userInfo.id);
      setDeleteDialogOpen(false);
      setDeleteConfirmation("");
      toast({ title: "Cuenta eliminada con éxito.", variant: "success" });
      setUserInfo(null);
      navigate("/");
    } catch (error: unknown) {
      setDeleteDialogOpen(false);
      if (error instanceof Error) {
        setErrorMessage(`Error al eliminar la cuenta: ${error.message}`);
      } else {
        setErrorMessage("Error desconocido al eliminar la cuenta.");
      }
    }
  }

  async function handleUpdateUser() {
    if (!formData) return;

    const validationError = validateForm(formData);
    if (validationError) {
      setErrorMessage(validationError);
      return;
    }

    try {
      const payload: UsuarioRequestDto = { ...formData };
      if (!payload.password) {
        delete payload.password;
      }

      const [updatedUser, updatedProfile] = await Promise.all([
        usuario.actualizarUsuario(userInfo!.id, payload),
        usuario.actualizarMiPerfil(profileData),
      ]);
      toast({ title: "Información actualizada con éxito.", variant: "success" });
      setUserInfo({ ...updatedUser, ...updatedProfile });
      setEditMode(false);
      setErrorMessage(null);
    } catch (error: unknown) {
      if (error instanceof Error) {
        setErrorMessage(`Error al actualizar la información: ${error.message}`);
      } else {
        setErrorMessage("Error desconocido al actualizar la información.");
      }
    }
  }

  if (!userInfo) {
    return (
      <div className="flex items-center justify-center py-16 text-gray-500 font-semibold">
        Cargando información del usuario...
      </div>
    );
  }

  const roleLabel = role === "ADMIN" ? "Administrador" : "Usuario";
  const creationDate = new Date(userInfo.createdAt).toLocaleDateString("es-ES", {
    month: "long",
    year: "numeric",
  });
  const formattedDateFull = new Date(userInfo.createdAt).toLocaleDateString("es-ES", {
    day: "numeric",
    month: "long",
    year: "numeric",
  });

  return (
    <div className="space-y-6">
      <AnimatePresence>
        {errorMessage && (
          <motion.div
            className="p-4 bg-danger/10 border border-danger/20 text-danger text-sm rounded-2xl font-semibold text-center"
            initial={{ opacity: 0, height: 0 }}
            animate={{ opacity: 1, height: "auto" }}
            exit={{ opacity: 0, height: 0 }}
          >
            {errorMessage}
          </motion.div>
        )}
      </AnimatePresence>

      {/* 1. Tarjeta Hero de Perfil Superior */}
      <motion.div
        className="bg-[#faf6f0] border border-gray-200/70 rounded-3xl p-6 sm:p-8 relative overflow-hidden flex flex-col lg:flex-row items-center justify-between gap-6 shadow-sm"
        variants={slideUp}
        initial="hidden"
        animate="visible"
      >
        <div className="flex flex-col sm:flex-row items-center sm:items-start text-center sm:text-left gap-5">
          {/* Avatar con botón de cámara */}
          <div className="relative flex-shrink-0">
            <div className="w-24 h-24 sm:w-28 sm:h-28 rounded-full bg-primary/15 flex items-center justify-center border-4 border-white shadow-md overflow-hidden">
              <img
                src={userInfo.avatarUrl || "/img/logos_Mesa de trabajo 1 copia 3.png"}
                alt={`${userInfo.firstname} ${userInfo.lastname}`}
                className="w-full h-full object-cover"
              />
            </div>
            <button
              type="button"
              onClick={() => setEditMode(true)}
              className="absolute bottom-1 right-1 bg-primary text-white p-2 rounded-full shadow-md hover:bg-primary-hover transition-transform hover:scale-105"
              title="Cambiar información"
              aria-label="Cambiar foto de perfil"
            >
              <FaCamera size={13} />
            </button>
          </div>

          <div>
            <h2 className="text-2xl sm:text-3xl font-extrabold text-gray-900 tracking-tight">
              {userInfo.firstname} {userInfo.lastname}
            </h2>
            <div className="mt-1">
              <span className="inline-block bg-primary/10 text-primary font-bold text-xs px-3 py-1 rounded-full">
                {roleLabel}
              </span>
            </div>
            <p className="mt-3 text-xs sm:text-sm text-gray-500 font-medium flex items-center justify-center sm:justify-start gap-2">
              <FaCalendarAlt className="text-gray-400" />
              <span>Miembro desde {creationDate}</span>
            </p>
          </div>
        </div>

        {/* Bloques informativos derechos */}
        <div className="w-full lg:w-auto grid grid-cols-1 sm:grid-cols-3 gap-3 border-t lg:border-t-0 lg:border-l border-gray-200/80 pt-4 lg:pt-0 lg:pl-8">
          <div className="flex items-center gap-3 bg-white/80 p-3 rounded-2xl border border-gray-100 shadow-xs">
            <div className="w-10 h-10 rounded-xl bg-emerald-100 text-emerald-700 flex items-center justify-center flex-shrink-0">
              <FaShieldAlt size={18} />
            </div>
            <div className="text-left">
              <span className="block text-[10px] text-gray-400 font-bold uppercase tracking-wider">Rol</span>
              <span className="block text-xs font-extrabold text-gray-900">{roleLabel}</span>
            </div>
          </div>

          <div className="flex items-center gap-3 bg-white/80 p-3 rounded-2xl border border-gray-100 shadow-xs">
            <div className="w-10 h-10 rounded-xl bg-purple-100 text-purple-700 flex items-center justify-center flex-shrink-0">
              <FaEnvelope size={18} />
            </div>
            <div className="text-left overflow-hidden">
              <span className="block text-[10px] text-gray-400 font-bold uppercase tracking-wider">Correo electrónico</span>
              <span className="block text-xs font-extrabold text-gray-900 truncate max-w-[140px] sm:max-w-[160px]">
                {userInfo.email}
              </span>
            </div>
          </div>

          <div className="flex items-center gap-3 bg-white/80 p-3 rounded-2xl border border-gray-100 shadow-xs">
            <div className="w-10 h-10 rounded-xl bg-amber-100 text-amber-700 flex items-center justify-center flex-shrink-0">
              <FaCalendarAlt size={18} />
            </div>
            <div className="text-left">
              <span className="block text-[10px] text-gray-400 font-bold uppercase tracking-wider">Registro</span>
              <span className="block text-xs font-extrabold text-gray-900">{formattedDateFull}</span>
            </div>
          </div>
        </div>
      </motion.div>

      {/* 2. Grid de dos columnas: Información Personal + Seguridad */}
      <motion.div className="grid grid-cols-1 gap-4 md:grid-cols-4" variants={slideUp}>
        <div className="rounded-2xl border border-gray-100 bg-white p-4 shadow-sm">
          <span className="block text-[10px] font-bold uppercase tracking-wider text-gray-400">Correo</span>
          <span className={`mt-1 inline-flex rounded-full px-3 py-1 text-xs font-bold ${
            userInfo.emailVerified ? "bg-emerald-50 text-emerald-700" : "bg-amber-50 text-amber-700"
          }`}>
            {userInfo.emailVerified ? "Verificado" : "No verificado"}
          </span>
        </div>
        <div className="rounded-2xl border border-gray-100 bg-white p-4 shadow-sm">
          <span className="block text-[10px] font-bold uppercase tracking-wider text-gray-400">Reputacion</span>
          <span className="mt-1 flex items-center gap-2 text-sm font-extrabold text-gray-900">
            <FaStar className="text-amber-400" />
            {reputation?.averageScore ? reputation.averageScore.toFixed(1) : "-"} / 5
          </span>
          <span className="text-xs text-gray-500">{reputation?.ratingCount || 0} calificaciones</span>
        </div>
        <div className="rounded-2xl border border-gray-100 bg-white p-4 shadow-sm">
          <span className="block text-[10px] font-bold uppercase tracking-wider text-gray-400">Ubicacion</span>
          <span className="mt-1 block text-sm font-bold text-gray-900">{userInfo.location || "No registrada"}</span>
        </div>
        <div className="rounded-2xl border border-gray-100 bg-white p-4 shadow-sm">
          <span className="block text-[10px] font-bold uppercase tracking-wider text-gray-400">Bio</span>
          <span className="mt-1 line-clamp-2 block text-sm font-bold text-gray-900">{userInfo.bio || "Sin bio"}</span>
        </div>
      </motion.div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {/* Columna Izquierda: Información Personal */}
        <motion.div
          className="bg-white rounded-3xl border border-gray-100 shadow-xl p-6 sm:p-8 flex flex-col justify-between"
          variants={slideUp}
        >
          <div>
            <div className="flex items-center justify-between gap-4 mb-6">
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 rounded-2xl bg-primary/10 text-primary flex items-center justify-center flex-shrink-0">
                  <FaUser size={18} />
                </div>
                <div>
                  <h3 className="text-base font-extrabold text-gray-900">Información Personal</h3>
                  <p className="text-xs text-gray-500">Actualiza tu información personal.</p>
                </div>
              </div>
              <button
                type="button"
                onClick={() => setEditMode(true)}
                className="px-3.5 py-1.5 rounded-xl border border-primary/40 text-primary hover:bg-primary/5 text-xs font-bold transition-all flex items-center gap-1.5 flex-shrink-0"
              >
                <FaPencilAlt size={11} />
                <span>Editar información</span>
              </button>
            </div>

            <div className="space-y-3">
              <div className="p-3.5 rounded-2xl border border-gray-100 flex items-center justify-between hover:border-gray-200 transition-colors">
                <div className="flex items-center gap-3">
                  <FaUser className="text-gray-400 text-sm" />
                  <div>
                    <span className="block text-[11px] text-gray-400 font-medium">Nombre completo</span>
                    <span className="block text-sm font-bold text-gray-900">{userInfo.firstname} {userInfo.lastname}</span>
                  </div>
                </div>
                <FaChevronRight className="text-gray-300 text-xs" />
              </div>

              <div className="p-3.5 rounded-2xl border border-gray-100 flex items-center justify-between hover:border-gray-200 transition-colors">
                <div className="flex items-center gap-3">
                  <FaPhone className="text-gray-400 text-sm" />
                  <div>
                    <span className="block text-[11px] text-gray-400 font-medium">Teléfono</span>
                    <span className="block text-sm font-bold text-gray-900">{userInfo.phone || "No registrado"}</span>
                  </div>
                </div>
                <FaChevronRight className="text-gray-300 text-xs" />
              </div>

              <div className="p-3.5 rounded-2xl border border-gray-100 flex items-center justify-between hover:border-gray-200 transition-colors">
                <div className="flex items-center gap-3">
                  <FaMapMarkerAlt className="text-gray-400 text-sm" />
                  <div>
                    <span className="block text-[11px] text-gray-400 font-medium">Dirección</span>
                    <span className="block text-sm font-bold text-gray-900">{userInfo.address || "No registrada"}</span>
                  </div>
                </div>
                <FaChevronRight className="text-gray-300 text-xs" />
              </div>

              <div className="p-3.5 rounded-2xl border border-gray-100 flex items-center justify-between hover:border-gray-200 transition-colors">
                <div className="flex items-center gap-3">
                  <FaShieldAlt className="text-gray-400 text-sm" />
                  <div>
                    <span className="block text-[11px] text-gray-400 font-medium">Rol</span>
                    <span className="block text-sm font-bold text-gray-900">{roleLabel}</span>
                  </div>
                </div>
                <FaChevronRight className="text-gray-300 text-xs" />
              </div>
            </div>
          </div>
        </motion.div>

        {/* Columna Derecha: Seguridad de la cuenta */}
        <motion.div
          className="bg-white rounded-3xl border border-gray-100 shadow-xl p-6 sm:p-8 flex flex-col justify-between"
          variants={slideUp}
        >
          <div>
            <div className="flex items-center gap-3 mb-6">
              <div className="w-10 h-10 rounded-2xl bg-primary/10 text-primary flex items-center justify-center flex-shrink-0">
                <FaLock size={18} />
              </div>
              <div>
                <h3 className="text-base font-extrabold text-gray-900">Seguridad de la cuenta</h3>
                <p className="text-xs text-gray-500">Mantén tu cuenta segura.</p>
              </div>
            </div>

            <div className="space-y-3">
              <button
                type="button"
                onClick={() => setEditMode(true)}
                className="w-full p-3.5 rounded-2xl border border-gray-100 flex items-center justify-between hover:border-gray-200 transition-colors cursor-pointer text-left"
              >
                <div className="flex items-center gap-3">
                  <FaKey className="text-gray-400 text-sm" />
                  <div>
                    <span className="block text-xs font-bold text-gray-900">Cambiar contraseña</span>
                    <span className="block text-[11px] text-gray-400 font-medium">Actualiza tu contraseña regularmente.</span>
                  </div>
                </div>
                <FaChevronRight className="text-gray-300 text-xs" />
              </button>

              <div className="p-3.5 rounded-2xl border border-gray-100 flex items-center justify-between hover:border-gray-200 transition-colors">
                <div className="flex items-center gap-3">
                  <FaDesktop className="text-gray-400 text-sm" />
                  <div>
                    <span className="block text-xs font-bold text-gray-900">Sesiones activas</span>
                    <span className="block text-[11px] text-gray-400 font-medium">Gestiona los dispositivos conectados.</span>
                  </div>
                </div>
                <FaChevronRight className="text-gray-300 text-xs" />
              </div>
            </div>

            {/* Banner de estado de seguridad */}
            <div className="bg-primary/5 border border-primary/10 rounded-2xl p-4 flex items-center justify-between mt-5">
              <div className="flex items-center gap-3">
                <div className="w-9 h-9 rounded-xl bg-primary/10 text-primary flex items-center justify-center flex-shrink-0">
                  <FaShieldAlt size={16} />
                </div>
                <div>
                  <span className="block text-xs font-bold text-gray-900">Tu cuenta está segura</span>
                  <span className="block text-[11px] text-gray-500 font-medium">No se han detectado actividades sospechosas.</span>
                </div>
              </div>
              <div className="w-7 h-7 rounded-full bg-emerald-100 text-emerald-600 flex items-center justify-center flex-shrink-0">
                <FaCheckCircle size={14} />
              </div>
            </div>
          </div>
        </motion.div>
      </div>

      {/* 3. Zona de Peligro: Eliminar Cuenta */}
      <motion.div
        className="bg-rose-50/50 border border-rose-100 rounded-3xl p-5 sm:p-6 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 shadow-sm"
        variants={slideUp}
      >
        <div className="flex items-center gap-4">
          <div className="w-11 h-11 rounded-2xl bg-rose-100 text-danger flex items-center justify-center flex-shrink-0 shadow-xs">
            <FaTrash size={18} />
          </div>
          <div>
            <h4 className="text-sm font-extrabold text-gray-900">Eliminar cuenta</h4>
            <p className="text-xs text-gray-500 mt-0.5">Esta acción es permanente y no se puede deshacer.</p>
          </div>
        </div>

        <button
          type="button"
          onClick={() => setDeleteDialogOpen(true)}
          className="px-4 py-2 rounded-xl border border-danger/30 text-danger hover:bg-danger/10 text-xs font-bold transition-all flex items-center gap-2 flex-shrink-0 shadow-xs"
        >
          <FaTrash size={12} />
          <span>Eliminar cuenta</span>
        </button>
      </motion.div>

      {/* Modal / Dialog de Edición de Usuario */}
      <Dialog
        open={editMode}
        onOpenChange={setEditMode}
        title="Editar información personal"
        description="Actualiza tus datos de contacto y contraseña."
        footer={
          <div className="flex justify-end gap-3 w-full">
            <Button variant="secondary" onClick={() => setEditMode(false)}>
              Cancelar
            </Button>
            <Button variant="primary" onClick={handleUpdateUser}>
              Guardar cambios
            </Button>
          </div>
        }
      >
        {formData && (
          <form className="space-y-4 text-left py-2">
            <div>
              <label htmlFor="firstname" className="block text-xs font-bold text-gray-700 mb-1">Nombre</label>
              <Input
                id="firstname"
                type="text"
                name="firstname"
                value={formData.firstname}
                onChange={handleInputChange}
                required
              />
            </div>
            <div>
              <label htmlFor="lastname" className="block text-xs font-bold text-gray-700 mb-1">Apellido</label>
              <Input
                id="lastname"
                type="text"
                name="lastname"
                value={formData.lastname}
                onChange={handleInputChange}
                required
              />
            </div>
            <div>
              <label htmlFor="email" className="block text-xs font-bold text-gray-700 mb-1">Correo electrónico</label>
              <Input
                id="email"
                type="email"
                name="email"
                value={formData.email}
                onChange={handleInputChange}
                required
              />
            </div>
            <div>
              <label htmlFor="phone" className="block text-xs font-bold text-gray-700 mb-1">Teléfono</label>
              <Input
                id="phone"
                type="text"
                name="phone"
                value={formData.phone}
                onChange={handleInputChange}
                required
              />
            </div>
            <div>
              <label htmlFor="address" className="block text-xs font-bold text-gray-700 mb-1">Dirección</label>
              <textarea
                id="address"
                name="address"
                value={formData.address}
                onChange={handleInputChange}
                rows={2}
                className="w-full rounded-xl border border-gray-300 bg-white px-4 py-2.5 text-sm text-gray-800 focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent outline-none transition-all"
                required
              ></textarea>
            </div>
            <div>
              <label htmlFor="location" className="block text-xs font-bold text-gray-700 mb-1">Ubicacion</label>
              <Input
                id="location"
                type="text"
                name="location"
                value={profileData.location || ""}
                onChange={handleProfileInputChange}
                placeholder="Ciudad o zona"
              />
            </div>
            <div>
              <label htmlFor="avatarUrl" className="block text-xs font-bold text-gray-700 mb-1">Avatar URL</label>
              <Input
                id="avatarUrl"
                type="url"
                name="avatarUrl"
                value={profileData.avatarUrl || ""}
                onChange={handleProfileInputChange}
                placeholder="https://..."
              />
            </div>
            <div>
              <label htmlFor="bio" className="block text-xs font-bold text-gray-700 mb-1">Bio</label>
              <textarea
                id="bio"
                name="bio"
                value={profileData.bio || ""}
                onChange={handleProfileInputChange}
                rows={3}
                maxLength={255}
                className="w-full rounded-xl border border-gray-300 bg-white px-4 py-2.5 text-sm text-gray-800 focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent outline-none transition-all"
                placeholder="Cuenta brevemente sobre ti"
              />
            </div>            <div>
              <label htmlFor="password" className="block text-xs font-bold text-gray-700 mb-1">Nueva Contraseña (opcional)</label>
              <Input
                id="password"
                type="password"
                name="password"
                value={formData.password}
                onChange={handleInputChange}
                placeholder="Déjalo en blanco para no cambiarla"
              />
            </div>
          </form>
        )}
      </Dialog>

      {/* Confirmación de Eliminación */}
      <Dialog
        open={deleteDialogOpen}
        onOpenChange={(open) => {
          setDeleteDialogOpen(open);
          if (!open) setDeleteConfirmation("");
        }}
        title="Eliminar cuenta"
        description="¿Estás seguro de que deseas eliminar tu cuenta? Esta acción no se puede deshacer."
        footer={
          <>
            <Button variant="secondary" onClick={() => setDeleteDialogOpen(false)}>
              Cancelar
            </Button>
            <Button variant="danger" onClick={handleDeleteUser} disabled={deleteConfirmation !== "ELIMINAR"}>
              Eliminar
            </Button>
          </>
        }
      >
        <div>
          <label htmlFor="deleteConfirmation" className="block text-xs font-bold text-gray-700 mb-1">
            Escribe ELIMINAR para confirmar
          </label>
          <Input
            id="deleteConfirmation"
            value={deleteConfirmation}
            onChange={(event) => setDeleteConfirmation(event.target.value)}
            placeholder="ELIMINAR"
          />
        </div>
      </Dialog>
    </div>
  );
}

