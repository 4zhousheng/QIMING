package com.zhousheng.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

//这个是当用户点开某一个订单时展示的该订单详细信息
@Data
public class OrderDetailedVo {
    private Integer orderId;
    private BigDecimal totalAmount;
    private LocalDateTime createTime;
    private List<OrderItemVo> orderItemVoList;
}
