package com.zhousheng.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhousheng.common.exception.*;
import com.zhousheng.domain.OrderDo;
import com.zhousheng.domain.OrderItemDo;
import com.zhousheng.domain.ProductSkuDo;
import com.zhousheng.dto.OrderCreateDto;
import com.zhousheng.dto.ProductSkuDto;
import com.zhousheng.mapper.OrderItemMapper;
import com.zhousheng.mapper.OrderMapper;
import com.zhousheng.mapper.ProductSkuMapper;
import com.zhousheng.service.IOrderItemService;
import com.zhousheng.service.OrderService;
import com.zhousheng.service.ProductSkuService;
import com.zhousheng.service.UserService;
import com.zhousheng.vo.OrderDetailedVo;
import com.zhousheng.vo.OrderSimpleVo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl
        extends ServiceImpl<OrderMapper,OrderDo>
        implements OrderService {
    // 格式化器，用来生成orderNo
    private static final DateTimeFormatter ORDER_NO_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
    private final OrderMapper orderMapper;
    private final ProductSkuMapper productSkuMapper;
    private final UserService userService;

    private final ProductSkuService productSkuService;
    private final IOrderItemService orderItemService;
    OrderServiceImpl(OrderMapper orderMapper,
                     ProductSkuMapper productSkuMapper,
                     UserService userService,
                     ProductSkuService productSkuService,
                     OrderItemServiceImpl orderItemService){
        this.orderMapper=orderMapper;
        this.productSkuMapper = productSkuMapper;
        this.userService = userService;
        this.productSkuService = productSkuService;
        this.orderItemService = orderItemService;
    }
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void createOrder(OrderCreateDto orderDto, Long userId) throws CreateOrderFailException, InsufficientBalanceException, StockNotEnoughException, ProductSkuNotFoundException {
        OrderDo orderDo = new OrderDo();
//        获取prodctSku来计算总金额
        List<ProductSkuDto> productSkuDtos = orderDto.getProductSkuDtos();
        List<Long> skuIdList = productSkuDtos.stream().map(ProductSkuDto::getSkuId).collect(Collectors.toList());
        if(skuIdList.isEmpty()){
            throw new ProductSkuNotFoundException("没有找到对应的productSku,请检查是否skuId正确");
        }
//        计算总金额
        List<ProductSkuDo> productSkuDos = productSkuMapper.selectBatchIds(skuIdList);
        Map<Long,BigDecimal> priceMap = productSkuDos.stream().collect(Collectors.toMap(ProductSkuDo::getId,ProductSkuDo::getPrice));
//        Map<Long,String> titleMap = productSkuDos.stream().collect(Collectors.toMap(ProductSkuDo::getId,ProductSkuDo::getTitle));
        BigDecimal totalAmount = BigDecimal.ZERO;
        for(var productSkuDto : productSkuDtos){
            Long skuId = productSkuDto.getSkuId();
//            OrderItemDo orderItemDo = new OrderItemDo();//这个用来插入order item记录 中的price和quantity避免重复获取
            BigDecimal price = priceMap.get(skuId);
            Integer intQuantity = productSkuDto.getQuantity();

            //计算某个sku的金额
            if(price != null){
                BigDecimal quantity = new BigDecimal(intQuantity);
                price = price.multiply(quantity);
            }
            totalAmount = totalAmount.add(price);
        }

//        插入order记录
        orderDo.setTotalAmount(totalAmount);
        orderDo.setUserId(userId);
        orderDo.setOrderNo(generateOrderNo());
        int affectedRows = orderMapper.insert(orderDo);
        if(affectedRows==0){
            throw new CreateOrderFailException("订单创建失败");
        }
        //更新order_item
        Long orderId = orderDo.getId(); //获取订单id
        orderItemService.createOrderItems(orderId,orderDto);
            //更新订单id
//        for(var orderItem : orderItemDoList) {
//            orderItem.setOrderId(orderId);
//        }
//        boolean updateOrderItem = orderItemService.saveBatch(orderItemDoList);
//        if(!updateOrderItem){
//            throw new CreateOrderFailException("更新orderItem失败");
//        }


//        扣用户钱
        userService.payMoney(userId,totalAmount);
//      扣商品库存
        productSkuService.deductStock(orderDto);
    }
    private String generateOrderNo() {
        // 1. 获取当前时间戳，精确到毫秒
        String timestamp = LocalDateTime.now().format(ORDER_NO_FORMATTER);

        // 2. 生成一个两位数的随机数 (00-99)
        int randomNum = ThreadLocalRandom.current().nextInt(100);
        String randomStr = String.format("%02d", randomNum); // 格式化为两位，不足补零
        // 3. 拼接成最终的订单号
        return timestamp + randomStr;
    }
    @Override
    public OrderDetailedVo showOrderDetails(Long orderId) throws OrderDetailsNotFoundException {
        OrderDetailedVo orderDetailedVo = orderMapper.getOrderDetailedVoWithOrderId(orderId);
        if(orderDetailedVo != null){
            return orderDetailedVo;
        }else {
            throw new OrderDetailsNotFoundException("没有找到对应order的信息，请检查orderId");
        }
    }

    @Override
    public List<OrderSimpleVo> getOrderByUserId(Long userId) throws UserIdGetException {
        if(userId == null || userId.longValue() < 0){
            throw new UserIdGetException("用户id不存在或错误");
        }
        List<OrderSimpleVo> result = new ArrayList<>();
        LambdaQueryWrapper<OrderDo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .eq(OrderDo::getUserId,userId)
                .orderByDesc(OrderDo::getCreateTime);
        List<OrderDo> orderDoList = orderMapper.selectList(queryWrapper);
        for(var orderDo : orderDoList){
            OrderSimpleVo orderSimpleVo = new OrderSimpleVo();
            BeanUtils.copyProperties(orderDo,orderSimpleVo);
            result.add(orderSimpleVo);
        }
        return result;
    }


}
