<!-- src/components/CartDetailDrawer.vue -->
<template>
  <el-drawer
    :model-value="modelValue"
    @update:modelValue="$emit('update:modelValue', $event)"
    title="购物车详情"
    direction="rtl"
    size="400px"
    :with-header="true"
  >
    <div class="cart-drawer-content">
      <el-empty v-if="cartStore.isEmpty" description="购物车还是空的呢" />

      <div v-else class="cart-items-list">
        <el-scrollbar>
          <div v-for="item in cartStore.itemList" :key="item.skuId" class="cart-item">
            <el-image :src="item.image" fit="cover" class="item-image" />
            <div class="item-info">
              <p class="item-title">{{ item.title }}</p>
              <div class="item-controls">
                <span class="item-price">¥ {{ item.price.toFixed(2) }}</span>
                <el-input-number
                  :model-value="item.quantity"
                  @change="(currentValue) => cartStore.updateItemQuantity(item.skuId, currentValue)"
                  :min="1"
                  :max="item.stock"
                  size="small"
                  controls-position="right"
                  style="width: 100px"
                />
              </div>
            </div>
            <el-button
              type="danger"
              :icon="Delete"
              link
              @click="cartStore.removeItem(item.skuId)"
              class="remove-btn"
            />
          </div>
        </el-scrollbar>
      </div>
    </div>
  </el-drawer>
</template>

<script setup lang="ts">
import { useCartStore } from '@/stores/ cart.ts';
import { Delete } from '@element-plus/icons-vue';

defineProps<{ modelValue: boolean }>();
defineEmits(['update:modelValue']);

const cartStore = useCartStore();
</script>

<style scoped>
.cart-drawer-content {
  height: 100%;
  display: flex;
  flex-direction: column;
}
.cart-items-list {
  flex-grow: 1;
  overflow: hidden;
}
.cart-item {
  display: flex;
  align-items: center;
  padding: 15px 0;
  border-bottom: 1px solid #e9e9eb;
  position: relative;
}
.item-image {
  width: 80px;
  height: 80px;
  border-radius: 4px;
  flex-shrink: 0;
}
.item-info {
  flex-grow: 1;
  margin-left: 15px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  height: 80px;
}
.item-title {
  font-size: 14px;
  color: #303133;
  margin: 0;
  line-height: 1.4;
  /* 最多显示两行 */
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}
.item-controls {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: auto;
}
.item-price {
  font-size: 16px;
  font-weight: bold;
  color: #f56c6c;
}
.remove-btn {
  margin-left: 10px;
}
</style>
