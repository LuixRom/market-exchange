import { FormEvent, useState } from "react";
import { FaFlag } from "react-icons/fa";
import { ReportTargetType } from "../interfaces/report/Report";
import { report } from "../services/report/report";
import { Button } from "./ui/Button";
import { useToast } from "./ui/Toast";

type ReportButtonProps = {
    targetType: ReportTargetType;
    targetId: number;
    label?: string;
};

export default function ReportButton({ targetType, targetId, label = "Reportar" }: ReportButtonProps) {
    const { toast } = useToast();
    const [open, setOpen] = useState(false);
    const [reason, setReason] = useState("");
    const [details, setDetails] = useState("");
    const [saving, setSaving] = useState(false);

    async function handleSubmit(event: FormEvent<HTMLFormElement>) {
        event.preventDefault();
        if (!reason.trim()) return;

        try {
            setSaving(true);
            await report.create({
                targetType,
                targetId,
                reason: reason.trim(),
                details: details.trim() || undefined,
            });
            setReason("");
            setDetails("");
            setOpen(false);
            toast({ title: "Reporte enviado", description: "Un administrador lo revisara.", variant: "success" });
        } catch (error) {
            console.error("Error al crear reporte:", error);
            toast({ title: "No se pudo enviar el reporte", variant: "danger" });
        } finally {
            setSaving(false);
        }
    }

    return (
        <>
            <Button type="button" variant="secondary" size="sm" onClick={() => setOpen(true)}>
                <FaFlag size={12} />
                {label}
            </Button>

            {open && (
                <div className="fixed inset-0 z-[90] flex items-center justify-center bg-black/40 px-4">
                    <form onSubmit={handleSubmit} className="w-full max-w-md rounded-card bg-white p-5 shadow-lg">
                        <div className="mb-4">
                            <h2 className="text-lg font-bold text-gray-900">Crear reporte</h2>
                            <p className="text-sm text-gray-500">Describe el problema para que administracion pueda revisarlo.</p>
                        </div>

                        <label className="block">
                            <span className="mb-1 block text-sm font-bold text-gray-700">Motivo</span>
                            <input
                                value={reason}
                                onChange={(event) => setReason(event.target.value)}
                                maxLength={120}
                                required
                                className="w-full rounded-card border border-gray-300 px-3 py-2 text-sm outline-none focus:border-transparent focus:ring-2 focus:ring-primary"
                            />
                        </label>

                        <label className="mt-3 block">
                            <span className="mb-1 block text-sm font-bold text-gray-700">Detalle</span>
                            <textarea
                                value={details}
                                onChange={(event) => setDetails(event.target.value)}
                                maxLength={1000}
                                rows={4}
                                className="w-full rounded-card border border-gray-300 px-3 py-2 text-sm outline-none focus:border-transparent focus:ring-2 focus:ring-primary"
                            />
                        </label>

                        <div className="mt-5 flex justify-end gap-3">
                            <Button type="button" variant="ghost" onClick={() => setOpen(false)} disabled={saving}>
                                Cancelar
                            </Button>
                            <Button type="submit" disabled={saving || !reason.trim()}>
                                {saving ? "Enviando..." : "Enviar reporte"}
                            </Button>
                        </div>
                    </form>
                </div>
            )}
        </>
    );
}
