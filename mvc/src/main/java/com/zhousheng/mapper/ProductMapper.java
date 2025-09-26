package com.zhousheng.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhousheng.domain.ProductDo;
import com.zhousheng.dto.ProductPageQueryDto;
import com.zhousheng.vo.ProductVo;
import org.apache.ibatis.annotations.*;

import java.util.List;
public interface ProductMapper extends BaseMapper<ProductDo> {
    @Select("""
        SELECT
            id, spu_no, title, category_id, brand_id, main_image, status, create_time, update_time, is_deleted
        FROM product 
""")
    List<ProductDo> getAllProduct();

    @Select("""
        SELECT
            id, spu_no, title, category_id, brand_id, main_image, status, update_time, is_deleted
        FROM product
        WHERE spu_no LIKE CONCAT(#{keyword},'%')
""")
    List<ProductDo> getProductByKeyword(
           @Param("keyword") String keyword);
    @Insert("""
        INSERT INTO 
        product(id, spu_no, title, main_image) 
        VALUES 
        (#{id},#{spuNo},#{title},#{mainImage})
""")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int addProduct(ProductDo product);
    @Update("""
        UPDATE
        product
        SET main_image=#{image}
        WHERE id = #{productId}
""")
    int updateMainImage(@Param("productId") Long productId,
                       @Param("image") String image);
    List<ProductVo> selectProductVoPage(@Param("page")Page<?> page, @Param("query")ProductPageQueryDto queryDto);
}
