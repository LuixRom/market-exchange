package com.dbp.proyectobackendmarketexchange.tradeproposal;

import com.dbp.proyectobackendmarketexchange.AbstractIntegrationTest;
import com.dbp.proyectobackendmarketexchange.category.domain.Category;
import com.dbp.proyectobackendmarketexchange.category.infrastructure.CategoryRepository;
import com.dbp.proyectobackendmarketexchange.exception.TradeProposalConflictException;
import com.dbp.proyectobackendmarketexchange.item.domain.Condition;
import com.dbp.proyectobackendmarketexchange.item.domain.Item;
import com.dbp.proyectobackendmarketexchange.item.domain.ItemStatus;
import com.dbp.proyectobackendmarketexchange.item.infrastructure.ItemRepository;
import com.dbp.proyectobackendmarketexchange.shipment.infrastructure.ShipmentRepository;
import com.dbp.proyectobackendmarketexchange.tradeproposal.domain.TradeProposal;
import com.dbp.proyectobackendmarketexchange.tradeproposal.domain.TradeProposalService;
import com.dbp.proyectobackendmarketexchange.tradeproposal.domain.TradeStatus;
import com.dbp.proyectobackendmarketexchange.tradeproposal.dto.TradeProposalResponseDto;
import com.dbp.proyectobackendmarketexchange.tradeproposal.infrastructure.TradeProposalRepository;
import com.dbp.proyectobackendmarketexchange.usuario.domain.Role;
import com.dbp.proyectobackendmarketexchange.usuario.domain.Usuario;
import com.dbp.proyectobackendmarketexchange.usuario.infrastructure.UsuarioRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifica que dos accepts concurrentes sobre dos TradeProposal PENDING que compiten
 * por el mismo requestedItem se resuelven de forma segura: exactamente una queda
 * ACCEPTED y la otra CANCELLED (nunca ambas ACCEPTED, nunca ambos items reservados
 * dos veces, nunca dos shipments). Usa @SpringBootTest (no @DataJpaTest) porque hace
 * falta el contexto completo con los @Service reales, y cada hilo necesita su propia
 * transacción confirmada para que el bloqueo pesimista tenga sentido.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class TradeProposalConcurrencyTest extends AbstractIntegrationTest {

    @Autowired
    private TradeProposalService tradeProposalService;

    @Autowired
    private TradeProposalRepository tradeProposalRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ShipmentRepository shipmentRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void twoCompetingAccepts_exactlyOneWinsAndTheOtherIsCancelled() throws Exception {
        // Este test hace commits reales (no es @DataJpaTest, no hay rollback) contra el
        // mismo contenedor Postgres compartido con los tests @DataJpaTest -por eso todo
        // lo que crea se borra explícitamente al final, para no contaminar los conteos
        // de otros tests (p.ej. TradeProposalRepositoryTest#testFindByStatus).
        Category category = categoryRepository.save(buildCategory());

        Usuario proposerA = usuarioRepository.save(buildUsuario("proposerA@example.com"));
        Usuario proposerB = usuarioRepository.save(buildUsuario("proposerB@example.com"));
        Usuario receiver = usuarioRepository.save(buildUsuario("receiver@example.com"));

        Item offeredItemA = itemRepository.save(buildItem("Ofrecido A", category, proposerA));
        Item offeredItemB = itemRepository.save(buildItem("Ofrecido B", category, proposerB));
        Item requestedItem = itemRepository.save(buildItem("Solicitado", category, receiver));

        TradeProposal proposalA = tradeProposalRepository.save(
                buildProposal(offeredItemA, requestedItem, proposerA, receiver));
        TradeProposal proposalB = tradeProposalRepository.save(
                buildProposal(offeredItemB, requestedItem, proposerB, receiver));

        try {
            runConcurrentAccepts(proposalA, proposalB, receiver, offeredItemA, offeredItemB, requestedItem);
        } finally {
            shipmentRepository.findAll().stream()
                    .filter(s -> s.getTradeProposal() != null
                            && (s.getTradeProposal().getId().equals(proposalA.getId())
                                || s.getTradeProposal().getId().equals(proposalB.getId())))
                    .forEach(s -> shipmentRepository.deleteById(s.getId()));
            tradeProposalRepository.deleteById(proposalA.getId());
            tradeProposalRepository.deleteById(proposalB.getId());
            itemRepository.deleteById(offeredItemA.getId());
            itemRepository.deleteById(offeredItemB.getId());
            itemRepository.deleteById(requestedItem.getId());
            usuarioRepository.deleteById(proposerA.getId());
            usuarioRepository.deleteById(proposerB.getId());
            usuarioRepository.deleteById(receiver.getId());
            categoryRepository.deleteById(category.getId());
        }
    }

    private void runConcurrentAccepts(TradeProposal proposalA, TradeProposal proposalB, Usuario receiver,
                                       Item offeredItemA, Item offeredItemB, Item requestedItem) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch startLatch = new CountDownLatch(1);

        Callable<TradeProposalResponseDto> acceptA = acceptTask(receiver, proposalA.getId(), startLatch);
        Callable<TradeProposalResponseDto> acceptB = acceptTask(receiver, proposalB.getId(), startLatch);

        Future<TradeProposalResponseDto> futureA = executor.submit(acceptA);
        Future<TradeProposalResponseDto> futureB = executor.submit(acceptB);

        startLatch.countDown();

        Outcome outcomeA = resolve(futureA);
        Outcome outcomeB = resolve(futureB);

        executor.shutdown();
        assertTrue(executor.awaitTermination(30, TimeUnit.SECONDS));

        // Invariante: exactamente una gana (ACCEPTED) y la otra pierde con
        // TradeProposalConflictException. No importa cuál de las dos.
        assertTrue(outcomeA.succeeded ^ outcomeB.succeeded,
                "Exactamente una de las dos propuestas debe haberse aceptado, no ambas ni ninguna");

        Outcome winner = outcomeA.succeeded ? outcomeA : outcomeB;
        Outcome loser = outcomeA.succeeded ? outcomeB : outcomeA;

        assertEquals(TradeStatus.ACCEPTED, winner.response.getStatus());
        assertTrue(loser.failure instanceof TradeProposalConflictException,
                "La propuesta perdedora debe fallar con TradeProposalConflictException, no con otra excepción");

        Long winnerProposalId = outcomeA.succeeded ? proposalA.getId() : proposalB.getId();
        Long loserProposalId = outcomeA.succeeded ? proposalB.getId() : proposalA.getId();

        TradeProposal winnerReloaded = tradeProposalRepository.findById(winnerProposalId).orElseThrow();
        TradeProposal loserReloaded = tradeProposalRepository.findById(loserProposalId).orElseThrow();
        assertEquals(TradeStatus.ACCEPTED, winnerReloaded.getStatus());
        assertEquals(TradeStatus.CANCELLED, loserReloaded.getStatus());

        Item offeredItemAReloaded = itemRepository.findById(offeredItemA.getId()).orElseThrow();
        Item offeredItemBReloaded = itemRepository.findById(offeredItemB.getId()).orElseThrow();
        Item requestedItemReloaded = itemRepository.findById(requestedItem.getId()).orElseThrow();

        assertEquals(ItemStatus.RESERVED, requestedItemReloaded.getStatus());
        boolean winningOfferedItemIsA = winnerProposalId.equals(proposalA.getId());
        assertEquals(ItemStatus.RESERVED, winningOfferedItemIsA ? offeredItemAReloaded.getStatus() : offeredItemBReloaded.getStatus());
        assertEquals(ItemStatus.APPROVED, winningOfferedItemIsA ? offeredItemBReloaded.getStatus() : offeredItemAReloaded.getStatus());

        assertTrue(shipmentRepository.existsByTradeProposalId(winnerProposalId), "Debe existir exactamente un shipment para la propuesta ganadora");
        assertFalse(shipmentRepository.existsByTradeProposalId(loserProposalId), "No debe crearse shipment para la propuesta cancelada");
    }

    private Callable<TradeProposalResponseDto> acceptTask(Usuario actingUser, Long proposalId, CountDownLatch startLatch) {
        return () -> {
            try {
                startLatch.await();
                SecurityContextHolder.getContext().setAuthentication(
                        new UsernamePasswordAuthenticationToken(actingUser, null,
                                List.of(new SimpleGrantedAuthority("USER"))));
                return tradeProposalService.acceptTradeProposal(proposalId);
            } finally {
                SecurityContextHolder.clearContext();
            }
        };
    }

    private Outcome resolve(Future<TradeProposalResponseDto> future) throws InterruptedException, TimeoutException {
        try {
            TradeProposalResponseDto response = future.get(15, TimeUnit.SECONDS);
            return new Outcome(true, response, null);
        } catch (ExecutionException ex) {
            return new Outcome(false, null, ex.getCause());
        }
    }

    private static class Outcome {
        final boolean succeeded;
        final TradeProposalResponseDto response;
        final Throwable failure;

        Outcome(boolean succeeded, TradeProposalResponseDto response, Throwable failure) {
            this.succeeded = succeeded;
            this.response = response;
            this.failure = failure;
        }
    }

    private Category buildCategory() {
        Category category = new Category();
        category.setName("Concurrencia " + System.nanoTime());
        return category;
    }

    private Usuario buildUsuario(String email) {
        Usuario usuario = new Usuario();
        usuario.setFirstname("Test");
        usuario.setLastname("User");
        usuario.setEmail(email + "." + System.nanoTime());
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
        item.setStatus(ItemStatus.APPROVED);
        item.setUsuario(owner);
        return item;
    }

    private TradeProposal buildProposal(Item offeredItem, Item requestedItem, Usuario proposer, Usuario receiver) {
        TradeProposal proposal = new TradeProposal();
        proposal.setOfferedItem(offeredItem);
        proposal.setRequestedItem(requestedItem);
        proposal.setProposer(proposer);
        proposal.setReceiver(receiver);
        proposal.setStatus(TradeStatus.PENDING);
        return proposal;
    }
}
