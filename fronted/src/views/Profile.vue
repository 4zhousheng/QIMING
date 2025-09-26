<template>
  <div class="profile-page">
    <el-card class="profile-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>个人中心</span>
        </div>
      </template>

      <div class="profile-content">
        <!-- 头像设置 -->
        <div class="avatar-section">
          <el-upload
            class="avatar-uploader"
            action="#"
            :show-file-list="false"
            :before-upload="handleBeforeAvatarUpload"
            :http-request="handleAvatarUpload"
          >
            <!-- 修正点 3: :src 的备用值为云端默认头像 URL -->
            <el-avatar :size="120" :src="userInfo.avatar || defaultAvatarUrl" class="user-avatar">
              <!-- 插槽中的 img 也使用云端 URL，保证显示一致 -->
              <img :src="defaultAvatarUrl" />
            </el-avatar>
            <div class="avatar-edit-mask">
              <el-icon><Edit /></el-icon>
              <span>更换头像</span>
            </div>
          </el-upload>
        </div>

        <!-- ... 其他 template 内容保持不变 ... -->

        <!-- 昵称设置 -->
        <div class="info-item nickname-section">
          <span class="info-label">用户昵称:</span>
          <div class="info-value" v-if="!isEditingNickname">
            <span>{{ userInfo.nickname || '未设置' }}</span>
            <el-button type="primary" link @click="isEditingNickname = true" class="edit-btn">
              <el-icon><Edit /></el-icon>
            </el-button>
          </div>
          <div class="info-value" v-else>
            <el-input v-model="newNickname" placeholder="请输入新昵称" size="small" style="width: 150px;"></el-input>
            <el-button type="primary" size="small" @click="handleSetNickname" :loading="nicknameLoading">保存</el-button>
            <el-button size="small" @click="isEditingNickname = false">取消</el-button>
          </div>
        </div>

        <!-- 用户名 -->
        <div class="info-item">
          <span class="info-label">用户名:</span>
          <span class="info-value">{{ userInfo.username }}</span>
        </div>

        <!-- 注册时间 -->
        <div class="info-item">
          <span class="info-label">注册时间:</span>
          <span class="info-value">{{ formattedCreateTime }}</span>
        </div>

        <!-- 余额显示 -->
        <div class="info-item balance-section">
          <span class="info-label">账户余额:</span>
          <span class="balance-value">¥ {{ userInfo.money?.toFixed(2) || '0.00' }}</span>
        </div>

        <!-- 充值入口 -->
        <div class="recharge-section">
          <el-button type="primary" plain @click="rechargeDialogVisible = true" size="large">
            <el-icon><Wallet /></el-icon>
            <span>立即充值</span>
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 充值弹窗 -->
    <el-dialog
      v-model="rechargeDialogVisible"
      title="账户充值"
      width="30%"
      :before-close="handleCloseRechargeDialog"
      center
    >
      <div class="recharge-dialog-content">
        <el-input-number
          v-model="rechargeAmount"
          :min="1"
          :precision="2"
          :step="10"
          controls-position="right"
          size="large"
          placeholder="请输入充值金额"
        />
        <p class="recharge-tip">请输入您希望充值的金额</p>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="rechargeDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleRecharge" :loading="rechargeLoading">
            确认充值
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { ElMessage } from 'element-plus';
import type { UploadProps, UploadRequestOptions, UploadRawFile } from 'element-plus';
import { getUserInfo, setNickname, setAvatar, recharge, type UserDetailVo } from '@/api/user';
import { Edit, Wallet } from '@element-plus/icons-vue';

// 修正点 1: 删除本地图片导入
// import defaultAvatar from '@/assets/default_avatar.png'; // <--- 已删除

// 修正点 2: 定义云端的默认头像URL
// 请将下面的 URL 替换为您自己存储在云端的默认头像地址
const defaultAvatarUrl = ref('https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png');


// --- 类型定义 ---
const userInfo = ref<UserDetailVo>({
  username: '',
  nickname: '',
  avatar: '', // avatar 字段现在会是 null 或者一个完整的 URL
  createTime: '',
  money: 0
});

// --- 响应式状态 ---
const isEditingNickname = ref(false);
const newNickname = ref('');
const nicknameLoading = ref(false);

const rechargeDialogVisible = ref(false);
const rechargeAmount = ref(10);
const rechargeLoading = ref(false);

// --- 计算属性 ---
const formattedCreateTime = computed(() => {
  if (!userInfo.value.createTime) return 'N/A';
  return new Date(userInfo.value.createTime).toLocaleString();
});

// --- 生命周期钩子 ---
onMounted(() => {
  fetchUserInfo();
});

