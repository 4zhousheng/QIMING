package com.zhousheng.service.impl;


import com.qcloud.cos.COSClient;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.exception.CosServiceException;
import com.qcloud.cos.model.CopyObjectRequest;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.zhousheng.config.CosProperties;
import com.zhousheng.service.CosService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

/**
 * 腾讯云对象存储（COS）服务类
 * 封装了文件的上传、移动和删除逻辑
 */
@Service
public class CosServiceImpl implements CosService {

    private static final Logger logger = LoggerFactory.getLogger(CosService.class);

    @Autowired
    private CosProperties cosProperties;

    @Autowired
    private COSClient cosClient;

    /**
     *
     * @param file
     * @param key
     * @return
     * @throws IOException
     */
    public String uploadFile(MultipartFile file, String key) throws IOException { // 参数从 directory 改为 key
        try (InputStream inputStream = file.getInputStream()) {
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentLength(file.getSize());
            objectMetadata.setContentType(file.getContentType());

            // 使用传入的、完整的 key
            PutObjectRequest putObjectRequest = new PutObjectRequest(
                    cosProperties.getBucketName(), key, inputStream, objectMetadata);

            logger.info("开始上传文件到COS, Bucket: {}, Key: {}", cosProperties.getBucketName(), key);
            cosClient.putObject(putObjectRequest);
            logger.info("文件上传成功, Key: {}", key);

            return key;
        } catch (CosServiceException e) {
            logger.error("COS服务端异常! Bucket: {}, Key: {}, ErrorMsg: {}", cosProperties.getBucketName(), key, e.getErrorMessage(), e);
            throw new RuntimeException("文件上传失败：COS服务端错误 - " + e.getErrorMessage());
        } catch (CosClientException e) {
            logger.error("COS客户端异常! Bucket: {}, Key: {}, ErrorMsg: {}", cosProperties.getBucketName(), key, e.getMessage(), e);
            throw new RuntimeException("文件上传失败：COS客户端错误 - " + e.getMessage());
        }
    }

    /**
     * 将 COS 上的一个文件从源路径移动到目标路径 (通过 复制+删除 实现)
     *
     * @param sourceKey      源文件的完整对象键 (e.g., "product/temp/2025-08-18/uuid.jpg")
     * @param destinationKey 目标文件的完整对象键 (e.g., "product/spu/12345/cover/uuid.jpg")
     * @return 移动成功后，新文件的完整访问 URL
     */
    public String moveFile(String sourceKey, String destinationKey) {
        String bucketName = cosProperties.getBucketName();

        try {
            // 1. 复制文件
            logger.info("准备复制文件, 从 Key: {} 到 Key: {}", sourceKey, destinationKey);
            CopyObjectRequest copyObjectRequest = new CopyObjectRequest(bucketName, sourceKey, bucketName, destinationKey);
            cosClient.copyObject(copyObjectRequest);
            logger.info("文件复制成功");

            // 2. 删除源文件
            logger.info("准备删除源文件, Key: {}", sourceKey);
            deleteFile(sourceKey); // 调用下面的删除方法
            logger.info("源文件删除成功");

            // 3. 构造并返回新文件的 URL
            return getFileUrl(destinationKey);

        } catch (CosServiceException e) {
            logger.error("COS服务端异常! 移动文件失败, SourceKey: {}, DestKey: {}, ErrorMsg: {}", sourceKey, destinationKey, e.getErrorMessage(), e);
            throw new RuntimeException("文件移动失败：COS服务端错误 - " + e.getErrorMessage());
        } catch (CosClientException e) {
            logger.error("COS客户端异常! 移动文件失败, SourceKey: {}, DestKey: {}, ErrorMsg: {}", sourceKey, destinationKey, e.getMessage(), e);
            throw new RuntimeException("文件移动失败：COS客户端错误 - " + e.getMessage());
        }
    }

    /**
     * 删除 COS 上的一个文件
     *
     * @param key 文件的完整对象键 (Object Key)
     */
    public void deleteFile(String key) {
        try {
            logger.info("准备删除文件, Bucket: {}, Key: {}", cosProperties.getBucketName(), key);
            cosClient.deleteObject(cosProperties.getBucketName(), key);
            logger.info("文件删除成功, Key: {}", key);
        } catch (CosServiceException e) {
            logger.error("COS服务端异常! 删除文件失败, Key: {}, ErrorMsg: {}", key, e.getErrorMessage(), e);
            throw new RuntimeException("文件删除失败：COS服务端错误 - " + e.getErrorMessage());
        } catch (CosClientException e) {
            logger.error("COS客户端异常! 删除文件失败, Key: {}, ErrorMsg: {}", key, e.getMessage(), e);
            throw new RuntimeException("文件删除失败：COS客户端错误 - " + e.getMessage());
        }
    }

    /**
     * 根据对象键 (Object Key) 获取文件的公网访问 URL
     *
     * @param key 文件的完整对象键
     * @return 文件的公网访问 URL
     */
    public String getFileUrl(String key) {
        return cosProperties.getRootUrl() + "/" + key;
    }
}