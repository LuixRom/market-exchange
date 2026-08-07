package com.dbp.proyectobackendmarketexchange.raiting;

import com.dbp.proyectobackendmarketexchange.AbstractContainerBaseTest;
import com.dbp.proyectobackendmarketexchange.category.domain.Category;
import com.dbp.proyectobackendmarketexchange.item.domain.Condition;
import com.dbp.proyectobackendmarketexchange.item.domain.Item;
import com.dbp.proyectobackendmarketexchange.item.domain.ItemStatus;
import com.dbp.proyectobackendmarketexchange.rating.domain.Rating;
import com.dbp.proyectobackendmarketexchange.rating.infrastructure.RatingRepository;
import com.dbp.proyectobackendmarketexchange.tradeproposal.domain.TradeProposal;
import com.dbp.proyectobackendmarketexchange.tradeproposal.domain.TradeStatus;
import com.dbp.proyectobackendmarketexchange.usuario.domain.Role;
import com.dbp.proyectobackendmarketexchange.usuario.domain.Usuario;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolationException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class RatingRepositoryTest extends AbstractContainerBaseTest {

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Usuario proposer;
    private Usuario receiver;
    private TradeProposal tradeProposal;

    @BeforeEach
    void setUp() {
        Category category = new Category();
        category.setName("Categoria de prueba");
        entityManager.persist(category);

        proposer = buildUsuario("proposer@example.com");
        entityManager.persist(proposer);

        receiver = buildUsuario("receiver@example.com");
        entityManager.persist(receiver);

        Item offeredItem = buildItem("Ofrecido", category, proposer);
        Item requestedItem = buildItem("Solicitado", category, receiver);
        entityManager.persist(offeredItem);
        entityManager.persist(requestedItem);

        tradeProposal = new TradeProposal();
        tradeProposal.setOfferedItem(offeredItem);
        tradeProposal.setRequestedItem(requestedItem);
        tradeProposal.setProposer(proposer);
        tradeProposal.setReceiver(receiver);
        tradeProposal.setStatus(TradeStatus.COMPLETED);
        entityManager.persist(tradeProposal);
        entityManager.flush();
    }

    private Usuario buildUsuario(String email) {
        Usuario usuario = new Usuario();
        usuario.setFirstname("Test");
        usuario.setLastname("User");
        usuario.setEmail(email);
        usuario.setPhone("123456789");
        usuario.setPassword("password123");
        usuario.setAddress("Direccion de prueba");
        usuario.setRole(Role.USER);
        return usuario;
    }

    private Item buildItem(String name, Category category, Usuario owner) {
        Item item = new Item();
        item.setName(name);
        item.setDescription("Descripción de " + name);
        item.setCategory(category);
        item.setCondition(Condition.NEW);
        item.setStatus(ItemStatus.EXCHANGED);
        item.setUsuario(owner);
        return item;
    }

    private Rating buildRating(Usuario reviewer, Usuario reviewedUser, int score) {
        Rating rating = new Rating();
        rating.setTradeProposal(tradeProposal);
        rating.setReviewer(reviewer);
        rating.setReviewedUser(reviewedUser);
        rating.setScore(score);
        rating.setComment("Comentario de prueba");
        return rating;
    }

    @Test
    @Transactional
    void testCreateRating_LinkedToTradeProposal() {
        Rating rating = ratingRepository.save(buildRating(proposer, receiver, 5));

        Optional<Rating> found = ratingRepository.findById(rating.getId());
        assertTrue(found.isPresent());
        assertEquals(tradeProposal.getId(), found.get().getTradeProposal().getId());
        assertEquals(proposer.getId(), found.get().getReviewer().getId());
        assertEquals(receiver.getId(), found.get().getReviewedUser().getId());
    }

    @Test
    @Transactional
    void testBothPartiesCanRateEachOther() {
        ratingRepository.save(buildRating(proposer, receiver, 5));
        ratingRepository.save(buildRating(receiver, proposer, 4));

        assertEquals(2, ratingRepository.findAll().size());
    }

    @Test
    @Transactional
    void testDuplicateRating_ViolatesUniqueConstraint() {
        entityManager.persistAndFlush(buildRating(proposer, receiver, 5));

        // Se pasa por el repositorio (bean @Repository) y no por TestEntityManager
        // directamente, para que la traducción de excepciones de Spring convierta la
        // ConstraintViolationException nativa de Hibernate en DataIntegrityViolationException.
        Rating duplicate = buildRating(proposer, receiver, 3);
        assertThrows(DataIntegrityViolationException.class,
                () -> ratingRepository.saveAndFlush(duplicate));
    }

    @Test
    @Transactional
    void testScoreOutOfRange_RejectedByBeanValidation() {
        Rating invalid = buildRating(proposer, receiver, 6);

        assertThrows(ConstraintViolationException.class, () -> entityManager.persistAndFlush(invalid));
    }

    @Test
    @Transactional
    void testAverageScoreAndCount() {
        entityManager.persistAndFlush(buildRating(proposer, receiver, 5));

        Usuario anotherReviewer = buildUsuario("otro@example.com");
        entityManager.persist(anotherReviewer);
        Item anotherOffered = buildItem("Otro ofrecido", tradeProposal.getOfferedItem().getCategory(), anotherReviewer);
        entityManager.persist(anotherOffered);
        TradeProposal anotherTrade = new TradeProposal();
        anotherTrade.setOfferedItem(anotherOffered);
        anotherTrade.setRequestedItem(tradeProposal.getRequestedItem());
        anotherTrade.setProposer(anotherReviewer);
        anotherTrade.setReceiver(receiver);
        anotherTrade.setStatus(TradeStatus.COMPLETED);
        entityManager.persist(anotherTrade);

        Rating secondRating = new Rating();
        secondRating.setTradeProposal(anotherTrade);
        secondRating.setReviewer(anotherReviewer);
        secondRating.setReviewedUser(receiver);
        secondRating.setScore(3);
        entityManager.persistAndFlush(secondRating);

        assertEquals(2L, ratingRepository.countByReviewedUserId(receiver.getId()));
        assertEquals(4.0, ratingRepository.findAverageScoreByReviewedUserId(receiver.getId()).orElseThrow(), 0.001);
    }
}
