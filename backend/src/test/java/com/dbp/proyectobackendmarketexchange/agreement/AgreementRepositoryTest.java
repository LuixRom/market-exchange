package com.dbp.proyectobackendmarketexchange.agreement;

import com.dbp.proyectobackendmarketexchange.AbstractContainerBaseTest;
import com.dbp.proyectobackendmarketexchange.agreement.domain.Agreement;
import com.dbp.proyectobackendmarketexchange.agreement.domain.State;
import com.dbp.proyectobackendmarketexchange.agreement.infrastructure.AgreementRepository;
import com.dbp.proyectobackendmarketexchange.category.domain.Category;
import com.dbp.proyectobackendmarketexchange.item.domain.Condition;
import com.dbp.proyectobackendmarketexchange.item.domain.Item;
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
public class AgreementRepositoryTest extends AbstractContainerBaseTest {

    @Autowired
    private AgreementRepository agreementRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Item itemIni1;
    private Item itemIni2;  // Agregar un segundo item_ini
    private Item itemFin1;
    private Item itemFin2;
    private Usuario usuarioIni;
    private Usuario usuarioFin;

    @BeforeEach
    public void setUp() {
        // Crear y persistir los objetos necesarios para el acuerdo
        Category category = new Category();
        category.setName("Categoria de prueba");
        entityManager.persist(category);

        usuarioIni = new Usuario();
        usuarioIni.setFirstname("Iniciador");
        usuarioIni.setLastname("Test");
        usuarioIni.setEmail("iniciador@example.com");
        usuarioIni.setPhone("123456789");
        usuarioIni.setPassword("password123");
        usuarioIni.setAddress("Direccion iniciador");
        usuarioIni.setRole(Role.USER);
        entityManager.persist(usuarioIni);

        usuarioFin = new Usuario();
        usuarioFin.setFirstname("Receptor");
        usuarioFin.setLastname("Test");
        usuarioFin.setEmail("receptor@example.com");
        usuarioFin.setPhone("987654321");
        usuarioFin.setPassword("password123");
        usuarioFin.setAddress("Direccion receptor");
        usuarioFin.setRole(Role.USER);
        entityManager.persist(usuarioFin);

        itemIni1 = new Item();
        itemIni1.setName("Item Inicial 1");
        itemIni1.setDescription("Descripción del item inicial 1");
        itemIni1.setCategory(category);
        itemIni1.setCondition(Condition.NEW);
        itemIni1.setUsuario(usuarioIni);
        entityManager.persist(itemIni1);

        itemIni2 = new Item();
        itemIni2.setName("Item Inicial 2");
        itemIni2.setDescription("Descripción del item inicial 2");
        itemIni2.setCategory(category);
        itemIni2.setCondition(Condition.NEW);
        itemIni2.setUsuario(usuarioIni);
        entityManager.persist(itemIni2);

        itemFin1 = new Item();
        itemFin1.setName("Item Final 1");
        itemFin1.setDescription("Descripción del item final 1");
        itemFin1.setCategory(category);
        itemFin1.setCondition(Condition.USED);
        itemFin1.setUsuario(usuarioFin);
        entityManager.persist(itemFin1);

        itemFin2 = new Item();
        itemFin2.setName("Item Final 2");
        itemFin2.setDescription("Descripción del item final 2");
        itemFin2.setCategory(category);
        itemFin2.setCondition(Condition.USED);
        itemFin2.setUsuario(usuarioFin);
        entityManager.persist(itemFin2);
    }

    @Test
    @Transactional
    public void testCreateAgreement() {

        Agreement agreement = new Agreement();
        agreement.setItem_ini(itemIni1);
        agreement.setItem_fin(itemFin1);
        agreement.setInitiator(usuarioIni);
        agreement.setRecipient(usuarioFin);
        agreement.setState(State.PENDING);
        agreement.setCreatedAt(LocalDateTime.now());


        agreement = agreementRepository.save(agreement);


        assertNotNull(agreement.getId());
        assertEquals(State.PENDING, agreement.getState());
    }

