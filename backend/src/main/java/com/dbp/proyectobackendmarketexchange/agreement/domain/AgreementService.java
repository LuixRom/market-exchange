package com.dbp.proyectobackendmarketexchange.agreement.domain;

import com.dbp.proyectobackendmarketexchange.agreement.dto.AgreementRequestDto;
import com.dbp.proyectobackendmarketexchange.agreement.dto.AgreementResponseDto;
import com.dbp.proyectobackendmarketexchange.agreement.infrastructure.AgreementRepository;
import com.dbp.proyectobackendmarketexchange.event.agreement.AgreementAceptadoEvent;
import com.dbp.proyectobackendmarketexchange.event.agreement.AgreementCreadoEvent;
import com.dbp.proyectobackendmarketexchange.event.agreement.AgreementRechazadoEvent;
import com.dbp.proyectobackendmarketexchange.exception.ResourceNotFoundException;
import com.dbp.proyectobackendmarketexchange.item.domain.Item;
import com.dbp.proyectobackendmarketexchange.item.infrastructure.ItemRepository;
import com.dbp.proyectobackendmarketexchange.shipment.domain.ShipmentService;
import com.dbp.proyectobackendmarketexchange.usuario.domain.Usuario;
import com.dbp.proyectobackendmarketexchange.usuario.infrastructure.UsuarioRepository;
import jakarta.transaction.Transactional;

import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service
public class AgreementService {

    private final ItemRepository itemRepository;
    private final ShipmentService shipmentService;
    private final UsuarioRepository usuarioRepository;
    private final AgreementRepository agreementRepository;
    private final ModelMapper modelMapper;
    private final ApplicationEventPublisher eventPublisher;

    public AgreementService(ItemRepository itemRepository, ShipmentService shipmentService, UsuarioRepository usuarioRepository, AgreementRepository agreementRepository, ModelMapper modelMapper, ApplicationEventPublisher eventPublisher) {
        this.itemRepository = itemRepository;
        this.shipmentService = shipmentService;
        this.usuarioRepository = usuarioRepository;
        this.agreementRepository = agreementRepository;
        this.modelMapper = modelMapper;
        this.eventPublisher = eventPublisher;
    }

