package com.zhousheng.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

//当用户点开个人订单的时候，出现的是订单列表的简单信息，点开某一个订单才能看到具体买了哪些东西
@Data
public class OrderSimpleVo {
    private Long id;
    private String orderNo;//订单编号
    private LocalDateTime createTime;//创建时间
    private BigDecimal totalAmount;
}
