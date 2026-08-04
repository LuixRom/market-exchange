import Api from "../../apis/api";
import { AuthResponse } from "../../interfaces/auth/AuthResponse";
import { ForgotPasswordRequest } from "../../interfaces/auth/ForgotPasswordRequest";
import { MessageResponse } from "../../interfaces/auth/MessageResponse";
import { ResetPasswordRequest } from "../../interfaces/auth/ResetPasswordRequest";
import { TokenRequest } from "../../interfaces/auth/TokenRequest";

export async function verifyEmail(token: string) {
    const api = await Api.getInstance();
    return api.post<TokenRequest, MessageResponse>({ token }, {
        url: "/auth/verify-email",
    });
}

export async function forgotPassword(email: string) {
    const api = await Api.getInstance();
    return api.post<ForgotPasswordRequest, MessageResponse>({ email }, {
        url: "/auth/forgot-password",
    });
}

export async function resetPassword(token: string, newPassword: string) {
    const api = await Api.getInstance();
    return api.post<ResetPasswordRequest, MessageResponse>({ token, newPassword }, {
        url: "/auth/reset-password",
    });
}

export async function refreshSession(refreshToken: string) {
    const api = await Api.getInstance();
    return api.post<TokenRequest, AuthResponse>({ token: refreshToken }, {
        url: "/auth/refresh",
    });
}

export async function logoutSession(refreshToken: string) {
    const api = await Api.getInstance();
    return api.post<TokenRequest, MessageResponse>({ token: refreshToken }, {
        url: "/auth/logout",
    });
}
