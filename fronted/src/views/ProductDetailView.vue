<!--<template>-->
<!--  <div class="detail-container">-->
<!--    &lt;!&ndash; 面包屑导航 &ndash;&gt;-->
<!--    <el-breadcrumb :separator-icon="ArrowRightIcon">-->
<!--      <el-breadcrumb-item :to="{ path: '/' }">商品列表</el-breadcrumb-item>-->
<!--      <el-breadcrumb-item v-if="productSpu">{{ productSpu.title }}</el-breadcrumb-item>-->
<!--      <el-breadcrumb-item v-else>商品详情</el-breadcrumb-item>-->
<!--    </el-breadcrumb>-->

<!--    <el-card class="detail-card" v-if="loading">-->
<!--      <el-skeleton :rows="10" animated />-->
<!--    </el-card>-->

<!--    <el-card class="detail-card" v-else-if="productSpu && skus.length > 0">-->
<!--      <el-row :gutter="40">-->
<!--        &lt;!&ndash; 左侧：图片画廊 &ndash;&gt;-->
<!--        <el-col :span="10">-->
<!--          <div class="gallery">-->
<!--            <div class="main-image-wrapper">-->
<!--              <el-image :src="mainImage" fit="contain" class="main-image" :preview-src-list="currentSkuImages" preview-teleported hide-on-click-modal />-->
<!--            </div>-->
<!--            <div class="thumbnail-wrapper" v-if="currentSkuImages.length > 1">-->
<!--              <div-->
<!--                v-for="(img, index) in currentSkuImages"-->
<!--                :key="index"-->
<!--                class="thumbnail-item"-->
<!--                :class="{ 'is-active': img === mainImage }"-->
<!--                @mouseover="changeMainImage(img)"-->
<!--              >-->
<!--                <el-image :src="img" fit="cover" class="thumbnail-image" lazy />-->
<!--              </div>-->
<!--            </div>-->
<!--          </div>-->
<!--        </el-col>-->

<!--        &lt;!&ndash; 右侧：商品信息与购买操作 &ndash;&gt;-->
<!--        <el-col :span="14">-->
<!--          <div class="product-info">-->
<!--            <h1 class="product-title">{{ selectedSku ? selectedSku.title : productSpu.title }}</h1>-->
<!--            <p class="description">-->
<!--              <span>SPU编码: {{ productSpu.spuNo }}</span>-->
<!--              <el-divider direction="vertical" />-->
<!--              <span>SKU ID: {{ selectedSku ? selectedSku.id : 'N/A' }}</span>-->
<!--            </p>-->

<!--            <div class="price-section">-->
<!--              <span class="label">价格:</span>-->
<!--              <span class="price">¥ {{ selectedSku ? (selectedSku.price || 0).toFixed(2) : '-&#45;&#45;' }}</span>-->
<!--            </div>-->

<!--            <el-divider />-->

<!--            <div class="spec-section">-->
<!--              <div v-for="specName in specKeys" :key="specName" class="spec-group">-->
<!--                <span class="label">{{ specName }}:</span>-->
<!--                <el-radio-group v-model="selectedSpec[specName]" @change="handleSpecChange(specName)" size="default">-->
<!--                  <el-radio-button-->
<!--                    v-for="value in specs[specName]"-->
<!--                    :key="value"-->
<!--                    :label="value"-->
<!--                    :disabled="isOptionDisabled(specName, value)"-->
<!--                  />-->
<!--                </el-radio-group>-->
<!--              </div>-->
<!--            </div>-->

<!--            <el-divider />-->

<!--            <div class="quantity-section">-->
<!--              <span class="label">数量:</span>-->
<!--              <el-input-number v-model="quantity" :min="1" :max="selectedSku ? selectedSku.stock : 1" :disabled="!isSkuSelected" />-->
<!--              <span class="stock-info" v-if="selectedSku">库存 {{ selectedSku.stock }} 件</span>-->
<!--            </div>-->


