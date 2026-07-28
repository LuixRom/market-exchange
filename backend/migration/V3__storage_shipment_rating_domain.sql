-- Fase 3: abstracción de storage para Item, Shipment con máquina de estados real, y
-- Rating ligado a un TradeProposal COMPLETED.
--
-- IMPORTANTE: mismo procedimiento que V2 -ejecutar a mano ANTES de desplegar el código
-- nuevo, con spring.jpa.hibernate.ddl-auto=none-, con una diferencia respecto a V2:
-- ddl-auto queda en "validate" de forma PERMANENTE de aquí en adelante (nunca más se
-- vuelve a "update"). Todo cambio de esquema futuro pasa por un script explícito como
-- este.
--
-- Orden recomendado de despliegue:
--   1. spring.jpa.hibernate.ddl-auto=none
--   2. Correr los preflights de la sección "Paso 0" y revisar la salida (en especial
--      cuántas filas de "rating" existen hoy — se archivan y la tabla se vacía, ver
--      sección 3 de este script).
--   3. Correr el bloque BEGIN...COMMIT completo (todo o nada).
--   4. Desplegar el código nuevo.
--   5. Arrancar con spring.jpa.hibernate.ddl-auto=validate y dejarlo así permanentemente.
--
-- Nota sobre las 11 imágenes reales existentes (item_1...item_13.jpg): este script NO
-- las mueve ni las toca. Siguen en
-- backend/src/main/java/com/dbp/proyectobackendmarketexchange/imagenes/ hasta que se
-- copien a mano a app.storage.local.base-directory (por defecto "uploads/", ver
-- application.properties) — instrucciones exactas al final de este archivo.

-- ============================================================================
-- Paso 0: preflight (ejecutar ANTES del bloque BEGIN...COMMIT, sobre el esquema
-- todavía viejo).
-- ============================================================================

-- Estructura actual, para comparar contra lo que este script asume:
--   \d item
--   \d shipment
--   \d rating

-- Preflight A: cuántas filas de "rating" existen hoy. Se archivan en rating_legacy_backup
-- y la tabla en vivo se vacía (decisión confirmada: no se dejó trade_proposal_id
-- nullable por compatibilidad histórica, se prefirió un esquema limpio NOT NULL desde
-- el inicio). Si este número no es 0, confirmá que archivar-y-vaciar sigue siendo lo
-- que querés antes de continuar.
SELECT COUNT(*) AS ratings_a_archivar FROM rating;

-- Preflight B: confirma el supuesto de que nada referencia rating.id como FK (por eso
-- TRUNCATE rating es seguro). Debe devolver 0 filas.
SELECT tc.table_name, kcu.column_name
FROM information_schema.table_constraints tc
JOIN information_schema.key_column_usage kcu ON tc.constraint_name = kcu.constraint_name
JOIN information_schema.constraint_column_usage ccu ON tc.constraint_name = ccu.constraint_name
WHERE tc.constraint_type = 'FOREIGN KEY' AND ccu.table_name = 'rating';

-- Preflight C: filas de shipment existentes que necesitarán backfill de created_at.
SELECT COUNT(*) AS shipments_a_backfillear FROM shipment WHERE TRUE;

BEGIN;

-- ============================================================================
-- 1. Item: nueva representación de la imagen (imageKey/imageProvider en vez de
--    imagePath). El VALOR de las 11 filas existentes no cambia -siguen siendo el mismo
--    nombre de archivo pelado que ya tenían-, solo cambian los NOMBRES de columna.
-- ============================================================================
ALTER TABLE item RENAME COLUMN image_path TO image_key;
ALTER TABLE item ADD COLUMN IF NOT EXISTS image_provider VARCHAR(20);

-- Backfill: toda fila que ya tenga una imagen se marca como proveída por "local" (el
-- único proveedor que existía antes de esta fase).
UPDATE item SET image_provider = 'LOCAL' WHERE image_key IS NOT NULL AND image_provider IS NULL;

-- Consistencia: o tiene ambas (key+provider) o ninguna.
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conrelid = 'item'::regclass AND conname = 'chk_item_image_key_provider'
    ) THEN
        ALTER TABLE item ADD CONSTRAINT chk_item_image_key_provider
            CHECK ((image_key IS NULL) = (image_provider IS NULL));
    END IF;
END $$;

