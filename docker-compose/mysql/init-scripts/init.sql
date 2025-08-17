CREATE DATABASE IF NOT EXISTS venus_commerce;
USE venus_commerce;
--     用户表
CREATE TABLE IF NOT EXISTS `user`(
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `username` VARCHAR(64) NOT NULL UNIQUE,
    `password` VARCHAR(255) NOT NULL,
    `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    CONSTRAINT pk_user_id PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
-- 产品表
CREATE TABLE IF NOT EXISTS `product`(
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `spu_no` VARCHAR(255) NOT NULL UNIQUE COMMENT '商品标准品名',
    `title`  VARCHAR(255) NOT NULL COMMENT '商品标题',
    `category_id` BIGINT NOT NULL COMMENT '分类ID',
    `brand_id` BIGINT NOT NULL COMMENT '品牌ID',
    `main_image` VARCHAR(512) NOT NULL COMMENT '商品图片URL',
--     商品上架状态0为下架，1为上架
    `status` TINYINT NOT NULL DEFAULT '0' COMMENT '上下架状态',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT UNSIGNED NOT NULL DEFAULT '0' COMMENT '是否删除: 0-未删除, 1-已删除',

    CONSTRAINT pk_product_id PRIMARY KEY(`id`),
    INDEX `idx_category_id` (`category_id`),
    INDEX `idx_brand_id` (`brand_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
-- 商品sku表，主要用来分型号，size，type
CREATE TABLE IF NOT EXISTS `product_sku`(
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `spu_id` BIGINT UNSIGNED NOT NULL COMMENT 'product id',
    `sku_no` VARCHAR(64) NOT NULL UNIQUE COMMENT 'SKU 业务编码',
    `title` VARCHAR(255) NOT NULL COMMENT 'sku商品标题',
    `price` DECIMAL(10,2) NOT NULL,
    `stock` INT UNSIGNED NOT NULL DEFAULT '0' COMMENT '库存',
    `images` TEXT NOT NULL COMMENT 'SKU图片集，用逗号分割',
    `spec` JSON NOT NULL COMMENT '规格参数',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    CONSTRAINT pk_product_sku_id PRIMARY KEY(`id`),
    CONSTRAINT fk_product_spu_id
        FOREIGN KEY (`spu_id`)
        REFERENCES product(`id`),
    INDEX `idx_product_sku_spu_id` (`spu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
-- 订单表
CREATE TABLE IF NOT EXISTS `orders`(
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `order_no` VARCHAR(64) NOT NULL UNIQUE COMMENT '订单号',
    `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    `total_amount` DECIMAL(10,2) NOT NULL COMMENT '总金额',
--     订单状态1为已支付，0为未支付
    `status` TINYINT UNSIGNED NOT NULL DEFAULT '0' COMMENT '订单状态: 0-待支付, 1-已支付, 2-已发货, 3-已完成, 4-已取消',
    `is_deleted` TINYINT UNSIGNED NOT NULL DEFAULT '0' COMMENT '是否删除: 0-未删除, 1-已删除',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    CONSTRAINT pk_order_id PRIMARY KEY(`id`),
    CONSTRAINT fk_order_user_id
        FOREIGN KEY(`user_id`)
            REFERENCES user(`id`),
    INDEX `idx_order_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 由于一个订单可能包含多个商品，所以我们需要一个order——item表存储某一个的商品
CREATE TABLE `order_item`(
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `order_id` BIGINT UNSIGNED NOT NULL COMMENT '订单ID',
    `sku_id` BIGINT UNSIGNED NOT NULL COMMENT '订单商品sku id',
    `sku_title` VARCHAR(255) NOT NULL,
    `sku_price` DECIMAL(10,2) NOT NULL,
    `quantity` INT UNSIGNED NOT NULL COMMENT '购买数量',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    CONSTRAINT pk_order_item_id PRIMARY KEY (`id`),
    CONSTRAINT fk_order_item_order_id
                         FOREIGN KEY (`order_id`)
                         REFERENCES `orders`(`id`),
    CONSTRAINT fk_order_item_sku_id
                         FOREIGN KEY(`sku_id`)
                         REFERENCES product_sku(`id`),
    INDEX `idx_order_item_order_id` (`order_id`),
    INDEX `idx_order_item_sku_id` (`sku_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;