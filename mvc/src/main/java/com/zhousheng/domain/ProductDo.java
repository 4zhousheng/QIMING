package com.zhousheng.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("product")
public class ProductDo {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String spuNo;
    private String title;
    private Long categoryId;
    private Long brandId;
    private String mainImage;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer isDeleted;
}