<!--            <div class="action-section">-->
<!--              <el-button-->
<!--                type="danger"-->
<!--                size="large"-->
<!--                :disabled="!isSkuSelected || (selectedSku && selectedSku.stock === 0)"-->
<!--                :icon="ShoppingCartIcon"-->
<!--                @click="handleAddToCart"-->
<!--              >-->
<!--              {{ selectedSku && selectedSku.stock === 0 ? '已售罄' : '加入购物车' }}-->
<!--              </el-button>-->
<!--            </div>-->
<!--          </div>-->
<!--        </el-col>-->
<!--      </el-row>-->
<!--    </el-card>-->

<!--    <el-empty v-else description="无法加载商品信息或该商品暂无规格" />-->
<!--  </div>-->
<!--</template>-->

<!--<script setup lang="ts">-->
<!--import { ref, onMounted, computed, watch, reactive, nextTick } from 'vue';-->
<!--import { useRoute } from 'vue-router';-->
<!--import type { ProductVo, ProductSkuVo } from '@/models/vo';-->
<!--import type { ApiResult } from '@/models/api';-->
<!--import { useCartStore } from '@/stores/ cart.ts'; // 1. 导入 cart store-->

<!--import api from '@/services/api';-->
<!--import { ElMessage } from 'element-plus';-->
<!--import { ArrowRight as ArrowRightIcon, ShoppingCart as ShoppingCartIcon } from '@element-plus/icons-vue';-->

<!--// -&#45;&#45; Props & State -&#45;&#45;-->
<!--const props = defineProps<{ spuId: string; }>();-->
<!--const route = useRoute();-->
<!--const loading = ref(true);-->
<!--const productSpu = ref<ProductVo | null>(null);-->
<!--const skus = ref<ProductSkuVo[]>([]);-->
<!--const specKeys = ref<string[]>([]);-->
<!--const specs = ref<Record<string, string[]>>({});-->
<!--const skuPathMap = ref<Record<string, ProductSkuVo>>({});-->
<!--const selectedSpec = reactive<Record<string, string | null>>({});-->
<!--const quantity = ref(1);-->
<!--const mainImage = ref('');-->
<!--const cartStore = useCartStore(); // 2. 获取 store 实例-->

<!--// -&#45;&#45; Computed -&#45;&#45;-->
<!--const selectedSku = computed(() => {-->
<!--  if (!specKeys.value || specKeys.value.length === 0) return null;-->
<!--  // 如果任意一个规格还没选择（null/undefined/'' 都视为未选），返回 null-->
<!--  for (const key of specKeys.value) {-->
<!--    const v = selectedSpec[key];-->
<!--    if (v === null || v === undefined || String(v).trim() === '') return null;-->
<!--  }-->
<!--  const currentPath = specKeys.value.map(key => String(selectedSpec[key])).join(';');-->
<!--  return skuPathMap.value[currentPath] || null;-->
<!--});-->

<!--const isSkuSelected = computed(() => selectedSku.value !== null);-->

<!--const currentSkuImages = computed(() => {-->
<!--  const imgsString = selectedSku.value?.images ?? productSpu.value?.mainImage ?? '';-->
<!--  if (!imgsString) return [];-->
<!--  return String(imgsString).split(',').map(s => s.trim()).filter(Boolean);-->
<!--});-->

<!--// -&#45;&#45; Watchers -&#45;&#45;-->
<!--watch(currentSkuImages, (newImages) => {-->
<!--  if (newImages && newImages.length > 0) {-->
<!--    mainImage.value = newImages[0];-->
<!--  } else if (productSpu.value) {-->
<!--    mainImage.value = productSpu.value.mainImage || '';-->
<!--  }-->
<!--}, { immediate: true });-->

