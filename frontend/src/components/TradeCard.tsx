import { ReactNode } from "react";
import { AgreementResponse, AgreementStatus } from "../interfaces/agreement/AgreementResponse";
import { Card } from "./ui/Card";

interface TradeCardProps {
    trade: AgreementResponse;
    onClick?: () => void;
    showState?: boolean;
    actions?: ReactNode;
}

const statusLabels: Record<AgreementStatus, string> = {
    PENDING: "Pendiente",
    ACCEPTED: "Aceptado",
    REJECTED: "Rechazado",
    CANCELLED: "Cancelado",
    EXPIRED: "Expirado",
    COMPLETED: "Completado",
};

export default function TradeCard({ trade, onClick, showState = true, actions }: TradeCardProps) {
    return (
        <Card
            onClick={onClick}
            className={`p-4 ${onClick ? "cursor-pointer hover:shadow-lg transition-shadow" : ""}`}
        >
            <div className="flex items-start justify-between gap-3">
                <div>
                    <h3 className="text-lg font-bold text-gray-900">Trade #{trade.id}</h3>
                    <p className="text-gray-700 mt-1">
                        <strong>Ofrece:</strong> {trade.offeredItemName}
                    </p>
                    <p className="text-gray-700">
                        <strong>Solicita:</strong> {trade.requestedItemName}
                    </p>
                </div>
                {showState && (
                    <span className="px-3 py-1 text-xs font-bold rounded-full bg-primary/10 text-primary whitespace-nowrap">
                        {statusLabels[trade.status] || trade.status}
                    </span>
                )}
            </div>

            <div className="mt-3 text-sm text-gray-500 space-y-1">
                <p>
                    <strong>Enviado por:</strong> {trade.proposerEmail}
                </p>
                <p>
                    <strong>Recibido por:</strong> {trade.receiverEmail}
                </p>
                {trade.initialMessage && (
                    <p className="text-gray-600 line-clamp-2">
                        <strong>Mensaje:</strong> {trade.initialMessage}
                    </p>
                )}
            </div>

            {actions && <div className="flex justify-end gap-3 mt-4">{actions}</div>}
        </Card>
    );
}
