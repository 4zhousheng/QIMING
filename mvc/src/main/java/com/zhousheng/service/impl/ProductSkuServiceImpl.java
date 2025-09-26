package com.zhousheng.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhousheng.common.exception.ProductSpuIdException;
import com.zhousheng.common.exception.StockNotEnoughException;
import com.zhousheng.common.utils.DateUtils;
import com.zhousheng.domain.ProductSkuDo;
import com.zhousheng.dto.OrderCreateDto;
import com.zhousheng.dto.ProductSkuCreateDto;
import com.zhousheng.dto.ProductSkuDto;
import com.zhousheng.mapper.ProductSkuMapper;
import com.zhousheng.service.CosService;
import com.zhousheng.service.ProductSkuService;
import com.zhousheng.vo.ProductSkuVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ProductSkuServiceImpl
        extends ServiceImpl<ProductSkuMapper,ProductSkuDo>
        implements ProductSkuService {
    private final ProductSkuMapper productSkuMapper;
    private final CosService cosService;
    ProductSkuServiceImpl(ProductSkuMapper productSkuMapper,CosService cosService){

        this.productSkuMapper = productSkuMapper;
        this.cosService = cosService;
    }

    /**
     * 二创版
     * 命名上把addProductSku改成了addSkusForProduct、
     *
     * @param productSpuId
     * @param productSkuDtos
     * @return
     */
    @Override
    public void addSkusForProduct(Long productSpuId,List<ProductSkuCreateDto> productSkuDtos) {
        List<ProductSkuDo> productSkuDoToInsert = productSkuDtos.stream().map(dto->{
            ProductSkuDo productSkuDo = new ProductSkuDo();
            productSkuDo.setSkuNo(dto.getSkuNo());
            productSkuDo.setSpec(dto.getSpec());
            productSkuDo.setPrice(dto.getPrice());
            productSkuDo.setStock(dto.getStock());
            productSkuDo.setTitle(dto.getTitle());
            productSkuDo.setSpuId(productSpuId);
            return productSkuDo;
        }).collect(Collectors.toList());
        try {
            this.saveBatch(productSkuDoToInsert);
        }catch (Exception e){
            e.printStackTrace();
            Throwable cause = e.getCause();
            while (cause != null) {
                cause.printStackTrace();
                cause = cause.getCause();
            }
        }
        updateImages(productSpuId,productSkuDoToInsert,productSkuDtos);
    }

    /**
     * 上传图片
     * @param productSpuId
     * @param productSkuDoToInsert
     * @param productSkuDtos
     */
    private void updateImages(Long productSpuId,List<ProductSkuDo> productSkuDoToInsert,List<ProductSkuCreateDto> productSkuDtos){

        List<ProductSkuDo> productSkuDoToUpdate = new ArrayList<>();
        for(int i = 0; i < productSkuDoToInsert.size(); i++){
            ProductSkuDo insertedSku = productSkuDoToInsert.get(i);
            ProductSkuCreateDto originalDto = productSkuDtos.get(i);
            ProductSkuDo updateSku = new ProductSkuDo();
            List<String> imageList = new ArrayList<>();
            if(originalDto.getImagesKey() != null && !originalDto.getImagesKey().isEmpty()){
                Long skuId = insertedSku.getId();
                updateSku.setId(skuId);
                List<String> imagesKey = originalDto.getImagesKey();
                insertedSku.getSpuId();
                for(var imageKey : imagesKey) {
                    try {
                        String fileName = UUID.randomUUID().toString();
                        String destination = String.format("product/sku/%d/%d/%s.jpg", productSpuId, skuId, fileName);
                        cosService.moveFile(imageKey, destination);
                        imageList.add(destination);
                    }catch (Exception e){
                        throw new RuntimeException("上传文件失败" + e.getMessage());
                    }
                }
                String images = String.join(",",imageList);
                updateSku.setImages(images);
                productSkuDoToUpdate.add(updateSku);
            }
        }
        if(!productSkuDoToUpdate.isEmpty()){
            this.updateBatchById(productSkuDoToUpdate);
        }
    }
    public List<ProductSkuDo> getProductSkuBySpuId(Long id) {
        List<ProductSkuDo> productSkuDos = productSkuMapper.selectBySpuId(id);
        return productSkuDos;
    }

    /**
     * 获取productSku Vo
     * @param spuId
     * @return
     */
    @Override
//这次改用流式写法
    public List<ProductSkuVo> getProductSkus(Long spuId) {
        List<ProductSkuDo> skuDoList = getProductSkuBySpuId(spuId);
        List<ProductSkuVo> skuVoList = skuDoList.stream()
                .map(skuDo -> { // 对列表中的每一个 skuDo 执行转换
                    ProductSkuVo skuVo = new ProductSkuVo();

                    BeanUtils.copyProperties(skuDo, skuVo);
                    String imageKeys = skuDo.getImages();
                    if (StringUtils.hasText(imageKeys)) {
                        String imageUrls = Arrays.stream(imageKeys.split(","))
                                .filter(StringUtils::hasText)
                                .map(cosService::getFileUrl)
                                .collect(Collectors.joining(","));
                        skuVo.setImages(imageUrls);
                    } else {
                        skuVo.setImages("");
                    }

                    return skuVo;
                })
                .collect(Collectors.toList()); // 将转换后的所有 skuVo 收集到一个新列表中
        return skuVoList;
    }
    /**
     * 扣减库存
     * @param orderCreateDto
     * @throws StockNotEnoughException
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deductStock(OrderCreateDto orderCreateDto) throws StockNotEnoughException {
        List<ProductSkuDto> products = orderCreateDto.getProductSkuDtos();
        if(products == null || products.isEmpty()){
            return ;
        }
        int affectedRows = productSkuMapper.batchDeductStock(products);
        if(affectedRows < products.size()){
            log.error("商品库存不足");
            throw new StockNotEnoughException();
        }
        log.info("订单库存扣减成功，订单信息：{}",orderCreateDto);
    }

    @Override
    public List<String> uploadSkuImages(List<MultipartFile> images) throws IOException {
        String currentDate = DateUtils.getCurrentDateString();
        List<String> result = new ArrayList<>();
        for(var image : images){
            String path = "/temp/product/sku/" + currentDate + "/" + UUID.randomUUID().toString() + ".jpg";
            String key = cosService.uploadFile(image, path);
            result.add(key);
        }
        return result;
    }

    @Override
    public BigDecimal getMinPriceBySpuId(Long id) {
        return productSkuMapper.findMinPriceBySpuId(id);
    }

}
