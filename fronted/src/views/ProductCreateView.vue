<template>
  <div class="product-create-container">
    <el-card class="create-product-card">
      <template #header>
        <h1>创建新商品</h1>
      </template>

      <el-form :model="productForm" label-width="120px" ref="productFormRef">
        <el-divider content-position="left"><h3>1. 商品主体 (SPU) 信息</h3></el-divider>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="SPU 编码" prop="spuNo">
              <el-input v-model="productForm.spuNo" placeholder="请输入SPU编码" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="商品标题" prop="title">
              <el-input v-model="productForm.title" placeholder="请输入商品主标题" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="商品主图">
          <el-upload
            action="#"
            :auto-upload="false"
            :on-change="handleMainImageChange"
            :show-file-list="false"
          >
            <el-button type="primary">选择文件</el-button>
          </el-upload>
          <div v-if="mainImagePreview" class="image-preview">
            <el-image :src="mainImagePreview" fit="contain" style="width: 100px; height: 100px; margin-left: 10px;" />
          </div>
        </el-form-item>

        <el-divider content-position="left"><h3>2. 商品规格 (SKU) 列表</h3></el-divider>

        <div v-for="(sku, index) in productForm.productSkus" :key="index" class="sku-item">
          <h4>SKU #{{ index + 1 }}</h4>
          <el-row :gutter="20">
            <el-col :span="12"><el-form-item label="SKU 编码"><el-input v-model="sku.skuNo" placeholder="请输入SKU编码" /></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="SKU 标题"><el-input v-model="sku.title" placeholder="请输入SKU副标题" /></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="库存"><el-input-number v-model="sku.stock" :min="0" /></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="价格"><el-input-number v-model="sku.price" :precision="2" :step="0.1" :min="0" /></el-form-item></el-col>
            <el-col :span="24"><el-form-item label="规格 (JSON)"><el-input v-model="sku.spec" type="textarea" placeholder='例如: {"颜色": "蓝色", "内存": "256G"}' /></el-form-item></el-col>
          </el-row>
          <el-form-item label="SKU 图片">
            <el-upload
              action="#"
              multiple
              :auto-upload="false"
              list-type="picture-card"
              :on-change="createSkuImageChangeHandler(index)"
            >
              <el-icon><Plus /></el-icon>
            </el-upload>
          </el-form-item>
          <el-button type="danger" @click="removeSku(index)" style="margin-left: 120px;">删除此 SKU</el-button>
        </div>

        <el-form-item>
          <el-button @click="addSku">添加 SKU</el-button>
        </el-form-item>

        <el-divider />

        <el-form-item>
          <el-button type="success" @click="handleSubmit" :loading="isSubmitting" style="width: 100%; font-size: 18px;">提交创建商品</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, nextTick } from 'vue';
import api from '@/services/api';
import { ElMessage, type UploadFile, type UploadFiles, type FormInstance } from 'element-plus';
import { Plus } from '@element-plus/icons-vue';

// --- 接口定义 ---
interface SkuForm {
  skuNo: string;
  title: string;
  stock: number;
  price: number;
  spec: string;
  imageFiles: File[];
  imagesKey?: string[];
}

// --- 响应式状态 ---
const productFormRef = ref<FormInstance>();
const productForm = reactive({
  spuNo: '',
  title: '',
  mainImageFile: null as File | null,
  productSkus: [] as SkuForm[],
});
const mainImagePreview = ref('');
const isSubmitting = ref(false);

// --- 事件处理器 ---
const handleMainImageChange = (uploadFile: UploadFile) => {
  if (uploadFile.raw) {
    productForm.mainImageFile = uploadFile.raw;
    mainImagePreview.value = URL.createObjectURL(uploadFile.raw);
  }
};

const createSkuImageChangeHandler = (skuIndex: number) => {
  return (uploadFile: UploadFile, uploadFiles: UploadFiles) => {
    if (productForm.productSkus[skuIndex]) {
      productForm.productSkus[skuIndex].imageFiles = uploadFiles.map(f => f.raw).filter(f => f !== undefined) as File[];
    }
  };
};

const addSku = () => {
  productForm.productSkus.push({
    skuNo: `SKU-${Date.now()}`,
    title: '',
    stock: 100,
    price: 99.99,
    spec: '{"color": "默认"}',
    imageFiles: []
  });
};

const removeSku = (index: number) => {
  productForm.productSkus.splice(index, 1);
};

