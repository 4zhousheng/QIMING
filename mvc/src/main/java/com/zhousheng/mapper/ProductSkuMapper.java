package com.zhousheng.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.zhousheng.domain.ProductSkuDo;
import com.zhousheng.dto.ProductSkuDto;
import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;
import java.util.List;
public interface ProductSkuMapper extends BaseMapper<ProductSkuDo> {
    @Select("""
        SELECT
            id, spu_id, sku_no, title, price, stock, images, spec, create_time, update_time
        FROM product_sku
        WHERE spu_id = #{spu_id}
""")
    List<ProductSkuDo> getProductSkuBySpuId(
            @Param("spu_id")Long spu_id);

//    @Insert("""
//        INSERT INTO
//        product_sku(id, spu_id, sku_no, title, price, stock, images, spec, create_time, update_time)
//        VALUES(#{id},#{spuId},#{skuIo},#{title},#{price},#{stock},#{images},#{spec},#{createTime},#{updateTime})
//""")
//    int addProductSku(ProductSkuDo productSku);

    int batchInsertProductSkus(@Param("skuDoList") List<ProductSkuDo> skuDoList);

    //    @Update("""
//        UPDATE
//        product_sku
//        SET stock=stock-#{quantity}
//        WHERE id=#{skuId}
//""")
//    int deductStock(ProductSkuDto productSkuDto);
    int batchDeductStock(List<ProductSkuDto> products);
//    找到最低价格作为一件商品的展示价格
    @Select("SELECT MIN(price) FROM product_sku WHERE spu_id = #{spuId}")
    BigDecimal findMinPriceBySpuId(@Param("spuId") Long spuId);
    @Select("SELECT * FROM product_sku WHERE spu_id = #{spuId}")
    @Results({
            @Result(column = "id", property = "id"),
            @Result(column = "spu_id", property = "spuId"),
            @Result(column = "sku_no", property = "skuNo"),
            @Result(column = "spec", property = "spec", typeHandler = JacksonTypeHandler.class)
    })
    List<ProductSkuDo> selectBySpuId(@Param("spuId") Long spuId);
}