<!--// -&#45;&#45; Methods -&#45;&#45;-->
<!--const parseSpecsAndBuildMap = () => {-->
<!--  const specsMap: Record<string, Set<string>> = {};-->
<!--  const pathMap: Record<string, ProductSkuVo> = {};-->

<!--  // 1) 收集所有 sku.spec 的 key（并集），避免只用 skus[0] 导致漏 key-->
<!--  const keySet = new Set<string>();-->
<!--  skus.value.forEach(sku => {-->
<!--    if (sku.spec && typeof sku.spec === 'object') {-->
<!--      Object.keys(sku.spec).forEach(k => keySet.add(k));-->
<!--    }-->
<!--  });-->
<!--  specKeys.value = Array.from(keySet);-->
<!--  specKeys.value.sort(); // 保持一个稳定顺序（你也可以按业务自定义顺序）-->

<!--  // 2) 构建每个 key 的可选值集合（并把值都转成字符串）-->
<!--  skus.value.forEach(sku => {-->
<!--    const path: string[] = [];-->
<!--    specKeys.value.forEach(key => {-->
<!--      const raw = sku.spec?.[key];-->
<!--      const value = raw !== undefined && raw !== null ? String(raw) : '';-->
<!--      path.push(value);-->
<!--      if (!specsMap[key]) specsMap[key] = new Set();-->
<!--      specsMap[key].add(value);-->
<!--    });-->
<!--    pathMap[path.join(';')] = sku;-->
<!--  });-->

<!--  // 3) 写入 specs & 初始化 selectedSpec（确保所有 key 在 reactive 对象上已存在）-->
<!--  Object.entries(specsMap).forEach(([key, set]) => {-->
<!--    specs.value[key] = Array.from(set);-->
<!--    // 初始化为 null（显式） — 这样后面赋值会触发响应-->
<!--    selectedSpec[key] = null;-->
<!--  });-->

<!--  skuPathMap.value = pathMap;-->
<!--};-->

<!--// 3. 创建一个新的方法来处理点击事件-->
<!--const handleAddToCart = () => {-->
<!--  if (!selectedSku.value) {-->
<!--    ElMessage.warning('请先选择完整的商品规格');-->
<!--    return;-->
<!--  }-->
<!--  cartStore.addItem(selectedSku.value, quantity.value);-->
<!--};-->


<!--const isOptionDisabled = (specName: string, specValue: string): boolean => {-->
<!--  // 如果存在一条 sku 能与当前其他已选项兼容且在该 spec 上等于 specValue，则该选项可用-->
<!--  for (const path in skuPathMap.value) {-->
<!--    const sku = skuPathMap.value[path];-->
<!--    if (String(sku.spec?.[specName]) === String(specValue)) {-->
<!--      const isCompatible = specKeys.value.every(key => {-->
<!--        const sel = selectedSpec[key];-->
<!--        // 未选择或当前是正在判断的规格，视为通过-->
<!--        if (sel === null || sel === undefined || key === specName) return true;-->
<!--        return String(sku.spec?.[key]) === String(sel);-->
<!--      });-->
<!--      if (isCompatible) return false;-->
<!--    }-->
<!--  }-->
<!--  return true;-->
<!--};-->

<!--const handleSpecChange = (changedSpecName: string) => {-->
<!--  quantity.value = 1;-->
<!--  // 如果用户改变了某项选择，重置与之不兼容的其他选择-->
<!--  specKeys.value.forEach(key => {-->
<!--    if (key === changedSpecName) return;-->
<!--    const cur = selectedSpec[key];-->
<!--    if (cur && isOptionDisabled(key, cur)) {-->
<!--      selectedSpec[key] = null;-->
<!--    }-->
<!--  });-->
<!--};-->

<!--const changeMainImage = (url: string) => {-->
<!--  mainImage.value = url;-->
<!--};-->

<!--/** 自动选择默认 SKU（更稳健的实现） */-->
<!--const selectDefaultSku = async () => {-->
<!--  if (!skus.value || skus.value.length === 0) return;-->

<!--  // 找到“最低价”的 SKU（对 price 做数值化处理并兜底）-->
<!--  const lowest = skus.value.reduce((prev, curr) => {-->
<!--    const pPrev = Number(prev?.price ?? Infinity);-->
<!--    const pCurr = Number(curr?.price ?? Infinity);-->
<!--    return pCurr < pPrev ? curr : prev;-->
<!--  }, skus.value[0]);-->

<!--  if (!lowest) return;-->

<!--  // 按照 specKeys 顺序逐项赋值（保证 selectedSpec 上的 key 与 specKeys 保持一致）-->
<!--  specKeys.value.forEach(key => {-->
<!--    const raw = lowest.spec?.[key];-->
<!--    selectedSpec[key] = raw !== undefined && raw !== null ? String(raw) : null;-->
<!--  });-->

<!--  quantity.value = 1;-->

<!--  // 等 Vue 更新计算属性和 watcher-->
<!--  await nextTick();-->

<!--  // 更新主图（如果有）-->
<!--  if (currentSkuImages.value && currentSkuImages.value.length > 0) {-->
<!--    mainImage.value = currentSkuImages.value[0];-->
<!--  } else if (productSpu.value) {-->
<!--    mainImage.value = productSpu.value.mainImage || '';-->
<!--  }-->
<!--};-->

