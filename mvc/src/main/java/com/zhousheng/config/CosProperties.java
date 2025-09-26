package com.zhousheng.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 腾讯云 COS 配置属性类
 * 通过 @ConfigurationProperties 注解，将 application.yml 中的 tencent.cos 配置项绑定到此类属性上。
 * @Component 注解使其成为一个 Spring Bean，可以被其他组件注入。
 */
@Data
@Component
@ConfigurationProperties(prefix = "tencent.cos")
public class CosProperties {

    /**
     * 存储桶所在的地域，例如 ap-guangzhou, ap-shanghai
     */
    private String region;

    /**
     * 存储桶名称，格式：BucketName-APPID
     */
    private String bucketName;

    /**
     * 访问密钥 ID (SecretId)
     */
    private String secretId;

    /**
     * 访问密钥 Key (SecretKey)
     */
    private String secretKey;

    //url根路径
    private String rootUrl; // 把 root-url 配置也加进来

}