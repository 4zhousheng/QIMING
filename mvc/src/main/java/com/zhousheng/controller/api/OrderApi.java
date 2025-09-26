package com.zhousheng.controller.api;


import com.zhousheng.common.exception.*;
import com.zhousheng.common.result.Result;
import com.zhousheng.dto.OrderCreateDto;
import com.zhousheng.security.CustomUserDetails;
import com.zhousheng.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
@Slf4j
@RestController
@RequestMapping("/api/order")
public class OrderApi {
    private final OrderService orderService;
    OrderApi(OrderService orderService){
        this.orderService = orderService;
    }
    @RequestMapping("/createOrder")
    Result createOrder(@RequestBody OrderCreateDto orderCreateDto,
                       Authentication authentication){
        CustomUserDetails userDetails = (CustomUserDetails)authentication.getPrincipal();
        Long userId = userDetails.getUserId();
        try {
            orderService.createOrder(orderCreateDto,userId);
        } catch (CreateOrderFailException e) {
            log.error("创建订单发生错误");
            return Result.fail(500,"创建订单失败");
        } catch (InsufficientBalanceException e) {
            log.error("用户余额不足");
            return Result.fail(500,"余额不足，请先充值后再试");
        } catch (StockNotEnoughException e) {
            log.error("商品库存不足");
            return Result.fail(500,"商品库存不足");
        } catch (ProductSkuNotFoundException e) {
            log.error(e.getMessage());
            return Result.fail(418,"没有找到该productSku");
        }
        return Result.success("订单创建成功");
    }
    @GetMapping("/{orderId}/details")
    Result getOrderDetails(@PathVariable("orderId") Long orderId){
        try {
            return Result.success(orderService.showOrderDetails(orderId));
        } catch (OrderDetailsNotFoundException e) {
            log.error(e.getMessage());
            return Result.fail(500,"找不到order信息");
        }
    }
}
