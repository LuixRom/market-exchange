package com.dbp.proyectobackendmarketexchange.tradeproposal;

import com.dbp.proyectobackendmarketexchange.AbstractContainerBaseTest;
import com.dbp.proyectobackendmarketexchange.category.domain.Category;
import com.dbp.proyectobackendmarketexchange.item.domain.Condition;
import com.dbp.proyectobackendmarketexchange.item.domain.Item;
import com.dbp.proyectobackendmarketexchange.item.domain.ItemStatus;
import com.dbp.proyectobackendmarketexchange.tradeproposal.domain.TradeProposal;
import com.dbp.proyectobackendmarketexchange.tradeproposal.domain.TradeStatus;
import com.dbp.proyectobackendmarketexchange.tradeproposal.infrastructure.TradeProposalRepository;
import com.dbp.proyectobackendmarketexchange.usuario.domain.Role;
import com.dbp.proyectobackendmarketexchange.usuario.domain.Usuario;
import jakarta.transaction.Transactional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class TradeProposalRepositoryTest extends AbstractContainerBaseTest {

    @Autowired
    private TradeProposalRepository tradeProposalRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Item offeredItem1;
    private Item offeredItem2;
    private Item requestedItem1;
    private Item requestedItem2;
    private Usuario proposer;
    private Usuario receiver;

    @BeforeEach
    public void setUp() {
        Category category = new Category();
        category.setName("Categoria de prueba");
        entityManager.persist(category);

        proposer = new Usuario();
        proposer.setFirstname("Proponente");
        proposer.setLastname("Test");
        proposer.setEmail("proponente@example.com");
        proposer.setPhone("123456789");
        proposer.setPassword("password123");
        proposer.setAddress("Direccion proponente");
        proposer.setRole(Role.USER);
        entityManager.persist(proposer);

        receiver = new Usuario();
        receiver.setFirstname("Receptor");
        receiver.setLastname("Test");
        receiver.setEmail("receptor@example.com");
        receiver.setPhone("987654321");
        receiver.setPassword("password123");
        receiver.setAddress("Direccion receptor");
        receiver.setRole(Role.USER);
        entityManager.persist(receiver);

        offeredItem1 = buildItem("Item Ofrecido 1", category, Condition.NEW, proposer);
        offeredItem2 = buildItem("Item Ofrecido 2", category, Condition.NEW, proposer);
        requestedItem1 = buildItem("Item Solicitado 1", category, Condition.USED, receiver);
        requestedItem2 = buildItem("Item Solicitado 2", category, Condition.USED, receiver);
    }

    private Item buildItem(String name, Category category, Condition condition, Usuario owner) {
        Item item = new Item();
        item.setName(name);
        item.setDescription("Descripción de " + name);
        item.setCategory(category);
        item.setCondition(condition);
        item.setStatus(ItemStatus.APPROVED);
        item.setUsuario(owner);
        entityManager.persist(item);
        return item;
    }

    private TradeProposal buildProposal(Item offered, Item requested, TradeStatus status) {
        TradeProposal proposal = new TradeProposal();
        proposal.setOfferedItem(offered);
        proposal.setRequestedItem(requested);
        proposal.setProposer(proposer);
        proposal.setReceiver(receiver);
        proposal.setStatus(status);
        proposal.setCreatedAt(LocalDateTime.now());
        return proposal;
    }

    @Test
    @Transactional
    public void testCreateTradeProposal() {
        TradeProposal proposal = buildProposal(offeredItem1, requestedItem1, TradeStatus.PENDING);

        proposal = tradeProposalRepository.save(proposal);

        assertNotNull(proposal.getId());
        assertEquals(TradeStatus.PENDING, proposal.getStatus());
    }

    @Test
    @Transactional
    public void testFindById() {
        TradeProposal proposal = buildProposal(offeredItem1, requestedItem1, TradeStatus.PENDING);
        entityManager.persist(proposal);

        Optional<TradeProposal> found = tradeProposalRepository.findById(proposal.getId());

        assertTrue(found.isPresent());
        assertEquals(proposal.getId(), found.get().getId());
    }

    @Test
    @Transactional
    public void testDeleteTradeProposal() {
        TradeProposal proposal = buildProposal(offeredItem1, requestedItem1, TradeStatus.PENDING);
        proposal = tradeProposalRepository.save(proposal);
        Long id = proposal.getId();

        tradeProposalRepository.deleteById(id);

        assertFalse(tradeProposalRepository.findById(id).isPresent());
    }

    @Test
    @Transactional
    public void testUpdateStatusToAccepted() {
        TradeProposal proposal = buildProposal(offeredItem1, requestedItem1, TradeStatus.PENDING);
        proposal = tradeProposalRepository.save(proposal);
        Long id = proposal.getId();

        proposal.setStatus(TradeStatus.ACCEPTED);
        tradeProposalRepository.save(proposal);

        Optional<TradeProposal> updated = tradeProposalRepository.findById(id);
        assertTrue(updated.isPresent());
        assertEquals(TradeStatus.ACCEPTED, updated.get().getStatus());
    }

    @Test
    @Transactional
    public void testFindByStatus() {
        TradeProposal pending = buildProposal(offeredItem1, requestedItem1, TradeStatus.PENDING);
        entityManager.persist(pending);

        TradeProposal accepted = buildProposal(offeredItem2, requestedItem2, TradeStatus.ACCEPTED);
        entityManager.persist(accepted);

        List<TradeProposal> pendingList = tradeProposalRepository.findByStatus(TradeStatus.PENDING);
        assertEquals(1, pendingList.size());
        assertEquals(TradeStatus.PENDING, pendingList.get(0).getStatus());

        List<TradeProposal> acceptedList = tradeProposalRepository.findByStatus(TradeStatus.ACCEPTED);
        assertEquals(1, acceptedList.size());
        assertEquals(TradeStatus.ACCEPTED, acceptedList.get(0).getStatus());
    }

    @Test
    @Transactional
    public void testExistsByOfferedItemIdAndRequestedItemIdAndStatus() {
        TradeProposal proposal = buildProposal(offeredItem1, requestedItem1, TradeStatus.PENDING);
        entityManager.persist(proposal);

        assertTrue(tradeProposalRepository.existsByOfferedItemIdAndRequestedItemIdAndStatus(
                offeredItem1.getId(), requestedItem1.getId(), TradeStatus.PENDING));
        assertFalse(tradeProposalRepository.existsByOfferedItemIdAndRequestedItemIdAndStatus(
                offeredItem2.getId(), requestedItem2.getId(), TradeStatus.PENDING));
    }

    @Test
    @Transactional
    public void testCancelCompetingProposals() {
        // Dos propuestas PENDING compitiendo por el mismo requestedItem1
        TradeProposal winner = buildProposal(offeredItem1, requestedItem1, TradeStatus.PENDING);
        entityManager.persist(winner);

        TradeProposal competitor = buildProposal(offeredItem2, requestedItem1, TradeStatus.PENDING);
        entityManager.persist(competitor);
        entityManager.flush();

        int updated = tradeProposalRepository.cancelCompetingProposals(
                List.of(offeredItem1.getId(), requestedItem1.getId()), winner.getId());

        assertEquals(1, updated);

        entityManager.clear();
        Optional<TradeProposal> competitorReloaded = tradeProposalRepository.findById(competitor.getId());
        assertTrue(competitorReloaded.isPresent());
        assertEquals(TradeStatus.CANCELLED, competitorReloaded.get().getStatus());

        Optional<TradeProposal> winnerReloaded = tradeProposalRepository.findById(winner.getId());
        assertTrue(winnerReloaded.isPresent());
        assertEquals(TradeStatus.PENDING, winnerReloaded.get().getStatus());
    }
}