<!--// -&#45;&#45; Lifecycle Hooks -&#45;&#45;-->
<!--onMounted(async () => {-->
<!--  loading.value = true;-->
<!--  try {-->
<!--    const state = window.history.state;-->
<!--    if (state && state.productData) {-->
<!--      productSpu.value = state.productData as ProductVo;-->
<!--    } else {-->
<!--      const spuResult = await api.get<any, ApiResult<ProductVo>>(`/product/spu/${props.spuId}`);-->
<!--      productSpu.value = spuResult.data;-->
<!--    }-->

<!--    if (!productSpu.value) throw new Error('无法获取商品信息');-->

<!--    const skuResult = await api.get<any, ApiResult<ProductSkuVo[]>>(`/product/sku/${props.spuId}`);-->
<!--    skus.value = skuResult.data || [];-->

<!--    if (skus.value.length > 0) {-->
<!--      parseSpecsAndBuildMap();-->
<!--      await nextTick(); // 保证 selectedSpec 初始化后再赋默认-->
<!--      await selectDefaultSku();-->
<!--    } else {-->
<!--      ElMessage.warning('该商品暂无可用规格');-->
<!--    }-->
<!--  } catch (error: any) {-->
<!--    ElMessage.error(error.message || '加载商品详情失败');-->
<!--    console.error(error);-->
<!--    productSpu.value = null;-->
<!--  } finally {-->
<!--    loading.value = false;-->
<!--  }-->
<!--});-->
<!--</script>-->
<template>
  <div class="detail-container">
    <!-- 面包屑导航 -->
    <el-breadcrumb :separator-icon="ArrowRightIcon">
      <el-breadcrumb-item :to="{ path: '/' }">商品列表</el-breadcrumb-item>
      <el-breadcrumb-item v-if="productSpu">{{ productSpu.title }}</el-breadcrumb-item>
      <el-breadcrumb-item v-else>商品详情</el-breadcrumb-item>
    </el-breadcrumb>

    <el-card class="detail-card" v-if="loading">
      <el-skeleton :rows="10" animated />
    </el-card>

    <el-card class="detail-card" v-else-if="productSpu && skus.length > 0">
      <el-row :gutter="40">
        <!-- 左侧：图片画廊 -->
        <el-col :span="10">
          <div class="gallery">
            <div class="main-image-wrapper">
              <el-image :src="mainImage" fit="contain" class="main-image" :preview-src-list="currentSkuImages" preview-teleported hide-on-click-modal />
            </div>
            <div class="thumbnail-wrapper" v-if="currentSkuImages.length > 1">
              <div
                v-for="(img, index) in currentSkuImages"
                :key="index"
                class="thumbnail-item"
                :class="{ 'is-active': img === mainImage }"
                @mouseover="changeMainImage(img)"
              >
                <el-image :src="img" fit="cover" class="thumbnail-image" lazy />
              </div>
            </div>
          </div>
        </el-col>

        <!-- 右侧：商品信息与购买操作 -->
        <el-col :span="14">
          <div class="product-info">
            <h1 class="product-title">{{ selectedSku ? selectedSku.title : productSpu.title }}</h1>
            <p class="description">
              <span>SPU编码: {{ productSpu.spuNo }}</span>
              <el-divider direction="vertical" />
              <span>SKU ID: {{ selectedSku ? selectedSku.id : 'N/A' }}</span>
            </p>

            <div class="price-section">
              <span class="label">价格:</span>
              <span class="price">¥ {{ selectedSku ? (selectedSku.price || 0).toFixed(2) : '---' }}</span>
            </div>

            <el-divider />

            <div class="spec-section">
              <div v-for="specName in specKeys" :key="specName" class="spec-group">
                <span class="label">{{ specName }}:</span>
                <el-radio-group v-model="selectedSpec[specName]" @change="handleSpecChange(specName)" size="default">
                  <el-radio-button
                    v-for="value in specs[specName]"
                    :key="value"
                    :label="value"
                    :disabled="isOptionDisabled(specName, value)"
                  />
                </el-radio-group>
              </div>
            </div>

            <el-divider />

            <div class="quantity-section">
              <span class="label">数量:</span>
              <el-input-number v-model="quantity" :min="1" :max="selectedSku ? selectedSku.stock : 1" :disabled="!isSkuSelected" />
              <span class="stock-info" v-if="selectedSku">库存 {{ selectedSku.stock }} 件</span>
            </div>

            <div class="action-section">
              <el-button
                type="danger"
                size="large"
                :disabled="!isSkuSelected || (selectedSku && selectedSku.stock === 0)"
                :icon="ShoppingCartIcon"
                @click="handleAddToCart"
              >
                {{ selectedSku && selectedSku.stock === 0 ? '已售罄' : '加入购物车' }}
              </el-button>
            </div>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <el-empty v-else description="无法加载商品信息或该商品暂无可用规格" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, watch, reactive, nextTick } from 'vue';
