import { FormEvent, useCallback, useEffect, useMemo, useState } from "react";
import { FaLock, FaUnlock, FaUserSlash } from "react-icons/fa";
import { UsuarioResponseDto } from "../interfaces/usuario/UsuarioResponseDto";
import { usuario } from "../services/user/user";
import { Button } from "../components/ui/Button";
import { Spinner } from "../components/ui/Spinner";
import { useToast } from "../components/ui/Toast";

type AdminAction = "suspend" | "block" | null;

export default function AdminUsersPage() {
    const { toast } = useToast();
    const [users, setUsers] = useState<UsuarioResponseDto[]>([]);
    const [query, setQuery] = useState("");
    const [selected, setSelected] = useState<UsuarioResponseDto | null>(null);
    const [action, setAction] = useState<AdminAction>(null);
    const [reason, setReason] = useState("");
    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);

    const filteredUsers = useMemo(() => {
        const term = query.trim().toLowerCase();
        if (!term) return users;
        return users.filter((user) =>
            `${user.firstname} ${user.lastname} ${user.email}`.toLowerCase().includes(term)
        );
    }, [query, users]);

    const loadUsers = useCallback(async () => {
        try {
            setLoading(true);
            setUsers(await usuario.listarUsuarios());
        } catch (error) {
            console.error("Error al cargar usuarios:", error);
            toast({ title: "No se pudieron cargar los usuarios", variant: "danger" });
        } finally {
            setLoading(false);
        }
    }, [toast]);

    useEffect(() => {
        loadUsers();
    }, [loadUsers]);

    function updateUser(updated: UsuarioResponseDto) {
        setUsers((current) => current.map((user) => (user.id === updated.id ? updated : user)));
        setSelected(updated);
    }

    async function runSimpleAction(target: UsuarioResponseDto, kind: "unsuspend" | "unblock") {
        try {
            setSaving(true);
            const updated = kind === "unsuspend"
                ? await usuario.quitarSuspension(target.id)
                : await usuario.desbloquearUsuario(target.id);
            updateUser(updated);
            toast({ title: "Usuario actualizado", variant: "success" });
        } catch (error) {
            console.error("Error al actualizar usuario:", error);
            toast({ title: "No se pudo actualizar el usuario", variant: "danger" });
        } finally {
            setSaving(false);
        }
    }

    async function handleReasonAction(event: FormEvent<HTMLFormElement>) {
        event.preventDefault();
        if (!selected || !action) return;

        try {
            setSaving(true);
            const updated = action === "suspend"
                ? await usuario.suspenderUsuario(selected.id, reason.trim() || undefined)
                : await usuario.bloquearUsuario(selected.id, reason.trim() || undefined);
            updateUser(updated);
            setAction(null);
            setReason("");
            toast({ title: "Usuario actualizado", variant: "success" });
        } catch (error) {
            console.error("Error al actualizar usuario:", error);
            toast({ title: "No se pudo actualizar el usuario", variant: "danger" });
        } finally {
            setSaving(false);
        }
    }

    return (
        <div className="w-full max-w-container mx-auto px-4 sm:px-6 py-8">
            <div className="mb-6">
                <h1 className="text-2xl sm:text-3xl font-extrabold text-gray-900">Usuarios</h1>
                <p className="mt-1 text-sm text-gray-600">Administra suspensiones y bloqueos.</p>
            </div>

            <input
                value={query}
                onChange={(event) => setQuery(event.target.value)}
                placeholder="Buscar por nombre o correo..."
                className="mb-4 w-full rounded-card border border-gray-300 px-4 py-3 text-sm outline-none focus:border-transparent focus:ring-2 focus:ring-primary"
            />

            {loading ? (
                <Spinner label="Cargando usuarios..." />
            ) : (
                <div className="grid grid-cols-1 gap-4 lg:grid-cols-[1fr_360px]">
                    <div className="overflow-hidden rounded-card border border-gray-100 bg-white shadow-card">
                        <div className="grid grid-cols-[1fr_140px_180px] gap-3 border-b border-gray-100 bg-muted px-4 py-3 text-xs font-bold uppercase text-gray-500">
                            <span>Usuario</span>
                            <span>Estado</span>
                            <span>Acciones</span>
                        </div>
                        {filteredUsers.map((user) => (
                            <div key={user.id} className="grid grid-cols-[1fr_140px_180px] gap-3 border-b border-gray-100 px-4 py-3 text-sm last:border-b-0">
                                <button type="button" onClick={() => setSelected(user)} className="text-left">
                                    <p className="font-bold text-gray-900">{user.firstname} {user.lastname}</p>
                                    <p className="text-xs text-gray-500">{user.email}</p>
                                </button>
                                <div className="space-y-1">
                                    <span className={`block rounded-full px-2 py-1 text-center text-xs font-bold ${user.blocked ? "bg-danger/10 text-danger" : "bg-emerald-50 text-emerald-700"}`}>
                                        {user.blocked ? "Bloqueado" : "Activo"}
                                    </span>
                                    {user.suspended && (
                                        <span className="block rounded-full bg-amber-50 px-2 py-1 text-center text-xs font-bold text-amber-700">
                                            Suspendido
                                        </span>
                                    )}
                                </div>
                                <div className="flex flex-wrap gap-2">
                                    {user.suspended ? (
                                        <Button size="sm" variant="secondary" disabled={saving} onClick={() => runSimpleAction(user, "unsuspend")}>
                                            <FaUnlock size={12} />
                                            Quitar
                                        </Button>
                                    ) : (
                                        <Button size="sm" variant="secondary" onClick={() => { setSelected(user); setAction("suspend"); }}>
                                            <FaUserSlash size={12} />
                                            Suspender
                                        </Button>
                                    )}
                                    {user.blocked ? (
                                        <Button size="sm" variant="secondary" disabled={saving} onClick={() => runSimpleAction(user, "unblock")}>
                                            <FaUnlock size={12} />
                                            Desbloquear
                                        </Button>
                                    ) : (
                                        <Button size="sm" variant="danger" onClick={() => { setSelected(user); setAction("block"); }}>
                                            <FaLock size={12} />
                                            Bloquear
                                        </Button>
                                    )}
                                </div>
                            </div>
                        ))}
                    </div>

                    <aside className="rounded-card border border-gray-100 bg-white p-4 shadow-card">
                        {selected ? (
                            <>
                                <h2 className="text-lg font-bold text-gray-900">{selected.firstname} {selected.lastname}</h2>
                                <p className="text-sm text-gray-500">{selected.email}</p>
                                <div className="mt-4 space-y-2 text-sm text-gray-600">
                                    <p><strong>Rol:</strong> {selected.role || "USER"}</p>
                                    <p><strong>Verificado:</strong> {selected.emailVerified ? "Si" : "No"}</p>
                                    {selected.suspensionReason && <p><strong>Suspension:</strong> {selected.suspensionReason}</p>}
                                    {selected.blockedReason && <p><strong>Bloqueo:</strong> {selected.blockedReason}</p>}
                                </div>

                                {action && (
                                    <form onSubmit={handleReasonAction} className="mt-5 border-t border-gray-100 pt-4">
                                        <h3 className="text-sm font-bold text-gray-900">
                                            {action === "suspend" ? "Suspender usuario" : "Bloquear usuario"}
                                        </h3>
                                        <textarea
                                            value={reason}
                                            onChange={(event) => setReason(event.target.value)}
                                            rows={4}
                                            maxLength={500}
                                            placeholder="Motivo opcional..."
                                            className="mt-2 w-full rounded-card border border-gray-300 px-3 py-2 text-sm"
                                        />
                                        <div className="mt-3 flex justify-end gap-2">
                                            <Button type="button" variant="ghost" disabled={saving} onClick={() => setAction(null)}>
                                                Cancelar
                                            </Button>
                                            <Button type="submit" variant={action === "block" ? "danger" : "primary"} disabled={saving}>
                                                Confirmar
                                            </Button>
                                        </div>
                                    </form>
                                )}
                            </>
                        ) : (
                            <p className="text-sm text-gray-500">Selecciona un usuario para ver detalles.</p>
                        )}
                    </aside>
                </div>
            )}
        </div>
    );
}
