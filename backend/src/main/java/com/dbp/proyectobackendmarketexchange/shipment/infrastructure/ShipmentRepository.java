package com.dbp.proyectobackendmarketexchange.shipment.infrastructure;

import com.dbp.proyectobackendmarketexchange.shipment.domain.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShipmentRepository extends JpaRepository<Shipment, Long> {
}
