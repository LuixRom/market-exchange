# Roadmap Backend - MarketExchange

Este roadmap ordena el camino desde el backend actual hacia una version real de producto para una plataforma de intercambio de objetos.

## Objetivo

Construir un backend estable, seguro y documentado que permita:

- Registrar y autenticar usuarios.
- Publicar objetos para intercambio.
- Aprobar y moderar publicaciones.
- Proponer, aceptar, rechazar y completar intercambios.
- Gestionar entregas/envios.
- Calificar usuarios despues de intercambios completados.
- Dar soporte a un frontend de producto real.

## Fase 1: Base Tecnica Solida

- [x] Crear `docker-compose.yml` para PostgreSQL.
- [x] Separar perfiles de Spring: `local`, `dev`, `prod`, `test`.
- [x] Crear `.env.example` con las variables necesarias.
- [x] Evitar secretos directos en `application.properties`.
- [x] Agregar Swagger/OpenAPI para documentar endpoints.
- [x] Documentar auth, items, agreements, shipments y ratings.
- [x] Crear seed inicial para usuario admin.
- [x] Crear seed inicial para categorias base.
- [x] Integrar migraciones reales con Flyway o Liquibase.
- [x] Evitar depender de `DDL_AUTO=update` para produccion.

## Fase 2: Seguridad y Cuentas

- [x] Centralizar validaciones de registro con annotations.
- [x] Normalizar emails antes de guardar y autenticar.
- [x] Evitar duplicados de email por mayusculas/minusculas.
- [x] Implementar verificacion de email.
- [x] Bloquear acciones sensibles hasta confirmar email.
- [x] Implementar recuperacion de contrasena.
- [x] Implementar refresh tokens.
- [x] Implementar logout real con revocacion de refresh token.
- [x] Ampliar perfil publico de usuario.
- [x] Agregar avatar/foto de perfil.
- [x] Agregar bio corta y ubicacion aproximada.

## Fase 3: Marketplace Real

- [x] Agregar paginacion en listados de items.
- [x] Agregar filtros por categoria, condicion, estado, texto y usuario.
- [x] Agregar ordenamiento por fecha o relevancia.
- [x] Mostrar publicamente solo items `APPROVED`.
- [x] Ocultar items `RESERVED`, `EXCHANGED` y `REJECTED` para usuarios comunes.
- [x] Permitir que admin vea todos los estados.
- [x] Soportar varias imagenes por item.
- [x] Definir imagen principal por item.
- [ ] Validar tamano y tipo de imagen.
- [x] Limpiar imagenes huerfanas.
- [x] Implementar favoritos o wishlist.

## Fase 4: Intercambio y Comunicacion

- [x] Permitir mensaje inicial en propuestas de intercambio.
- [x] Permitir cancelar una propuesta propia.
- [x] Crear endpoint de propuestas enviadas por el usuario.
- [x] Crear endpoint de propuestas recibidas por el usuario.
- [x] Revisar y documentar estados de propuesta: `PENDING`, `ACCEPTED`, `REJECTED`, `CANCELLED`, `COMPLETED`.
- [x] Implementar chat ligado a una propuesta.
- [x] Marcar mensajes como leidos.
- [x] Implementar notificaciones internas en base de datos.
- [x] Implementar WebSockets para chat, notificaciones y cambios de propuestas.
- [x] Notificar propuesta recibida.
- [x] Notificar propuesta aceptada o rechazada.
- [x] Notificar actualizaciones de envio.
- [x] Notificar rating recibido.
- [x] Revisar flujo de envio presencial vs envio externo.
- [x] Hacer tracking opcional.
- [x] Evaluar confirmacion de entrega por ambas partes.

### Estados de Propuesta

- `PENDING`: propuesta creada, esperando respuesta del receptor.
- `ACCEPTED`: propuesta aceptada; se reserva cada item y se crea la entrega.
- `REJECTED`: propuesta rechazada por el receptor.
- `CANCELLED`: propuesta cancelada por el proponente o por administrador antes de aceptarse.
- `COMPLETED`: intercambio finalizado despues de que ambas partes confirman la entrega.

### Flujo de Entrega

- `EXTERNAL_SHIPPING`: entrega externa con tracking opcional. Flujo sugerido: `PENDING -> PREPARING -> IN_TRANSIT -> DELIVERED`. La propuesta pasa a `COMPLETED` cuando ambas partes confirman entrega.
- `IN_PERSON`: intercambio presencial sin tracking. Ambas partes pueden confirmar entrega directamente desde la entrega activa. La propuesta pasa a `COMPLETED` cuando ambas partes confirman.
- Endpoint de confirmacion: `PUT /shipments/{id}/confirm-delivery`.

## Fase 5: Confianza, Moderacion y Calidad

