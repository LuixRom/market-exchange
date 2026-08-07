// src/apis/api.ts

import axios, { AxiosError, AxiosInstance, AxiosRequestConfig } from "axios";

type RetriableAxiosRequestConfig = AxiosRequestConfig & {
  _retry?: boolean;
};

export function getApiBaseUrl(): string {
  const apiUrl = import.meta.env.VITE_API_URL?.trim();
  if (apiUrl) {
    return apiUrl.replace(/\/$/, "");
  }

  const legacyBaseUrl = import.meta.env.VITE_BASE_URL?.trim();
  if (legacyBaseUrl) {
    return `http://${legacyBaseUrl}:8080`;
  }

  return "http://localhost:8080";
}

export default class Api {
  private static _instance: Api | null = null;

  private readonly _axiosInstance: AxiosInstance;

  private _authorization: string | null;

  public set authorization(value: string) {
    this._authorization = value;
  }

  private constructor(basePath: string, authorization: string | null) {
    this._authorization = authorization || sessionStorage.getItem("accessToken") || null;

    this._axiosInstance = axios.create({
      baseURL: basePath,
    });

    // Interceptor para agregar la autorizacion a cada solicitud.
    this._axiosInstance.interceptors.request.use(
      (config) => {
        const isPublicAuthEndpoint = [
          "/auth/register",
          "/auth/login",
          "/auth/verify-email",
          "/auth/forgot-password",
          "/auth/reset-password",
          "/auth/refresh",
        ].some((endpoint) => config.url?.includes(endpoint));

        if (!isPublicAuthEndpoint && this._authorization) {
          config.headers.Authorization = `Bearer ${this._authorization}`;
        }
        return config;
      },
      (error) => Promise.reject(error)
    );

    this._axiosInstance.interceptors.response.use(
      (response) => response,
      async (error: AxiosError) => {
        const originalRequest = error.config as RetriableAxiosRequestConfig | undefined;
        const refreshToken = sessionStorage.getItem("refreshToken");
        const isRefreshRequest = originalRequest?.url?.includes("/auth/refresh");

        if (
          error.response?.status === 401 &&
          originalRequest &&
          !originalRequest._retry &&
          refreshToken &&
          !isRefreshRequest
        ) {
          originalRequest._retry = true;

          try {
            const response = await this._axiosInstance.post<{
              token: string;
              refreshToken?: string;
              emailVerified?: boolean;
            }>("/auth/refresh", { token: refreshToken });

            this._authorization = response.data.token;
            sessionStorage.setItem("accessToken", response.data.token);
            if (response.data.refreshToken) {
              sessionStorage.setItem("refreshToken", response.data.refreshToken);
            }
            if (typeof response.data.emailVerified === "boolean") {
              sessionStorage.setItem("emailVerified", String(response.data.emailVerified));
            }

            originalRequest.headers = {
              ...originalRequest.headers,
              Authorization: `Bearer ${response.data.token}`,
            };

            return this._axiosInstance.request(originalRequest);
          } catch (refreshError) {
            sessionStorage.removeItem("accessToken");
            sessionStorage.removeItem("refreshToken");
            sessionStorage.removeItem("emailVerified");
            this._authorization = null;
            throw refreshError;
          }
        }

        throw error;
      }
    );
  }

  public static async getInstance() {
    this._instance ??= new Api(getApiBaseUrl(), null);

    return this._instance;
  }

  public static clearAuthorization() {
    if (this._instance) {
      this._instance._authorization = null;
    }
  }

  public async request<ResponseType>(config: AxiosRequestConfig) {
    const configOptions: AxiosRequestConfig = {
      ...config,
    };

    return this._axiosInstance.request<ResponseType>(configOptions);
  }

  public get<ResponseType>(config: AxiosRequestConfig) {
    const configOptions: AxiosRequestConfig = {
      ...config,
      method: "GET",
    };

    return this.request<ResponseType>(configOptions);
  }

  public post<RequestBodyType, ResponseBodyType>(
    data: RequestBodyType,
    options: AxiosRequestConfig
  ) {
    const configOptions: AxiosRequestConfig = {
      ...options,
      method: "POST",
      data,
    };

    return this.request<ResponseBodyType>(configOptions);
  }

  public delete(options: AxiosRequestConfig) {
    const configOptions: AxiosRequestConfig = {
      ...options,
      method: "DELETE",
    };

    return this.request<void>(configOptions);
  }

  public put<RequestBodyType, ResponseBodyType>(
    data: RequestBodyType,
    options: AxiosRequestConfig
  ) {
    const configOptions: AxiosRequestConfig = {
      ...options,
      method: "PUT",
      data,
    };

    return this.request<ResponseBodyType>(configOptions);
  }

  public patch<RequestBodyType, ResponseBodyType>(
    data: RequestBodyType,
    options: AxiosRequestConfig
  ) {
    const configOptions: AxiosRequestConfig = {
      ...options,
      method: "PATCH",
      data,
    };

    return this.request<ResponseBodyType>(configOptions);
  }
}
