import { FormEvent, useEffect, useMemo, useState } from "react";
import { FaCheck, FaMapMarkerAlt, FaTruck, FaUserCheck } from "react-icons/fa";
import { AgreementResponse } from "../interfaces/agreement/AgreementResponse";
import { ShipmentMethod, ShipmentResponse } from "../interfaces/shipment/Shipment";
import { shipment as shipmentService } from "../services/shipment/shipment";
import { Button } from "./ui/Button";
import { Spinner } from "./ui/Spinner";
import { useToast } from "./ui/Toast";

type ShipmentPanelProps = {
    trade: AgreementResponse;
    currentUserId: number | null;
    onTradeCompleted?: () => void;
};

const statusLabels: Record<string, string> = {
    PENDING: "Pendiente",
    PREPARING: "En preparacion",
    IN_TRANSIT: "En camino",
    DELIVERED: "Entregado",
    CANCELLED: "Cancelado",
};

const methodLabels: Record<ShipmentMethod, string> = {
    EXTERNAL_SHIPPING: "Envio externo",
    IN_PERSON: "Entrega presencial",
};

function toDateInputValue(value?: string | null) {
    if (!value) return "";
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) return "";
    const local = new Date(date.getTime() - date.getTimezoneOffset() * 60000);
    return local.toISOString().slice(0, 16);
}

function fromDateInputValue(value: string) {
    if (!value) return undefined;
    return new Date(value).toISOString();
}

function formatDate(value?: string | null) {
    if (!value) return "Pendiente";
    return new Date(value).toLocaleString([], {
        day: "2-digit",
        month: "2-digit",
        year: "numeric",
        hour: "2-digit",
        minute: "2-digit",
    });
}

