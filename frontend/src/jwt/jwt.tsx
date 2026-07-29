import {jwtDecode} from "jwt-decode";

interface JwtPayload {
    role: string; // Asegúrate de que coincide con el campo en tu JWT
}

export default function extractRoleFromToken(token: string): string | null {
    try {
        const decoded = jwtDecode<JwtPayload>(token);
        return decoded.role || null;
    } catch (error) {
        console.error("Error decodificando el token:", error);
        return null;
    }
}
