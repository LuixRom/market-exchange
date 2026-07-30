# MarketExchange Backend - Guia Para Frontend

Esta guia resume como levantar el backend localmente, que variables configurar y que endpoints/funcionalidades tiene disponibles el frontend.

## URLs Locales

- Backend API: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Health check: `http://localhost:8080/actuator/health`
- Liveness check: `http://localhost:8080/actuator/health/liveness`
- Readiness check: `http://localhost:8080/actuator/health/readiness`
- WebSocket STOMP: `http://localhost:8080/ws`
- Frontend esperado para CORS: `http://localhost:5173`

## Levantar Rapido Para Probar Frontend

Este es el camino recomendado si el frontend quiere probar contra el backend local usando la base y storage de Supabase.

### 1. Requisitos

- Java 17.
- Maven wrapper incluido en el proyecto (`mvnw.cmd`).
- Una terminal PowerShell ubicada en `backend`.
- Credenciales reales de Supabase para DB y Storage.

Docker no es obligatorio si se usa Supabase como base de datos.

### 2. Ir Al Backend

```powershell
cd C:\Users\Hanksvi\Desktop\MarketExchange\market-exchange\backend
```

### 3. Configurar Java 17

```powershell
$env:JAVA_HOME="C:\Program Files\Java\jdk-17"
$env:Path="$env:JAVA_HOME\bin;$env:Path"
java -version
```

Debe mostrar Java 17.

### 4. Configurar Variables En PowerShell

Estas variables viven solo en la terminal actual. Si se cierra la terminal, hay que volver a ponerlas.

```powershell
$env:SPRING_PROFILES_ACTIVE="dev"

$env:DB_URL="jdbc:postgresql://aws-0-sa-east-1.pooler.supabase.com:5432/postgres?sslmode=require"
$env:DB_USERNAME="postgres.tuvdcedzcvbcuczcctzk"
$env:DB_PASSWORD="TU_PASSWORD_REAL_DE_SUPABASE"

$env:JWT_SECRET="UNA_CLAVE_LARGA_LOCAL_DE_AL_MENOS_32_CARACTERES"

$env:STORAGE_PROVIDER="supabase"
$env:SUPABASE_URL="https://TU_PROJECT_REF.supabase.co"
$env:SUPABASE_SECRET_KEY="TU_SERVICE_ROLE_KEY"
$env:SUPABASE_STORAGE_BUCKET="market-exchange-items"

$env:FRONTEND_BASE_URL="http://localhost:5173"
$env:CORS_ALLOWED_ORIGINS="http://localhost:5173"

$env:SEED_ENABLED="false"
$env:MANAGEMENT_HEALTH_MAIL_ENABLED="false"
```

Notas:

- `DB_PASSWORD` es la password de la base de datos de Supabase, no la publishable key.
- `SUPABASE_SECRET_KEY` debe ser la service role key y no debe subirse al repo.
- `MANAGEMENT_HEALTH_MAIL_ENABLED=false` evita que `/actuator/health` falle en local por no tener SMTP configurado.

### 5. Levantar Backend

```powershell
.\mvnw.cmd spring-boot:run
```

El backend queda disponible en:

```txt
http://localhost:8080
```

### 6. Verificar Que Esta Arriba

En otra PowerShell:

```powershell
curl http://localhost:8080/actuator/health/liveness -UseBasicParsing
curl http://localhost:8080/actuator/health/readiness -UseBasicParsing
```

Si `liveness` responde `UP`, la app esta viva. Si `readiness` responde `UP`, la app esta lista para recibir requests.

Tambien se puede verificar en Supabase:

```sql
select * from flyway_schema_history order by installed_rank;
```

Si hay filas, el backend conecto y ejecuto migraciones.

### 7. Probar Desde Frontend

El frontend debe apuntar a:

```env
VITE_API_URL=http://localhost:8080
```

Despues de hacer login, enviar el token asi:

