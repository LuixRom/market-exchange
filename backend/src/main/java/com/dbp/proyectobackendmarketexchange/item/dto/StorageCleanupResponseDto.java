package com.dbp.proyectobackendmarketexchange.item.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class StorageCleanupResponseDto {
    private int scanned;
    private int deleted;
    private List<String> deletedKeys;
}
