<template>
  <div class="product-list-container">
    <!-- 搜索与排序区域 -->
    <el-card class="action-card" shadow="never">
      <el-row :gutter="20" align="middle">
        <!-- 搜索框 -->
        <el-col :span="18" :xs="24" :sm="18">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索商品标题或SPU编码..."
            size="large"
            clearable
            @clear="handleSearch"
            @keyup.enter="handleSearch"
          >
            <template #append>
              <el-button :icon="SearchIcon" @click="handleSearch" :loading="loading" />
            </template>
          </el-input>
        </el-col>
        <!-- 价格排序下拉框 -->
        <el-col :span="6" :xs="24" :sm="6">
          <el-select v-model="sortOrder" placeholder="排序方式" size="large" @change="handleSortChange" style="width: 100%;">
            <el-option label="默认排序" value="default" />
            <el-option label="价格从低到高" value="price_asc" />
            <el-option label="价格从高到低" value="price_desc" />
          </el-select>
        </el-col>
      </el-row>
    </el-card>

    <!-- 列表区域 -->
    <el-card class="table-card" shadow="never">
      <!-- 加载中的骨架屏 -->
      <el-skeleton :rows="5" animated v-if="loading" />

      <!-- 加载完成后的内容 -->
      <div v-else>
        <!-- 有数据时，显示卡片列表 -->
        <div v-if="cardData && cardData.length > 0">
          <el-row :gutter="20">
            <el-col
              v-for="product in cardData"
              :key="product.id"
              :xs="24" :sm="12" :md="8" :lg="6"
              class="card-col"
            >
              <router-link
                :to="{ name: 'product-detail', params: { spuId: product.id }, state: { productData: product } }"
                custom
                v-slot="{ navigate }"
              >
              <!--
                使用 div 包裹并添加点击事件和禁用 class
                这样做比用 <a> 标签更灵活
              -->
              <div @click="navigateToDetail(product)"
                   :class="['product-card-wrapper', { 'is-disabled': product.status === ProductStatus.OFF_SHELF }]">
                <el-card :body-style="{ padding: '0px' }" class="product-card" shadow="hover">
                  <div class="image-container">
                    <el-image :src="product.mainImage" fit="cover" class="product-image" lazy />
                    <!-- 已下架的遮罩层 -->
                    <div v-if="product.status === ProductStatus.OFF_SHELF" class="image-overlay">
                      <div class="overlay-content">
                        <el-icon :size="40"><ShopIcon /></el-icon>
                        <span>已下架</span>
                      </div>
                    </div>
                  </div>
                  <div class="card-content">
                    <div class="title-line">
                      <!-- “热卖中” 标签 -->
                      <el-tag v-if="product.status === ProductStatus.ON_SHELF" type="danger" effect="dark" size="small" class="hot-sale-tag">热卖中</el-tag>
                      <h3 class="product-title" :title="product.title">{{ product.title }}</h3>
                    </div>
                    <div class="bottom">
                      <span class="product-price">¥ {{ product.minPrice ? product.minPrice.toFixed(2) : '0.00' }}</span>
                      <el-button type="primary" link>查看详情</el-button>
                    </div>
                  </div>
                </el-card>
              </div>
              </router-link>
            </el-col>
          </el-row>

          <!-- 分页组件 -->
          <div class="pagination-container" v-if="pagination.total > 0">
            <el-pagination
              background
              layout="total, sizes, prev, pager, next, jumper"
              :total="pagination.total"
              :current-page="pagination.pageNum"
              :page-size="pagination.pageSize"
              :page-sizes="[12, 24, 36, 48]"
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
            />
          </div>
        </div>

        <!-- 没有数据时，显示空状态 -->
        <el-empty v-else description="暂无商品数据" />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import api from '@/services/api';
// 导入所有需要的图标
import { Search as SearchIcon, Picture as PictureIcon, Shop as ShopIcon } from '@element-plus/icons-vue';
import type { Page } from '@/models/page';
import type { ProductVo } from '@/models/vo';