import { useRoute } from 'vue-router';
import type { ProductVo, ProductSkuVo } from '@/models/vo';
import type { ApiResult } from '@/models/api';
import api from '@/services/api';
import { ElMessage } from 'element-plus';
import { ArrowRight as ArrowRightIcon, ShoppingCart as ShoppingCartIcon } from '@element-plus/icons-vue';
import { useCartStore } from '@/stores/ cart.ts';

const route = useRoute();
const product = ref<ProductVo | null>(null);
// --- Props & State ---
const props = defineProps<{ spuId: string; }>();
const cartStore = useCartStore();
const loading = ref(true);
const productSpu = ref<ProductVo | null>(null);
const skus = ref<ProductSkuVo[]>([]);
const specKeys = ref<string[]>([]);
const specs = ref<Record<string, string[]>>({});
const skuPathMap = ref<Record<string, ProductSkuVo>>({});
const selectedSpec = reactive<Record<string, string | null>>({});
const quantity = ref(1);
const mainImage = ref('');

// --- Computed ---
const selectedSku = computed(() => {
  if (!specKeys.value || specKeys.value.length === 0) return null;
  for (const key of specKeys.value) {
    const v = selectedSpec[key];
    if (v === null || v === undefined || String(v).trim() === '') return null;
  }
  const currentPath = specKeys.value.map(key => String(selectedSpec[key])).join(';');
  return skuPathMap.value[currentPath] || null;
});

const isSkuSelected = computed(() => selectedSku.value !== null);

const currentSkuImages = computed(() => {
  const imgsString = selectedSku.value?.images ?? productSpu.value?.mainImage ?? '';
  if (!imgsString) return [];
  return String(imgsString).split(',').map(s => s.trim()).filter(Boolean);
});

// --- Watchers ---
watch(currentSkuImages, (newImages) => {
  if (newImages && newImages.length > 0) {
    mainImage.value = newImages[0];
  } else if (productSpu.value) {
    mainImage.value = productSpu.value.mainImage || '';
  }
}, { immediate: true });

watch(
  () => props.spuId,
  (newSpuId, oldSpuId) => {
    if (newSpuId && newSpuId !== oldSpuId) {
      fetchProductDetails(newSpuId);
    }
  }
);

// --- Methods ---
const parseSpecsAndBuildMap = () => {
  const specsMap: Record<string, Set<string>> = {};
  const pathMap: Record<string, ProductSkuVo> = {};
  const keySet = new Set<string>();

  skus.value.forEach(sku => {
    if (sku.spec && typeof sku.spec === 'object') {
      Object.keys(sku.spec).forEach(k => keySet.add(k));
    }
  });
  specKeys.value = Array.from(keySet).sort();

  skus.value.forEach(sku => {
    const path: string[] = [];
    specKeys.value.forEach(key => {
      const raw = sku.spec?.[key];
      const value = raw !== undefined && raw !== null ? String(raw) : '';
      path.push(value);
      if (!specsMap[key]) specsMap[key] = new Set();
      specsMap[key].add(value);
    });
    pathMap[path.join(';')] = sku;
  });

  Object.entries(specsMap).forEach(([key, set]) => {
    specs.value[key] = Array.from(set);
    selectedSpec[key] = null;
  });

  skuPathMap.value = pathMap;
};

