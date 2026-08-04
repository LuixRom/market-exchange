import { FormEvent, useCallback, useEffect, useState } from "react";
import { FaFlag } from "react-icons/fa";
import { ReportResponse, ReportStatus } from "../interfaces/report/Report";
import { report } from "../services/report/report";
import { Button } from "../components/ui/Button";
import { Spinner } from "../components/ui/Spinner";
import { useToast } from "../components/ui/Toast";

const statuses: ReportStatus[] = ["PENDING", "REVIEWED", "RESOLVED", "DISMISSED"];

const statusLabels: Record<ReportStatus, string> = {
    PENDING: "Pendiente",
    REVIEWED: "Revisado",
    RESOLVED: "Resuelto",
    DISMISSED: "Descartado",
};

function formatDate(value?: string) {
    return value ? new Date(value).toLocaleString() : "-";
}

export default function AdminReportsPage() {
    const { toast } = useToast();
    const [reports, setReports] = useState<ReportResponse[]>([]);
    const [statusFilter, setStatusFilter] = useState<ReportStatus | "ALL">("PENDING");
    const [selected, setSelected] = useState<ReportResponse | null>(null);
    const [reviewStatus, setReviewStatus] = useState<ReportStatus>("REVIEWED");
    const [adminNotes, setAdminNotes] = useState("");
    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);

    const loadReports = useCallback(async () => {
        try {
            setLoading(true);
            const response = await report.list(statusFilter === "ALL" ? undefined : statusFilter);
            setReports(response);
        } catch (error) {
            console.error("Error al cargar reportes:", error);
            toast({ title: "No se pudieron cargar los reportes", variant: "danger" });
        } finally {
            setLoading(false);
        }
    }, [statusFilter, toast]);

    useEffect(() => {
        loadReports();
    }, [loadReports]);

    function selectReport(entry: ReportResponse) {
        setSelected(entry);
        setReviewStatus(entry.status === "PENDING" ? "REVIEWED" : entry.status);
        setAdminNotes(entry.adminNotes || "");
    }

    async function handleReview(event: FormEvent<HTMLFormElement>) {
        event.preventDefault();
        if (!selected) return;

        try {
            setSaving(true);
            const updated = await report.review(selected.id, {
                status: reviewStatus,
                adminNotes: adminNotes.trim() || undefined,
            });
            setReports((current) => current.map((entry) => (entry.id === updated.id ? updated : entry)));
            setSelected(updated);
            toast({ title: "Reporte actualizado", variant: "success" });
        } catch (error) {
            console.error("Error al revisar reporte:", error);
            toast({ title: "No se pudo revisar el reporte", variant: "danger" });
        } finally {
            setSaving(false);
        }
    }

    return (
        <div className="w-full max-w-container mx-auto px-4 sm:px-6 py-8">
            <div className="mb-6 flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between">
                <div>
                    <h1 className="text-2xl sm:text-3xl font-extrabold text-gray-900">Reportes</h1>
                    <p className="mt-1 text-sm text-gray-600">Revisa reportes de usuarios, items y propuestas.</p>
                </div>
                <select
                    value={statusFilter}
                    onChange={(event) => setStatusFilter(event.target.value as ReportStatus | "ALL")}
                    className="rounded-card border border-gray-300 bg-white px-3 py-2 text-sm font-bold text-gray-800"
                >
                    <option value="ALL">Todos</option>
                    {statuses.map((status) => (
                        <option key={status} value={status}>{statusLabels[status]}</option>
                    ))}
                </select>
            </div>

            {loading ? (
                <Spinner label="Cargando reportes..." />
            ) : (
                <div className="grid grid-cols-1 gap-4 lg:grid-cols-[1fr_380px]">
                    <div className="space-y-3">
                        {reports.length === 0 ? (
                            <div className="rounded-card border border-gray-100 bg-white p-8 text-center text-gray-500">
                                No hay reportes para este filtro.
                            </div>
                        ) : (
                            reports.map((entry) => (
                                <button
                                    key={entry.id}
                                    type="button"
                                    onClick={() => selectReport(entry)}
                                    className={`w-full rounded-card border bg-white p-4 text-left shadow-xs transition-all hover:shadow-card ${
                                        selected?.id === entry.id ? "border-primary" : "border-gray-100"
                                    }`}
                                >
                                    <div className="flex items-start justify-between gap-3">
                                        <div>
                                            <p className="flex items-center gap-2 text-sm font-bold text-gray-900">
                                                <FaFlag className="text-primary" />
                                                {entry.targetType} #{entry.targetId}
                                            </p>
                                            <p className="mt-1 text-sm text-gray-600">{entry.reason}</p>
                                        </div>
                                        <span className="rounded-full bg-primary/10 px-3 py-1 text-xs font-bold text-primary">
                                            {statusLabels[entry.status]}
                                        </span>
                                    </div>
                                    <p className="mt-2 text-xs text-gray-400">
                                        Reportado por {entry.reporterEmail} - {formatDate(entry.createdAt)}
                                    </p>
                                </button>
                            ))
                        )}
                    </div>

                    <aside className="rounded-card border border-gray-100 bg-white p-4 shadow-card">
                        {selected ? (
                            <form onSubmit={handleReview}>
                                <h2 className="text-lg font-bold text-gray-900">Reporte #{selected.id}</h2>
                                <div className="mt-3 space-y-2 text-sm text-gray-600">
                                    <p><strong>Objetivo:</strong> {selected.targetType} #{selected.targetId}</p>
                                    <p><strong>Usuario:</strong> {selected.reporterEmail}</p>
                                    <p><strong>Motivo:</strong> {selected.reason}</p>
                                    {selected.details && <p><strong>Detalle:</strong> {selected.details}</p>}
                                </div>
                                <label className="mt-4 block">
                                    <span className="mb-1 block text-sm font-bold text-gray-700">Estado</span>
                                    <select
                                        value={reviewStatus}
                                        onChange={(event) => setReviewStatus(event.target.value as ReportStatus)}
                                        className="w-full rounded-card border border-gray-300 px-3 py-2 text-sm"
                                    >
                                        {statuses.map((status) => (
                                            <option key={status} value={status}>{statusLabels[status]}</option>
                                        ))}
                                    </select>
                                </label>
                                <label className="mt-3 block">
                                    <span className="mb-1 block text-sm font-bold text-gray-700">Notas admin</span>
                                    <textarea
                                        value={adminNotes}
                                        onChange={(event) => setAdminNotes(event.target.value)}
                                        rows={5}
                                        maxLength={1000}
                                        className="w-full rounded-card border border-gray-300 px-3 py-2 text-sm"
                                    />
                                </label>
                                <Button type="submit" disabled={saving} className="mt-4 w-full">
                                    {saving ? "Guardando..." : "Guardar revision"}
                                </Button>
                            </form>
                        ) : (
                            <p className="text-sm text-gray-500">Selecciona un reporte para revisarlo.</p>
                        )}
                    </aside>
                </div>
            )}
        </div>
    );
}
