package com.zhousheng.service;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zhousheng.common.exception.*;
import com.zhousheng.domain.OrderDo;
import com.zhousheng.dto.OrderCreateDto;
import com.zhousheng.vo.OrderDetailedVo;
import com.zhousheng.vo.OrderSimpleVo;

import java.util.List;

public interface OrderService extends IService<OrderDo> {
    void createOrder(OrderCreateDto order,Long userId) throws CreateOrderFailException, InsufficientBalanceException, StockNotEnoughException, ProductSkuNotFoundException;
    List<OrderSimpleVo> getOrderByUserId(Long id) throws UserIdGetException;
    OrderDetailedVo showOrderDetails(Long orderId) throws OrderDetailsNotFoundException;

}