```http
Authorization: Bearer <accessToken>
```

## Formas De Levantar El Backend

### Opcion A: DB Supabase, backend en IntelliJ o terminal

Usar las variables de la seccion "Levantar Rapido Para Probar Frontend".

En IntelliJ, crear una Run Configuration de Spring Boot y poner las variables en `Environment variables`.

Importante: IntelliJ no lee automaticamente las variables que se pusieron en PowerShell.

### Opcion B: DB en Docker, backend en IntelliJ o terminal

```powershell
cd C:\Users\Hanksvi\Desktop\MarketExchange\market-exchange\backend
copy .env.example .env
docker compose up -d postgres

$env:JAVA_HOME="C:\Program Files\Java\jdk-17"
$env:Path="$env:JAVA_HOME\bin;$env:Path"
.\mvnw.cmd spring-boot:run
```

En IntelliJ, crear una Run Configuration de Spring Boot y poner las variables del `.env` en `Environment variables`.

### Opcion C: Backend y DB en Docker Compose

```powershell
cd C:\Users\Hanksvi\Desktop\MarketExchange\market-exchange\backend
copy .env.example .env
docker compose up -d --build
docker compose logs -f backend
```

Importante: fuera de Docker el backend usa `localhost:5555`; dentro de Docker Compose usa `postgres:5432` porque `postgres` es el nombre del servicio interno.

## Variables De Entorno

Copiar `backend/.env.example` a `backend/.env`.

Minimas para Supabase:

```env
SPRING_PROFILES_ACTIVE=dev

DB_URL=jdbc:postgresql://aws-0-sa-east-1.pooler.supabase.com:5432/postgres?sslmode=require
DB_USERNAME=postgres.tuvdcedzcvbcuczcctzk
DB_PASSWORD=

JWT_SECRET=

STORAGE_PROVIDER=supabase
SUPABASE_URL=
SUPABASE_SECRET_KEY=
SUPABASE_STORAGE_BUCKET=market-exchange-items

FRONTEND_BASE_URL=http://localhost:5173
CORS_ALLOWED_ORIGINS=http://localhost:5173
```

Minimas para DB local con Docker:

```env
POSTGRES_DB=marketexchange
POSTGRES_USER=postgres
POSTGRES_PASSWORD=
POSTGRES_PORT=5555
BACKEND_PORT=8080

SPRING_PROFILES_ACTIVE=local
DB_URL=jdbc:postgresql://localhost:5555/marketexchange
DB_USERNAME=postgres
DB_PASSWORD=
JWT_SECRET=
```

No commitear `.env`. El repo solo debe tener `.env.example`.

Variables importantes por bloque:

- DB/Flyway: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `DDL_AUTO`, `FLYWAY_ENABLED`, `FLYWAY_BASELINE_ON_MIGRATE`
- Auth: `JWT_SECRET`, `EMAIL_VERIFICATION_TOKEN_HOURS`, `PASSWORD_RESET_TOKEN_MINUTES`, `REFRESH_TOKEN_DAYS`
- Frontend/CORS: `FRONTEND_BASE_URL`, `FRONTEND_VERIFY_EMAIL_PATH`, `CORS_ALLOWED_ORIGINS`
- Mail: `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`
- Storage imagenes: `STORAGE_PROVIDER`, `STORAGE_LOCAL_BASE_DIRECTORY`, `SUPABASE_URL`, `SUPABASE_SECRET_KEY`, `SUPABASE_STORAGE_BUCKET`
- Seed: `SEED_ENABLED`, `SEED_ADMIN_*`, `SEED_USER1_*`, `SEED_USER2_*`

## Cuentas Seed

Si `SEED_ENABLED=true`, se crean estas cuentas:

| Rol | Email | Password |
| --- | --- | --- |
| ADMIN | `admin@marketexchange.local` | valor de `SEED_ADMIN_PASSWORD` |
| USER | `cliente1@marketexchange.local` | valor de `SEED_USER1_PASSWORD` |
| USER | `cliente2@marketexchange.local` | valor de `SEED_USER2_PASSWORD` |

