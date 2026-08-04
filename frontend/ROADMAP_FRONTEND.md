# MarketExchange Frontend Roadmap

Este roadmap deja ordenado lo que falta para alinear el frontend con el backend actual de MarketExchange.

## Estado Actual

El frontend ya cubre el flujo base:

- Login y registro.
- Dashboard.
- Categorias.
- Creacion y listado de items.
- Aprobacion basica de items para admin.
- Creacion y detalle basico de propuestas de intercambio.
- Perfil/cuenta de usuario.

El backend ya tiene mas funcionalidades listas que todavia no tienen pantalla o integracion completa en frontend:

- Verificacion de email.
- Recuperacion de contrasena.
- Refresh token y logout real.
- Galeria de hasta 4 imagenes por item.
- Favoritos.
- Chat por propuesta.
- WebSockets para mensajes, notificaciones y eventos.
- Notificaciones.
- Shipments/entrega.
- Ratings/reputacion.
- Reportes y moderacion avanzada.
- Administracion de usuarios suspendidos/bloqueados.

## Fase 0 - Alineacion Tecnica

Objetivo: dejar el frontend estable para consumir el backend actual.

- Crear `.env.example` del frontend.
- Unificar variable de API:
  - Opcion recomendada: `VITE_API_URL=http://localhost:8080`.
  - Ajustar `src/apis/api.ts` para usar `VITE_API_URL`.
- Mantener compatibilidad temporal con `VITE_BASE_URL=localhost` si hace falta.
- Corregir textos con encoding roto, por ejemplo acentos o signos de apertura mal renderizados.
- Revisar build:
  - `npm install`
  - `npm run build`
  - `npm run lint`
- Documentar como levantar:
  - `npm run dev`
  - backend requerido en `http://localhost:8080`.

## Fase 1 - Auth Completo

Objetivo: que frontend soporte el flujo real de autenticacion del backend.

- Actualizar `AuthResponse`:
  - `token`
  - `refreshToken`
  - `emailVerified`
  - `emailVerificationToken` si aplica en desarrollo.
- Guardar `refreshToken` de forma consistente.
- Implementar refresh token:
  - Interceptor Axios para renovar sesion ante `401`.
  - Si refresh falla, cerrar sesion.
- Implementar logout real:
  - `POST /auth/logout`
  - Borrar tokens locales.
- Crear pantalla de verificacion de email:
  - Ruta: `/verify-email?token=...`
  - Endpoint: `POST /auth/verify-email`
  - Estados UI: verificando, exito, error, token expirado.
- Actualizar registro:
  - Mostrar mensaje: revisar correo para verificar cuenta.
  - No asumir login automatico si `emailVerified=false`.
- Crear recuperacion de contrasena:
  - Ruta: `/forgot-password`
  - Endpoint: `POST /auth/forgot-password`
  - Ruta: `/reset-password?token=...`
  - Endpoint: `POST /auth/reset-password`
- Cambiar link actual de login:
  - De `href="#"`
  - A `Link to="/forgot-password"`.

## Fase 2 - Items Y Catalogo

Objetivo: aprovechar el catalogo real y la galeria de imagenes.

- Migrar listados a `/item/catalog`.
- Soportar filtros backend:
  - `page`
  - `size`
  - `sort`
  - `categoryId`
  - `userId`
  - `condition`
  - `status`
  - `q`
- Implementar paginacion.
- Implementar busqueda server-side.
- Actualizar `ItemResponse` con los campos reales del backend.
- Crear detalle de item:
  - Ruta sugerida: `/dashboard/items/:id`.
  - Galeria de imagenes.
  - Estado de item.
  - Boton de proponer intercambio.
  - Boton favorito.
- Actualizar formulario de item:
  - Permitir hasta 4 imagenes.
  - Usar campo `images`.
  - Mantener compatibilidad con `image` solo si hace falta.
  - Validar cantidad maxima antes de enviar.
- Agregar gestion de imagenes:
  - Ver galeria.
  - Marcar principal.
  - Eliminar imagen.
