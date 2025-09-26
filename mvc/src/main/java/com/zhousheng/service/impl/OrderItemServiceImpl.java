package com.zhousheng.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhousheng.common.exception.CreateOrderFailException;
import com.zhousheng.domain.OrderItemDo;
import com.zhousheng.domain.ProductSkuDo;
import com.zhousheng.dto.OrderCreateDto;
import com.zhousheng.dto.ProductSkuDto;
import com.zhousheng.mapper.OrderItemMapper;
import com.zhousheng.mapper.ProductSkuMapper;
import com.zhousheng.service.IOrderItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrderItemServiceImpl
        extends ServiceImpl<OrderItemMapper, OrderItemDo>
        implements IOrderItemService {
    @Autowired
    ProductSkuMapper productSkuMapper;

    /**
     * 插入订单对应商品信息
     * @param orderId
     * @param orderCreateDto
     */
    public void createOrderItems(Long orderId, OrderCreateDto orderCreateDto) throws CreateOrderFailException {
        List<ProductSkuDto> productSkuDtos =
                orderCreateDto.getProductSkuDtos();
//注释掉这两句，性能不行
//        List<Long> skuIdList = productSkuDtos.stream().map(ProductSkuDto::getSkuId).collect(Collectors.toList());//获取商品sku信息方便计算
//        Map<Long, Integer> skuQuantityMap = productSkuDtos.stream().collect(Collectors.toMap(productSku -> productSku.getSkuId(), productSkuDto -> productSkuDto.getQuantity()));
        List<Long> skuIdList = new ArrayList<>();
        Map<Long,Integer> skuQuantityMap = new HashMap<>();
        for(var productSkuDto : productSkuDtos){
            skuIdList.add(productSkuDto.getSkuId());
            skuQuantityMap.put(productSkuDto.getSkuId(),productSkuDto.getQuantity());
        }

        List<ProductSkuDo> productSkuDos = productSkuMapper.selectBatchIds(skuIdList);
        List<OrderItemDo> orderItemDoList = productSkuDos.stream().map(productSkuDo -> {
            OrderItemDo orderItemDo = new OrderItemDo();
            orderItemDo.setSkuId(productSkuDo.getId());
            orderItemDo.setOrderId(orderId);
            orderItemDo.setSkuTitle(productSkuDo.getTitle());
            orderItemDo.setQuantity(skuQuantityMap.get(productSkuDo.getId()));
            orderItemDo.setSkuPrice(productSkuDo.getPrice());
            return orderItemDo;
        }).collect(Collectors.toList());
        boolean saveOrderItemsSuccess = this.saveBatch(orderItemDoList);
        if(!saveOrderItemsSuccess){
            throw new CreateOrderFailException("创建订单失败，失败原因，商品订单信息有误！");
        }

    }
}
