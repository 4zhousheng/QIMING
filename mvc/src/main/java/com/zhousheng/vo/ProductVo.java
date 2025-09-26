package com.zhousheng.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Data
public class ProductVo {
    private Long id;
    private String spuNo;
    private String title;
    private String mainImage;
    private BigDecimal minPrice;
    private Integer status;
}
