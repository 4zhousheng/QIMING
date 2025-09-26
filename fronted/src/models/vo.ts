
// src/models/vo.ts
export interface ProductVo {
  id: number;
  spuNo: string;
  title: string;
  mainImage: string;
  minPrice: number; // 后端 BigDecimal 会序列化为 number
  status: number;
}
export interface ProductSkuVo {
  id: number;
  title: string;
  stock: number;
  price: number;
  images: string;
  // 【核心改动】将 spec 的类型从 string 改为 Record<string, string>
  // Record<string, string> 是 TypeScript 中表示 { [key: string]: string } 的一种方式
  spec: Record<string, string>;
}