Las cuentas seed nacen con email verificado. Para activarlas, usar `SEED_ENABLED=true` y definir las tres contrasenas `SEED_*_PASSWORD` en el `.env` local.

## Autenticacion

Rutas publicas:

- `POST /auth/register`
- `POST /auth/login`
- `POST /auth/verify-email`
- `POST /auth/forgot-password`
- `POST /auth/reset-password`
- `POST /auth/refresh`
- `POST /auth/logout`

El resto normalmente requiere:

```http
Authorization: Bearer <accessToken>
```

Flujo recomendado:

1. Registrar usuario con `/auth/register`.
2. Usuario recibe correo de verificacion si SMTP esta configurado.
3. Frontend abre `/verify-email?token=...`.
4. Frontend llama `POST /auth/verify-email`.
5. Usuario ya puede hacer login.

Recuperar password:

1. `POST /auth/forgot-password` con email.
2. Backend envia link a `FRONTEND_BASE_URL`.
3. Frontend abre pantalla de reset con `token`.
4. `POST /auth/reset-password`.

## Funcionalidades Implementadas

- Registro/login con JWT, refresh token, logout y verificacion de email.
- Recuperacion de password por token.
- Perfil de usuario editable.
- Catalogo paginado y filtrable.
- Favoritos.
- Items con galeria de hasta 4 imagenes.
- Moderacion de items por admin.
- Reportes de contenido.
- Propuestas de intercambio.
- Estados y envios de intercambio.
- Chat entre participantes de una propuesta.
- Notificaciones REST y WebSocket.
- Ratings/reputacion tras intercambio completado.
- Bloqueo/suspension de usuarios por admin.
- Actuator health/info/metrics.

## Endpoints Principales

### Auth

| Metodo | Ruta | Auth | Uso |
| --- | --- | --- | --- |
| POST | `/auth/register` | Publico | Registrar usuario |
| POST | `/auth/login` | Publico | Login |
| POST | `/auth/verify-email` | Publico | Verificar correo |
| POST | `/auth/forgot-password` | Publico | Solicitar reset de password |
| POST | `/auth/reset-password` | Publico | Cambiar password con token |
| POST | `/auth/refresh` | Publico | Renovar access token |
| POST | `/auth/logout` | Publico/Auth | Revocar refresh token |

### Usuarios

| Metodo | Ruta | Auth |
| --- | --- | --- |
| GET | `/usuarios/me` | USER |
| PUT | `/usuarios/me/profile` | USER |
| GET | `/usuarios/{id}` | USER/ADMIN segun regla |
| PUT | `/usuarios/{id}` | USER |
| DELETE | `/usuarios/{id}` | USER |
| GET | `/usuarios/listar` | ADMIN |
| PUT | `/usuarios/{id}/suspend` | ADMIN |
| PUT | `/usuarios/{id}/unsuspend` | ADMIN |
| PUT | `/usuarios/{id}/block` | ADMIN |
| PUT | `/usuarios/{id}/unblock` | ADMIN |

### Categorias

| Metodo | Ruta | Auth |
| --- | --- | --- |
| GET | `/category` | USER |
| GET | `/category/{id}` | USER |
| POST | `/category` | ADMIN |
| PUT | `/category/{id}` | ADMIN |
| DELETE | `/category/{id}` | ADMIN |

### Items / Catalogo

