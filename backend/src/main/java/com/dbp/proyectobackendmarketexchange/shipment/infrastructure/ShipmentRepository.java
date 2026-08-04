package com.dbp.proyectobackendmarketexchange.shipment.infrastructure;

import com.dbp.proyectobackendmarketexchange.shipment.domain.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShipmentRepository extends JpaRepository<Shipment, Long> {
    boolean existsByTradeProposalId(Long tradeProposalId);
    Optional<Shipment> findByTradeProposalId(Long tradeProposalId);
}
