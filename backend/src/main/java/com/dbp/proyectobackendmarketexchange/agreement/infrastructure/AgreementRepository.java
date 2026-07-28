package com.dbp.proyectobackendmarketexchange.agreement.infrastructure;


import com.dbp.proyectobackendmarketexchange.agreement.domain.Agreement;
import com.dbp.proyectobackendmarketexchange.agreement.domain.State;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AgreementRepository extends JpaRepository<Agreement, Long> {

    List<Agreement> findByState(State state);
}