-- ============================================================================
-- 2. Shipment: auditoría temporal completa (createdAt/updatedAt/preparedAt/shippedAt/
--    deliveredAt/cancelledAt) y trackingCode único cuando tiene valor. Shipment era el
--    único dominio sin ningún timestamp hasta ahora.
-- ============================================================================
ALTER TABLE shipment ADD COLUMN IF NOT EXISTS created_at TIMESTAMP;
UPDATE shipment SET created_at = NOW() WHERE created_at IS NULL;
ALTER TABLE shipment ALTER COLUMN created_at SET NOT NULL;

ALTER TABLE shipment ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP;
ALTER TABLE shipment ADD COLUMN IF NOT EXISTS prepared_at TIMESTAMP;
ALTER TABLE shipment ADD COLUMN IF NOT EXISTS shipped_at TIMESTAMP;
ALTER TABLE shipment ADD COLUMN IF NOT EXISTS delivered_at TIMESTAMP;
ALTER TABLE shipment ADD COLUMN IF NOT EXISTS cancelled_at TIMESTAMP;
ALTER TABLE shipment ADD COLUMN IF NOT EXISTS tracking_code VARCHAR(100);

-- Único solo cuando tiene valor (dos envíos sin tracking_code todavía no chocan entre sí).
CREATE UNIQUE INDEX IF NOT EXISTS uq_shipment_tracking_code
    ON shipment (tracking_code)
    WHERE tracking_code IS NOT NULL;

-- ============================================================================
-- 3. Rating: se archivan las filas existentes (si las hay) y se reconstruye la tabla
--    ligada a TradeProposal, con trade_proposal_id NOT NULL desde el inicio -decisión
--    confirmada: se prefirió un esquema limpio a dejar una columna nullable "legacy"-.
-- ============================================================================

-- Respaldo: si ya existe (script re-ejecutado tras un COMMIT exitoso previo), ni se
-- recrea ni se re-ejecuta el SELECT -esto es una guarda de idempotencia ENTRE
-- ejecuciones separadas del script, no una protección de reintento a mitad de una
-- transacción fallida (eso ya lo cubre el BEGIN...COMMIT atómico de todo el bloque).
CREATE TABLE IF NOT EXISTS rating_legacy_backup AS SELECT * FROM rating;

-- Confirmado (preflight B): nada referencia rating.id como FK — las únicas relaciones
-- son las columnas salientes rater_usuario_id/usuario_id hacia usuario, no al revés.
TRUNCATE rating RESTART IDENTITY;

ALTER TABLE rating RENAME COLUMN rating TO score;
ALTER TABLE rating RENAME COLUMN usuario_id TO reviewed_user_id;
ALTER TABLE rating RENAME COLUMN rater_usuario_id TO reviewer_id;

ALTER TABLE rating ADD COLUMN IF NOT EXISTS trade_proposal_id BIGINT;
ALTER TABLE rating ADD CONSTRAINT fk_rating_trade_proposal
    FOREIGN KEY (trade_proposal_id) REFERENCES trade_proposal(id);
ALTER TABLE rating ALTER COLUMN trade_proposal_id SET NOT NULL;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conrelid = 'rating'::regclass AND conname = 'chk_rating_score_range'
    ) THEN
        ALTER TABLE rating ADD CONSTRAINT chk_rating_score_range CHECK (score BETWEEN 1 AND 5);
    END IF;
END $$;

CREATE UNIQUE INDEX IF NOT EXISTS uq_rating_trade_proposal_reviewer
    ON rating (trade_proposal_id, reviewer_id);

COMMIT;

-- ============================================================================
-- Paso operativo manual (NO lo hace este script): copiar las 11 imágenes existentes
-- ============================================================================
-- Archivos a copiar (sin renombrar, sin modificar el binario):
--   item_10_1732498641528.jpg, item_13_1732455093600.jpg, item_1_1732488795069.jpg,
--   item_2_1732488893298.jpg, item_3_1732491849230.jpg, item_4_1732491886637.jpg,
--   item_5_1732492944196.jpg, item_6_1732495021156.jpg, item_7_1732497559226.jpg,
--   item_8_1732497667621.jpg, item_9_1732498580866.jpg
-- Desde: backend/src/main/java/com/dbp/proyectobackendmarketexchange/imagenes/
-- Hacia: backend/uploads/  (o la ruta absoluta configurada en
--        app.storage.local.base-directory) — mismos nombres de archivo.
-- Cuándo: después de correr esta migración y desplegar el código de esta fase, pero
--         antes de servir tráfico real. Hasta que se copien, GET /item/{id}/image
--         devuelve 404 solo para estos 11 items puntuales (los items nuevos no se ven
--         afectados). La carpeta vieja se puede borrar en una fase posterior, una vez
--         confirmada la copia.