- Implementar favoritos:
  - `POST /item/{itemId}/favorite`
  - `DELETE /item/{itemId}/favorite`
  - `GET /item/favorites`
  - Pantalla o tab "Favoritos".

## Fase 3 - Propuestas De Intercambio

Objetivo: cubrir el ciclo completo de propuesta.

- Separar vistas:
  - Propuestas enviadas.
  - Propuestas recibidas.
  - Historial.
- Consumir endpoints:
  - `GET /agreements/sent`
  - `GET /agreements/received`
  - `GET /agreements/{id}`
  - `POST /agreements`
  - `PUT /agreements/{id}/accept`
  - `PUT /agreements/{id}/reject`
  - `PUT /agreements/{id}/cancel`
- Mostrar estados:
  - `PENDING`
  - `ACCEPTED`
  - `REJECTED`
  - `CANCELLED`
  - `COMPLETED`
- Mejorar detalle de propuesta:
  - Items involucrados.
  - Participantes.
  - Estado.
  - Acciones disponibles segun usuario y estado.
- Evitar crear propuesta duplicada desde UI.
- Mostrar cuando un item queda reservado/no disponible.

## Fase 4 - Chat Y WebSockets

Objetivo: tener mensajeria real entre participantes de una propuesta.

- Instalar cliente STOMP/SockJS si se decide usar SockJS:
  - `@stomp/stompjs`
  - `sockjs-client`
- Crear cliente WebSocket central:
  - Conectar a `http://localhost:8080/ws`.
  - Enviar header `Authorization: Bearer <token>`.
  - Reconectar automaticamente.
- Chat por propuesta:
  - Ruta o panel dentro de `/dashboard/agreements/:id`.
  - Cargar historial: `GET /agreements/{tradeProposalId}/messages`.
  - Enviar mensaje REST: `POST /agreements/{tradeProposalId}/messages`.
  - Enviar/recibir por WS:
    - SEND `/app/agreements/{tradeProposalId}/messages`
    - SUBSCRIBE `/user/queue/agreement-messages`
- Estados UI:
  - Enviando.
  - Error de envio.
  - Mensaje propio vs mensaje del otro usuario.
  - Scroll automatico.

## Fase 5 - Notificaciones

Estado: implementada.

Objetivo: avisar cambios importantes sin refrescar.

- Crear campana de notificaciones en Navbar.
- Consumir REST:
  - `GET /notifications`
  - `GET /notifications?unreadOnly=true`
  - `GET /notifications/unread-count`
  - `PUT /notifications/{id}/read`
- Suscribirse por WS:
  - `/user/queue/notifications`
  - `/user/queue/agreement-events`
- Mostrar eventos:
  - Propuesta recibida.
  - Propuesta aceptada/rechazada/cancelada.
  - Mensaje nuevo.
  - Cambio de estado de envio.
  - Moderacion de item.

## Fase 6 - Entrega / Shipments

Estado: implementada.

Objetivo: cerrar el ciclo real del intercambio.

- Crear pantalla o seccion de entrega dentro del detalle de propuesta aceptada.
- Consumir endpoints:
  - `GET /shipments/{id}`
  - `PUT /shipments/{id}/prepare`
  - `PUT /shipments/{id}/ship`
  - `PUT /shipments/{id}/deliver`
  - `PUT /shipments/{id}/confirm-delivery`
  - `PUT /shipments/{id}/cancel`
- Mostrar tipos:
  - `EXTERNAL_SHIPPING`
  - `IN_PERSON`
- Mostrar estados:
  - `PENDING`
  - `PREPARING`
  - `IN_TRANSIT`
  - `DELIVERED`
  - `CANCELLED`
- Desbloquear rating cuando el intercambio quede completado.

## Fase 7 - Ratings Y Reputacion

Estado: implementada.

Objetivo: permitir calificar despues de intercambios completados.

- Crear componente de rating.
- Crear modal/pantalla para calificar:
  - `POST /ratings/crear`
  - `PUT /ratings/{id}`
