package com.zhousheng.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
public class ProductSkuVo {
    private Long id;
    private String title;
    private Integer stock;
    private BigDecimal price;
    private String images;
    private Map<String,String> spec;
}
