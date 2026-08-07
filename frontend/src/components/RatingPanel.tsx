import { FormEvent, useEffect, useMemo, useState } from "react";
import { FaStar, FaRegStar, FaUserCircle } from "react-icons/fa";
import { AgreementResponse } from "../interfaces/agreement/AgreementResponse";
import { RatingReputation, RatingResponse } from "../interfaces/rating/Rating";
import { rating as ratingService } from "../services/rating/rating";
import { Button } from "./ui/Button";
import { Spinner } from "./ui/Spinner";
import { useToast } from "./ui/Toast";

type RatingPanelProps = {
    trade: AgreementResponse;
    currentUserId: number | null;
};

type ScoreInputProps = {
    label: string;
    value: number;
    onChange: (value: number) => void;
};

function ScoreInput({ label, value, onChange }: Readonly<ScoreInputProps>) {
    return (
        <div>
            <p className="mb-1 text-sm font-bold text-gray-700">{label}</p>
            <div className="flex gap-1">
                {[1, 2, 3, 4, 5].map((score) => (
                    <button
                        key={score}
                        type="button"
                        onClick={() => onChange(score)}
                        className="rounded-full p-1 text-amber-400 transition-transform hover:scale-110 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary"
                        aria-label={`${label}: ${score}`}
                    >
                        {score <= value ? <FaStar size={20} /> : <FaRegStar size={20} />}
                    </button>
                ))}
            </div>
        </div>
    );
}

function StarSummary({ value }: Readonly<{ value?: number | null }>) {
    const normalized = value ?? 0;
    return (
        <span className="inline-flex items-center gap-1 text-amber-400">
            {[1, 2, 3, 4, 5].map((score) =>
                score <= Math.round(normalized) ? <FaStar key={score} size={13} /> : <FaRegStar key={score} size={13} />
            )}
        </span>
    );
}

function formatDate(value?: string | null) {
    if (!value) return "";
    return new Date(value).toLocaleDateString([], {
        day: "2-digit",
        month: "2-digit",
        year: "numeric",
    });
}

