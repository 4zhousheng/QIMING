package com.zhousheng.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderCreateDto {
    private List<ProductSkuDto> productSkuDtos;
}
