# MarketExchange Frontend

Frontend React + TypeScript + Vite para MarketExchange.

## Requisitos

- Node.js 20 o compatible.
- Backend levantado en `http://localhost:8080`.

## Configuracion Local

Crear un `.env` desde el ejemplo:

```powershell
copy .env.example .env
```

Variable principal:

```env
VITE_API_URL=http://localhost:8080
```

`VITE_BASE_URL=localhost` sigue funcionando como fallback temporal, pero la variable recomendada es `VITE_API_URL`.

## Comandos

Instalar dependencias:

```powershell
npm install
```

Levantar frontend:

```powershell
npm run dev
```

Build:

```powershell
npm run build
```

Lint:

```powershell
npm run lint
```

## URLs

- Frontend local: `http://localhost:5173`
- Backend API: `http://localhost:8080`
- Swagger backend: `http://localhost:8080/swagger-ui.html`

