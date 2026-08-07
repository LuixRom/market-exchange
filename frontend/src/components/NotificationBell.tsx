import { ReactNode, useEffect, useMemo, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { FaBell, FaBoxOpen, FaCheckCircle, FaEnvelope, FaExchangeAlt } from "react-icons/fa";
import { DropdownMenu, DropdownMenuItem } from "./ui/DropdownMenu";
import { notification } from "../services/notification/notification";
import { realtimeClient } from "../services/realtime/stompClient";
import { NotificationResponse } from "../interfaces/notification/NotificationResponse";

function notificationIcon(type: string) {
    if (type.includes("CHAT")) return <FaEnvelope size={14} />;
    if (type.includes("ITEM")) return <FaBoxOpen size={14} />;
    if (type.includes("ACCEPTED") || type.includes("COMPLETED")) return <FaCheckCircle size={14} />;
    return <FaExchangeAlt size={14} />;
}

function notificationPath(entry: NotificationResponse) {
    if (entry.tradeProposalId) return `/dashboard/agreements/${entry.tradeProposalId}`;
    if (entry.itemId) return `/dashboard/item/${entry.itemId}`;
    return "/dashboard/cuenta";
}

function addNotification(current: NotificationResponse[], entry: NotificationResponse) {
    if (current.some((existing) => existing.id === entry.id)) return current;
    return [entry, ...current].slice(0, 20);
}

function formatNotificationTime(value?: string) {
    if (!value) return "";
    return new Date(value).toLocaleString([], {
        day: "2-digit",
        month: "2-digit",
        hour: "2-digit",
        minute: "2-digit",
    });
}

export default function NotificationBell() {
    const navigate = useNavigate();
    const [items, setItems] = useState<NotificationResponse[]>([]);
    const [unreadCount, setUnreadCount] = useState(0);
    const [loading, setLoading] = useState(true);

    const visibleItems = useMemo(() => items.slice(0, 8), [items]);

    async function refreshNotifications() {
        const [latest, count] = await Promise.all([
            notification.list(false),
            notification.unreadCount(),
        ]);
        setItems(latest);
        setUnreadCount(count);
    }

    useEffect(() => {
        let active = true;

        async function load() {
            try {
                setLoading(true);
                await refreshNotifications();
            } catch (error) {
                console.warn("No se pudieron cargar las notificaciones:", error);
            } finally {
                if (active) setLoading(false);
            }
        }

        load();

        return () => {
            active = false;
        };
    }, []);

    useEffect(() => {
        let notificationSubscription: { unsubscribe: () => void } | null = null;
        let agreementSubscription: { unsubscribe: () => void } | null = null;
        let active = true;

        async function subscribe() {
            try {
                await realtimeClient.connect();
                if (!active) return;

                notificationSubscription = realtimeClient.subscribeToNotifications((entry) => {
                    setItems((current) => addNotification(current, entry));
                    if (!entry.read) {
                        setUnreadCount((current) => current + 1);
                    }
                });

                agreementSubscription = realtimeClient.subscribeToAgreementEvents(() => {
                    refreshNotifications().catch(() => undefined);
                });
            } catch {
                // El listado inicial ya se cargo via REST; el realtime es best-effort.
            }
        }

        subscribe();

        return () => {
            active = false;
            notificationSubscription?.unsubscribe();
            agreementSubscription?.unsubscribe();
        };
    }, []);

    async function openNotification(entry: NotificationResponse) {
        try {
            if (!entry.read) {
                const updated = await notification.markAsRead(entry.id);
                setItems((current) =>
                    current.map((item) => (item.id === entry.id ? updated : item))
                );
                setUnreadCount((current) => Math.max(current - 1, 0));
            }
        } catch (error) {
            console.warn("No se pudo marcar la notificacion como leida:", error);
        } finally {
            navigate(notificationPath(entry));
        }
    }

    let listContent: ReactNode;
    if (loading) {
        listContent = <p className="px-3 py-6 text-center text-sm text-gray-500">Cargando...</p>;
    } else if (visibleItems.length === 0) {
        listContent = <p className="px-3 py-6 text-center text-sm text-gray-500">Sin notificaciones todavia.</p>;
    } else {
        listContent = visibleItems.map((entry) => (
            <DropdownMenuItem
                key={entry.id}
                onSelect={() => openNotification(entry)}
                className={`mb-1 flex gap-3 rounded-card p-3 ${
                    entry.read ? "bg-white" : "bg-primary/5"
                }`}
            >
                <span className="mt-0.5 inline-flex h-8 w-8 shrink-0 items-center justify-center rounded-full bg-primary/10 text-primary">
                    {notificationIcon(entry.type)}
                </span>
                <span className="min-w-0 flex-1">
                    <span className="block truncate text-sm font-bold text-gray-900">
                        {entry.title}
                    </span>
                    <span className="mt-0.5 block line-clamp-2 text-xs text-gray-600">
                        {entry.message}
                    </span>
                    <span className="mt-1 block text-[11px] text-gray-400">
                        {formatNotificationTime(entry.createdAt)}
                    </span>
                </span>
                {!entry.read && <span className="mt-1 h-2 w-2 shrink-0 rounded-full bg-primary" />}
            </DropdownMenuItem>
        ));
    }

    return (
        <DropdownMenu
            align="end"
            trigger={
                <button
                    type="button"
                    className="relative inline-flex h-10 w-10 items-center justify-center rounded-full border border-primary/20 bg-primary/10 text-primary transition-all hover:bg-primary/20 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2"
                    aria-label="Notificaciones"
                >
                    <FaBell size={16} />
                    {unreadCount > 0 && (
                        <span className="absolute -right-1 -top-1 min-w-5 rounded-full bg-danger px-1.5 py-0.5 text-[10px] font-bold leading-none text-white">
                            {unreadCount > 9 ? "9+" : unreadCount}
                        </span>
                    )}
                </button>
            }
        >
            <div className="w-80 max-w-[calc(100vw-2rem)] p-2">
                <div className="flex items-center justify-between border-b border-gray-100 px-2 pb-2">
                    <div>
                        <p className="text-sm font-bold text-gray-900">Notificaciones</p>
                        <p className="text-xs text-gray-500">
                            {unreadCount > 0 ? `${unreadCount} sin leer` : "Todo al dia"}
                        </p>
                    </div>
                    <Link to="/dashboard/cuenta" className="text-xs font-bold text-primary">
                        Ver cuenta
                    </Link>
                </div>

                <div className="mt-2 max-h-96 overflow-y-auto">
                    {listContent}
                </div>
            </div>
        </DropdownMenu>
    );
}
