# 🔄 MarketExchange

Plataforma web de trueque que permite intercambiar objetos entre usuarios sin utilizar dinero.

MarketExchange permite publicar artículos, buscar productos, enviar propuestas de intercambio, negociar mediante chat en tiempo real, coordinar entregas y construir reputación mediante calificaciones.

La plataforma incluye autenticación JWT, verificación por correo electrónico, moderación de contenido, notificaciones en tiempo real, almacenamiento de imágenes, reportes y panel administrativo.

---

## 🌐 Demo en producción

La aplicación se encuentra desplegada públicamente.

### Frontend

🔗 https://market-exchange-frontend.vercel.app

### Backend API

🔗 https://market-exchange-backend.onrender.com

### Health Check

🔗 https://market-exchange-backend.onrender.com/actuator/health/liveness

---

## 🛠️ Tecnologías

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.4-brightgreen)
![React](https://img.shields.io/badge/React-18-61DAFB)
![TypeScript](https://img.shields.io/badge/TypeScript-5.6-3178C6)
![Vite](https://img.shields.io/badge/Vite-5-646CFF)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Supabase-336791)
![Supabase](https://img.shields.io/badge/Supabase-Database%20%26%20Storage-3ECF8E?logo=supabase)
![Render](https://img.shields.io/badge/Backend-Render-46E3B7?logo=render)
![Vercel](https://img.shields.io/badge/Frontend-Vercel-black?logo=vercel)
![Docker](https://img.shields.io/badge/Docker-2496ED?logo=docker&logoColor=white)
![License](https://img.shields.io/badge/license-MIT-blue)

---

## 📑 Tabla de contenidos

- [Demo en producción](#-demo-en-producción)
- [Descripción general](#-descripción-general)
- [Características principales](#-características-principales)
- [Arquitectura](#-arquitectura)
- [Infraestructura de producción](#️-infraestructura-de-producción)
- [Stack tecnológico](#-stack-tecnológico)
- [Estructura del repositorio](#-estructura-del-repositorio)
- [Dominio de negocio](#-dominio-de-negocio)
- [Puesta en marcha](#-puesta-en-marcha)
  - [Requisitos previos](#requisitos-previos)
  - [Backend](#backend)
  - [Frontend](#frontend)
  - [Docker Compose](#docker-compose)
- [Variables de entorno](#-variables-de-entorno)
- [Despliegue](#-despliegue)
- [API y endpoints principales](#-api-y-endpoints-principales)
- [WebSockets en tiempo real](#-websockets-en-tiempo-real)
- [Testing](#-testing)
- [Seguridad](#-seguridad)
- [Observabilidad](#-observabilidad)
- [Calidad de código](#-calidad-de-código)
- [Documentación adicional](#-documentación-adicional)
- [Autores](#-autores)
- [Licencia](#-licencia)

---

# 📖 Descripción general

MarketExchange resuelve la necesidad de intercambiar bienes de forma directa, segura y sin dinero de por medio, promoviendo la reutilización de objetos y la economía circular.

La plataforma cubre el ciclo completo de un intercambio:

1. Un usuario crea una cuenta y verifica su correo electrónico.
2. Publica un artículo con imágenes, categoría, descripción y condición.
3. Un administrador revisa y modera el artículo.
4. Otro usuario encuentra el artículo en el catálogo.
5. Envía una propuesta ofreciendo uno de sus propios artículos.
6. Ambas partes pueden comunicarse mediante chat en tiempo real.
7. El propietario acepta o rechaza la propuesta.
8. Cuando una propuesta es aceptada se genera el flujo de entrega.
9. Ambas partes confirman la recepción.
10. El intercambio queda completado.
11. Los usuarios pueden calificarse mutuamente.

El proyecto está desarrollado como un **monorepo**:

```text
market-exchange/
├── backend/
└── frontend/
```

- `backend/` — API REST y WebSocket desarrollada con Spring Boot.
- `frontend/` — SPA desarrollada con React, TypeScript y Vite.

---

# ✨ Características principales

- **Autenticación y cuentas**
  - Registro de usuarios.
  - Login con JWT.
  - Access Token y Refresh Token.
  - Logout con revocación.
  - Verificación de correo electrónico.
  - Recuperación de contraseña.
  - Cambio de contraseña mediante token.

- **Catálogo de artículos**
  - Publicación de artículos.
  - Hasta 4 imágenes por artículo.
  - Imagen principal.
  - Categorías.
  - Condición del producto.
  - Paginación.
  - Filtros.
  - Ordenamiento.
  - Búsqueda por texto.

- **Favoritos**
  - Agregar artículos a favoritos.
  - Eliminar favoritos.
  - Listar artículos favoritos.

- **Propuestas de intercambio**
  - Enviar propuestas.
  - Ver propuestas enviadas.
  - Ver propuestas recibidas.
  - Aceptar.
  - Rechazar.
  - Cancelar.
  - Finalizar intercambios.
  - Reserva automática de artículos.
  - Cancelación de propuestas competidoras.

- **Chat en tiempo real**
  - Mensajes asociados a propuestas.
  - Comunicación mediante WebSocket.
  - STOMP + SockJS.

- **Notificaciones**
  - Notificaciones persistidas en base de datos.
  - Notificaciones push mediante WebSocket.
  - Propuestas.
  - Mensajes.
  - Envíos.
  - Calificaciones.
  - Moderación.

- **Envíos y entregas**
  - Entrega presencial.
  - Envío externo.
  - Tracking.
  - Confirmación doble de entrega.

- **Sistema de reputación**
  - Calificaciones de 1 a 5.
  - Comunicación.
  - Puntualidad.
  - Estado del producto.
  - Comentarios.

- **Moderación**
  - Aprobación y rechazo de artículos.
  - Historial de moderación.
  - Reportes.
  - Suspensión de usuarios.
  - Bloqueo de cuentas.

- **Panel administrativo**
  - Gestión de usuarios.
  - Moderación de artículos.
  - Gestión de categorías.
  - Revisión de reportes.

- **Observabilidad**
  - Spring Boot Actuator.
  - Health checks.
  - Liveness / Readiness.
  - Métricas.
  - Prometheus.

---

# 🏗️ Arquitectura

```text
                            INTERNET
                               │
                               ▼
                 ┌─────────────────────────┐
                 │      Frontend SPA       │
                 │                         │
                 │   React + TypeScript    │
                 │        Vite             │
                 │                         │
                 │      Vercel HTTPS       │
                 └────────────┬────────────┘
                              │
                   HTTPS / REST / WebSocket
                              │
                              ▼
                 ┌─────────────────────────┐
                 │       Backend API       │
                 │                         │
                 │     Spring Boot 3       │
                 │        Java 17          │
                 │                         │
                 │      Render HTTPS       │
                 └────────────┬────────────┘
                              │
              ┌───────────────┼────────────────┐
              │               │                │
              ▼               ▼                ▼
     ┌────────────────┐ ┌──────────────┐ ┌──────────────┐
     │   PostgreSQL   │ │   Supabase   │ │  Gmail SMTP  │
     │                │ │   Storage    │ │              │
     │    Supabase    │ │   Imágenes   │ │   Correos    │
     └────────────────┘ └──────────────┘ └──────────────┘
              │
              ▼
           Flyway
       Migraciones SQL
```

### Comunicación

El frontend consume la API mediante:

```text
Axios → HTTPS → Spring Boot
```

La comunicación en tiempo real utiliza:

```text
React
  ↓
SockJS
  ↓
STOMP
  ↓
Spring WebSocket
```

La autenticación utiliza:

```text
Authorization: Bearer <JWT>
```

Los tokens se envían explícitamente mediante headers HTTP/STOMP.

---

# ☁️ Infraestructura de producción

| Componente | Tecnología / Servicio |
|---|---|
| Frontend | Vercel |
| Backend | Render |
| Base de datos | Supabase PostgreSQL |
| Storage | Supabase Storage |
| Correos | Gmail SMTP |
| Contenedores | Docker |
| Migraciones | Flyway |
| Control de versiones | Git + GitHub |
| Análisis estático | SonarQube |
| Health monitoring | Spring Boot Actuator |

## URLs

| Servicio | URL |
|---|---|
| Aplicación | https://market-exchange-frontend.vercel.app |
| Backend API | https://market-exchange-backend.onrender.com |
| Liveness | https://market-exchange-backend.onrender.com/actuator/health/liveness |

---

# 🧰 Stack tecnológico

## Backend

| Categoría | Tecnología |
|---|---|
| Lenguaje | Java 17 |
| Framework | Spring Boot 3.3.4 |
| Persistencia | Spring Data JPA |
| Base de datos | PostgreSQL / Supabase PostgreSQL |
| Migraciones | Flyway |
| Pool de conexiones | HikariCP |
| Seguridad | Spring Security |
| Autenticación | JWT (`java-jwt`) |
| Tiempo real | Spring WebSocket + STOMP/SockJS |
| Seguridad WebSocket | Spring Security Messaging |
| API Docs | springdoc-openapi / Swagger |
| Observabilidad | Spring Boot Actuator |
| Métricas | Micrometer / Prometheus |
| Correos | Spring Mail |
| Templates email | Thymeleaf |
| Storage | Supabase Storage / almacenamiento local |
| Mapeo | ModelMapper |
| Archivos | Apache Tika |
| Testing | JUnit 5 |
| Mocking | Mockito |
| Integración | Testcontainers |
| Contenedores | Docker + Docker Compose |

---

## Frontend

| Categoría | Tecnología |
|---|---|
| UI | React 18 |
| Lenguaje | TypeScript 5.6 |
| Build Tool | Vite 5 |
| CSS | Tailwind CSS 3 |
| Componentes | Radix UI |
| Routing | React Router DOM 6 |
| HTTP | Axios |
| Tiempo real | `@stomp/stompjs` |
| Fallback WebSocket | `sockjs-client` |
| Animaciones | Framer Motion |
| JWT | `jwt-decode` |
| Testing | Vitest |
| Testing UI | Testing Library |
| DOM testing | jsdom |
| Linting | ESLint |
| Static Analysis | SonarQube |

---

# 📁 Estructura del repositorio

```text
market-exchange/
│
├── backend/
│   │
│   ├── src/main/java/...
│   │   ├── auth/
│   │   ├── usuario/
│   │   ├── item/
│   │   ├── category/
│   │   ├── tradeproposal/
│   │   ├── shipment/
│   │   ├── rating/
│   │   ├── chat/
│   │   ├── notification/
│   │   ├── report/
│   │   ├── storage/
│   │   ├── realtime/
│   │   ├── event/
│   │   ├── exception/
│   │   └── config/
│   │
│   ├── src/test/java/
│   ├── migration/
│   ├── Dockerfile
│   ├── docker-compose.yml
│   ├── pom.xml
│   ├── .env.example
│   └── README.md
│
├── frontend/
│   │
│   ├── src/
│   │   ├── pages/
│   │   ├── components/
│   │   ├── services/
│   │   ├── interfaces/
│   │   ├── context/
│   │   ├── routes/
│   │   ├── jwt/
│   │   └── apis/
│   │
│   ├── package.json
│   ├── vite.config.ts
│   ├── .env.example
│   └── README.md
│
├── LICENSE
└── README.md
```

---

# 🧩 Dominio de negocio

| Entidad | Descripción |
|---|---|
| **Usuario** | Cuenta de la plataforma con roles `USER` o `ADMIN`, información de perfil, reputación y estados de cuenta. |
| **Item** | Producto publicado para intercambio. |
| **Category** | Categoría a la que pertenece un artículo. |
| **TradeProposal** | Propuesta de intercambio entre dos usuarios. |
| **Shipment** | Entrega asociada a una propuesta aceptada. |
| **Rating** | Calificación posterior a un intercambio. |
| **Chat / Message** | Comunicación entre usuarios de una propuesta. |
| **Notification** | Notificaciones generadas por eventos del sistema. |
| **Report** | Reportes que requieren revisión administrativa. |

## Estados relevantes

### Item

```text
PENDING_APPROVAL
      ↓
APPROVED / REJECTED
      ↓
RESERVED
      ↓
EXCHANGED
```

### TradeProposal

```text
PENDING
  ↓
ACCEPTED / REJECTED / CANCELLED
  ↓
COMPLETED
```

### Shipment

```text
PENDING
  ↓
PREPARING
  ↓
IN_TRANSIT
  ↓
DELIVERED
```

También puede pasar a:

```text
CANCELLED
```

### Report

```text
PENDING
  ↓
REVIEWED
  ↓
RESOLVED / DISMISSED
```

---

# 💻 Puesta en marcha

## Requisitos previos

Para desarrollo local:

- Java 17.
- Node.js 20 o compatible.
- npm.
- Maven Wrapper incluido en el proyecto.
- PostgreSQL local o remoto.
- Docker y Docker Compose opcionales.

---

## Backend

```powershell
cd backend
```

Crear el `.env` a partir del ejemplo:

```powershell
copy .env.example .env
```

Completar las variables requeridas:

```env
DB_URL=
DB_USERNAME=
DB_PASSWORD=
JWT_SECRET=
```

Ejecutar Spring Boot:

```powershell
.\mvnw.cmd spring-boot:run
```

El backend estará disponible en:

```text
http://localhost:8080
```

### Health Check

```text
http://localhost:8080/actuator/health
```

### Swagger local

```text
http://localhost:8080/swagger-ui.html
```

> Swagger puede estar restringido en producción mediante Spring Security.

---

## Frontend

```powershell
cd frontend
```

Instalar dependencias:

```powershell
npm install
```

Crear:

```text
.env
```

con:

```env
VITE_API_URL=http://localhost:8080
```

Ejecutar:

```powershell
npm run dev
```

Disponible en:

```text
http://localhost:5173
```

### Otros comandos

```powershell
npm run build
npm run lint
npm run test
npm run test:run
npm run coverage
```

---

## Docker Compose

Desde:

```powershell
cd backend
```

Ejecutar:

```powershell
docker compose up -d --build
```

Ver logs:

```powershell
docker compose logs -f backend
```

---

# 🔐 Variables de entorno

## Backend

### Base de datos

```env
DB_URL=
DB_USERNAME=
DB_PASSWORD=
```

### Spring

```env
SPRING_PROFILES_ACTIVE=prod
```

### JWT

```env
JWT_SECRET=
```

### CORS

```env
FRONTEND_BASE_URL=
CORS_ALLOWED_ORIGINS=
```

En producción:

```text
FRONTEND_BASE_URL=https://market-exchange-frontend.vercel.app
CORS_ALLOWED_ORIGINS=https://market-exchange-frontend.vercel.app
```

### SMTP

```env
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=
MAIL_PASSWORD=
MAIL_FROM_NAME=MarketExchange
```

### Storage

```env
STORAGE_PROVIDER=supabase

SUPABASE_URL=
SUPABASE_SECRET_KEY=
SUPABASE_STORAGE_BUCKET=
```

### HikariCP

Para controlar las conexiones disponibles hacia PostgreSQL:

```env
DB_MAX_POOL_SIZE=3
DB_MIN_IDLE=1
```

### Actuator

En producción puede deshabilitarse el health indicator SMTP:

```env
MANAGEMENT_HEALTH_MAIL_ENABLED=false
```

Esto evita que el health check dependa de la disponibilidad del servidor SMTP.

---

## Frontend

### Desarrollo

```env
VITE_API_URL=http://localhost:8080
```

### Producción

En Vercel:

```env
VITE_API_URL=https://market-exchange-backend.onrender.com
```

> Las variables `VITE_*` son visibles desde el navegador. Nunca se deben colocar secretos, contraseñas, claves privadas ni credenciales de base de datos en ellas.

---

# 🚀 Despliegue

## Frontend — Vercel

El frontend React/Vite se despliega desde GitHub mediante Vercel.

Configuración:

```text
Root Directory: frontend
Framework: Vite
```

Variable:

```env
VITE_API_URL=https://market-exchange-backend.onrender.com
```

Producción:

```text
https://market-exchange-frontend.vercel.app
```

---

## Backend — Render

El backend Spring Boot se ejecuta en Render mediante Docker.

Configuración principal:

```text
Root Directory: backend
Runtime: Docker
Dockerfile: Dockerfile
```

Health Check:

```text
/actuator/health/liveness
```

Producción:

```text
https://market-exchange-backend.onrender.com
```

El Dockerfile utiliza una construcción multi-stage:

```text
Java 17 JDK
    ↓
Maven Build
    ↓
Spring Boot JAR
    ↓
Java 17 JRE
```

Render asigna dinámicamente el puerto mediante:

```text
PORT
```

y Spring Boot utiliza:

```properties
server.port=${PORT:8080}
```

---

## Base de datos — Supabase

La base de datos de producción utiliza:

```text
Supabase PostgreSQL
```

El backend accede a PostgreSQL mediante:

```text
Spring Data JPA
      ↓
HikariCP
      ↓
Supabase PostgreSQL
```

Las migraciones de esquema se administran mediante:

```text
Flyway
```

---

## Almacenamiento — Supabase Storage

Las imágenes publicadas por los usuarios se almacenan en:

```text
Supabase Storage
```

El backend es el único componente autorizado para acceder mediante la clave privada de Storage.

El frontend nunca recibe:

```text
SUPABASE_SECRET_KEY
```

---

# 🔌 API y endpoints principales

Todas las rutas protegidas requieren:

```http
Authorization: Bearer <accessToken>
```

salvo las rutas públicas.

| Módulo | Endpoints |
|---|---|
| Auth | `POST /auth/register` |
| Auth | `POST /auth/login` |
| Auth | `POST /auth/verify-email` |
| Auth | `POST /auth/forgot-password` |
| Auth | `POST /auth/reset-password` |
| Auth | `POST /auth/refresh` |
| Auth | `POST /auth/logout` |
| Usuarios | `GET /usuarios/me` |
| Usuarios | `PUT /usuarios/me/profile` |
| Categorías | `GET /category` |
| Categorías | `POST /category` |
| Items | `POST /item` |
| Items | `GET /item/catalog` |
| Items | `GET /item/mine` |
| Favoritos | `GET /item/favorites` |
| Propuestas | `GET /agreements/sent` |
| Propuestas | `GET /agreements/received` |
| Propuestas | `POST /agreements` |
| Propuestas | `PUT /agreements/{id}/accept` |
| Propuestas | `PUT /agreements/{id}/reject` |
| Propuestas | `PUT /agreements/{id}/cancel` |
| Chat | `GET /agreements/{id}/messages` |
| Chat | `POST /agreements/{id}/messages` |
| Shipment | `PUT /shipments/{id}/prepare` |
| Shipment | `PUT /shipments/{id}/ship` |
| Shipment | `PUT /shipments/{id}/deliver` |
| Shipment | `PUT /shipments/{id}/confirm-delivery` |
| Ratings | `POST /ratings/crear` |
| Ratings | `PUT /ratings/{id}` |
| Notificaciones | `GET /notifications` |
| Notificaciones | `GET /notifications/unread-count` |
| Reports | `POST /reports` |
| Admin | `GET /admin/reports` |
| Health | `GET /actuator/health` |
| Liveness | `GET /actuator/health/liveness` |
| Readiness | `GET /actuator/health/readiness` |

La documentación detallada de la integración frontend/backend se encuentra en:

```text
backend/README_FRONTEND.md
```

---

# ⚡ WebSockets en tiempo real

Endpoint:

```text
/ws
```

En desarrollo:

```text
http://localhost:8080/ws
```

En producción el frontend utiliza el backend desplegado en Render.

La autenticación WebSocket utiliza JWT mediante el header STOMP:

```text
Authorization: Bearer <token>
```

## Destinos

| Acción | Destino |
|---|---|
| SEND | `/app/agreements/{tradeProposalId}/messages` |
| SUBSCRIBE | `/user/queue/agreement-messages` |
| SUBSCRIBE | `/user/queue/notifications` |
| SUBSCRIBE | `/user/queue/agreement-events` |

Esto permite recibir en tiempo real:

- mensajes;
- nuevas propuestas;
- cambios de estado;
- notificaciones;
- eventos relacionados con intercambios.

---

# 🧪 Testing

## Backend

Tecnologías:

- JUnit 5.
- Mockito.
- Spring Security Test.
- Testcontainers.
- PostgreSQL.

Ejecutar:

```powershell
cd backend
.\mvnw.cmd clean test
```

La suite incluye más de 230 tests, incluyendo:

- tests unitarios;
- tests de servicios;
- tests de controladores;
- tests de seguridad;
- tests de integración;
- tests relacionados con concurrencia de propuestas.

---

## Frontend

Tecnologías:

- Vitest.
- Testing Library.
- Jest DOM.
- jsdom.

Ejecutar modo watch:

```powershell
npm run test
```

Ejecutar una vez:

```powershell
npm run test:run
```

Cobertura:

```powershell
npm run coverage
```

---

# 🔒 Seguridad

MarketExchange aplica diferentes mecanismos de seguridad:

- Contraseñas cifradas mediante BCrypt.
- Autenticación stateless mediante JWT.
- Access Tokens.
- Refresh Tokens revocables.
- Autorización basada en roles.
- Roles `USER` y `ADMIN`.
- Verificación obligatoria de correo.
- Recuperación segura de contraseña.
- Tokens de un solo uso.
- Expiración de tokens.
- Bloqueo de cuentas.
- Suspensión de usuarios.
- CORS configurable por entorno.
- Validación mediante Bean Validation.
- Autenticación WebSocket mediante JWT.
- Secretos almacenados como variables de entorno.
- Credenciales de base de datos únicamente disponibles en backend.
- Supabase Secret Key únicamente disponible en backend.

El frontend nunca contiene:

```text
DB_PASSWORD
JWT_SECRET
SUPABASE_SECRET_KEY
MAIL_PASSWORD
```

---

# 📊 Observabilidad

Spring Boot Actuator expone health checks y métricas.

### Health

```text
GET /actuator/health
```

### Liveness

```text
GET /actuator/health/liveness
```

### Readiness

```text
GET /actuator/health/readiness
```

### Info

```text
GET /actuator/info
```

### Métricas protegidas

```text
GET /actuator/metrics
GET /actuator/prometheus
```

En producción, Render utiliza:

```text
/actuator/health/liveness
```

como Health Check del servicio.

---

# 📈 Calidad de código

El proyecto utiliza herramientas de análisis estático y calidad de código.

## Backend

SonarQube analiza:

- Security.
- Reliability.
- Maintainability.
- Duplications.
- Coverage.
- Security Hotspots.

## Frontend

Se utilizan:

- ESLint.
- TypeScript.
- Vitest.
- Testing Library.
- SonarQube.

Antes de producción se ejecutan:

```powershell
npm run build
npm run lint
npm run coverage
```

y en backend:

```powershell
.\mvnw.cmd clean test
```

---

# 📚 Documentación adicional

### Backend

[`backend/README_FRONTEND.md`](backend/README_FRONTEND.md)

Guía detallada de integración frontend/backend, endpoints y autenticación.

[`backend/README.md`](backend/README.md)

Documentación académica y técnica del backend.

### Frontend

[`frontend/README.md`](frontend/README.md)

Guía del frontend.

[`frontend/ROADMAP_FRONTEND.md`](frontend/ROADMAP_FRONTEND.md)

Estado de desarrollo e integración del frontend.

---

# 👥 Autores

- Romero Padilla, Luis Anthony
- Nunayalle Brañes, Llorent Eloy
- Vargas Iglesias, Hanks Jean Pierce

---

# 📜 Licencia

Este proyecto está distribuido bajo la licencia [MIT](LICENSE).