const handleSubmit = async () => {
  isSubmitting.value = true;
  try {
    // 1. 严格校验文件是否存在
    if (!productForm.mainImageFile) {
      throw new Error('请必须上传商品主图');
    }

    // 2. 上传主图
    const mainImageFormData = new FormData();
    // 【核心修正】这里的 'image' 必须和你后端 Controller 方法的参数名一致
    // @PostMapping("/uploadMainImage") Result<String> uploadMainImage(MultipartFile image)
    mainImageFormData.append('image', productForm.mainImageFile);
    console.log("准备上传mainImage");
    for (const entry of mainImageFormData.entries()) {
      // entry 是一个数组 [key, value]
      console.log("Key:", entry[0]);
      console.log("Value:", entry[1]); // 在这里展开 Value
    }
    console.log('即将 append 的文件对象:', productForm.mainImageFile);
// --- 在 handleSubmit 内，上传完图片后： ---
// 注意：我把 upload 返回先存到 respMain/respSku，然后取 resp.data || resp（兼容两种实现）
    const mainResp = await api.post('/product/uploadMainImage', mainImageFormData);
    const mainImageKey = (mainResp && (mainResp.data ?? mainResp)) as string;
    console.log('mainImageKey (resolved):', mainImageKey);

// 并行上传 SKU 图片并把返回的数组写回 sku.imagesKey
    const uploadPromises = productForm.productSkus.map(async (sku) => {
      if (!sku.imageFiles || sku.imageFiles.length === 0) {
        throw new Error(`SKU "${sku.title || sku.skuNo}" 必须上传图片`);
      }
      const skuImagesFormData = new FormData();
      sku.imageFiles.forEach(file => skuImagesFormData.append('images', file));
      const skuResp = await api.post('/product/uploadSkuImages', skuImagesFormData);
      sku.imagesKey = (skuResp && (skuResp.data ?? skuResp)) as string[];
      console.log(`sku ${sku.skuNo} imagesKey:`, sku.imagesKey);
    });

    await Promise.all(uploadPromises);

// 组装 DTO：把 spec 从字符串解析成对象（Map）
    const productCreateDto = {
      spuNo: productForm.spuNo,
      title: productForm.title,
      mainImageKey: mainImageKey,
      productSkus: productForm.productSkus.map(sku => {
        // 校验并解析 spec
        let specObj: Record<string, string> = {};
        try {
          if (typeof sku.spec === 'string') {
            specObj = JSON.parse(sku.spec);
          } else if (sku.spec && typeof sku.spec === 'object') {
            specObj = sku.spec as Record<string, string>;
          } else {
            specObj = {};
          }
        } catch (e: any) {
          throw new Error(`SKU "${sku.skuNo}" 的 spec 不是合法 JSON: ${e.message}`);
        }

        if (!sku.imagesKey || sku.imagesKey.length === 0) {
          throw new Error(`SKU "${sku.skuNo}" 的 imagesKey 为空，请先上传 SKU 图片并确认返回值`);
        }

        return {
          skuNo: sku.skuNo,
          title: sku.title,
          stock: sku.stock,
          price: sku.price,
          imagesKey: sku.imagesKey,
          spec: specObj // 注意：这里是 object (Map) —— 与后端 Map<String,String> 对齐
        };
      })
    };

// 可选：调试打印最终要发的 payload（非常重要）
    console.log('>>> final createProduct payload:', JSON.stringify(productCreateDto, null, 2));

    // 5. 提交最终数据
    await api.post('/product/createProduct', productCreateDto);

    ElMessage.success('商品创建成功！');
    resetForm();

  } catch (error: any) {
    ElMessage.error('操作失败: ' + (error.message || '未知错误'));
  } finally {
    isSubmitting.value = false;
  }
};

const resetForm = () => {
  productForm.spuNo = '';
  productForm.title = '';
  productForm.mainImageFile = null;
  productForm.productSkus = [];
  mainImagePreview.value = '';
  nextTick(() => {
    productFormRef.value?.resetFields();
  });
};
</script>

<style scoped>
/* 后台内容页面的标准布局样式 */
.product-create-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}
.create-product-card {
  /* ... */
}
.sku-item {
  border: 1px solid #e9e9eb;
  padding: 20px;
  margin-bottom: 20px;
  border-radius: 4px;
}
.image-preview .el-image {
  border: 1px solid #dcdfe6;
  border-radius: 6px;
}
</style>
