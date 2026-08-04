import { FormEvent, useEffect, useRef, useState } from "react";
import { FaPaperPlane } from "react-icons/fa";
import { chat } from "../services/chat/chat";
import { realtimeClient } from "../services/realtime/stompClient";
import { ChatMessageResponse } from "../interfaces/chat/ChatMessage";
import { Button } from "./ui/Button";
import { Spinner } from "./ui/Spinner";
import { useToast } from "./ui/Toast";

type AgreementChatProps = {
    tradeProposalId: number;
    currentUserId: number | null;
    currentUserEmail?: string | null;
};

export default function AgreementChat({ tradeProposalId, currentUserId, currentUserEmail }: AgreementChatProps) {
    const { toast } = useToast();
    const bottomRef = useRef<HTMLDivElement | null>(null);
    const [messages, setMessages] = useState<ChatMessageResponse[]>([]);
    const [content, setContent] = useState("");
    const [loading, setLoading] = useState(true);
    const [sending, setSending] = useState(false);
    const [connected, setConnected] = useState(false);

    useEffect(() => {
        async function loadMessages() {
            try {
                setLoading(true);
                const history = await chat.listMessages(tradeProposalId);
                setMessages(history);
            } catch {
                toast({ title: "No se pudo cargar el chat", variant: "danger" });
            } finally {
                setLoading(false);
            }
        }

        loadMessages();
    }, [tradeProposalId, toast]);

    useEffect(() => {
        let subscription: { unsubscribe: () => void } | null = null;
        let active = true;

        realtimeClient
            .connect(setConnected)
            .then(() => {
                if (!active) return;

                subscription = realtimeClient.subscribeToAgreementMessages(tradeProposalId, (message) => {
                    setMessages((current) => {
                        if (current.some((existing) => existing.id === message.id)) {
                            return current;
                        }
                        return [...current, message];
                    });
                });
            })
            .catch(() => {
                setConnected(false);
            });

        return () => {
            active = false;
            subscription?.unsubscribe();
        };
    }, [tradeProposalId]);

    useEffect(() => {
        bottomRef.current?.scrollIntoView({ behavior: "smooth" });
    }, [messages]);

    async function handleSubmit(event: FormEvent<HTMLFormElement>) {
        event.preventDefault();

        const message = content.trim();
        if (!message || sending) return;

        try {
            setSending(true);
            const response = await chat.sendMessage(tradeProposalId, message);
            setMessages((current) => {
                if (current.some((existing) => existing.id === response.id)) {
                    return current;
                }
                return [...current, response];
            });
            setContent("");
        } catch {
            setContent(message);
            toast({ title: "No se pudo enviar el mensaje", variant: "danger" });
        } finally {
            setSending(false);
        }
    }

    if (loading) {
        return <Spinner label="Cargando chat..." />;
    }

    return (
        <section className="mt-6 pt-6 border-t border-gray-100">
            <div className="flex items-center justify-between gap-3 mb-4">
                <div>
                    <h2 className="text-lg font-bold text-gray-900">Chat de la propuesta</h2>
                    <p className="text-xs text-gray-500">
                        {connected ? "Conectado en tiempo real" : "Modo historial/rest, reconectando WebSocket..."}
                    </p>
                </div>
                <span className={`w-2.5 h-2.5 rounded-full ${connected ? "bg-emerald-500" : "bg-amber-500"}`} />
            </div>

            <div className="h-80 overflow-y-auto rounded-2xl border border-gray-100 bg-gray-50 p-4 space-y-3">
                {messages.length > 0 ? (
                    messages.map((message) => {
                        const own =
                            (currentUserId !== null && message.senderId === currentUserId) ||
                            (currentUserEmail &&
                                message.senderEmail.toLowerCase() === currentUserEmail.toLowerCase());
                        return (
                            <div key={message.id} className={`flex ${own ? "justify-end" : "justify-start"}`}>
                                <div
                                    className={`max-w-[80%] rounded-2xl px-4 py-2 shadow-xs ${
                                        own
                                            ? "bg-primary text-white rounded-br-sm"
                                            : "bg-white text-gray-800 border border-gray-100 rounded-bl-sm"
                                    }`}
                                >
                                    <p className={`text-[11px] font-bold mb-1 ${own ? "text-white/80" : "text-gray-400"}`}>
                                        {own ? "Tu" : message.senderEmail}
                                    </p>
                                    <p className="text-sm whitespace-pre-wrap break-words">{message.content}</p>
                                    {message.createdAt && (
                                        <p className={`text-[10px] mt-1 ${own ? "text-white/70" : "text-gray-400"}`}>
                                            {new Date(message.createdAt).toLocaleTimeString([], {
                                                hour: "2-digit",
                                                minute: "2-digit",
                                            })}
                                        </p>
                                    )}
                                </div>
                            </div>
                        );
                    })
                ) : (
                    <div className="h-full flex items-center justify-center text-sm text-gray-500 text-center">
                        Todavia no hay mensajes. Escribe el primero para coordinar el intercambio.
                    </div>
                )}
                <div ref={bottomRef} />
            </div>

            <form onSubmit={handleSubmit} className="mt-4 flex gap-3">
                <input
                    value={content}
                    onChange={(event) => setContent(event.target.value)}
                    maxLength={1000}
                    placeholder="Escribe un mensaje..."
                    className="flex-1 px-4 py-3 text-sm bg-white border border-gray-300 rounded-xl focus:ring-2 focus:ring-primary focus:border-transparent outline-none transition-all text-gray-900"
                />
                <Button type="submit" disabled={!content.trim() || sending}>
                    <FaPaperPlane size={13} />
                    {sending ? "Enviando..." : "Enviar"}
                </Button>
            </form>
        </section>
    );
}
