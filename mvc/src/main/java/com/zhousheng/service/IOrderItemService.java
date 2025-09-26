package com.zhousheng.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhousheng.common.exception.CreateOrderFailException;
import com.zhousheng.domain.OrderItemDo;
import com.zhousheng.dto.OrderCreateDto;

public interface IOrderItemService extends IService<OrderItemDo> {
    public void createOrderItems(Long orderId, OrderCreateDto orderCreateDto) throws CreateOrderFailException;
    }
