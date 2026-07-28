package com.dbp.proyectobackendmarketexchange.agreement.dto;

import com.dbp.proyectobackendmarketexchange.agreement.domain.State;
import lombok.Getter;
    import lombok.Setter;


    @Getter
    @Setter
    public class AgreementResponseDto {
        private Long id;
        private State state;
        private String itemIniName;
        private String itemFinName;
        private String userNameIni;
        private String userNameFin;
        private Long id_Ini;
        private Long id_Fin;
        private Long id_itemIni;
        private Long id_itemFin;
    }