// src/apis/api.ts

import axios, { AxiosInstance, AxiosRequestConfig } from "axios";

export function getApiBaseUrl(): string {
  return `http://${import.meta.env.VITE_BASE_URL}:8080`;
}

export default class Api {
  private static _instance: Api | null = null;

  private _axiosInstance: AxiosInstance;

  private _authorization: string | null;

  public set authorization(value: string) {
    this._authorization = value;
  }

  private constructor(basePath: string, authorization: string | null) {
    this._authorization = authorization || localStorage.getItem("accessToken") || null;

    this._axiosInstance = axios.create({
      baseURL: basePath,
    });

    // Interceptor para agregar la autorización a cada solicitud
    this._axiosInstance.interceptors.request.use(
      (config) => {
        // Si el endpoint es de registro, no incluir Authorization
        const isRegisterEndpoint = config.url?.includes("/auth/register");
        if (!isRegisterEndpoint && this._authorization) {
          config.headers.Authorization = `Bearer ${this._authorization}`;
        }
        return config;
      },
      (error) => Promise.reject(error)
    );
  }

  public static async getInstance() {
    if (!this._instance) {
      this._instance = new Api(getApiBaseUrl(), null);
    }

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
      data: data,
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
      data: data,
    };

    return this.request<ResponseBodyType>(configOptions);
  }
}