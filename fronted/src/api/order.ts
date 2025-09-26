// src/api/order.ts
import api, { type ApiResult } from '@/services/api';
// 从 store 中导入 CartItem 类型，因为它包含了我们需要的所有信息
import type { CartItem } from '@/stores/ cart.ts';

/**
 * @interface ProductSkuDto
 * @description 前端的数据结构，用于匹配后端的 ProductSkuDto。
 * skuId 是 number 类型，在JS中可以安全地表示Java的Long。
 */
interface ProductSkuDto {
  skuId: number;
  quantity: number;
}

/**
 * @interface OrderCreateDto
 * @description 前端的数据结构，用于匹配后端的 OrderCreateDto。
 * 它的核心是包含一个 ProductSkuDto 数组的 productSkuDtos 字段。
 */
interface OrderCreateDto {
  productSkuDtos: ProductSkuDto[];
}

/**
 * 创建订单
 * @param cartItems - 从 Pinia store 中获取的购物车商品列表
 * @returns 返回一个 Promise，其中包含API的响应结果
 */
export const createOrder = (cartItems: CartItem[]): Promise<ApiResult<string>> => {
  // 1. 根据后端的 DTO 结构，构建请求体对象
  const orderDto: OrderCreateDto = {
    // 2. 字段名从 'items' 修改为 'productSkuDtos'
    productSkuDtos: cartItems.map(item => ({
      // 3. DTO 内部字段名确认是 'skuId' 和 'quantity'，与 CartItem 结构一致
      skuId: item.skuId,
      quantity: item.quantity
    }))
  };

  // 4. 发送 POST 请求，将构建好的 orderDto 作为请求体
  // axios 会自动将这个JS对象序列化为 JSON 字符串
  return api.post('/order/createOrder', orderDto);
};