export default function ShipmentPanel({ trade, currentUserId, onTradeCompleted }: ShipmentPanelProps) {
    const { toast } = useToast();
    const [shipment, setShipment] = useState<ShipmentResponse | null>(null);
    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);
    const [initiatorAddress, setInitiatorAddress] = useState("");
    const [receiveAddress, setReceiveAddress] = useState("");
    const [deliveryDate, setDeliveryDate] = useState("");
    const [method, setMethod] = useState<ShipmentMethod>("EXTERNAL_SHIPPING");
    const [trackingCode, setTrackingCode] = useState("");

    const isProposer = currentUserId !== null && currentUserId === trade.proposerId;
    const isReceiver = currentUserId !== null && currentUserId === trade.receiverId;
    const isParticipant = isProposer || isReceiver;

    const currentUserAlreadyConfirmed = useMemo(() => {
        if (!shipment) return false;
        return isProposer
            ? Boolean(shipment.proposerDeliveryConfirmedAt)
            : Boolean(shipment.receiverDeliveryConfirmedAt);
    }, [isProposer, shipment]);

    const canEdit = shipment?.status === "PENDING" || shipment?.status === "PREPARING";
    const canPrepare = isProposer && shipment?.status === "PENDING";
    const canShip = isProposer && shipment?.status === "PREPARING" && shipment.method === "EXTERNAL_SHIPPING";
    const canCancel = isParticipant && (shipment?.status === "PENDING" || shipment?.status === "PREPARING");
    const canConfirm =
        isParticipant &&
        shipment?.status !== "CANCELLED" &&
        !shipment?.deliveryConfirmedByBoth &&
        !currentUserAlreadyConfirmed &&
        (shipment?.method === "IN_PERSON" || shipment?.status === "IN_TRANSIT" || shipment?.status === "DELIVERED");

    useEffect(() => {
        let active = true;

        async function loadShipment() {
            try {
                setLoading(true);
                const response = await shipmentService.getByTradeProposalId(trade.id);
                if (!active) return;
                setShipment(response);
                setInitiatorAddress(response.initiatorAddress || "");
                setReceiveAddress(response.receiveAddress || "");
                setDeliveryDate(toDateInputValue(response.deliveryDate));
                setMethod(response.method);
                setTrackingCode(response.trackingCode || "");
            } catch (error) {
                console.error("Error al cargar la entrega:", error);
                toast({ title: "No se pudo cargar la entrega", variant: "danger" });
            } finally {
                if (active) setLoading(false);
            }
        }

        loadShipment();

        return () => {
            active = false;
        };
    }, [toast, trade.id]);

    async function updateLocal(next: Promise<ShipmentResponse>, successMessage: string) {
        try {
            setSaving(true);
            const response = await next;
            setShipment(response);
            setInitiatorAddress(response.initiatorAddress || "");
            setReceiveAddress(response.receiveAddress || "");
            setDeliveryDate(toDateInputValue(response.deliveryDate));
            setMethod(response.method);
            setTrackingCode(response.trackingCode || "");
            toast({ title: successMessage, variant: "success" });
            if (response.deliveryConfirmedByBoth) {
                onTradeCompleted?.();
            }
        } catch (error) {
            console.error("Error al actualizar la entrega:", error);
            toast({ title: "No se pudo actualizar la entrega", variant: "danger" });
        } finally {
            setSaving(false);
        }
    }

    async function handleSubmit(event: FormEvent<HTMLFormElement>) {
        event.preventDefault();
        if (!shipment) return;

        const payload = {
            method,
            deliveryDate: fromDateInputValue(deliveryDate),
            ...(isProposer ? { initiatorAddress } : {}),
            ...(isReceiver ? { receiveAddress } : {}),
        };

        await updateLocal(shipmentService.update(shipment.id, payload), "Entrega actualizada");
    }

    if (loading) {
        return (
            <section className="mt-6 pt-6 border-t border-gray-100">
                <Spinner label="Cargando entrega..." />
            </section>
        );
    }

    if (!shipment) {
        return null;
    }

    return (
        <section className="mt-6 pt-6 border-t border-gray-100">
            <div className="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
                <div>
                    <h2 className="text-lg font-bold text-gray-900">Entrega del intercambio</h2>
                    <p className="text-sm text-gray-500">
                        Coordina la entrega y confirma cuando ambos hayan recibido sus items.
                    </p>
                </div>
                <span className="inline-flex w-fit items-center gap-2 rounded-full bg-primary/10 px-3 py-1 text-xs font-bold text-primary">
                    <FaTruck size={13} />
                    {statusLabels[shipment.status]}
                </span>
            </div>

            <div className="mt-4 grid grid-cols-1 gap-3 md:grid-cols-3">
                <div className="rounded-card border border-gray-100 bg-muted p-4">
                    <p className="text-xs font-bold uppercase text-gray-400">Metodo</p>
                    <p className="mt-1 font-bold text-gray-900">{methodLabels[shipment.method]}</p>
                </div>
                <div className="rounded-card border border-gray-100 bg-muted p-4">
                    <p className="text-xs font-bold uppercase text-gray-400">Fecha estimada</p>
                    <p className="mt-1 font-bold text-gray-900">{formatDate(shipment.deliveryDate)}</p>
                </div>
                <div className="rounded-card border border-gray-100 bg-muted p-4">
                    <p className="text-xs font-bold uppercase text-gray-400">Tracking</p>
                    <p className="mt-1 font-bold text-gray-900">{shipment.trackingCode || "Sin codigo"}</p>
                </div>
            </div>

            <form onSubmit={handleSubmit} className="mt-4 grid grid-cols-1 gap-4 md:grid-cols-2">
                <label className="block">
                    <span className="mb-1 flex items-center gap-2 text-sm font-bold text-gray-700">
                        <FaMapMarkerAlt size={13} />
                        Direccion del proponente
                    </span>
                    <textarea
                        value={initiatorAddress}
                        onChange={(event) => setInitiatorAddress(event.target.value)}
                        disabled={!canEdit || !isProposer}
                        rows={3}
                        className="w-full rounded-card border border-gray-300 bg-white px-3 py-2 text-sm text-gray-900 outline-none transition-all focus:border-transparent focus:ring-2 focus:ring-primary disabled:bg-gray-100 disabled:text-gray-500"
                    />
                </label>

                <label className="block">
                    <span className="mb-1 flex items-center gap-2 text-sm font-bold text-gray-700">
                        <FaMapMarkerAlt size={13} />
                        Direccion del receptor
                    </span>
                    <textarea
                        value={receiveAddress}
                        onChange={(event) => setReceiveAddress(event.target.value)}
                        disabled={!canEdit || !isReceiver}
                        rows={3}
                        className="w-full rounded-card border border-gray-300 bg-white px-3 py-2 text-sm text-gray-900 outline-none transition-all focus:border-transparent focus:ring-2 focus:ring-primary disabled:bg-gray-100 disabled:text-gray-500"
                    />
                </label>

                <label className="block">
                    <span className="mb-1 block text-sm font-bold text-gray-700">Tipo de entrega</span>
                    <select
                        value={method}
                        onChange={(event) => setMethod(event.target.value as ShipmentMethod)}
                        disabled={!canEdit}
                        className="w-full rounded-card border border-gray-300 bg-white px-3 py-2 text-sm text-gray-900 outline-none transition-all focus:border-transparent focus:ring-2 focus:ring-primary disabled:bg-gray-100 disabled:text-gray-500"
                    >
                        <option value="EXTERNAL_SHIPPING">Envio externo</option>
                        <option value="IN_PERSON">Entrega presencial</option>
                    </select>
                </label>

                <label className="block">
                    <span className="mb-1 block text-sm font-bold text-gray-700">Fecha estimada</span>
                    <input
                        type="datetime-local"
                        value={deliveryDate}
                        onChange={(event) => setDeliveryDate(event.target.value)}
                        disabled={!canEdit}
                        className="w-full rounded-card border border-gray-300 bg-white px-3 py-2 text-sm text-gray-900 outline-none transition-all focus:border-transparent focus:ring-2 focus:ring-primary disabled:bg-gray-100 disabled:text-gray-500"
                    />
                </label>

                <div className="md:col-span-2 flex flex-wrap justify-end gap-3">
                    {canEdit && (
                        <Button type="submit" disabled={saving}>
                            Guardar entrega
                        </Button>
                    )}
                </div>
            </form>

            {shipment.method === "EXTERNAL_SHIPPING" && canShip && (
                <div className="mt-4 rounded-card border border-gray-100 bg-white p-4">
                    <label className="block">
                        <span className="mb-1 block text-sm font-bold text-gray-700">Codigo de seguimiento</span>
                        <input
                            value={trackingCode}
                            onChange={(event) => setTrackingCode(event.target.value)}
                            maxLength={100}
                            placeholder="Opcional"
                            className="w-full rounded-card border border-gray-300 bg-white px-3 py-2 text-sm text-gray-900 outline-none transition-all focus:border-transparent focus:ring-2 focus:ring-primary"
                        />
                    </label>
                </div>
            )}

            <div className="mt-4 grid grid-cols-1 gap-3 md:grid-cols-2">
                <div className="rounded-card border border-gray-100 bg-muted p-4">
                    <p className="flex items-center gap-2 text-sm font-bold text-gray-900">
                        <FaUserCheck size={14} />
                        Confirmacion proponente
                    </p>
                    <p className="mt-1 text-sm text-gray-500">{formatDate(shipment.proposerDeliveryConfirmedAt)}</p>
                </div>
                <div className="rounded-card border border-gray-100 bg-muted p-4">
                    <p className="flex items-center gap-2 text-sm font-bold text-gray-900">
                        <FaUserCheck size={14} />
                        Confirmacion receptor
                    </p>
                    <p className="mt-1 text-sm text-gray-500">{formatDate(shipment.receiverDeliveryConfirmedAt)}</p>
                </div>
            </div>

            <div className="mt-4 flex flex-wrap justify-end gap-3">
                {canPrepare && (
                    <Button disabled={saving} onClick={() => updateLocal(shipmentService.prepare(shipment.id), "Entrega en preparacion")}>
                        Preparar
                    </Button>
                )}
                {canShip && (
                    <Button disabled={saving} onClick={() => updateLocal(shipmentService.ship(shipment.id, { trackingCode }), "Entrega en camino")}>
                        Enviar
                    </Button>
                )}
                {canConfirm && (
                    <Button disabled={saving} onClick={() => updateLocal(shipmentService.confirmDelivery(shipment.id), "Entrega confirmada")}>
                        <FaCheck size={13} />
                        Confirmar entrega
                    </Button>
                )}
                {canCancel && (
                    <Button disabled={saving} variant="danger" onClick={() => updateLocal(shipmentService.cancel(shipment.id), "Entrega cancelada")}>
                        Cancelar entrega
                    </Button>
                )}
            </div>

            {shipment.deliveryConfirmedByBoth && (
                <div className="mt-4 rounded-card border border-emerald-200 bg-emerald-50 p-4 text-sm font-bold text-emerald-700">
                    Intercambio completado. Ambas partes confirmaron la entrega.
                </div>
            )}
        </section>
    );
}
