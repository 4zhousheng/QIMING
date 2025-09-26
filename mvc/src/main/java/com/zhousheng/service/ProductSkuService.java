package com.zhousheng.service;

import com.zhousheng.common.exception.ProductSpuIdException;
import com.zhousheng.common.exception.StockNotEnoughException;
import com.zhousheng.domain.ProductSkuDo;
import com.zhousheng.dto.OrderCreateDto;
import com.zhousheng.dto.ProductSkuCreateDto;
import com.zhousheng.vo.ProductSkuVo;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

public interface ProductSkuService {
    void addSkusForProduct(Long productSpuId,List<ProductSkuCreateDto> productSku);
    List<ProductSkuVo> getProductSkus(Long spuId) throws ProductSpuIdException;

    void deductStock(OrderCreateDto orderCreateDto) throws StockNotEnoughException;
    List<String> uploadSkuImages(List<MultipartFile> images) throws IOException;
    BigDecimal getMinPriceBySpuId(Long id);
}
