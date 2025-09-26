package com.zhousheng.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
public class ProductSkuCreateDto {
    private String skuNo;
    private String title;
    private Integer stock;
    private BigDecimal price;
    private List<String> imagesKey;
    private Map<String,String> spec;
}
