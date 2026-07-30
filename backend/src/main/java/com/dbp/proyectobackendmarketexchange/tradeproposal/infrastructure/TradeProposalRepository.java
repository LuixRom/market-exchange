package com.dbp.proyectobackendmarketexchange.tradeproposal.infrastructure;

import com.dbp.proyectobackendmarketexchange.tradeproposal.domain.TradeProposal;
import com.dbp.proyectobackendmarketexchange.tradeproposal.domain.TradeStatus;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TradeProposalRepository extends JpaRepository<TradeProposal, Long> {

    List<TradeProposal> findByStatus(TradeStatus status);
    List<TradeProposal> findByProposerIdOrderByCreatedAtDesc(Long proposerId);
    List<TradeProposal> findByReceiverIdOrderByCreatedAtDesc(Long receiverId);

    boolean existsByOfferedItemIdAndRequestedItemIdAndStatus(Long offeredItemId, Long requestedItemId, TradeStatus status);

    @Query("select t.offeredItem.id as offeredItemId, t.requestedItem.id as requestedItemId, " +
            "t.receiver.id as receiverId, t.status as status from TradeProposal t where t.id = :id")
    Optional<TradeProposalSummary> findSummaryById(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "5000")})
    @Query("select t from TradeProposal t where t.id = :id")
    Optional<TradeProposal> findByIdForUpdate(@Param("id") Long id);

    @Modifying(flushAutomatically = true)
    @Query("update TradeProposal t set t.status = com.dbp.proyectobackendmarketexchange.tradeproposal.domain.TradeStatus.CANCELLED, " +
            "t.updatedAt = CURRENT_TIMESTAMP " +
            "where t.status = com.dbp.proyectobackendmarketexchange.tradeproposal.domain.TradeStatus.PENDING " +
            "and t.id <> :excludeId " +
            "and (t.offeredItem.id in :itemIds or t.requestedItem.id in :itemIds)")
    int cancelCompetingProposals(@Param("itemIds") List<Long> itemIds, @Param("excludeId") Long excludeId);
}