export default function RatingPanel({ trade, currentUserId }: Readonly<RatingPanelProps>) {
    const { toast } = useToast();
    const [ratings, setRatings] = useState<RatingResponse[]>([]);
    const [reputation, setReputation] = useState<RatingReputation | null>(null);
    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);
    const [score, setScore] = useState(5);
    const [communicationScore, setCommunicationScore] = useState(5);
    const [punctualityScore, setPunctualityScore] = useState(5);
    const [itemConditionScore, setItemConditionScore] = useState(5);
    const [comment, setComment] = useState("");

    const counterpart = useMemo(() => {
        if (currentUserId === trade.proposerId) {
            return { id: trade.receiverId, email: trade.receiverEmail };
        }
        if (currentUserId === trade.receiverId) {
            return { id: trade.proposerId, email: trade.proposerEmail };
        }
        return null;
    }, [currentUserId, trade.proposerEmail, trade.proposerId, trade.receiverEmail, trade.receiverId]);

    const ownRating = useMemo(
        () => ratings.find((entry) => entry.tradeProposalId === trade.id && entry.reviewerId === currentUserId) || null,
        [currentUserId, ratings, trade.id]
    );

    useEffect(() => {
        let active = true;

        async function loadRatings() {
            if (!counterpart) {
                setLoading(false);
                return;
            }

            try {
                setLoading(true);
                const [ratingList, reputationInfo] = await Promise.all([
                    ratingService.listByUser(counterpart.id),
                    ratingService.getReputation(counterpart.id),
                ]);
                if (!active) return;
                setRatings(ratingList);
                setReputation(reputationInfo);

                const existing = ratingList.find(
                    (entry) => entry.tradeProposalId === trade.id && entry.reviewerId === currentUserId
                );
                if (existing) {
                    setScore(existing.score);
                    setCommunicationScore(existing.communicationScore || existing.score);
                    setPunctualityScore(existing.punctualityScore || existing.score);
                    setItemConditionScore(existing.itemConditionScore || existing.score);
                    setComment(existing.comment || "");
                }
            } catch (error) {
                console.error("Error al cargar reputacion:", error);
                toast({ title: "No se pudo cargar la reputacion", variant: "danger" });
            } finally {
                if (active) setLoading(false);
            }
        }

        loadRatings();

        return () => {
            active = false;
        };
    }, [counterpart, currentUserId, toast, trade.id]);

    async function handleSubmit(event: FormEvent<HTMLFormElement>) {
        event.preventDefault();
        if (!counterpart) return;

        const payload = {
            tradeProposalId: trade.id,
            score,
            communicationScore,
            punctualityScore,
            itemConditionScore,
            comment: comment.trim() || undefined,
        };

        try {
            setSaving(true);
            const saved = ownRating
                ? await ratingService.update(ownRating.id, payload)
                : await ratingService.create(payload);

            setRatings((current) => {
                const exists = current.some((entry) => entry.id === saved.id);
                if (exists) {
                    return current.map((entry) => (entry.id === saved.id ? saved : entry));
                }
                return [saved, ...current];
            });
            const nextReputation = await ratingService.getReputation(counterpart.id);
            setReputation(nextReputation);
            toast({ title: ownRating ? "Calificacion actualizada" : "Calificacion enviada", variant: "success" });
        } catch (error) {
            console.error("Error al guardar calificacion:", error);
            toast({
                title: "No se pudo guardar la calificacion",
                description: "Puede que ya haya vencido la ventana de edicion o calificacion.",
                variant: "danger",
            });
        } finally {
            setSaving(false);
        }
    }

    if (trade.status !== "COMPLETED") {
        return null;
    }

    if (!counterpart) {
        return null;
    }

    if (loading) {
        return (
            <section className="mt-6 pt-6 border-t border-gray-100">
                <Spinner label="Cargando reputacion..." />
            </section>
        );
    }

    let submitLabel = "Enviar calificacion";
    if (saving) {
        submitLabel = "Guardando...";
    } else if (ownRating) {
        submitLabel = "Actualizar calificacion";
    }

    return (
        <section className="mt-6 pt-6 border-t border-gray-100">
            <div className="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
                <div>
                    <h2 className="text-lg font-bold text-gray-900">Calificacion del intercambio</h2>
                    <p className="text-sm text-gray-500">
                        Evalua a la otra parte y revisa su reputacion.
                    </p>
                </div>
                <div className="rounded-card border border-gray-100 bg-muted px-4 py-3 text-right">
                    <p className="text-xs font-bold uppercase text-gray-400">Reputacion</p>
                    <p className="mt-1 text-xl font-extrabold text-gray-900">
                        {reputation?.averageScore ? reputation.averageScore.toFixed(1) : "-"}
                        <span className="ml-1 text-sm text-gray-500">/ 5</span>
                    </p>
                    <p className="text-xs text-gray-500">{reputation?.ratingCount || 0} calificaciones</p>
                </div>
            </div>

            <div className="mt-4 grid grid-cols-1 gap-4 lg:grid-cols-[1.1fr_0.9fr]">
                <form onSubmit={handleSubmit} className="rounded-card border border-gray-100 bg-white p-4">
                    <div className="mb-4 flex items-center gap-3">
                        <div className="flex h-10 w-10 items-center justify-center rounded-full bg-primary/10 text-primary">
                            <FaUserCircle size={20} />
                        </div>
                        <div>
                            <p className="text-sm font-bold text-gray-900">{counterpart.email}</p>
                            <p className="text-xs text-gray-500">
                                {ownRating ? "Ya calificaste este intercambio." : "Disponible despues de completar la entrega."}
                            </p>
                        </div>
                    </div>

                    <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
                        <ScoreInput label="General" value={score} onChange={setScore} />
                        <ScoreInput label="Comunicacion" value={communicationScore} onChange={setCommunicationScore} />
                        <ScoreInput label="Puntualidad" value={punctualityScore} onChange={setPunctualityScore} />
                        <ScoreInput label="Estado del item" value={itemConditionScore} onChange={setItemConditionScore} />
                    </div>

                    <label className="mt-4 block">
                        <span className="mb-1 block text-sm font-bold text-gray-700">Comentario</span>
                        <textarea
                            value={comment}
                            onChange={(event) => setComment(event.target.value)}
                            maxLength={500}
                            rows={4}
                            placeholder="Cuenta como fue el intercambio..."
                            className="w-full rounded-card border border-gray-300 bg-white px-3 py-2 text-sm text-gray-900 outline-none transition-all focus:border-transparent focus:ring-2 focus:ring-primary"
                        />
                    </label>

                    <div className="mt-4 flex justify-end">
                        <Button type="submit" disabled={saving}>
                            {submitLabel}
                        </Button>
                    </div>
                </form>

                <div className="rounded-card border border-gray-100 bg-muted p-4">
                    <h3 className="text-sm font-bold text-gray-900">Ratings recientes</h3>
                    <div className="mt-3 space-y-3">
                        {ratings.length === 0 ? (
                            <p className="text-sm text-gray-500">Este usuario todavia no tiene calificaciones.</p>
                        ) : (
                            ratings.slice(0, 4).map((entry) => (
                                <div key={entry.id} className="rounded-card bg-white p-3 shadow-xs">
                                    <div className="flex items-center justify-between gap-3">
                                        <p className="truncate text-sm font-bold text-gray-900">{entry.reviewerName}</p>
                                        <StarSummary value={entry.score} />
                                    </div>
                                    {entry.comment && (
                                        <p className="mt-2 line-clamp-3 text-sm text-gray-600">{entry.comment}</p>
                                    )}
                                    <p className="mt-2 text-xs text-gray-400">{formatDate(entry.createdAt)}</p>
                                </div>
                            ))
                        )}
                    </div>
                </div>
            </div>
        </section>
    );
}
