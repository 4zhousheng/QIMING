package com.zhousheng.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhousheng.domain.OrderDo;
import com.zhousheng.domain.OrderItemDo;
import com.zhousheng.dto.OrderCreateDto;
import com.zhousheng.vo.OrderDetailedVo;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
public interface OrderMapper extends BaseMapper<OrderDo> {

    @Select("""
        SELECT
            id,order_no,user_id,total_amount,status,ID, ORDER_NO, USER_ID, TOTAL_AMOUNT, STATUS, IS_DELETED, CREATE_TIME, UPDATE_TIME
        FROM orders
        WHERE user_id=#{userId}
""")
    List<OrderDo> getOrderByUserId(@Param("userId") Long id);

    @Insert("""
        INSERT INTO 
        orders(id, order_no, user_id, total_amount)
        VALUES
        (#{id},#{orderNo},#{userId},#{totalAmount})
""")
    int addOrder(OrderDo order);

    @Insert("""
        INSERT INTO
        order_item(id, order_id, sku_id, sku_title, sku_price, quantity, create_time, update_time) 
        VALUES(#{id},#{orderId},#{skuId},#{skuTitle},#{skuPrice},#{quantity},#{createTime},#{updateTime})
""")
    int addOrderDetail(OrderItemDo orderItemDo);
//    int addOrderItems(Long orderId, OrderCreateDto orderCreateDto);
    OrderDetailedVo getOrderDetailedVoWithOrderId(Long orderId);
}

