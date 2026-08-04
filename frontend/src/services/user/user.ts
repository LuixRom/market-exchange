import {UsuarioResponseDto} from "../../interfaces/usuario/UsuarioResponseDto.ts";
import {ProfileUpdateRequest, UsuarioRequestDto} from "../../interfaces/usuario/UsuarioRequestDto.ts";
import Api from "../../apis/api.ts";

export const usuario = {
    // Obtener un usuario por ID
    async obtenerUsuarioPorId(id: number): Promise<UsuarioResponseDto> {
        const api = await Api.getInstance();
        const response = await api.get<UsuarioResponseDto>({ url: `/usuarios/${id}` });
        return response.data;
    },

    // Listar todos los usuarios
    async listarUsuarios(): Promise<UsuarioResponseDto[]> {
        const api = await Api.getInstance();
        const response = await api.get<UsuarioResponseDto[]>({ url: "/usuarios/listar" });
        return response.data;
    },

    async suspenderUsuario(id: number, reason?: string): Promise<UsuarioResponseDto> {
        const api = await Api.getInstance();
        const response = await api.put<{ reason?: string }, UsuarioResponseDto>({ reason }, {
            url: `/usuarios/${id}/suspend`,
        });
        return response.data;
    },

    async quitarSuspension(id: number): Promise<UsuarioResponseDto> {
        const api = await Api.getInstance();
        const response = await api.put<Record<string, never>, UsuarioResponseDto>({}, {
            url: `/usuarios/${id}/unsuspend`,
        });
        return response.data;
    },

    async bloquearUsuario(id: number, reason?: string): Promise<UsuarioResponseDto> {
        const api = await Api.getInstance();
        const response = await api.put<{ reason?: string }, UsuarioResponseDto>({ reason }, {
            url: `/usuarios/${id}/block`,
        });
        return response.data;
    },

    async desbloquearUsuario(id: number): Promise<UsuarioResponseDto> {
        const api = await Api.getInstance();
        const response = await api.put<Record<string, never>, UsuarioResponseDto>({}, {
            url: `/usuarios/${id}/unblock`,
        });
        return response.data;
    },

    // Actualizar un usuario
    async actualizarUsuario(id: number, data: UsuarioRequestDto): Promise<UsuarioResponseDto> {
        const api = await Api.getInstance();
        const response = await api.put<UsuarioRequestDto, UsuarioResponseDto>(data, {
            url: `/usuarios/${id}`,
        });
        return response.data;
    },

    // Eliminar un usuario
    async eliminarUsuario(id: number): Promise<void> {
        const api = await Api.getInstance();
        await api.delete({ url: `/usuarios/${id}` });
    },

    // Obtener información del usuario autenticado
    async getMyInfo(): Promise<UsuarioResponseDto> {
        const api = await Api.getInstance();
        const response = await api.get<UsuarioResponseDto>({ url: "/usuarios/me" });
        return response.data;
    },

    async actualizarMiPerfil(data: ProfileUpdateRequest): Promise<UsuarioResponseDto> {
        const api = await Api.getInstance();
        const response = await api.put<ProfileUpdateRequest, UsuarioResponseDto>(data, {
            url: "/usuarios/me/profile",
        });
        return response.data;
    },
};