    @Test
    @Transactional
    public void testFindById() {

        Agreement agreement = new Agreement();
        agreement.setItem_ini(itemIni1);
        agreement.setItem_fin(itemFin1);
        agreement.setInitiator(usuarioIni);
        agreement.setRecipient(usuarioFin);
        agreement.setState(State.PENDING);
        agreement.setCreatedAt(LocalDateTime.now());
        entityManager.persist(agreement);


        Optional<Agreement> foundAgreement = agreementRepository.findById(agreement.getId());
        assertTrue(foundAgreement.isPresent());
        assertEquals(agreement.getId(), foundAgreement.get().getId());
    }

    @Test
    @Transactional
    public void testDeleteAgreement() {

        Agreement agreement = new Agreement();
        agreement.setItem_ini(itemIni1);
        agreement.setItem_fin(itemFin1);
        agreement.setInitiator(usuarioIni);
        agreement.setRecipient(usuarioFin);
        agreement.setState(State.PENDING);
        agreement.setCreatedAt(LocalDateTime.now());
        agreement = agreementRepository.save(agreement);
        Long agreementId = agreement.getId();


        agreementRepository.deleteById(agreementId);


        Optional<Agreement> deletedAgreement = agreementRepository.findById(agreementId);
        assertFalse(deletedAgreement.isPresent());
    }

    @Test
    @Transactional
    public void testUpdateAgreementStateToAccepted() {

        Agreement agreement = new Agreement();
        agreement.setItem_ini(itemIni1);
        agreement.setItem_fin(itemFin1);
        agreement.setInitiator(usuarioIni);
        agreement.setRecipient(usuarioFin);
        agreement.setState(State.PENDING);
        agreement.setCreatedAt(LocalDateTime.now());
        agreement = agreementRepository.save(agreement);
        Long agreementId = agreement.getId();


        agreement.setState(State.ACCEPTED);
        agreementRepository.save(agreement);


        Optional<Agreement> updatedAgreement = agreementRepository.findById(agreementId);
        assertTrue(updatedAgreement.isPresent());
        assertEquals(State.ACCEPTED, updatedAgreement.get().getState());
    }

    @Test
    @Transactional
    public void testUpdateAgreementStateToRejected() {

        Agreement agreement = new Agreement();
        agreement.setItem_ini(itemIni1);
        agreement.setItem_fin(itemFin1);
        agreement.setInitiator(usuarioIni);
        agreement.setRecipient(usuarioFin);
        agreement.setState(State.PENDING);
        agreement.setCreatedAt(LocalDateTime.now());
        agreement = agreementRepository.save(agreement);
        Long agreementId = agreement.getId();


        agreement.setState(State.REJECTED);
        agreementRepository.save(agreement);


        Optional<Agreement> updatedAgreement = agreementRepository.findById(agreementId);
        assertTrue(updatedAgreement.isPresent());
        assertEquals(State.REJECTED, updatedAgreement.get().getState());
    }

    @Test
    @Transactional
    public void testFindByState() {

        Agreement pendingAgreement = new Agreement();
        pendingAgreement.setItem_ini(itemIni1);
        pendingAgreement.setItem_fin(itemFin1);
        pendingAgreement.setInitiator(usuarioIni);
        pendingAgreement.setRecipient(usuarioFin);
        pendingAgreement.setState(State.PENDING);
        pendingAgreement.setCreatedAt(LocalDateTime.now());
        entityManager.persist(pendingAgreement);

        Agreement acceptedAgreement = new Agreement();
        acceptedAgreement.setItem_ini(itemIni2);
        acceptedAgreement.setItem_fin(itemFin2);
        acceptedAgreement.setInitiator(usuarioIni);
        acceptedAgreement.setRecipient(usuarioFin);
        acceptedAgreement.setState(State.ACCEPTED);
        acceptedAgreement.setCreatedAt(LocalDateTime.now());
        entityManager.persist(acceptedAgreement);


        List<Agreement> pendingAgreements = agreementRepository.findByState(State.PENDING);
        assertEquals(1, pendingAgreements.size());
        assertEquals(State.PENDING, pendingAgreements.get(0).getState());


        List<Agreement> acceptedAgreements = agreementRepository.findByState(State.ACCEPTED);
        assertEquals(1, acceptedAgreements.size());
        assertEquals(State.ACCEPTED, acceptedAgreements.get(0).getState());
    }
}