# MarketExchange

Plataforma web de trueque (intercambio de objetos sin dinero de por medio) que conecta a usuarios para publicar artículos, negociar propuestas de intercambio, coordinar la entrega y calificarse mutuamente al finalizar. Incluye chat en tiempo real, notificaciones, moderación de contenido y panel de administración.

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.4-brightgreen)
![React](https://img.shields.io/badge/React-18-61DAFB)
![TypeScript](https://img.shields.io/badge/TypeScript-5.6-3178C6)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-336791)
![License](https://img.shields.io/badge/license-MIT-blue)

## Tabla de contenidos

- [Descripción general](#descripción-general)
- [Características principales](#características-principales)
- [Arquitectura](#arquitectura)
- [Stack tecnológico](#stack-tecnológico)
- [Estructura del repositorio](#estructura-del-repositorio)
- [Dominio de negocio](#dominio-de-negocio)
- [Puesta en marcha](#puesta-en-marcha)
  - [Requisitos previos](#requisitos-previos)
  - [Backend](#backend)
  - [Frontend](#frontend)
  - [Todo junto con Docker Compose](#todo-junto-con-docker-compose)
- [Variables de entorno](#variables-de-entorno)
- [API y endpoints principales](#api-y-endpoints-principales)
- [WebSockets en tiempo real](#websockets-en-tiempo-real)
- [Testing](#testing)
- [Seguridad](#seguridad)
- [Observabilidad](#observabilidad)
- [Documentación adicional](#documentación-adicional)
- [Autores](#autores)
- [Licencia](#licencia)

## Descripción general

MarketExchange resuelve la necesidad de intercambiar bienes de forma directa, segura y sin dinero de por medio, promoviendo una economía circular. La plataforma cubre el ciclo completo del trueque:

1. Un usuario publica un artículo con imágenes, categoría y condición.
2. Un administrador lo modera (aprueba o rechaza con motivo).
3. Otro usuario propone un intercambio ofreciendo uno de sus propios artículos.
4. Ambas partes negocian por chat en tiempo real.
5. Al aceptar la propuesta, se genera automáticamente un envío/entrega (presencial o con tracking externo).
6. Al confirmar la entrega ambas partes, el intercambio queda completado.
7. Ambos usuarios se califican mutuamente, construyendo reputación dentro de la comunidad.

El proyecto es un monorepo con dos aplicaciones independientes:

- `backend/` — API REST + WebSocket construida con Spring Boot.
- `frontend/` — SPA construida con React, TypeScript y Vite.

## Características principales

- **Autenticación y cuentas**: registro, login con JWT, refresh tokens, logout con revocación, verificación de email y recuperación de contraseña.
- **Catálogo de artículos**: publicación con hasta 4 imágenes, galería con imagen principal, paginación, filtros (categoría, condición, estado, texto) y ordenamiento.
- **Favoritos**: marcar/desmarcar artículos y listarlos.
- **Propuestas de intercambio**: envío, recepción, aceptación, rechazo, cancelación y finalización, con reserva automática de artículos y cancelación de propuestas competidoras.
- **Chat por propuesta**: mensajería en tiempo real ligada a cada propuesta de intercambio.
- **Notificaciones**: en base de datos y push por WebSocket (propuestas, mensajes, envíos, calificaciones, moderación).
- **Envíos/entregas**: flujo presencial (`IN_PERSON`) y con tracking externo (`EXTERNAL_SHIPPING`), con confirmación doble de entrega.
- **Reputación**: calificaciones (1-5) con criterios adicionales (comunicación, puntualidad, estado del producto) y ventanas de creación/edición.
- **Confianza y moderación**: reportes de usuarios, artículos y propuestas; historial de moderación; bloqueo y suspensión de cuentas.
- **Panel de administración**: gestión de usuarios, moderación de artículos y revisión de reportes.
- **Observabilidad**: Spring Boot Actuator con health checks, métricas y endpoint Prometheus.

## Arquitectura

```
┌─────────────────────┐        HTTPS / REST         ┌──────────────────────────┐
│                      │ ───────────────────────────▶│                          │
│   Frontend (SPA)     │                              │   Backend (API REST)     │
│   React + Vite       │        WebSocket/STOMP       │   Spring Boot            │
│   TypeScript         │ ◀───────────────────────────▶│   Java 17                │
│                      │                              │                          │
└─────────────────────┘                              └───────────┬──────────────┘
                                                                   │
                                                     ┌─────────────┼─────────────┐
                                                     ▼             ▼             ▼
                                              PostgreSQL   Almacenamiento   SMTP (correo)
                                              (Flyway)     (local / Supabase Storage)
```

- El backend expone una API REST documentada con Swagger/OpenAPI y un endpoint STOMP/SockJS (`/ws`) para chat, notificaciones y eventos de propuestas en tiempo real.
- El frontend consume la API vía Axios y se conecta al WebSocket con `@stomp/stompjs` + `sockjs-client`.
- El almacenamiento de imágenes admite dos proveedores intercambiables: sistema de archivos local o Supabase Storage.
- Las migraciones de base de datos se gestionan con Flyway (sin depender de `ddl-auto=update` en producción).

## Stack tecnológico

### Backend

| Categoría | Tecnología |
| --- | --- |
| Lenguaje | Java 17 |
| Framework | Spring Boot 3.3.4 |
| Persistencia | Spring Data JPA + PostgreSQL 16 |
| Migraciones | Flyway |
| Seguridad | Spring Security + JWT (`java-jwt`) |
| Tiempo real | Spring WebSocket (STOMP/SockJS) + Spring Security Messaging |
| Documentación API | springdoc-openapi (Swagger UI) |
| Observabilidad | Spring Boot Actuator + Micrometer/Prometheus |
| Correo | Spring Mail + Thymeleaf (plantillas de email) |
| Mapeo de objetos | ModelMapper |
| Detección de tipo de archivo | Apache Tika |
| Testing | JUnit 5, Mockito, Spring Security Test, Testcontainers (PostgreSQL) |
| Contenedores | Docker + Docker Compose |

### Frontend

| Categoría | Tecnología |
| --- | --- |
| Librería UI | React 18 |
| Lenguaje | TypeScript 5.6 |
| Build tool | Vite 5 |
| Estilos | Tailwind CSS 3 + `tailwind-merge` + `class-variance-authority` |
| Componentes headless | Radix UI (dialog, dropdown, toast, tooltip) |
| Ruteo | React Router DOM 6 |
| HTTP client | Axios |
| Tiempo real | `@stomp/stompjs` + `sockjs-client` |
| Animaciones | Framer Motion |
| Autenticación | JWT decodificado con `jwt-decode` |
| Testing | Vitest, Testing Library (React/DOM/Jest-DOM), jsdom |
| Calidad de código | ESLint + typescript-eslint |
| Análisis estático | SonarQube (`@sonar/scan`) |

## Estructura del repositorio

```
me-project/
├── backend/                         API REST Spring Boot
│   ├── src/main/java/.../
│   │   ├── auth/                    Registro, login, JWT, verificación, recuperación de contraseña
│   │   ├── usuario/                 Perfiles, roles, bloqueo/suspensión
│   │   ├── item/                    Artículos, galería de imágenes, favoritos, moderación
│   │   ├── category/                Categorías del catálogo
│   │   ├── tradeproposal/           Propuestas de intercambio
│   │   ├── shipment/                Envíos y confirmación de entrega
│   │   ├── rating/                  Calificaciones y reputación
│   │   ├── chat/                    Mensajería ligada a propuestas
│   │   ├── notification/            Notificaciones internas
│   │   ├── report/                  Reportes de usuarios/artículos/propuestas
│   │   ├── storage/                 Proveedores de almacenamiento (local / Supabase)
│   │   ├── realtime/                Configuración WebSocket/STOMP
│   │   ├── event/                   Eventos de dominio asíncronos
│   │   ├── exception/               Manejo global de errores
│   │   └── config/                  Seguridad, CORS, OpenAPI, etc.
│   ├── src/test/java/               Tests unitarios, de integración y de seguridad
│   ├── migration/                   Migraciones SQL adicionales (Flyway)
│   ├── docker-compose.yml           PostgreSQL + backend en contenedores
│   ├── Dockerfile                   Build multi-stage (Java 17)
│   └── .env.example                 Variables de entorno de referencia
│
├── frontend/                        SPA React + TypeScript + Vite
│   ├── src/
│   │   ├── pages/                   Vistas de la aplicación (login, catálogo, cuenta, admin, etc.)
│   │   ├── components/              Componentes reutilizables (chat, cards, formularios, navbar)
│   │   ├── services/                Clientes de la API agrupados por dominio
│   │   ├── interfaces/              Tipos TypeScript de request/response
│   │   ├── context/                 Contexto de autenticación (`AuthProvider`)
│   │   ├── routes/                  Rutas protegidas
│   │   ├── jwt/                     Utilidades de decodificación de JWT
│   │   └── apis/                    Configuración base de Axios
│   └── .env.example                 Variables de entorno de referencia
│
├── LICENSE
└── README.md
```

## Dominio de negocio

| Entidad | Descripción |
| --- | --- |
| **Usuario** | Cuenta de la plataforma (rol `USER`/`ADMIN`), perfil, reputación, estado de verificación/bloqueo. |
| **Item** | Artículo publicado para intercambio: nombre, descripción, categoría, condición, estado de moderación y galería de imágenes. |
| **Category** | Categoría a la que pertenecen los artículos. |
| **TradeProposal** | Propuesta de intercambio entre dos usuarios sobre dos artículos, con estados `PENDING`, `ACCEPTED`, `REJECTED`, `CANCELLED`, `COMPLETED`. |
| **Shipment** | Entrega asociada a una propuesta aceptada, presencial o con tracking externo. |
| **Rating** | Calificación (1-5) y comentario dejado por un usuario a otro tras un intercambio completado. |
| **Chat/Message** | Mensajes intercambiados entre los participantes de una propuesta. |
| **Notification** | Notificación interna generada por eventos del sistema. |
| **Report** | Reporte de un usuario, artículo o propuesta para revisión de un administrador. |

### Estados relevantes

- **Item**: `PENDING_APPROVAL` → `APPROVED` / `REJECTED` → `RESERVED` → `EXCHANGED`.
- **TradeProposal**: `PENDING` → `ACCEPTED` / `REJECTED` / `CANCELLED` → `COMPLETED`.
- **Shipment**: `PENDING` → `PREPARING` → `IN_TRANSIT` → `DELIVERED` (o `CANCELLED`).
- **Report**: `PENDING` → `REVIEWED` → `RESOLVED` / `DISMISSED`.

## Puesta en marcha

### Requisitos previos

- **Java 17** y Maven Wrapper (incluido: `mvnw` / `mvnw.cmd`).
- **Node.js 20** o compatible, con npm.
- **PostgreSQL 16** (local vía Docker o una instancia remota, por ejemplo Supabase).
- **Docker y Docker Compose** (opcional pero recomendado).

### Backend

```powershell
cd backend
copy .env.example .env
# Completar DB_URL, DB_USERNAME, DB_PASSWORD, JWT_SECRET, etc. en .env

# Levantar solo la base de datos con Docker
docker compose up -d postgres

# Ejecutar el backend
.\mvnw.cmd spring-boot:run
```

El backend queda disponible en `http://localhost:8080`.

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Health check: `http://localhost:8080/actuator/health`

### Frontend

```powershell
cd frontend
copy .env.example .env
# Verificar que VITE_API_URL apunte al backend, por defecto http://localhost:8080

npm install
npm run dev
```

El frontend queda disponible en `http://localhost:5173`.

Otros comandos útiles:

```powershell
npm run build      # compila TypeScript y genera el build de producción
npm run lint       # analiza el código con ESLint
npm run test       # ejecuta los tests con Vitest
npm run coverage   # ejecuta los tests con reporte de cobertura
```

### Todo junto con Docker Compose

Para levantar PostgreSQL y el backend juntos en contenedores:

```powershell
cd backend
copy .env.example .env
docker compose up -d --build
docker compose logs -f backend
```

El frontend se ejecuta aparte con `npm run dev` apuntando a `VITE_API_URL=http://localhost:8080`.

## Variables de entorno

### Backend (`backend/.env`)

| Variable | Descripción |
| --- | --- |
| `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | Conexión a PostgreSQL. |
| `SPRING_PROFILES_ACTIVE` | Perfil activo: `local`, `dev`, `prod` o `test`. |
| `DDL_AUTO`, `FLYWAY_ENABLED`, `FLYWAY_BASELINE_ON_MIGRATE` | Control de esquema y migraciones. |
| `JWT_SECRET` | Clave para firmar los tokens JWT (mínimo 32 caracteres). |
| `EMAIL_VERIFICATION_TOKEN_HOURS`, `PASSWORD_RESET_TOKEN_MINUTES`, `REFRESH_TOKEN_DAYS` | Vigencia de tokens de auth. |
| `FRONTEND_BASE_URL`, `FRONTEND_VERIFY_EMAIL_PATH`, `FRONTEND_RESET_PASSWORD_PATH` | URLs usadas en los correos enviados al frontend. |
| `CORS_ALLOWED_ORIGINS` | Orígenes permitidos por CORS. |
| `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`, `MAIL_FROM_NAME` | Configuración SMTP para envío de correos. |
| `STORAGE_PROVIDER` | `local` o `supabase`. |
| `STORAGE_LOCAL_BASE_DIRECTORY` | Carpeta para uploads cuando el proveedor es local. |
| `SUPABASE_URL`, `SUPABASE_SECRET_KEY`, `SUPABASE_STORAGE_BUCKET` | Credenciales de Supabase Storage. |
| `SEED_ENABLED`, `SEED_ADMIN_*`, `SEED_USER1_*`, `SEED_USER2_*` | Datos semilla de cuentas de prueba. |

Ver el archivo completo de referencia en [`backend/.env.example`](backend/.env.example). **Nunca** commitear el archivo `.env` real.

### Frontend (`frontend/.env`)

| Variable | Descripción |
| --- | --- |
| `VITE_API_URL` | URL base del backend, por ejemplo `http://localhost:8080`. |
| `VITE_BASE_URL` | Fallback heredado (`localhost`), solo por compatibilidad temporal. |

Ver [`frontend/.env.example`](frontend/.env.example).

## API y endpoints principales

Todas las rutas requieren `Authorization: Bearer <accessToken>` salvo las públicas indicadas.

| Módulo | Rutas destacadas |
| --- | --- |
| Auth (público) | `POST /auth/register`, `/auth/login`, `/auth/verify-email`, `/auth/forgot-password`, `/auth/reset-password`, `/auth/refresh`, `/auth/logout` |
| Usuarios | `GET /usuarios/me`, `PUT /usuarios/me/profile`, `GET /usuarios/listar` (ADMIN), `PUT /usuarios/{id}/suspend`, `/unsuspend`, `/block`, `/unblock` |
| Categorías | `GET /category`, `POST /category` (ADMIN), `PUT /category/{id}` (ADMIN) |
| Artículos | `POST /item`, `GET /item/catalog` (paginado/filtrable), `GET /item/mine`, `GET /item/favorites`, `POST /item/{id}/approve` (ADMIN) |
| Propuestas | `GET /agreements/sent`, `/received`, `POST /agreements`, `PUT /agreements/{id}/accept`, `/reject`, `/cancel` |
| Chat | `GET /agreements/{id}/messages`, `POST /agreements/{id}/messages` |
| Envíos | `PUT /shipments/{id}/prepare`, `/ship`, `/deliver`, `/confirm-delivery`, `/cancel` |
| Ratings | `POST /ratings/crear`, `PUT /ratings/{id}`, `GET /ratings/usuario/{id}/reputation` |
| Notificaciones | `GET /notifications`, `GET /notifications/unread-count`, `PUT /notifications/{id}/read` |
| Reportes | `POST /reports`, `GET /admin/reports` (ADMIN), `PUT /admin/reports/{id}/review` (ADMIN) |
| Operación | `GET /actuator/health`, `/actuator/info`, `/actuator/metrics` (ADMIN), `/actuator/prometheus` (ADMIN) |

Documentación interactiva completa (schemas, parámetros y respuestas) disponible en Swagger UI una vez levantado el backend: `http://localhost:8080/swagger-ui.html`. El detalle exhaustivo de cada endpoint, filtros del catálogo y reglas de negocio está en [`backend/README_FRONTEND.md`](backend/README_FRONTEND.md).

## WebSockets en tiempo real

Endpoint STOMP/SockJS: `http://localhost:8080/ws`.

Autenticación al conectar: enviar el JWT en el header STOMP `Authorization: Bearer <token>` durante `CONNECT`.

| Tipo | Destino | Uso |
| --- | --- | --- |
| SEND | `/app/agreements/{tradeProposalId}/messages` | Enviar mensaje de chat |
| SUBSCRIBE | `/user/queue/agreement-messages` | Recibir mensajes de chat |
| SUBSCRIBE | `/user/queue/notifications` | Recibir notificaciones |
| SUBSCRIBE | `/user/queue/agreement-events` | Recibir cambios de estado de propuestas |

## Testing

**Backend** (JUnit 5, Mockito, Spring Security Test, Testcontainers):

```powershell
cd backend
.\mvnw.cmd clean test
```

Cobertura actual: suite con más de 230 tests, incluyendo unitarios de servicio, de integración de controladores, de seguridad y de concurrencia (propuestas de intercambio).

**Frontend** (Vitest + Testing Library):

```powershell
cd frontend
npm run test         # modo watch
npm run test:run     # una sola corrida
npm run coverage     # con reporte de cobertura
```

## Seguridad

- Contraseñas cifradas con BCrypt.
- Autenticación stateless con JWT de acceso + refresh token revocable.
- Autorización basada en roles (`USER`, `ADMIN`) a nivel de endpoint.
- Verificación de email obligatoria antes de habilitar acciones sensibles.
- Recuperación de contraseña mediante token de un solo uso con expiración.
- Bloqueo y suspensión de cuentas administradas por rol `ADMIN`.
- CORS configurado explícitamente por entorno (`CORS_ALLOWED_ORIGINS`).
- Validación de entrada centralizada con Bean Validation.

## Observabilidad

- Health check público: `GET /actuator/health`, `/actuator/health/liveness`, `/actuator/health/readiness`.
- Info pública: `GET /actuator/info`.
- Métricas protegidas (rol `ADMIN`): `/actuator/metrics`, `/actuator/prometheus`.
- Logs estandarizados, sin `System.out.println` en el flujo principal.

## Documentación adicional

- [`backend/README_FRONTEND.md`](backend/README_FRONTEND.md) — Guía detallada de endpoints, variables de entorno y flujos de autenticación pensada para el consumo desde el frontend.
- [`backend/README.md`](backend/README.md) — Documento académico del proyecto (contexto, modelo de entidades, testing y seguridad).
- [`frontend/README.md`](frontend/README.md) — Guía rápida de instalación y comandos del frontend.
- [`frontend/ROADMAP_FRONTEND.md`](frontend/ROADMAP_FRONTEND.md) — Estado de avance e integración del frontend contra las funcionalidades del backend.

## Autores

- Romero Padilla, Luis Anthony
- Nunayalle Brañes, Llorent Eloy
- Vargas Iglesias, Hanks Jean Pierce

## Licencia

Este proyecto está distribuido bajo la licencia [MIT](LICENSE).
