-- Fase 2: rediseño del dominio de intercambios (Agreement -> TradeProposal)
--
-- IMPORTANTE: ejecutar este script a mano ANTES de desplegar el código nuevo, con
-- spring.jpa.hibernate.ddl-auto=none (NO update: update nunca renombra, solo agrega,
-- así que si el código nuevo arranca contra el esquema viejo -o al revés- Hibernate
-- bifurca el esquema creando una tabla/columnas nuevas vacías en paralelo).
--
-- Orden recomendado de despliegue:
--   1. spring.jpa.hibernate.ddl-auto=none
--   2. Correr los preflights de la sección "Paso 0" y revisar que no devuelvan filas
--      (si devuelven filas, hay datos que romperían los UNIQUE de más abajo: resuélvelos
--      a mano antes de continuar). Verificar también los nombres reales de
--      columnas/constraints y ajustar este script si difieren de los asumidos.
--   3. Correr el bloque BEGIN...COMMIT completo (todo o nada).
--   4. Desplegar el código nuevo (entidades TradeProposal/ItemStatus/ShipmentStatus).
--   5. Arrancar con spring.jpa.hibernate.ddl-auto=validate y DEJARLO ASÍ de forma
--      permanente (no volver a "update"): de aquí en adelante, todo cambio de esquema
--      pasa por un script de migración explícito, revisado antes de aplicarse -no por
--      el auto-DDL de Hibernate-. Esto es intencional y prepara el terreno para
--      adoptar Flyway/Liquibase más adelante.

-- ============================================================================
-- Paso 0: preflight (ejecutar ANTES del bloque BEGIN...COMMIT, sobre el esquema
-- todavía viejo). Si cualquiera de las consultas de abajo devuelve filas, hay que
-- resolverlas a mano (cancelar/editar duplicados) antes de continuar: los UNIQUE que
-- este script agrega más abajo fallarían con esos datos presentes.
-- ============================================================================

-- Estructura y constraints actuales, para comparar contra lo que este script asume:
--   \d agreement
--   \d item
--   \d shipment
--   SELECT conname FROM pg_constraint WHERE conrelid = 'agreement'::regclass AND contype = 'u';

-- Preflight A: propuestas PENDING duplicadas por (item_ini_id, item_fin_id).
-- Si esto devuelve filas, CREATE UNIQUE INDEX uq_trade_proposal_active_pair (paso 3)
-- fallará.
SELECT item_ini_id, item_fin_id, COUNT(*) AS duplicados
FROM agreement
WHERE state = 'PENDING'
GROUP BY item_ini_id, item_fin_id
HAVING COUNT(*) > 1;

-- Preflight B: más de un shipment asociado al mismo agreement.
-- Si esto devuelve filas, el UNIQUE sobre shipment.trade_proposal_id (paso 4b)
-- fallará.
SELECT agreement_id, COUNT(*) AS duplicados
FROM shipment
GROUP BY agreement_id
HAVING COUNT(*) > 1;

-- ============================================================================
-- Migración (atómica: todo o nada)
-- ============================================================================

BEGIN;

-- 1. Rename de tabla y columnas del dominio de intercambios.
--    Los nombres asumidos (item_ini_id, item_fin_id, initiator_id, recipient_id, state)
--    son los que genera Hibernate por convención para @OneToOne/@ManyToOne sin
--    @JoinColumn explícito sobre campos ya llamados item_ini/item_fin/initiator/recipient/state.
ALTER TABLE agreement RENAME TO trade_proposal;
ALTER TABLE trade_proposal RENAME COLUMN item_ini_id TO offered_item_id;
ALTER TABLE trade_proposal RENAME COLUMN item_fin_id TO requested_item_id;
ALTER TABLE trade_proposal RENAME COLUMN initiator_id TO proposer_id;
ALTER TABLE trade_proposal RENAME COLUMN recipient_id TO receiver_id;
ALTER TABLE trade_proposal RENAME COLUMN state TO status;

-- 2. OneToOne -> ManyToOne: soltar los UNIQUE que Hibernate generó para el OneToOne
--    original (varias propuestas deben poder apuntar al mismo item ahora).
--    Solo se eliminan constraints UNIQUE DE UNA SOLA COLUMNA sobre offered_item_id o
--    requested_item_id -es decir, exactamente el artefacto que dejó el @OneToOne
--    viejo-; cualquier UNIQUE compuesto legítimo (offered_item_id + otra columna,
--    por ejemplo) NO se toca porque array_length(conkey, 1) = 1 lo excluye.
DO $$
DECLARE
    r RECORD;
BEGIN
    FOR r IN
        SELECT c.conname
        FROM pg_constraint c
        JOIN pg_attribute a
          ON a.attrelid = c.conrelid AND a.attnum = c.conkey[1]
        WHERE c.conrelid = 'trade_proposal'::regclass
          AND c.contype = 'u'
          AND array_length(c.conkey, 1) = 1
          AND a.attname IN ('offered_item_id', 'requested_item_id')
    LOOP
        EXECUTE format('ALTER TABLE trade_proposal DROP CONSTRAINT %I', r.conname);
    END LOOP;
END $$;

-- 3. Cierra a nivel de base de datos la misma carrera que "propuesta activa duplicada"
--    intenta evitar a nivel de aplicación: dos inserts concurrentes con el mismo par
--    (offeredItem, requestedItem) en PENDING. Backstop real, no solo el chequeo en el
--    servicio. Ya idempotente vía IF NOT EXISTS.
CREATE UNIQUE INDEX IF NOT EXISTS uq_trade_proposal_active_pair
    ON trade_proposal (offered_item_id, requested_item_id)
    WHERE status = 'PENDING';

-- 4a. Shipment: rename de la FK (agreement -> trade_proposal) y nueva columna de estado
--     (Shipment no tenía ningún campo de estado hasta ahora).
ALTER TABLE shipment RENAME COLUMN agreement_id TO trade_proposal_id;
ALTER TABLE shipment ADD COLUMN IF NOT EXISTS status VARCHAR(20) NOT NULL DEFAULT 'PENDING';

-- 4b. Garantiza a nivel de base de datos "exactamente un Shipment por TradeProposal"
--     -hoy solo lo garantiza el mapeo @OneToOne(mappedBy=...) del lado de la entidad,
--     que no es una restricción real de esquema-. Idempotente: si la constraint ya
--     existe (misma semántica: UNIQUE de una sola columna sobre trade_proposal_id),
--     no se vuelve a crear. Si el preflight B encontró duplicados, este paso falla y
--     aborta toda la transacción -hay que resolver los duplicados primero-.
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint c
        JOIN pg_attribute a
          ON a.attrelid = c.conrelid AND a.attnum = c.conkey[1]
        WHERE c.conrelid = 'shipment'::regclass
          AND c.contype = 'u'
          AND array_length(c.conkey, 1) = 1
          AND a.attname = 'trade_proposal_id'
    ) THEN
        ALTER TABLE shipment
            ADD CONSTRAINT uq_shipment_trade_proposal_id UNIQUE (trade_proposal_id);
    END IF;
END $$;

-- 5. Item: PENDING_REVIEW reemplaza a PENDING como valor inicial del ciclo de vida.
--    Hay que reescribir las filas existentes ANTES de desplegar el enum nuevo: el
--    ItemStatus de la Fase 2 ya no tiene la constante PENDING, así que Hibernate
--    fallaría al deserializar cualquier fila que todavía diga 'PENDING'.
UPDATE item SET status = 'PENDING_REVIEW' WHERE status = 'PENDING';

COMMIT;
