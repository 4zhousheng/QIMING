package com.zhousheng.vo;

import lombok.Data;

import java.math.BigDecimal;
@Data
public class OrderItemVo {
//    前端可以通过skuId点击进入商品详情
    private Long skuId;
    private String skuTitle;
    private String skuImage;//对应图片的存储地址
    private Integer status; //未支付为0，已支付为1
    private BigDecimal skuPrice;
    private Integer quantity;
}
