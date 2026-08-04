export interface UsuarioResponseDto {
    id: number;
    firstname:string;
    lastname: string;
    email: string;
    address: string;
    phone: string;
    role?: string;
    emailVerified?: boolean;
    bio?: string;
    avatarUrl?: string;
    location?: string;
    createdAt: string;
    blocked?: boolean;
    blockedAt?: string | null;
    blockedReason?: string | null;
    suspended?: boolean;
    suspendedAt?: string | null;
    suspensionReason?: string | null;
}
