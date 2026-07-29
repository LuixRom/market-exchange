import React, { useEffect, useState } from "react";
import { AnimatePresence, motion } from "framer-motion";
import { usuario } from "../services/user/user"; // Importa el servicio de usuario
import { UsuarioResponseDto } from "../interfaces/usuario/UsuarioResponseDto";
import { UsuarioRequestDto } from "../interfaces/usuario/UsuarioRequestDto";
import { useAuth } from "../context/AuthProvider"; // Importa el contexto de autenticación
import { useNavigate } from "react-router-dom"; // Importa useNavigate para redirigir al usuario
import { Input } from "./ui/Input";
import { Button } from "./ui/Button";
import { Dialog } from "./ui/Dialog";
import { useToast } from "./ui/Toast";

export default function UserSettings() {
    const { role } = useAuth(); // Rol del usuario, calculado una vez en AuthProvider
    const { toast } = useToast();
    const [userInfo, setUserInfo] = useState<UsuarioResponseDto | null>(null);
    const [editMode, setEditMode] = useState<boolean>(false);
    const [formData, setFormData] = useState<UsuarioRequestDto | null>(null);
    const [errorMessage, setErrorMessage] = useState<string | null>(null);
    const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
    const navigate = useNavigate(); // Hook para redirigir

    // Cargar la información del usuario al montar el componente
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
                    password: "", // Se deja vacía; solo se envía si el usuario decide cambiarla
                    role: role || "USER", // Usa el rol obtenido del token
                });
            } catch (error: unknown) {
                if (error instanceof Error) {
                    setErrorMessage(`Error al obtener la información del usuario: ${error.message}`);
                } else {
                    setErrorMessage("Error desconocido al obtener la información del usuario.");
                }
            }
        }

        fetchUserInfo().catch(console.error);
    }, [role]); // Vuelve a cargar si cambia el rol

    // Manejar cambios en el formulario de edición
    function handleInputChange(event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) {
        const { name, value } = event.target;
        setFormData((prev) => (prev ? { ...prev, [name]: value } : null));
    }

    // Validar los campos del formulario antes de enviar
    function validateForm(data: UsuarioRequestDto | null): string | null {
        if (!data) return "Los datos del formulario no son válidos.";
        if (!data.firstname) return "El nombre es obligatorio.";
        if (!data.lastname) return "El apellido es obligatorio.";
        if (!data.email) return "El correo electrónico es obligatorio.";
        if (!data.phone) return "El teléfono es obligatorio.";
        if (!data.address) return "La dirección es obligatoria.";
        return null;
    }

    // Eliminar al usuario
    async function handleDeleteUser() {
        if (!userInfo) return;

        try {
            await usuario.eliminarUsuario(userInfo.id);
            setDeleteDialogOpen(false);
            toast({ title: "Cuenta eliminada con éxito.", variant: "success" });
            setUserInfo(null);
            navigate("/"); // Redirige al usuario a la página principal
        } catch (error: unknown) {
            setDeleteDialogOpen(false);
            if (error instanceof Error) {
                setErrorMessage(`Error al eliminar la cuenta: ${error.message}`);
            } else {
                setErrorMessage("Error desconocido al eliminar la cuenta.");
            }
        }
    }

    // Actualizar la información del usuario
    async function handleUpdateUser() {
        if (!formData) return;

        // Validación de los campos
        const validationError = validateForm(formData);
        if (validationError) {
            setErrorMessage(validationError);
            return;
        }

        try {
            // Solo se envía la contraseña si el usuario escribió una nueva
            const payload: UsuarioRequestDto = { ...formData };
            if (!payload.password) {
                delete payload.password;
            }

            const updatedUser = await usuario.actualizarUsuario(userInfo!.id, payload);
            toast({ title: "Información actualizada con éxito.", variant: "success" });
            setUserInfo(updatedUser);
            setEditMode(false);
            setErrorMessage(null); // Limpia el mensaje de error
        } catch (error: unknown) {
            if (error instanceof Error) {
                setErrorMessage(`Error al actualizar la información: ${error.message}`);
            } else {
                setErrorMessage("Error desconocido al actualizar la información.");
            }
        }
    }

    return (
        <div className="flex flex-col items-center justify-center">
            <div className="max-w-lg w-full">
                <AnimatePresence>
                    {errorMessage && (
                        <motion.div
                            className="text-danger text-center mb-4"
                            initial={{ opacity: 0, height: 0 }}
                            animate={{ opacity: 1, height: "auto" }}
                            exit={{ opacity: 0, height: 0 }}
                            transition={{ duration: 0.2 }}
                        >
                            {errorMessage}
                        </motion.div>
                    )}
                </AnimatePresence>

                {userInfo && !editMode ? (
                    <>
                        <p className="text-lg mb-2">
                            <strong>Nombre:</strong> {userInfo.firstname}
                        </p>
                        <p className="text-lg mb-2">
                            <strong>Apellido:</strong> {userInfo.lastname}
                        </p>
                        <p className="text-lg mb-2">
                            <strong>Email:</strong> {userInfo.email}
                        </p>
                        <p className="text-lg mb-2">
                            <strong>Teléfono:</strong> {userInfo.phone}
                        </p>
                        <p className="text-lg mb-2">
                            <strong>Dirección:</strong> {userInfo.address}
                        </p>
                        <p className="text-lg mb-2">
                            <strong>Rol:</strong> {role}
                        </p>
                        <p className="text-lg">
                            <strong>Fecha de Creación:</strong> {new Date(userInfo.createdAt).toLocaleDateString()}
                        </p>

                        <div className="mt-6 flex justify-between">
                            <Button variant="danger" onClick={() => setDeleteDialogOpen(true)}>
                                Eliminar Cuenta
                            </Button>
                            <Button variant="primary" onClick={() => setEditMode(true)}>
                                Editar Información
                            </Button>
                        </div>
                    </>
                ) : editMode && formData ? (
                    <>
                        <form className="space-y-4">
                            <div>
                                <label className="block font-bold mb-1">Nombre</label>
                                <Input
                                    type="text"
                                    name="firstname"
                                    value={formData.firstname}
                                    onChange={handleInputChange}
                                    required
                                />
                            </div>
                            <div>
                                <label className="block font-bold mb-1">Apellido</label>
                                <Input
                                    type="text"
                                    name="lastname"
                                    value={formData.lastname}
                                    onChange={handleInputChange}
                                    required
                                />
                            </div>
                            <div>
                                <label className="block font-bold mb-1">Email</label>
                                <Input
                                    type="email"
                                    name="email"
                                    value={formData.email}
                                    onChange={handleInputChange}
                                    required
                                />
                            </div>
                            <div>
                                <label className="block font-bold mb-1">Teléfono</label>
                                <Input
                                    type="text"
                                    name="phone"
                                    value={formData.phone}
                                    onChange={handleInputChange}
                                    required
                                />
                            </div>
                            <div>
                                <label className="block font-bold mb-1">Dirección</label>
                                <textarea
                                    name="address"
                                    value={formData.address}
                                    onChange={handleInputChange}
                                    className="w-full rounded-card border border-border bg-surface px-4 py-2.5 text-gray-800 shadow-sm transition-colors duration-200 focus:outline-none focus:ring-2 focus:ring-primary focus:border-primary"
                                    required
                                ></textarea>
                            </div>
                            <div>
                                <label className="block font-bold mb-1">Nueva Contraseña (opcional)</label>
                                <Input
                                    type="password"
                                    name="password"
                                    value={formData.password}
                                    onChange={handleInputChange}
                                    placeholder="Déjalo en blanco para no cambiarla"
                                />
                            </div>
                        </form>
                        <div className="mt-6 flex justify-between">
                            <Button variant="secondary" onClick={() => setEditMode(false)}>
                                Cancelar
                            </Button>
                            <Button variant="primary" onClick={handleUpdateUser}>
                                Guardar Cambios
                            </Button>
                        </div>
                    </>
                ) : (
                    <p className="text-center text-gray-500">Cargando información del usuario...</p>
                )}
            </div>

            <Dialog
                open={deleteDialogOpen}
                onOpenChange={setDeleteDialogOpen}
                title="Eliminar cuenta"
                description="¿Estás seguro de que deseas eliminar tu cuenta? Esta acción no se puede deshacer."
                footer={
                    <>
                        <Button variant="secondary" onClick={() => setDeleteDialogOpen(false)}>
                            Cancelar
                        </Button>
                        <Button variant="danger" onClick={handleDeleteUser}>
                            Eliminar
                        </Button>
                    </>
                }
            />
        </div>
    );
}
