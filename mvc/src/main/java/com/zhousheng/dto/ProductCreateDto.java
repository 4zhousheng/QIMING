package com.zhousheng.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class ProductCreateDto {
    private String spuNo;
    private String title;
    private List<ProductSkuCreateDto> productSkus;
    private String mainImageKey;
}
