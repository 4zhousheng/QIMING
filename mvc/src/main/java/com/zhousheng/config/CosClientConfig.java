package com.zhousheng.config;


import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.http.HttpProtocol;
import com.qcloud.cos.region.Region;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 腾讯云 COS 客户端配置类
 * 这个类的作用是手动创建一个 COSClient 实例，并将其注册为 Spring 的 Bean，
 * 这样就可以在项目的任何地方通过 @Autowired 来注入和使用它。
 */
@Configuration // 声明这是一个 Spring 配置类
public class CosClientConfig {

    @Autowired
    private CosProperties cosProperties; // 注入我们上面定义的属性类

    /**
     * 使用 @Bean 注解，创建一个 COSClient 的 Bean。
     * Spring 容器会负责管理这个 Bean 的生命周期（默认是单例）。
     * 方法名 cosClient 可以任意取，但返回类型必须是 COSClient。
     *
     * @return 配置好的 COSClient 实例
     */
    @Bean
    public COSClient cosClient() {
        // 1. 初始化用户身份信息 (secretId, secretKey)
        // 从 CosProperties 对象中安全地获取密钥
        COSCredentials cred = new BasicCOSCredentials(cosProperties.getSecretId(), cosProperties.getSecretKey());

        // 2. 设置 bucket 的地域, ClientConfig 中包含了后续请求 COS 的姿势, 如 Dns Resolver、Proxy 等
        Region region = new Region(cosProperties.getRegion());
        ClientConfig clientConfig = new ClientConfig(region);

        // 3. 推荐使用 https 协议，保证数据传输安全
        clientConfig.setHttpProtocol(HttpProtocol.https);

        // 4. 生成 cos 客户端。
        return new COSClient(cred, clientConfig);
    }
}