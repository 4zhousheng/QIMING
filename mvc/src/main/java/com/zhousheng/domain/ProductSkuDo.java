package com.zhousheng.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;
import org.apache.ibatis.type.JdbcType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@TableName("product_sku")
public class ProductSkuDo {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long spuId;
    private String skuNo;
    private String title;
    private Integer stock;
    private BigDecimal price;
    private String images = "";
    @TableField(value = "spec",typeHandler = JacksonTypeHandler.class)
    private Map<String,String> spec;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
