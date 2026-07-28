package com.dbp.proyectobackendmarketexchange.tradeproposal.infrastructure;

import com.dbp.proyectobackendmarketexchange.tradeproposal.domain.TradeStatus;

/**
 * Proyección liviana (solo escalares) usada antes de tomar los locks pesimistas en
 * acceptTradeProposal/rejectTradeProposal. A propósito NO carga la entidad completa:
 * si cargáramos el TradeProposal/Item completos aquí, quedarían gestionados en el
 * contexto de persistencia y la relectura posterior bajo lock (findByIdForUpdate)
 * devolvería esa MISMA instancia cacheada (identity map de JPA) en vez de reflejar
 * el valor recién confirmado por la transacción que ganó la carrera -el SQL sí
 * esperaría el lock correctamente, pero el objeto Java seguiría con el estado viejo-.
 */
public interface TradeProposalSummary {
    Long getOfferedItemId();
    Long getRequestedItemId();
    Long getReceiverId();
    TradeStatus getStatus();
}
