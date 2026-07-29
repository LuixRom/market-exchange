export interface AgreementRequest {
    itemIniId: number; // ID del ítem ofrecido por el iniciador
    itemFinId: number; // ID del ítem ofrecido por el receptor
    usuarioIniId: number; // ID del usuario que inicia el trade
    usuarioFinId: number; // ID del usuario receptor
}