| Metodo | Ruta | Auth | Notas |
| --- | --- | --- | --- |
| POST | `/item` | USER | `multipart/form-data`; soporta `image` legacy o `images` |
| GET | `/item/catalog` | USER/ADMIN | Paginado y filtros |
| GET | `/item` | USER/ADMIN | Listado general |
| GET | `/item/{itemId}` | USER |
| PUT | `/item/{itemId}` | USER/ADMIN |
| DELETE | `/item/{itemId}` | USER/ADMIN |
| GET | `/item/mine` | USER | Items del usuario autenticado |
| GET | `/item/category/{categoryId}` | USER |
| GET | `/item/user/{userId}` | USER |
| GET | `/item/{itemId}/image` | USER/ADMIN | Imagen principal legacy |
| GET | `/item/{itemId}/images` | USER/ADMIN |
| GET | `/item/{itemId}/images/{imageId}` | USER/ADMIN |
| POST | `/item/{itemId}/images` | USER/ADMIN | Maximo 4 imagenes por item |
| DELETE | `/item/{itemId}/images/{imageId}` | USER/ADMIN |
| PUT | `/item/{itemId}/images/{imageId}/primary` | USER/ADMIN |
| POST | `/item/{itemId}/favorite` | USER |
| DELETE | `/item/{itemId}/favorite` | USER |
| GET | `/item/favorites` | USER |
| POST | `/item/{itemId}/approve?approve=true|false&reason=...` | ADMIN |
| GET | `/item/{itemId}/moderation-history` | ADMIN |
| DELETE | `/item/images/orphans` | ADMIN | Limpieza de imagenes huerfanas |

Filtros utiles de `/item/catalog`:

```txt
page=0
size=12
sort=createdAt,desc
categoryId=1
userId=2
condition=NEW
status=APPROVED
q=bicicleta
```

### Propuestas De Intercambio

| Metodo | Ruta | Auth |
| --- | --- | --- |
| GET | `/agreements` | USER/ADMIN |
| GET | `/agreements/sent` | USER |
| GET | `/agreements/received` | USER |
| POST | `/agreements` | USER |
| GET | `/agreements/{id}` | USER/ADMIN |
| PUT | `/agreements/{id}/accept` | USER/ADMIN |
| PUT | `/agreements/{id}/reject` | USER/ADMIN |
| PUT | `/agreements/{id}/cancel` | USER/ADMIN |
| DELETE | `/agreements/{id}` | ADMIN |

Al aceptar una propuesta:

- La propuesta ganadora queda `ACCEPTED`.
- Propuestas competidoras quedan `CANCELLED`.
- Items quedan reservados.
- Se crea un shipment.
- Se emiten notificaciones/eventos.

### Chat

| Metodo | Ruta | Auth |
| --- | --- | --- |
| GET | `/agreements/{tradeProposalId}/messages` | USER |
| POST | `/agreements/{tradeProposalId}/messages` | USER |

El chat existe dentro de una propuesta. Solo participantes pueden leer/enviar.

### Shipments / Entrega

| Metodo | Ruta | Auth |
| --- | --- | --- |
| GET | `/shipments` | ADMIN |
| GET | `/shipments/{id}` | USER/ADMIN |
| PUT | `/shipments/{id}` | USER/ADMIN |
| PUT | `/shipments/{id}/prepare` | USER/ADMIN |
| PUT | `/shipments/{id}/ship` | USER/ADMIN |
| PUT | `/shipments/{id}/deliver` | USER/ADMIN |
| PUT | `/shipments/{id}/confirm-delivery` | USER/ADMIN |
| PUT | `/shipments/{id}/cancel` | USER/ADMIN |
| DELETE | `/shipments/{id}` | ADMIN |

Tipos de entrega:

- `EXTERNAL_SHIPPING`
- `IN_PERSON`

El intercambio se considera completado cuando ambas partes confirman entrega.

### Ratings / Reputacion

| Metodo | Ruta | Auth |
| --- | --- | --- |
| POST | `/ratings/crear` | USER |
| PUT | `/ratings/{id}` | USER |
| GET | `/ratings/usuario/{usuarioId}` | USER |
| GET | `/ratings/usuario/{usuarioId}/reputation` | USER |
| GET | `/ratings/listar` | ADMIN |
| DELETE | `/ratings/{id}` | ADMIN |

