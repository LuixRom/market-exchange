package com.dbp.proyectobackendmarketexchange.config;

import com.dbp.proyectobackendmarketexchange.item.domain.Item;
import com.dbp.proyectobackendmarketexchange.item.dto.ItemResponseDto;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class ModelMapperConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();

        // Mantener la configuración existente
        modelMapper.getConfiguration().setAmbiguityIgnored(true);

        // Agregar la configuración para mapear userName de Item a ItemResponseDto
        modelMapper.typeMap(Item.class, ItemResponseDto.class).addMappings(mapper -> {
            mapper.map(src -> src.getUsuario().getEmail(), ItemResponseDto::setUserName);
        });

        return modelMapper;
    }
}