const isOptionDisabled = (specName: string, specValue: string): boolean => {
  for (const path in skuPathMap.value) {
    const sku = skuPathMap.value[path];
    if (String(sku.spec?.[specName]) === String(specValue)) {
      const isCompatible = specKeys.value.every(key => {
        const sel = selectedSpec[key];
        if (sel === null || sel === undefined || key === specName) return true;
        return String(sku.spec?.[key]) === String(sel);
      });
      if (isCompatible) return false;
    }
  }
  return true;
};

const handleSpecChange = (changedSpecName: string) => {
  quantity.value = 1;
  specKeys.value.forEach(key => {
    if (key === changedSpecName) return;
    const cur = selectedSpec[key];
    if (cur && isOptionDisabled(key, cur)) {
      selectedSpec[key] = null;
    }
  });
};

const changeMainImage = (url: string) => {
  mainImage.value = url;
};

const selectDefaultSku = async () => {
  if (!skus.value || skus.value.length === 0) return;
  const lowest = skus.value.reduce((prev, curr) => {
    const pPrev = Number(prev?.price ?? Infinity);
    const pCurr = Number(curr?.price ?? Infinity);
    return pCurr < pPrev ? curr : prev;
  }, skus.value[0]);
  if (!lowest) return;
  specKeys.value.forEach(key => {
    const raw = lowest.spec?.[key];
    selectedSpec[key] = raw !== undefined && raw !== null ? String(raw) : null;
  });
  quantity.value = 1;
  await nextTick();
  if (currentSkuImages.value && currentSkuImages.value.length > 0) {
    mainImage.value = currentSkuImages.value[0];
  } else if (productSpu.value) {
    mainImage.value = productSpu.value.mainImage || '';
  }
};

const handleAddToCart = () => {
  if (!selectedSku.value) {
    ElMessage.warning('请先选择完整的商品规格');
    return;
  }
  cartStore.addItem(selectedSku.value, quantity.value);
};

const fetchProductDetails = async (id: string) => {
  loading.value = true;
  productSpu.value = null;
  skus.value = [];
  mainImage.value = '';
  Object.keys(selectedSpec).forEach(key => delete selectedSpec[key]);

  try {
    const spuResult = await api.get<any, ApiResult<ProductVo>>(`/product/spu/${id}`);
    if (!spuResult.data) throw new Error('无法获取商品信息');
    productSpu.value = spuResult.data;

    const skuResult = await api.get<any, ApiResult<ProductSkuVo[]>>(`/product/sku/${id}`);
    skus.value = skuResult.data || [];

    if (skus.value.length > 0) {
      parseSpecsAndBuildMap();
      await nextTick();
      await selectDefaultSku();
    } else {
      ElMessage.warning('该商品暂无可用规格');
    }
  } catch (error: any) {
    ElMessage.error(error.message || '加载商品详情失败');
    console.error(error);
  } finally {
    loading.value = false;
  }
};

// --- Lifecycle Hooks ---
onMounted(() => {
  fetchProductDetails(props.spuId);
});
// onMounted(async () => {
//   try {
//     const result = await api.get<any, ApiResult<ProductVo>>(`/product/spu/${route.params.spuId}`);
//     productSpu.value = result.data;
//   } catch (error) {
//     console.error('加载商品详情失败', error);
//   }
// });
</script>