- Mostrar reputacion en:
  - Perfil de usuario.
  - Detalle de item.
  - Propuesta/intercambio.
- Consumir:
  - `GET /ratings/usuario/{usuarioId}`
  - `GET /ratings/usuario/{usuarioId}/reputation`
- Validar reglas visuales:
  - Solo propuestas `COMPLETED`.
  - Ventana de 14 dias para crear.
  - Ventana de 48 horas para editar.

## Fase 8 - Reportes Y Moderacion

Estado: implementada.

Objetivo: dar herramientas de seguridad y administracion.

- Crear flujo de reportar:
  - Usuario.
  - Item.
  - Propuesta.
  - Endpoint: `POST /reports`.
- Crear panel admin de reportes:
  - `GET /admin/reports`
  - `GET /admin/reports/{id}`
  - `PUT /admin/reports/{id}/review`
- Mejorar moderacion de items:
  - Aprobar/rechazar con razon.
  - Ver historial: `GET /item/{itemId}/moderation-history`.
- Crear panel admin de usuarios:
  - Listar usuarios.
  - Suspender/desuspender.
  - Bloquear/desbloquear.
- Agregar permisos por rol en rutas y acciones UI.

## Fase 9 - Perfil Y Cuenta

Estado: implementada.

Objetivo: dejar cuenta de usuario lista para producto.

- Completar edicion de perfil:
  - Nombre.
  - Apellido.
  - Telefono.
  - Direccion.
- Mostrar email verificado/no verificado.
- Mostrar reputacion.
- Mostrar items publicados.
- Mostrar favoritos.
- Mostrar historial de intercambios.
- Confirmacion fuerte antes de eliminar cuenta.

## Fase 10 - Calidad, UX Y Preparacion Final

Objetivo: dejar frontend listo para demo/producto.

- Manejo global de errores.
- Estados loading/vacio/error en todas las pantallas.
- Toasts consistentes.
- Formularios con validacion clara.
- Responsive mobile/desktop.
- Accesibilidad basica:
  - Labels.
  - Focus.
  - Estados disabled.
  - Contraste.
- Revisar encoding en todos los textos.
- Revisar nombres de rutas y navegacion.
- Agregar tests donde tenga sentido:
  - Servicios.
  - AuthProvider.
  - Pantallas criticas.
- Validar con backend real:
  - Registro.
  - Verificacion email.
  - Login.
  - Crear item con 4 imagenes.
  - Proponer intercambio.
  - Aceptar/rechazar.
  - Chat.
  - Shipment.
  - Rating.
  - Reporte.

## Prioridad Recomendada

Orden sugerido para avanzar sin romper el trabajo existente:

1. Fase 0 - Alineacion tecnica.
2. Fase 1 - Auth completo.
3. Fase 2 - Items/catalogo/galeria.
4. Fase 3 - Propuestas.
5. Fase 4 - Chat/WebSockets.
6. Fase 5 - Notificaciones.
7. Fase 6 - Shipments.
8. Fase 7 - Ratings.
9. Fase 8 - Reportes/admin.
10. Fase 9 y 10 - Cuenta, polish y QA.

## Checklist De Compatibilidad Backend

- [ ] `VITE_API_URL` apunta a `http://localhost:8080`.
- [ ] Login usa `username` y `password`.
- [ ] Auth guarda `token` y `refreshToken`.
- [ ] Registro muestra flujo de verificacion.
- [ ] Existe `/verify-email`.
- [ ] Existe `/forgot-password`.
- [ ] Existe `/reset-password`.
- [ ] Items soportan 4 imagenes.
- [ ] Catalogo usa filtros/paginacion backend.
- [ ] Favoritos integrados.
- [ ] Propuestas enviadas/recibidas integradas.
- [ ] Chat integrado con historial REST y tiempo real WS.
- [ ] Notificaciones REST/WS integradas.
- [ ] Shipment integrado.
- [ ] Rating/reputacion integrado.
- [ ] Reportes integrados.
- [ ] Admin panel actualizado.
- [ ] Textos sin encoding roto.
- [ ] Build pasa sin errores.
