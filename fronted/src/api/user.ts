import api, { type ApiResult } from '@/services/api'; // 确保路径正确

// 根据您后端的 Vo 定义 TypeScript 接口，增强类型安全
export interface UserDetailVo {
  username: string;
  nickname: string;
  avatar: string;
  createTime: string; // 后端返回 LocalDateTime，通常序列化为字符串
  money: number;
}

/**
 * 获取当前登录用户的信息
 */
export const getUserInfo = (): Promise<ApiResult<UserDetailVo>> => {
  return api.get('/user/info');
};

/**
 * 设置用户昵称
 * @param nickName - 新的昵称
 */
export const setNickname = (nickName: string): Promise<ApiResult<null>> => {
  // 后端 @PostMapping + String 参数，通常期望 application/x-www-form-urlencoded 格式
  const params = new URLSearchParams();
  params.append('nickName', nickName);
  return api.post('/user/nickname/set', params);
};

/**
 * 设置用户头像
 * @param avatarFile - 头像文件对象
 */
export const setAvatar = (avatarFile: File): Promise<ApiResult<null>> => {
  const formData = new FormData();
  formData.append('avatar', avatarFile);
  // axios 会为 FormData 自动设置正确的 Content-Type
  return api.post('/user/avatar/set', formData);
};

/**
 * 用户充值
 * @param money - 充值金额
 */
export const recharge = (money: number): Promise<ApiResult<string>> => {
  const params = new URLSearchParams();
  params.append('money', String(money));
  return api.post('/user/recharge', params);
};
/**
 * 新增：获取当前用户的昵称
 * @returns 返回用户的昵称字符串，如果未设置则可能为 null 或空字符串
 */
export const getNickname = (): Promise<ApiResult<string | null>> => {
  return api.get('/user/nickname/get');
};

/**
 * 新增：获取当前用户的头像 URL
 * @returns 返回用户头像的 URL 字符串
 */
export const getAvatar = (): Promise<ApiResult<string>> => {
  // 假设您的获取头像API是这个路径，如果不是请修改
  return api.get('/user/avatar/get');
}