Reglas:

- Solo se puede calificar una propuesta `COMPLETED`.
- Hay ventana de 14 dias para crear rating.
- Hay ventana de 48 horas para editar rating.

### Notificaciones

| Metodo | Ruta | Auth |
| --- | --- | --- |
| GET | `/notifications` | USER |
| GET | `/notifications?unreadOnly=true` | USER |
| GET | `/notifications/unread-count` | USER |
| PUT | `/notifications/{id}/read` | USER |

### Reportes Y Moderacion

| Metodo | Ruta | Auth |
| --- | --- | --- |
| POST | `/reports` | USER |
| GET | `/admin/reports` | ADMIN |
| GET | `/admin/reports/{id}` | ADMIN |
| PUT | `/admin/reports/{id}/review` | ADMIN |

Targets de reporte:

- `USER`
- `ITEM`
- `TRADE_PROPOSAL`

Estados:

- `PENDING`
- `REVIEWED`
- `RESOLVED`
- `DISMISSED`

## WebSockets

Endpoint STOMP/SockJS:

```txt
http://localhost:8080/ws
```

En el `CONNECT`, enviar JWT en header:

```txt
Authorization: Bearer <accessToken>
```

Destinos:

| Tipo | Destino | Uso |
| --- | --- | --- |
| SEND | `/app/agreements/{tradeProposalId}/messages` | Enviar mensaje de chat |
| SUBSCRIBE | `/user/queue/agreement-messages` | Recibir mensajes de chat |
| SUBSCRIBE | `/user/queue/notifications` | Recibir notificaciones |
| SUBSCRIBE | `/user/queue/agreement-events` | Recibir cambios/eventos de propuestas |

El envio WebSocket usa el mismo body que el POST REST de mensajes.

## Imagenes

Los items soportan hasta 4 imagenes.

Para crear item con imagenes:

```txt
POST /item
Content-Type: multipart/form-data
```

Campos esperados:

- Datos del item segun DTO.
- `images`: lista de archivos.
- `image`: archivo legacy opcional.

Para mostrar imagenes:

- Principal legacy: `GET /item/{itemId}/image`
- Galeria: `GET /item/{itemId}/images`
- Archivo puntual: `GET /item/{itemId}/images/{imageId}`

## Estados Importantes Para UI

Items:

- `PENDING_APPROVAL`
- `APPROVED`
- `REJECTED`
- `RESERVED`
- `EXCHANGED`

Propuestas:

- `PENDING`
- `ACCEPTED`
- `REJECTED`
- `CANCELLED`
- `COMPLETED`

Shipments:

- `PENDING`
- `PREPARING`
- `IN_TRANSIT`
- `DELIVERED`
- `CANCELLED`

## Comandos Utiles

Tests:

```powershell
cd C:\Users\Hanksvi\Desktop\MarketExchange\market-exchange\backend
$env:JAVA_HOME="C:\Program Files\Java\jdk-17"
$env:Path="$env:JAVA_HOME\bin;$env:Path"
.\mvnw.cmd clean test
```

Ver logs Docker:

```powershell
docker compose logs -f backend
docker compose logs -f postgres
```

Parar contenedores:

```powershell
docker compose down
```

Parar y borrar volumen de DB local:

```powershell
docker compose down -v
```

## Notas Para Frontend

- Siempre enviar `Authorization: Bearer <token>` salvo en `/auth/**`, `/actuator/health` y `/actuator/info`.
- Las cuentas no seed deben verificar email antes de poder hacer login.
- El frontend debe implementar pantallas para `/verify-email?token=...` y `/reset-password?token=...`.
- Para desarrollo local sin SMTP real, se puede registrar igual, pero el correo no saldra si `MAIL_USERNAME`/`MAIL_PASSWORD` no estan configurados.
- La referencia mas exacta de schemas esta en Swagger: `http://localhost:8080/swagger-ui.html`.
