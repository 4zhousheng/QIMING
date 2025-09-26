package com.zhousheng.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface CosService {
    String uploadFile(MultipartFile file, String directory) throws IOException ;
    String moveFile(String sourceKey, String destinationKey);
    void deleteFile(String key) ;
    String getFileUrl(String key);
}
