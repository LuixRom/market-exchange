import { ReactNode } from "react";
import { AgreementResponse } from "../interfaces/agreement/AgreementResponse";
import { Card } from "./ui/Card";

interface TradeCardProps {
    trade: AgreementResponse;
    onClick?: () => void;
    showState?: boolean;
    actions?: ReactNode;
}

export default function TradeCard({ trade, onClick, showState = true, actions }: TradeCardProps) {
    return (
        <Card
            onClick={onClick}
            className={`p-4 ${onClick ? "cursor-pointer hover:shadow-lg transition-shadow" : ""}`}
        >
            <h3 className="text-lg font-bold text-gray-900">Trade ID: {trade.id}</h3>
            <p className="text-gray-700 mt-1">
                <strong>Ítem Ofrecido:</strong> {trade.itemIniName}
            </p>
            <p className="text-gray-700">
                <strong>Ítem Recibido:</strong> {trade.itemFinName}
            </p>
            <p className="text-sm text-gray-500 mt-1">
                <strong>Iniciado por:</strong> {trade.iniUsername}
            </p>
            <p className="text-sm text-gray-500">
                <strong>Recibido por:</strong> {trade.finUsername}
            </p>
            {showState && (
                <p className="text-sm text-gray-500">
                    <strong>Estado:</strong> {trade.state}
                </p>
            )}
            {actions && <div className="flex justify-end gap-3 mt-4">{actions}</div>}
        </Card>
    );
}