    public List<AgreementResponseDto> getAllAgreements() {

        return agreementRepository.findAll().stream()
                .map(agreement -> {
                    AgreementResponseDto responseDto = modelMapper.map(agreement, AgreementResponseDto.class);

                    // Seteamos manualmente las propiedades adicionales
                    responseDto.setItemFinName(agreement.getItem_fin().getName());  // Nombre del item final
                    responseDto.setItemIniName(agreement.getItem_ini().getName());  // Nombre del item inicial
                    responseDto.setId_Fin(agreement.getRecipient().getId());  // Email del receptor
                    responseDto.setId_Ini(agreement.getInitiator().getId());  // Email del iniciador
                    responseDto.setUserNameIni(agreement.getInitiator().getEmail());
                    responseDto.setUserNameFin(agreement.getRecipient().getEmail());
                    responseDto.setId_itemIni(agreement.getItem_ini().getId());
                    responseDto.setId_itemFin(agreement.getItem_fin().getId());

                    return responseDto;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public AgreementResponseDto createAgreement(AgreementRequestDto agreementRequestDto) {

        if (agreementRequestDto.getState() == State.ACCEPTED || agreementRequestDto.getState() == State.REJECTED) {
            throw new IllegalArgumentException("No se puede crear un acuerdo directamente en estado ACCEPTED o REJECTED");
        }

        Agreement agreement = new Agreement();


        Item itemIni = itemRepository.findById(agreementRequestDto.getItemIniId())
                .orElseThrow(() -> new ResourceNotFoundException("Item inicial no encontrado"));

        Item itemFin = itemRepository.findById(agreementRequestDto.getItemFinId())
                .orElseThrow(() -> new ResourceNotFoundException("Item final no encontrado"));

        Usuario usuarioIni = usuarioRepository.findById(agreementRequestDto.getUsuarioIniId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario iniciador no encontrado"));

        Usuario usuarioFin = usuarioRepository.findById(agreementRequestDto.getUsuarioFinId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario receptor no encontrado"));


        agreement.setItem_ini(itemIni);
        agreement.setItem_fin(itemFin);
        agreement.setInitiator(usuarioIni);
        agreement.setRecipient(usuarioFin);


        agreement.setState(State.PENDING);


        Agreement savedAgreement = agreementRepository.save(agreement);

        // Publicar evento de creación del acuerdo
        eventPublisher.publishEvent(new AgreementCreadoEvent(this, savedAgreement));



        AgreementResponseDto responseDto = modelMapper.map(savedAgreement, AgreementResponseDto.class);
        responseDto.setItemFinName(itemFin.getName());
        responseDto.setItemIniName(itemIni.getName());
        responseDto.setId_Ini(usuarioIni.getId());
        responseDto.setId_Fin(usuarioFin.getId());
        responseDto.setUserNameFin(usuarioFin.getEmail());
        responseDto.setUserNameIni(usuarioIni.getEmail());
        responseDto.setId_itemIni(agreement.getItem_ini().getId());
        responseDto.setId_itemFin(agreement.getItem_fin().getId());
        return responseDto;
    }

    public AgreementResponseDto getAgreementById(Long id) {
        Agreement agreement = agreementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agreement not found"));

        //seteo manual
        AgreementResponseDto responseDto = new AgreementResponseDto();
        responseDto.setId(agreement.getId());
        responseDto.setState(agreement.getState());
        responseDto.setItemIniName(agreement.getItem_ini().getName());
        responseDto.setItemFinName(agreement.getItem_fin().getName());
        responseDto.setId_Ini(agreement.getInitiator().getId());
        responseDto.setId_Fin(agreement.getRecipient().getId());
        responseDto.setUserNameFin(agreement.getRecipient().getEmail());
        responseDto.setUserNameIni(agreement.getInitiator().getEmail());
        responseDto.setId_itemIni(agreement.getItem_ini().getId());
        responseDto.setId_itemFin(agreement.getItem_fin().getId());
        return responseDto;
    }

    @Transactional
    public AgreementResponseDto acceptAgreement(Long id) {
        Agreement agreement = agreementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agreement not found"));

        // Verificamos que el estado actual sea PENDING
        if (agreement.getState() != State.PENDING) {
            throw new IllegalArgumentException("Solo los acuerdos en estado PENDING pueden ser aceptados");
        }

        // Cambiamos el estado a ACCEPTED
        agreement.setState(State.ACCEPTED);

        // Guardamos el acuerdo actualizado en la base de datos
        Agreement savedAgreement = agreementRepository.save(agreement);

        // Publicar evento de aceptación del acuerdo
        eventPublisher.publishEvent(new AgreementAceptadoEvent(this, savedAgreement));

        // Creamos el Shipment asociado al acuerdo aceptado
        shipmentService.createShipmentForAgreement(savedAgreement);

        // Retornamos el response DTO
        AgreementResponseDto responseDto = modelMapper.map(savedAgreement, AgreementResponseDto.class);
        responseDto.setItemFinName(savedAgreement.getItem_fin().getName());
        responseDto.setItemIniName(savedAgreement.getItem_ini().getName());
        responseDto.setId_Ini(savedAgreement.getInitiator().getId());
        responseDto.setId_Fin(savedAgreement.getRecipient().getId());
        responseDto.setUserNameFin(savedAgreement.getRecipient().getEmail());
        responseDto.setUserNameIni(savedAgreement.getInitiator().getEmail());
        responseDto.setId_itemIni(agreement.getItem_ini().getId());
        responseDto.setId_itemFin(agreement.getItem_fin().getId());
        return responseDto;
    }

    @Transactional
    public AgreementResponseDto rejectAgreement(Long id) {
        Agreement agreement = agreementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agreement not found"));

        // Verificamos que el estado actual sea PENDING
        if (agreement.getState() != State.PENDING) {
            throw new IllegalArgumentException("Solo los acuerdos en estado PENDING pueden ser rechazados");
        }

        // Cambiamos el estado a REJECTED
        agreement.setState(State.REJECTED);

        // Guardamos el acuerdo actualizado en la base de datos
        Agreement savedAgreement = agreementRepository.save(agreement);

        // Publicar evento de rechazo del acuerdo
        eventPublisher.publishEvent(new AgreementRechazadoEvent(this, savedAgreement));



        // Retornamos el response DTO
        AgreementResponseDto responseDto = modelMapper.map(savedAgreement, AgreementResponseDto.class);
        responseDto.setItemFinName(savedAgreement.getItem_fin().getName());
        responseDto.setItemIniName(savedAgreement.getItem_ini().getName());
        responseDto.setId_Ini(savedAgreement.getRecipient().getId());
        responseDto.setId_Fin(savedAgreement.getInitiator().getId());
        responseDto.setUserNameFin(savedAgreement.getRecipient().getEmail());
        responseDto.setUserNameIni(savedAgreement.getInitiator().getEmail());
        responseDto.setId_itemIni(agreement.getItem_ini().getId());
        responseDto.setId_itemFin(agreement.getItem_fin().getId());
        return responseDto;
    }

    @Transactional
    public AgreementResponseDto updateAgreement(Long id, AgreementRequestDto agreementRequestDto) {
        Agreement existingAgreement = agreementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agreement not found"));

        if (existingAgreement.getState() == State.REJECTED || existingAgreement.getState() == State.ACCEPTED) {
            throw new IllegalArgumentException("No se puede cambiar el estado de un acuerdo que ya ha sido ACCEPTED o REJECTED");
        }

        AgreementResponseDto responseDto = new AgreementResponseDto();

        modelMapper.map(existingAgreement, responseDto);
        responseDto.setItemFinName(existingAgreement.getItem_fin().getName());
        responseDto.setItemIniName(existingAgreement.getItem_ini().getName());
        responseDto.setId_Ini(existingAgreement.getInitiator().getId());
        responseDto.setId_Fin(existingAgreement.getRecipient().getId());
        responseDto.setUserNameIni(existingAgreement.getInitiator().getEmail());
        responseDto.setUserNameFin(existingAgreement.getRecipient().getEmail());
        responseDto.setId_itemIni(existingAgreement.getItem_ini().getId());
        responseDto.setId_itemFin(existingAgreement.getItem_fin().getId());
        return responseDto;
    }

    public void deleteAgreement(Long id) {
        agreementRepository.deleteById(id);
    }

}
