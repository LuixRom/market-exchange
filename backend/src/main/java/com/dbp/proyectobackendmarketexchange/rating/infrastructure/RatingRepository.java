package com.dbp.proyectobackendmarketexchange.rating.infrastructure;

import com.dbp.proyectobackendmarketexchange.rating.domain.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RatingRepository extends JpaRepository<Rating, Long> {

    List<Rating> findByReviewedUserId(Long reviewedUserId);

    boolean existsByTradeProposalIdAndReviewerId(Long tradeProposalId, Long reviewerId);

    long countByReviewedUserId(Long reviewedUserId);

    @Query("select avg(r.score) from Rating r where r.reviewedUser.id = :userId")
    Optional<Double> findAverageScoreByReviewedUserId(@Param("userId") Long userId);
}