- [x] Implementar reportes de usuarios.
- [x] Implementar reportes de items.
- [x] Implementar reportes de intercambios.
- [x] Crear panel/endpoints admin para revisar reportes.
- [x] Agregar motivo de rechazo al moderar items.
- [x] Guardar historial de moderacion.
- [x] Ampliar rating con criterios como comunicacion, puntualidad y estado del producto.
- [x] Definir ventana de edicion para ratings.
- [x] Evitar ratings fuera del contexto de un intercambio completado.
- [x] Implementar bloqueo de usuarios.
- [x] Implementar suspension de cuentas.
- [x] Bloquear publicaciones/intercambios de usuarios suspendidos.

### Reportes y Moderacion

- Reportar contenido: `POST /reports`.
- Objetivos reportables: `USER`, `ITEM`, `TRADE_PROPOSAL`.
- Panel admin de reportes: `GET /admin/reports`, `GET /admin/reports/{id}`, `PUT /admin/reports/{id}/review`.
- Moderar item con motivo opcional: `POST /item/{itemId}/approve?approve=false&reason=...`.
- Historial de moderacion de item: `GET /item/{itemId}/moderation-history`.

### Confianza de Usuarios

- Suspender cuenta: `PUT /usuarios/{id}/suspend`.
- Quitar suspension: `PUT /usuarios/{id}/unsuspend`.
- Bloquear cuenta: `PUT /usuarios/{id}/block`.
- Desbloquear cuenta: `PUT /usuarios/{id}/unblock`.
- Usuarios suspendidos o bloqueados no pueden iniciar sesion, publicar items ni crear propuestas.

### Ratings

- Rating solo permitido en intercambios `COMPLETED`.
- Ventana para crear rating: 14 dias desde el cierre del intercambio.
- Ventana para editar rating: 48 horas desde la creacion.
- Criterios adicionales: comunicacion, puntualidad y estado del producto.

## Fase 6: Operacion y Produccion

- [x] Agregar unit tests de servicios.
- [x] Agregar integration tests de endpoints.
- [x] Agregar tests de seguridad.
- [ ] Agregar tests del flujo completo: registro -> item -> proposal -> shipment -> rating.
- [x] Limpiar y estandarizar logs.
- [x] Agregar Spring Boot Actuator.
- [x] Agregar health checks.
- [x] Agregar metricas basicas.
- [x] Revisar manejo global de errores.
- [ ] Optimizar consultas y evitar N+1.
- [x] Agregar indices en base de datos.
- [ ] Usar DTO projections donde convenga.
- [x] Preparar Dockerfile final.
- [x] Preparar Docker Compose para dev/prod.
- [x] Configurar variables por entorno.
- [ ] Agregar CI/CD basico.

### Operacion Implementada

- Health publico: `GET /actuator/health`.
- Info publico: `GET /actuator/info`.
- Metricas protegidas para admin: `/actuator/metrics` y `/actuator/prometheus`.
- Dockerfile multi-stage para construir y ejecutar el backend con Java 17.
- Docker Compose ampliado para levantar Postgres + backend.
- Volumen persistente para uploads locales.
- Logs sin `System.out.println` en runtime principal.

### Auditoria de Tests

- Comando ejecutado: `./mvnw clean test`.
- Resultado actual: suite verde.
- Tests detectados: 234.
- Resultado local: 221 ejecutados, 13 saltados.
- Los 13 saltados son tests con Testcontainers cuando Docker no esta disponible; si Docker esta levantado, JUnit intenta correrlos.
- Tests modernizados para auth con verificacion de email, galeria de imagenes, notificaciones, ratings ampliados y confirmacion doble de entrega.

## Orden Recomendado de Trabajo

1. Docker Compose, perfiles y `.env.example`.
2. Swagger/OpenAPI.
3. Seed admin y categorias.
4. Migraciones con Flyway o Liquibase.
5. Paginacion y filtros de items.
6. Mejorar propuestas: enviadas, recibidas, cancelar y mensaje inicial.
7. Notificaciones internas.
8. Recuperacion y verificacion de email.
9. Chat.
10. Reportes y moderacion.
11. Tests y deploy.

## WebSockets Implementados

- Endpoint STOMP/SockJS: `/ws`.
- Autenticacion: enviar el access token JWT en el header STOMP `Authorization: Bearer <token>` durante `CONNECT`. Tambien se acepta header `token`.
- Enviar mensaje de chat: publicar en `/app/agreements/{tradeProposalId}/messages` con body `{ "content": "..." }`.
- Escuchar mensajes de acuerdos: suscribirse a `/user/queue/agreement-messages`.
- Escuchar notificaciones: suscribirse a `/user/queue/notifications`.
- Escuchar cambios de propuestas: suscribirse a `/user/queue/agreement-events`.

## Definicion de v1 Backend Estable

La primera version realista del backend debe incluir:

- [ ] Auth solido.
- [ ] Items publicables y buscables.
- [ ] Propuestas de intercambio completas.
- [ ] Envios manejables.
- [ ] Ratings funcionales.
- [ ] Admin basico.
- [ ] Swagger/OpenAPI.
- [ ] Docker Compose.
- [ ] Seed inicial.
- [ ] Tests principales.
