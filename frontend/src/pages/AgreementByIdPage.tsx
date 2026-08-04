import { useEffect, useMemo, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { Agreement } from "../services/agreement/Agreement";
import { AgreementResponse } from "../interfaces/agreement/AgreementResponse";
import { item } from "../services/item/item";
import { usuario } from "../services/user/user";
import { ItemCondition, ItemResponse, ItemStatus } from "../interfaces/item/ItemResponse";
import { fetchItemImage } from "../services/image/image";
import { Card } from "../components/ui/Card";
import { Spinner } from "../components/ui/Spinner";
import { Button } from "../components/ui/Button";
import { useToast } from "../components/ui/Toast";
import AgreementChat from "../components/AgreementChat";
import ShipmentPanel from "../components/ShipmentPanel";
import RatingPanel from "../components/RatingPanel";
import ReportButton from "../components/ReportButton";

const statusLabels: Record<string, string> = {
    PENDING: "Pendiente",
    ACCEPTED: "Aceptado",
    REJECTED: "Rechazado",
    CANCELLED: "Cancelado",
    EXPIRED: "Expirado",
    COMPLETED: "Completado",
};

function historicalItem(
    id: number,
    name: string,
    ownerEmail: string,
    status: ItemStatus
): ItemResponse {
    return {
        id,
        name,
        description: "Detalle historico del item intercambiado.",
        categoryName: "No disponible",
        condition: ItemCondition.USED,
        userName: ownerEmail,
        status,
        user_id: 0,
    };
}

export default function AgreementByIdPage() {
    const { id } = useParams<{ id: string }>();
    const navigate = useNavigate();
    const { toast } = useToast();
    const [trade, setTrade] = useState<AgreementResponse | null>(null);
    const [requestedItem, setRequestedItem] = useState<ItemResponse | null>(null);
    const [offeredItem, setOfferedItem] = useState<ItemResponse | null>(null);
    const [currentUserId, setCurrentUserId] = useState<number | null>(null);
    const [currentUserEmail, setCurrentUserEmail] = useState<string | null>(null);
    const [imageUrls, setImageUrls] = useState<{ [key: number]: string }>({});
    const [errorMessage, setErrorMessage] = useState<string | null>(null);
    const [saving, setSaving] = useState(false);

    const canReceiverAct = useMemo(
        () => trade?.status === "PENDING" && currentUserId === trade.receiverId,
        [currentUserId, trade]
    );
    const canProposerCancel = useMemo(
        () => trade?.status === "PENDING" && currentUserId === trade.proposerId,
        [currentUserId, trade]
    );
    const counterpartId = useMemo(() => {
        if (!trade || currentUserId === null) return null;
        if (currentUserId === trade.proposerId) return trade.receiverId;
        if (currentUserId === trade.receiverId) return trade.proposerId;
        return null;
    }, [currentUserId, trade]);

    useEffect(() => {
        async function fetchTradeData() {
            try {
                if (!id) return;

                const [fetchedTrade, userInfo] = await Promise.all([
                    Agreement.getAgreementById(Number(id)),
                    usuario.getMyInfo().catch(() => null),
                ]);
                setTrade(fetchedTrade);
                setCurrentUserId(userInfo?.id ?? null);
                setCurrentUserEmail(userInfo?.email ?? null);

                const [offered, requested] = await Promise.all([
                    item.getItemById(fetchedTrade.offeredItemId).catch(() =>
                        historicalItem(
                            fetchedTrade.offeredItemId,
                            fetchedTrade.offeredItemName,
                            fetchedTrade.proposerEmail,
                            fetchedTrade.status === "COMPLETED" ? ItemStatus.EXCHANGED : ItemStatus.RESERVED
                        )
                    ),
                    item.getItemById(fetchedTrade.requestedItemId).catch(() =>
                        historicalItem(
                            fetchedTrade.requestedItemId,
                            fetchedTrade.requestedItemName,
                            fetchedTrade.receiverEmail,
                            fetchedTrade.status === "COMPLETED" ? ItemStatus.EXCHANGED : ItemStatus.RESERVED
                        )
                    ),
                ]);
                setOfferedItem(offered);
                setRequestedItem(requested);
            } catch (error) {
                console.error("Error al obtener los datos del tradeo:", error);
                setErrorMessage("Error al obtener los datos del tradeo.");
            }
        }

        fetchTradeData();
    }, [id]);

    useEffect(() => {
        const loadImages = async () => {
            const accessToken = localStorage.getItem("accessToken");
            if (!accessToken) return;

            const entries = [offeredItem, requestedItem].filter(Boolean) as ItemResponse[];
            const results = await Promise.all(
                entries.map(async (entry) => {
                    try {
                        const url = await fetchItemImage(entry, accessToken);
                        return { id: entry.id, url };
                    } catch {
                        return { id: entry.id, url: "/default-placeholder.png" };
                    }
                })
            );
            setImageUrls(results.reduce((acc, { id, url }) => ({ ...acc, [id]: url }), {}));
        };

        if (offeredItem || requestedItem) {
            loadImages();
        }
    }, [offeredItem, requestedItem]);

    async function updateTrade(action: "accept" | "reject" | "cancel") {
        if (!trade) return;

        try {
            setSaving(true);
            const updated =
                action === "accept"
                    ? await Agreement.acceptAgreement(trade.id)
                    : action === "reject"
                        ? await Agreement.rejectAgreement(trade.id)
                        : await Agreement.cancelAgreement(trade.id);
            setTrade(updated);
            toast({ title: "Tradeo actualizado", variant: "success" });
        } catch (error) {
            console.error("Error al actualizar el tradeo:", error);
            toast({ title: "No se pudo actualizar el tradeo", variant: "danger" });
        } finally {
            setSaving(false);
        }
    }

    if (errorMessage) {
        return <div className="text-danger text-center py-10">{errorMessage}</div>;
    }

    if (!trade) {
        return <Spinner label="Cargando informacion del tradeo..." />;
    }

    const renderItemCard = (title: string, entry: ItemResponse | null) => (
        <div>
            <h2 className="text-lg font-semibold text-gray-700 mb-2">{title}</h2>
            {entry ? (
                <div className="border border-border p-4 rounded-card bg-muted">
                    <h3 className="text-lg font-bold text-gray-900">{entry.name}</h3>
                    <div className="w-full h-64 bg-white mt-2 rounded-card flex items-center justify-center overflow-hidden border border-gray-100">
                        <img
                            src={imageUrls[entry.id] || "/default-placeholder.png"}
                            alt={entry.name}
                            className="w-full h-full object-contain p-3"
                        />
                    </div>
                    <p className="mt-3 text-gray-700">{entry.description}</p>
                    <p className="text-sm text-gray-500 mt-2">
                        <strong>Publicado por:</strong> {entry.userName}
                    </p>
                    <p className="text-sm text-gray-500">
                        <strong>Categoria:</strong> {entry.categoryName}
                    </p>
                    <p className="text-sm text-gray-500">
                        <strong>Condicion:</strong> {entry.condition === "NEW" ? "Nuevo" : "Usado"}
                    </p>
                </div>
            ) : (
                <p>Cargando item...</p>
            )}
        </div>
    );

    return (
        <div className="bg-muted min-h-screen p-6">
            <Card className="max-w-5xl mx-auto p-6">
                <div className="flex flex-col sm:flex-row sm:items-start sm:justify-between gap-4 mb-6">
                    <div>
                        <h1 className="text-2xl font-bold text-gray-900">
                            Trade #{trade.id}
                        </h1>
                        <p className="text-sm text-gray-500 mt-1">
                            Estado: <span className="font-bold text-primary">{statusLabels[trade.status] || trade.status}</span>
                        </p>
                    </div>
                    <Button variant="secondary" onClick={() => navigate("/dashboard/cuenta")}>
                        Volver a mis trades
                    </Button>
                </div>

                {trade.initialMessage && (
                    <div className="bg-primary/5 border border-primary/10 rounded-card p-4 mb-6">
                        <p className="text-sm font-bold text-gray-900 mb-1">Mensaje inicial</p>
                        <p className="text-sm text-gray-700">{trade.initialMessage}</p>
                    </div>
                )}

                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    {renderItemCard("Item ofrecido", offeredItem)}
                    {renderItemCard("Item solicitado", requestedItem)}
                </div>

                <div className="mt-4 flex flex-wrap justify-end gap-3">
                    <ReportButton targetType="TRADE_PROPOSAL" targetId={trade.id} label="Reportar trade" />
                    {counterpartId && (
                        <ReportButton targetType="USER" targetId={counterpartId} label="Reportar usuario" />
                    )}
                </div>

                {(canReceiverAct || canProposerCancel) && (
                    <div className="flex justify-end gap-3 mt-6 pt-6 border-t border-gray-100">
                        {canReceiverAct && (
                            <>
                                <Button disabled={saving} onClick={() => updateTrade("accept")}>
                                    Aceptar propuesta
                                </Button>
                                <Button disabled={saving} variant="danger" onClick={() => updateTrade("reject")}>
                                    Rechazar
                                </Button>
                            </>
                        )}
                        {canProposerCancel && (
                            <Button disabled={saving} variant="danger" onClick={() => updateTrade("cancel")}>
                                Cancelar propuesta
                            </Button>
                        )}
                    </div>
                )}

                {(trade.status === "ACCEPTED" || trade.status === "COMPLETED") && (
                    <ShipmentPanel
                        trade={trade}
                        currentUserId={currentUserId}
                        onTradeCompleted={() => setTrade((current) => current ? { ...current, status: "COMPLETED" } : current)}
                    />
                )}

                {trade.status === "COMPLETED" && (
                    <RatingPanel trade={trade} currentUserId={currentUserId} />
                )}

                <AgreementChat
                    tradeProposalId={trade.id}
                    currentUserId={currentUserId}
                    currentUserEmail={currentUserEmail}
                />
            </Card>
        </div>
    );
}
