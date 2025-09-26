<template>
  <el-dialog
    v-model="visible"
    :title="title"
    width="80%"
    top="5vh"
    destroy-on-close
    class="sku-dialog"
  >
    <!-- 商品头部信息 -->
    <div v-if="product" class="sku-header">
      <div class="product-info">
        <el-image
          :src="product.mainImage"
          fit="cover"
          class="product-thumbnail"
        />
        <div class="product-details">
          <h3>{{ product.title }}</h3>
          <div class="spu-info">
            <el-tag type="info">SPU: {{ product.spuNo }}</el-tag>
            <el-tag :type="getStatusTagType(product.status)" effect="plain">
              {{ getStatusText(product.status) }}
            </el-tag>
          </div>
          <div class="price-display">
            <span class="price-label">最低价：</span>
            <span class="price-value">¥ {{ product.minPrice?.toFixed(2) }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- SKU列表表格 -->
    <el-table
      :data="skus"
      style="width: 100%"
      row-key="id"
      stripe
      border
      v-loading="loading"
    >
      <el-table-column prop="skuNo" label="SKU编号" width="200" />
      <el-table-column label="规格">
        <template #default="{ row }">
          <div v-if="row.specs" class="specs-container">
            <el-tag
              v-for="(value, key) in row.specs"
              :key="key"
              type="info"
              class="spec-tag"
            >
              {{ key }}: {{ value }}
            </el-tag>
          </div>
          <span v-else>无规格</span>
        </template>
      </el-table-column>
      <el-table-column label="价格" width="150">
        <template #default="{ row }">
          <span class="price-cell">¥ {{ row.price?.toFixed(2) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="stock" label="库存" width="120" align="center">
        <template #default="{ row }">
          <span :class="{'low-stock': row.stock < 10}">{{ row.stock }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="120" align="center">
        <template #default="{ row }">
          <el-tag
            :type="row.status === 1 ? 'success' : 'danger'"
            effect="light"
            size="small"
          >
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150" align="center">
        <template #default="{ row }">
          <el-button type="primary" size="small" plain @click="handleEditSku(row)">编辑</el-button>
          <el-button type="danger" size="small" plain @click="handleDeleteSku(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 对话框底部 -->
    <template #footer>
      <el-button @click="close">关闭</el-button>
      <el-button type="primary" @click="handleAddSku">添加 SKU</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import type { ProductVo, ProductSkuVo } from '@/models/vo';
import {ElMessage} from "element-plus";

const props = defineProps<{
  product: ProductVo | null;
  skus: ProductSkuVo[];
}>();

const emit = defineEmits(['add-sku', 'edit-sku', 'delete-sku']);

// 对话框可见性
const visible = ref(false);
const loading = ref(false);

// 计算对话框标题
const title = computed(() => {
  return props.product
    ? `商品规格管理 - ${props.product.title}`
    : '商品规格管理';
});

// 打开对话框方法
const open = () => {
  visible.value = true;
};

// 关闭对话框方法
const close = () => {
  visible.value = false;
};

// 添加SKU处理函数
const handleAddSku = () => {
  if (props.product) {
    emit('add-sku', props.product);
  } else {
    ElMessage.warning('未选择商品');
  }
};

// 编辑SKU处理函数
const handleEditSku = (sku: ProductSkuVo) => {
  emit('edit-sku', sku);
};

// 删除SKU处理函数
const handleDeleteSku = (sku: ProductSkuVo) => {
  emit('delete-sku', sku);
};

// 获取状态文本
const getStatusText = (status: number | undefined) => {
  if (status === undefined) return '未知状态';
  switch (status) {
    case 1: return '在售中';
    case 0: return '已下架';
    case 2: return '缺货中';
    default: return '未知状态';
  }
};

// 获取状态标签类型
const getStatusTagType = (status: number | undefined) => {
  if (status === undefined) return 'info';
  switch (status) {
    case 1: return 'success';
    case 0: return 'info';
    case 2: return 'warning';
    default: return 'info';
  }
};

// 暴露方法给父组件
defineExpose({
  open,
  close
});
</script>

<style scoped lang="scss">
.sku-dialog {
  border-radius: 15px;
  overflow: hidden;

  .el-dialog__header {
    background: linear-gradient(to right, #f0f7ff, #e6f7ff);
    border-bottom: 1px solid #e4e7ed;
    margin: 0;
    padding: 16px 24px;
  }

  .el-dialog__title {
    font-weight: 600;
    font-size: 18px;
    color: #409eff;
  }
}

.sku-header {
  padding: 15px 0;
  margin-bottom: 15px;
  border-bottom: 1px dashed #e4e7ed;
}

.product-info {
  display: flex;
  align-items: center;
  gap: 20px;
}

.product-thumbnail {
  width: 100px;
  height: 100px;
  border-radius: 8px;
  border: 1px solid #ebeef5;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.product-details {
  flex: 1;

  h3 {
    font-size: 18px;
    font-weight: 600;
    margin-bottom: 10px;
    color: #303133;
  }
}

.spu-info {
  display: flex;
  gap: 10px;
  margin-bottom: 15px;
}

.price-display {
  display: flex;
  align-items: center;
  gap: 8px;

  .price-label {
    font-size: 14px;
    color: #606266;
  }

  .price-value {
    font-size: 20px;
    font-weight: bold;
    color: #ff4d00;
  }
}

.specs-container {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.spec-tag {
  margin-right: 5px;
  margin-bottom: 5px;
}

.price-cell {
  font-weight: bold;
  color: #ff4d00;
}

.low-stock {
  color: #f56c6c;
  font-weight: bold;
}
</style>
