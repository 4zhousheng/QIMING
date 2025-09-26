package com.zhousheng.dto;

import lombok.Data;

@Data
public class ProductPageQueryDto {
    private int pageNum = 1;
    private int pageSize = 10;
    private String keyword;
}