// 使用常量/枚举定义状态，提高代码可读性和可维护性
const ProductStatus = {
  ON_SHELF: 0, // 0-上架(热卖中)
  OFF_SHELF: 1, // 1-已下架
};

const router = useRouter();

// --- 状态定义 ---
const loading = ref(true);
const searchKeyword = ref('');
const sortOrder = ref('default'); // 新增排序状态
const cardData = ref<ProductVo[]>([]);
const pagination = reactive({
  pageNum: 1,
  pageSize: 12,
  total: 0,
});

// --- API 调用 ---
const fetchProductList = async () => {
  loading.value = true;
  try {
    const queryDto = {
      pageNum: Number(pagination.pageNum),
      pageSize: Number(pagination.pageSize),
      keyword: searchKeyword.value,
      sort: sortOrder.value // 将排序参数传递给后端
    };
    const response = await api.post<any, Page<ProductVo>>('/product/list', queryDto);

    cardData.value = response.records || [];
    pagination.total = response.total || 0;
  } catch (error) {
    console.error("获取商品列表失败:", error);
    cardData.value = [];
    pagination.total = 0;
  } finally {
    loading.value = false;
  }
};

// --- 事件处理器 ---
const handleSearch = () => {
  pagination.pageNum = 1; // 每次搜索都回到第一页
  fetchProductList();
};

const handleSortChange = () => {
  // 改变排序方式本质上是一次新的搜索
  handleSearch();
};

const handleSizeChange = (newPageSize: number) => {
  pagination.pageNum = 1; // 改变每页数量时，也回到第一页
  pagination.pageSize = newPageSize;
  fetchProductList();
};

const handleCurrentChange = (newPageNum: number) => {
  pagination.pageNum = newPageNum;
  fetchProductList();
};



const navigateToDetail = (product: ProductVo) => {
  if (product.status === ProductStatus.ON_SHELF) {
    router.push({
      name: 'product-detail',
      params: { spuId: product.id.toString() },
      // state: { productData: product }
    });
  }
};
// --- 生命周期钩子 ---
onMounted(() => {
  fetchProductList();
});
</script>

<style scoped>
.product-list-container {
  padding: 20px;
}
.action-card {
  margin-bottom: 20px;
}
.card-col {
  margin-bottom: 20px;
}
.product-card-wrapper {
  cursor: pointer;
  display: block;
  border-radius: 8px; /* 将圆角和过渡应用到 wrapper 上 */
  overflow: hidden; /* 配合圆角 */
  transition: all 0.3s ease;
}
.product-card-wrapper:not(.is-disabled):hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 20px rgba(0,0,0,0.12);
}
/* 已下架商品的样式 */
.product-card-wrapper.is-disabled {
  cursor: not-allowed;
  pointer-events: none; /* 关键！禁止所有鼠标事件 */
}
.is-disabled .product-card {
  /* 让整个卡片视觉上变灰 */
  filter: grayscale(90%);
  opacity: 0.7;
}

.image-container {
  position: relative;
  width: 100%;
  padding-top: 100%;
  background-color: #f5f7fa;
}
.product-image {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
}
.image-slot {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
  color: #c0c4cc;
  font-size: 30px;
}
/* 已下架遮罩层 */
.image-overlay {
  position: absolute;
  top: 0; left: 0;
  width: 100%; height: 100%;
  background-color: rgba(0, 0, 0, 0.4);
  display: flex;
  justify-content: center;
  align-items: center;
}
.overlay-content {
  text-align: center;
  color: white;
}
.overlay-content .el-icon {
  display: block;
  margin: 0 auto 5px;
}

.card-content { padding: 14px; }
.title-line {
  display: flex;
  align-items: flex-start;
  margin-bottom: 8px;
  min-height: 48px; /* 给标题留出两行的高度 */
}
.hot-sale-tag { margin-right: 8px; flex-shrink: 0; }
.product-title {
  font-size: 16px;
  color: #303133;
  font-weight: 500;
  /* 限制标题最多显示两行，超出部分用省略号 */
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  line-height: 1.5;
}
.bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 10px;
}
.product-price { font-size: 20px; font-weight: bold; color: #ff4d00; }

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>
