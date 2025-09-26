// src/stores/cart.ts
import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import type { ProductSkuVo } from '@/models/vo';
import { ElMessage } from 'element-plus';

// 定义购物车中单个商品的类型接口
export interface CartItem {
  skuId: number;
  quantity: number;
  title: string;
  price: number;
  image: string; // 我们只存一张主图用于展示
  stock: number;
}

export const useCartStore = defineStore('cart', () => {
  // --- State ---
  // 使用 Map 来存储购物车商品，key 为 skuId，便于快速增删改查
  const items = ref<Map<number, CartItem>>(new Map());

  // --- Getters (Computed) ---
  // 购物车中的商品列表（数组形式，便于渲染）
  const itemList = computed(() => Array.from(items.value.values()));

  // 购物车是否为空
  const isEmpty = computed(() => items.value.size === 0);

  // 购物车商品总数
  const totalQuantity = computed(() => {
    return Array.from(items.value.values()).reduce((sum, item) => sum + item.quantity, 0);
  });

  // 购物车总价
  const totalPrice = computed(() => {
    const total = Array.from(items.value.values()).reduce((sum, item) => sum + item.price * item.quantity, 0);
    return total.toFixed(2);
  });

  // --- Actions ---
  /**
   * 添加商品到购物车
   * @param sku - 要添加的商品 SKU 对象
   * @param quantity - 添加的数量
   */
  function addItem(sku: ProductSkuVo, quantity: number) {
    if (!sku || !sku.id) {
      ElMessage.error('无效的商品信息');
      return;
    }

    if (items.value.has(sku.id)) {
      // 如果购物车中已存在，则增加数量
      const existingItem = items.value.get(sku.id)!;
      const newQuantity = existingItem.quantity + quantity;

      if (newQuantity > sku.stock) {
        ElMessage.warning(`商品 "${sku.title}" 库存不足，购物车中最多只能添加 ${sku.stock} 件`);
        existingItem.quantity = sku.stock;
      } else {
        existingItem.quantity = newQuantity;
        ElMessage.success({
          message: `已将 ${quantity} 件 "${sku.title}" 添加到购物车`,
          duration: 1500,
        });
      }
    } else {
      // 如果是新商品，则创建
      if (quantity > sku.stock) {
        ElMessage.warning(`商品 "${sku.title}" 库存不足，最多只能添加 ${sku.stock} 件`);
        quantity = sku.stock;
      }

      const mainImage = sku.images?.split(',')[0]?.trim() || ''; // 取第一张图
      items.value.set(sku.id, {
        skuId: sku.id,
        quantity,
        title: sku.title,
        price: sku.price,
        image: mainImage,
        stock: sku.stock
      });
      ElMessage.success({
        message: `成功将 "${sku.title}" 加入购物车`,
        duration: 1500,
      });
    }
  }

  /**
   * 更新购物车中某个商品的数量
   * @param skuId - 商品 SKU ID
   * @param quantity - 新的数量
   */
  function updateItemQuantity(skuId: number, quantity: number) {
    if (items.value.has(skuId)) {
      const item = items.value.get(skuId)!;
      if (quantity > 0 && quantity <= item.stock) {
        item.quantity = quantity;
      } else if (quantity > item.stock) {
        ElMessage.warning(`库存不足，最多只能添加 ${item.stock} 件`);
        item.quantity = item.stock;
      } else {
        // 如果数量小于等于 0，则移除
        removeItem(skuId);
      }
    }
  }

  /**
   * 从购物车中移除一个商品
   * @param skuId - 商品 SKU ID
   */
  function removeItem(skuId: number) {
    if (items.value.has(skuId)) {
      items.value.delete(skuId);
      ElMessage.info('已从购物车中移除一件商品');
    }
  }

  /**
   * 清空购物车
   */
  function clearCart() {
    items.value.clear();
    ElMessage.success('购物车已清空');
  }

  return {
    items,
    itemList,
    isEmpty,
    totalQuantity,
    totalPrice,
    addItem,
    updateItemQuantity,
    removeItem,
    clearCart,
  }
});
