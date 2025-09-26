package com.zhousheng.controller.api;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhousheng.common.enums.ResultCodeEnum;
import com.zhousheng.common.exception.KeywordNotFoundException;
import com.zhousheng.common.exception.ProductSpuIdException;
import com.zhousheng.common.result.Result;
import com.zhousheng.dto.ProductCreateDto;
import com.zhousheng.dto.ProductPageQueryDto;
import com.zhousheng.service.CosService;
import com.zhousheng.service.IProductService;
import com.zhousheng.service.ProductSkuService;
import com.zhousheng.vo.ProductSkuVo;
import com.zhousheng.vo.ProductVo;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/product")
public class ProductApi {
    private final CosService cosService;
    private final IProductService productService;
    private final ProductSkuService productSkuService;
    ProductApi(IProductService productService, ProductSkuService productSkuService, CosService cosService)
    {
        this.productService = productService;
        this.cosService = cosService;
        this.productSkuService = productSkuService;
    }

    @PostMapping("/createProduct")
    Result createProduct(@RequestBody ProductCreateDto productCreateDto){
        try {
            productService.createProduct(productCreateDto);
        }catch (Exception e){
            log.error("创建商品时出现错误: " + e.getMessage());
            return Result.fail(ResultCodeEnum.FAIL);
        }
        return Result.success();
    }

    @PostMapping("/uploadMainImage")
    Result<String> uploadMainImage(@RequestParam(value = "image",required = false) MultipartFile image, HttpServletRequest request){
        System.out.println("Request class = " + request.getClass());
        System.out.println("Content-Type = " + request.getContentType());
        if (request instanceof MultipartHttpServletRequest multipartRequest) {
            System.out.println("is MultipartHttpServletRequest");
            System.out.println("fileMap size = " + multipartRequest.getFileMap().size());
            multipartRequest.getFileMap().forEach((k, f) ->
                    System.out.println("field=" + k + ", fileName=" + f.getOriginalFilename())
            );
        } else {
            System.out.println("Not a multipart request");
        }
        String key = "";
        try {
            key = productService.uploadMainImage(image);
        } catch (IOException e) {
            log.error("上传main图片失败");
            return Result.fail(500,"上传图片失败");
        }
        return Result.success(key);
    }

    @PostMapping("/uploadSkuImages")
    Result<List<String>> uploadSkuImages(@RequestParam(value = "images")List<MultipartFile> images){
        List<String> skuKeys = null;
        try {
            skuKeys = productSkuService.uploadSkuImages(images);
        } catch (IOException e) {
            log.error("上传商品sku图片发生错误 ： " + e.getMessage());
            return Result.fail(500,"图片上传失败,请重试");
        }
        return Result.success(skuKeys);
    }



    @PostMapping("/list")
    Page<ProductVo> getProductPagesByKeyword(@RequestBody ProductPageQueryDto queryDto){
            return productService.getProductPagesByKeyword(queryDto);
    }

    @GetMapping("/sku/{spuId}")
    Result<List<ProductSkuVo>> getProductSkus(@PathVariable("spuId") Long spuId){
        try {
            return Result.success(productSkuService.getProductSkus(spuId));
        } catch (ProductSpuIdException e) {
            log.error("错误，商品Id为空或值异常");
            return Result.fail(300,"spuId不能为空或特殊值");
        }
    }
    @GetMapping("/spu/{spuId}")
    Result<ProductVo> getProductSpu(@PathVariable("spuId") Long spuId){
        try{
            ProductVo productSpu = productService.getProductSpu(spuId);
            return Result.success(productSpu);
        }catch (Exception e){
            return Result.fail(488,"获取spu信息失败");
        }
    }
}
