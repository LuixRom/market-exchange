package com.dbp.proyectobackendmarketexchange.auth.infrastructure;

import com.dbp.proyectobackendmarketexchange.auth.domain.AccountToken;
import com.dbp.proyectobackendmarketexchange.auth.domain.AccountTokenType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountTokenRepository extends JpaRepository<AccountToken, Long> {
    Optional<AccountToken> findByTokenAndType(String token, AccountTokenType type);
}
