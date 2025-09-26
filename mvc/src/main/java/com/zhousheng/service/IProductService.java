package com.zhousheng.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zhousheng.common.exception.KeywordNotFoundException;
import com.zhousheng.common.exception.UpdateFailException;
import com.zhousheng.domain.ProductDo;
import com.zhousheng.domain.ProductSkuDo;
import com.zhousheng.dto.ProductCreateDto;
import com.zhousheng.dto.ProductPageQueryDto;
import com.zhousheng.vo.ProductVo;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface IProductService extends IService<ProductDo> {

    Page<ProductVo> getProductPagesByKeyword(ProductPageQueryDto queryDto);
    Page<ProductVo> getProductPages(ProductPageQueryDto queryDto);

    void createProduct(ProductCreateDto productCreateDto) throws IOException, UpdateFailException;
    String uploadMainImage(MultipartFile image) throws IOException;
    ProductVo getProductSpu(Long spuId);
}
