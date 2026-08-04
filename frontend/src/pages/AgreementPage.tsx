import React, { useEffect, useState } from "react";
import { motion } from "framer-motion";
import { useNavigate, useParams } from "react-router-dom";
import { AgreementRequest } from "../interfaces/agreement/AgreementRequest";
import { Agreement } from "../services/agreement/Agreement";
import { ItemResponse } from "../interfaces/item/ItemResponse";
import { item } from "../services/item/item";
import { fetchItemImage } from "../services/image/image";
import { Card } from "../components/ui/Card";
import { Input } from "../components/ui/Input";
import { Button } from "../components/ui/Button";
import { useToast } from "../components/ui/Toast";
import { Spinner } from "../components/ui/Spinner";
import { staggerChildren, slideUp } from "../lib/motion";

export default function AgreementPage() {
    const { id } = useParams<{ id: string }>();
    const navigate = useNavigate();
    const { toast } = useToast();
    const [items, setItems] = useState<ItemResponse[]>([]);
    const [filteredItems, setFilteredItems] = useState<ItemResponse[]>([]);
    const [searchTerm, setSearchTerm] = useState<string>("");
    const [initialMessage, setInitialMessage] = useState<string>("");
    const [errorMessage, setErrorMessage] = useState<string | null>(null);
    const [requestedItem, setRequestedItem] = useState<ItemResponse | null>(null);
    const [offeredItem, setOfferedItem] = useState<ItemResponse | null>(null);
    const [imageUrls, setImageUrls] = useState<{ [key: number]: string }>({});
    const [loading, setLoading] = useState<boolean>(true);
    const [submitting, setSubmitting] = useState<boolean>(false);

    useEffect(() => {
        async function fetchData() {
            if (!id) return;

            try {
                setLoading(true);
                const [targetItem, myItems] = await Promise.all([
                    item.getItemById(Number(id)),
                    item.getMyItems(),
                ]);

                const approvedItems = myItems.filter(
                    (candidate) => candidate.status === "APPROVED" && candidate.id !== targetItem.id
                );

                setRequestedItem(targetItem);
                setItems(approvedItems);
                setFilteredItems(approvedItems);
            } catch {
                setErrorMessage("No se pudo cargar la informacion para crear el trade.");
            } finally {
                setLoading(false);
            }
        }

        fetchData();
    }, [id]);

    useEffect(() => {
        async function loadImages() {
            const accessToken = localStorage.getItem("accessToken");
            if (!accessToken) return;

            const imagesToLoad = [requestedItem, ...items].filter(Boolean) as ItemResponse[];
            const loaded = await Promise.all(
                imagesToLoad.map(async (entry) => {
                    try {
                        const url = await fetchItemImage(entry, accessToken);
                        return { id: entry.id, url };
                    } catch {
                        return { id: entry.id, url: "/default-placeholder.png" };
                    }
                })
            );
            setImageUrls(loaded.reduce((acc, image) => ({ ...acc, [image.id]: image.url }), {}));
        }

        if (requestedItem || items.length > 0) {
            loadImages();
        }
    }, [requestedItem, items]);

    const handleSearchChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        const term = event.target.value.toLowerCase();
        setSearchTerm(term);
        setFilteredItems(items.filter((item) => item.name.toLowerCase().includes(term)));
    };

    const handleTrade = async () => {
        if (!requestedItem || !offeredItem) {
            toast({ title: "Debes seleccionar un item para ofrecer.", variant: "danger" });
            return;
        }

        const agreementRequest: AgreementRequest = {
            offeredItemId: offeredItem.id,
            requestedItemId: requestedItem.id,
            initialMessage: initialMessage.trim() || undefined,
        };

        try {
            setSubmitting(true);
            const created = await Agreement.createAgreement(agreementRequest);
            toast({ title: "Propuesta enviada", variant: "success" });
            navigate(`/dashboard/agreements/${created.id}`);
        } catch (error) {
            console.error("Error al crear la propuesta:", error);
            toast({ title: "No se pudo crear la propuesta.", variant: "danger" });
        } finally {
            setSubmitting(false);
        }
    };

    if (loading) {
        return <Spinner label="Cargando propuesta..." />;
    }

    return (
        <div className="bg-muted min-h-screen p-6">
            <div className="max-w-7xl mx-auto space-y-6">
                <div>
                    <h1 className="text-2xl sm:text-3xl font-extrabold text-gray-900">Crear propuesta de intercambio</h1>
                    <p className="text-sm text-gray-600 mt-1">Elige uno de tus items aprobados y envia una propuesta al duenio.</p>
                </div>

                {errorMessage && (
                    <div className="text-danger text-center text-sm">{errorMessage}</div>
                )}

                <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                    <Card className="p-5">
                        <h2 className="text-xl font-bold text-gray-900 mb-4">Item solicitado</h2>
                        {requestedItem ? (
                            <div>
                                <div className="w-full h-72 bg-gray-50 rounded-card flex items-center justify-center overflow-hidden border border-gray-100">
                                    <img
                                        src={imageUrls[requestedItem.id] || "/default-placeholder.png"}
                                        alt={requestedItem.name}
                                        className="w-full h-full object-contain p-3"
                                    />
                                </div>
                                <h3 className="text-lg font-bold text-gray-900 mt-4">{requestedItem.name}</h3>
                                <p className="text-gray-700 mt-1">{requestedItem.description}</p>
                                <div className="flex flex-wrap gap-2 mt-3 text-xs font-semibold">
                                    <span className="bg-primary/10 text-primary px-3 py-1 rounded-full">{requestedItem.categoryName}</span>
                                    <span className="bg-muted text-gray-600 px-3 py-1 rounded-full">
                                        {requestedItem.condition === "NEW" ? "Nuevo" : "Usado"}
                                    </span>
                                </div>
                                <p className="text-sm text-gray-500 mt-3">
                                    Publicado por: <span className="font-bold text-gray-900">{requestedItem.userName}</span>
                                </p>
                            </div>
                        ) : (
                            <p className="text-gray-500">No se encontro el item solicitado.</p>
                        )}
                    </Card>

                    <Card className="p-5">
                        <h2 className="text-xl font-bold text-gray-900 mb-4">Ofrezco</h2>
                        <div className="mb-4">
                            <label htmlFor="search" className="block text-sm font-medium text-gray-700 mb-2">
                                Buscar entre mis items:
                            </label>
                            <Input
                                id="search"
                                type="text"
                                value={searchTerm}
                                onChange={handleSearchChange}
                                placeholder="Escribe aqui para buscar..."
                            />
                        </div>

                        {filteredItems.length > 0 ? (
                            <motion.ul
                                className="space-y-3 max-h-[460px] overflow-y-auto pr-1"
                                variants={staggerChildren}
                                initial="hidden"
                                animate="visible"
                            >
                                {filteredItems.map((candidate) => (
                                    <motion.li
                                        key={candidate.id}
                                        variants={slideUp}
                                        className={`border p-3 rounded-card cursor-pointer transition-shadow flex gap-3 ${
                                            offeredItem?.id === candidate.id ? "border-primary bg-primary/5" : "border-border hover:shadow-md"
                                        }`}
                                        onClick={() => setOfferedItem(candidate)}
                                    >
                                        <div className="w-20 h-20 bg-gray-50 rounded-card flex-shrink-0 flex items-center justify-center overflow-hidden border border-gray-100">
                                            <img
                                                src={imageUrls[candidate.id] || "/default-placeholder.png"}
                                                alt={candidate.name}
                                                className="w-full h-full object-contain p-1"
                                            />
                                        </div>
                                        <div>
                                            <h3 className="text-base font-bold text-gray-900">{candidate.name}</h3>
                                            <p className="text-sm text-gray-600 line-clamp-2">{candidate.description}</p>
                                            <p className="text-xs text-gray-500 mt-1">{candidate.categoryName}</p>
                                        </div>
                                    </motion.li>
                                ))}
                            </motion.ul>
                        ) : (
                            <p className="text-gray-500">
                                {searchTerm ? "No se encontraron items con esa busqueda." : "No tienes items aprobados para ofrecer."}
                            </p>
                        )}
                    </Card>
                </div>

                <Card className="p-5">
                    <label htmlFor="initialMessage" className="block text-sm font-bold text-gray-800 mb-2">
                        Mensaje inicial opcional
                    </label>
                    <textarea
                        id="initialMessage"
                        value={initialMessage}
                        onChange={(event) => setInitialMessage(event.target.value)}
                        maxLength={500}
                        rows={3}
                        placeholder="Ej: Hola, me interesa tu item. Te ofrezco este producto porque esta en buen estado..."
                        className="w-full p-4 text-sm bg-white border border-gray-300 rounded-xl focus:ring-2 focus:ring-primary focus:border-transparent outline-none transition-all text-gray-900 resize-none"
                    />
                    <div className="flex items-center justify-between gap-4 mt-4">
                        <span className="text-xs text-gray-400">{initialMessage.length}/500</span>
                        <Button onClick={handleTrade} disabled={!offeredItem || submitting}>
                            {submitting ? "Enviando..." : "Enviar propuesta"}
                        </Button>
                    </div>
                </Card>
            </div>
        </div>
    );
}