<style scoped>
.detail-container {
  max-width: 1200px;
  margin: 20px auto;
  padding: 20px;
}
.detail-card {
  margin-top: 20px;
}
.gallery {
  display: flex;
  flex-direction: column;
}
.main-image-wrapper {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  margin-bottom: 15px;
  padding: 10px;
  background-color: #fff;
}
.main-image {
  width: 100%;
  height: 450px;
}
.thumbnail-wrapper {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  margin-top: 10px;
}
.thumbnail-item {
  width: 80px;
  height: 80px;
  border: 2px solid #e4e7ed;
  border-radius: 4px;
  cursor: pointer;
  transition: border-color 0.3s;
  padding: 2px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.thumbnail-item.is-active {
  border-color: #f56c6c;
}
.thumbnail-image {
  width: 100%;
  height: 100%;
}

.product-info {
  padding-left: 20px;
}
.product-title {
  font-size: 24px;
  font-weight: 700;
  margin-bottom: 10px;
  line-height: 1.3;
  color: #303133;
}
.description {
  color: #909399;
  font-size: 14px;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
}
.price-section {
  background-color: #fff3f3;
  padding: 20px;
  border-radius: 4px;
  display: flex;
  align-items: baseline;
}
.price-section .label {
  color: #606266;
  font-size: 16px;
}
.price-section .price {
  color: #f56c6c;
  font-size: 28px;
  font-weight: bold;
  margin-left: 10px;
}
.spec-section {
  margin: 20px 0;
}
.spec-group {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}
.spec-group .label {
  font-size: 16px;
  color: #606266;
  width: 80px;
  flex-shrink: 0;
}
.quantity-section {
  display: flex;
  align-items: center;
  gap: 15px;
  margin: 20px 0;
}
.quantity-section .label {
  font-size: 16px;
  color: #606266;
  width: 80px;
  flex-shrink: 0;
}
.stock-info {
  color: #909399;
  font-size: 14px;
}
.action-section {
  margin-top: 30px;
}
.action-section .el-button {
  width: 200px;
  font-size: 18px;
}
.el-radio-button.is-disabled :deep(.el-radio-button__inner) {
  background-color: #f5f7fa;
  border-color: #e4e7ed;
  color: #c0c4cc;
  cursor: not-allowed;
  box-shadow: none;
}
</style>

<!--<style scoped>-->
<!--/* 粘贴之前的 ProductDetailView.vue 的所有样式 */-->
<!--.detail-container { max-width: 1200px; margin: 20px auto; }-->
<!--.detail-card { margin-top: 20px; }-->
<!--.gallery { display: flex; flex-direction: column; }-->
<!--.main-image-wrapper { border: 1px solid #ebeef5; border-radius: 8px; margin-bottom: 15px; padding: 10px; }-->
<!--.main-image { width: 100%; height: 450px; }-->
<!--.thumbnail-wrapper { display: flex; gap: 10px; flex-wrap: wrap; margin-top:10px;}-->
<!--.thumbnail-item { width: 80px; height: 80px; border: 2px solid #e4e7ed; border-radius: 4px; cursor: pointer; transition: border-color 0.3s; padding: 2px; }-->
<!--.thumbnail-item.is-active { border-color: #f56c6c; }-->
<!--.thumbnail-image { width: 100%; height: 100%; }-->

<!--.product-info { padding-left: 20px; }-->
<!--.product-title { font-size: 24px; font-weight: 700; margin-bottom: 10px; line-height: 1.3; }-->
<!--.description { color: #909399; font-size: 14px; margin-bottom: 20px; display: flex; align-items: center;}-->
<!--.price-section { background-color: #fff3f3; padding: 20px; border-radius: 4px; display: flex; align-items: baseline; }-->
<!--.price-section .label { color: #606266; font-size: 16px; }-->
<!--.price-section .price { color: #f56c6c; font-size: 28px; font-weight: bold; margin-left: 10px; }-->
<!--.spec-section { margin: 20px 0; }-->
<!--.spec-group { display: flex; align-items: center; margin-bottom: 20px; }-->
<!--.spec-group .label { font-size: 16px; color: #606266; width: 80px; flex-shrink: 0; }-->
<!--.quantity-section { display: flex; align-items: center; gap: 15px; margin: 20px 0; }-->
<!--.quantity-section .label { font-size: 16px; color: #606266; width: 80px; flex-shrink: 0; }-->
<!--.stock-info { color: #909399; font-size: 14px; }-->
<!--.action-section { margin-top: 30px; }-->
<!--.action-section .el-button { width: 200px; font-size: 18px; }-->
<!--/* 为禁用的 el-radio-button 添加更明显的样式 */-->
<!--.el-radio-button.is-disabled :deep(.el-radio-button__inner) {-->
<!--  background-color: #f5f7fa;-->
<!--  border-color: #e4e7ed;-->
<!--  color: #c0c4cc;-->
<!--  cursor: not-allowed;-->
<!--  box-shadow: none;-->
<!--}-->
<!--</style>-->