// --- 方法 (逻辑保持不变，但现在处理的是 URL) ---
const fetchUserInfo = async () => {
  try {
    const res = await getUserInfo();
    if (res.code === 200) {
      userInfo.value = res.data;
    } else {
      ElMessage.error(res.message || '获取用户信息失败');
    }
  } catch (error) {
    console.error("获取用户信息异常:", error);
  }
};

const handleSetNickname = async () => {
  if (!newNickname.value || newNickname.value.trim() === '') {
    ElMessage.warning('昵称不能为空');
    return;
  }
  nicknameLoading.value = true;
  try {
    const res = await setNickname(newNickname.value);
    if (res.code === 200) {
      ElMessage.success('昵称设置成功');
      userInfo.value.nickname = newNickname.value;
      isEditingNickname.value = false;
    } else {
      ElMessage.error(res.message || '设置昵称失败');
    }
  } catch (error) {
    console.error("设置昵称异常:", error);
  } finally {
    nicknameLoading.value = false;
  }
};

const handleBeforeAvatarUpload: UploadProps['beforeUpload'] = (rawFile: UploadRawFile) => {
  const isJPGorPNG = ['image/jpeg', 'image/png'].includes(rawFile.type);
  const isLt2M = rawFile.size / 1024 / 1024 < 2;

  if (!isJPGorPNG) {
    ElMessage.error('头像图片只能是 JPG/PNG 格式!');
  }
  if (!isLt2M) {
    ElMessage.error('头像图片大小不能超过 2MB!');
  }
  return isJPGorPNG && isLt2M;
};

// 头像上传成功后，后端会更新用户的 avatar 字段为新的 URL
// 我们只需重新 fetchUserInfo() 即可刷新界面
const handleAvatarUpload = async (options: UploadRequestOptions) => {
  try {
    const res = await setAvatar(options.file);
    if (res.code === 200) {
      ElMessage.success('头像上传成功');
      await fetchUserInfo();
    } else {
      ElMessage.error(res.message || '头像上传失败');
    }
  } catch (error) {
    console.error("头像上传异常:", error);
  }
};

const handleRecharge = async () => {
  if (!rechargeAmount.value || rechargeAmount.value <= 0) {
    ElMessage.warning('请输入有效的充值金额');
    return;
  }
  rechargeLoading.value = true;
  try {
    const res = await recharge(rechargeAmount.value);
    if (res.code === 200) {
      ElMessage.success(res.data || res.message || '充值成功！');
      rechargeDialogVisible.value = false;
      await fetchUserInfo();
    } else {
      ElMessage.error(res.message || '充值失败');
    }
  } catch (error) {
    console.error("充值异常:", error);
  } finally {
    rechargeLoading.value = false;
  }
};

const handleCloseRechargeDialog = (done: () => void) => {
  done();
};
</script>

<style scoped>
/* 样式部分保持不变 */
.profile-page {
  display: flex;
  justify-content: center;
  align-items: flex-start;
  padding: 40px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 50px);
}

.profile-card {
  width: 100%;
  max-width: 700px;
  border-radius: 12px;
}

.card-header {
  font-size: 20px;
  font-weight: bold;
  text-align: center;
  color: #303133;
}

.profile-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 25px;
  padding: 20px 0;
}

.avatar-section {
  position: relative;
  cursor: pointer;
}

.avatar-uploader .user-avatar {
  transition: filter 0.3s ease;
}

.avatar-uploader:hover .user-avatar {
  filter: brightness(0.7);
}

.avatar-edit-mask {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  color: white;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  font-size: 14px;
  opacity: 0;
  transition: opacity 0.3s ease;
  border-radius: 50%;
  pointer-events: none;
}

.avatar-uploader:hover .avatar-edit-mask {
  opacity: 1;
}

.avatar-edit-mask .el-icon {
  font-size: 24px;
  margin-bottom: 5px;
}

.info-item {
  display: flex;
  align-items: center;
  font-size: 16px;
  width: 80%;
  padding: 10px 0;
  border-bottom: 1px solid #e4e7ed;
}

.info-label {
  width: 120px;
  color: #606266;
  text-align: right;
  margin-right: 20px;
}

.info-value {
  color: #303133;
  display: flex;
  align-items: center;
  gap: 10px;
}

.nickname-section .edit-btn {
  margin-left: 10px;
}

.balance-section {
  border-bottom: none;
}

.balance-value {
  font-size: 24px;
  font-weight: bold;
  color: #e6a23c;
}

.recharge-section {
  margin-top: 20px;
}

.recharge-dialog-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 15px;
}

.recharge-tip {
  color: #909399;
  font-size: 12px;
}
</style>
