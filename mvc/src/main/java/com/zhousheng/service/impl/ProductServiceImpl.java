package com.zhousheng.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhousheng.common.exception.UpdateFailException;
import com.zhousheng.common.utils.DateUtils;
import com.zhousheng.domain.ProductDo;
import com.zhousheng.dto.ProductCreateDto;
import com.zhousheng.dto.ProductPageQueryDto;
import com.zhousheng.dto.ProductSkuCreateDto;
import com.zhousheng.mapper.ProductMapper;
import com.zhousheng.service.CosService;
import com.zhousheng.service.IProductService;
import com.zhousheng.service.ProductSkuService;
import com.zhousheng.vo.ProductVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class ProductServiceImpl
        extends ServiceImpl<ProductMapper, ProductDo>
        implements IProductService {
    private final ProductMapper productMapper;
    private final CosService cosService;
    private final ProductSkuService productSkuService;

    ProductServiceImpl(ProductMapper productMapper, CosService cosService,ProductSkuService productSkuService){
        this.productMapper = productMapper;
        this.cosService = cosService;
        this.productSkuService = productSkuService;
    }

    @Override
    public Page<ProductVo> getProductPages(ProductPageQueryDto queryDto) {
        queryDto.setKeyword("");
        return getProductPagesByKeyword(queryDto);
    }

    /**
     * 分页查询
     * @param queryDto
     * @return
     */
    @Override
    public Page<ProductVo> getProductPagesByKeyword(ProductPageQueryDto queryDto){
        Page<ProductVo> page = new Page<>(queryDto.getPageNum(),queryDto.getPageSize());
        List<ProductVo> records = productMapper.selectProductVoPage(page, queryDto);
//        将数据库中存储的相对路径转换为绝对路径
        for(var record : records){
            record.setMainImage(cosService.getFileUrl(record.getMainImage()));
        }
        page.setRecords(records);
        return page;
    }

    /**
     * 创建商品
     * @param productCreateDto
     * @throws IOException
     * @throws UpdateFailException
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void createProduct(ProductCreateDto productCreateDto) throws IOException, UpdateFailException {
        ProductDo productDo = new ProductDo();
        productDo.setSpuNo(productCreateDto.getSpuNo());
        productDo.setTitle(productCreateDto.getTitle());
//        获取临时存储路径，
        String tempMainImagePath = productCreateDto.getMainImageKey();
        productDo.setMainImage(tempMainImagePath);
        productMapper.addProduct(productDo);
        Long productId = productDo.getId();
        updateImagePath(productId, tempMainImagePath);
//        对productSku进行更新
        List<ProductSkuCreateDto> productSkus = productCreateDto.getProductSkus(); 


        productSkuService.addSkusForProduct(productId,productSkus);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateImagePath(Long id,String oldPath) throws UpdateFailException {
        String fileName = UUID.randomUUID().toString().replace("-","");
//        构建最终存储路径
        String destination = "product/" + id + "/cover/"+fileName+".jpg";
        cosService.moveFile(oldPath,destination);
        int affectedRows = productMapper.updateMainImage(id, destination);
        if(affectedRows == 0){
            log.error("修改存储地址失败，请检查后再提交");
            throw new UpdateFailException("修改商品主图片路径失败");
        }
        log.info("更新商品主图片存储地址");
    }


    @Override
    public String uploadMainImage(MultipartFile image) throws IOException {
        String originalFileName = image.getOriginalFilename();
        String fileExtension = null;
//为了防止用户上传没有后缀名的文件
        int dotIndex = originalFileName.lastIndexOf('.');
        if (dotIndex >= 0 && dotIndex < originalFileName.length() - 1) {
            fileExtension = originalFileName.substring(dotIndex);
        }else{
            log.error("文件无后缀名");
            throw new RuntimeException("错误，用户上传文件无后缀名");
        }
        // 构建目录路径
        String currentDate = DateUtils.getCurrentDateString();
        String directory = "temp/product/spu/" + currentDate + "/";
        String path = directory + UUID.randomUUID().toString().replace("-","") + fileExtension;
        String key = cosService.uploadFile(image, path);
        return key;
    }

    @Override
    public ProductVo getProductSpu(Long spuId) {
        ProductDo productDo = productMapper.selectById(spuId);
        ProductVo productVo = new ProductVo();
        BigDecimal minPrice = productSkuService.getMinPriceBySpuId(spuId);
        BeanUtils.copyProperties(productDo,productVo);
        productVo.setMinPrice(minPrice);
        productVo.setMainImage(cosService.getFileUrl(productDo.getMainImage()));
        return productVo;
    }
}
