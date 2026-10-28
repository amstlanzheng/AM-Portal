package com.am.storage.service;

import com.am.common.constant.CommonConstant;
import com.am.common.exception.BusinessException;
import com.am.common.result.ResultCode;
import com.am.framework.config.MinioConfig;
import com.am.storage.entity.FileInfo;
import com.am.storage.mapper.FileInfoMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.minio.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinioService extends ServiceImpl<FileInfoMapper, FileInfo> {

    private final MinioClient minioClient;
    private final MinioConfig minioConfig;

    public FileInfo uploadFile(MultipartFile file, String directory, Long userId) {
        // 校验文件类型
        String originalFilename = file.getOriginalFilename();
        String ext = getFileExtension(originalFilename);
        if (!isAllowedType(ext)) {
            throw new BusinessException(ResultCode.FILE_TYPE_ERROR);
        }
        // 校验文件大小
        if (file.getSize() > CommonConstant.MAX_FILE_SIZE) {
            throw new BusinessException(ResultCode.FILE_SIZE_ERROR);
        }

        try {
            // 确保 bucket 存在
            ensureBucket();

            String storedName = UUID.randomUUID().toString().replace("-", "") + "." + ext;
            String objectName = directory + "/" + storedName;

            // 上传到 MinIO
            InputStream inputStream = file.getInputStream();
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(objectName)
                    .stream(inputStream, file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());
            inputStream.close();

            // 获取访问 URL
            String publicUrl = minioConfig.getPublicUrl();
            if (publicUrl == null || publicUrl.isBlank()) {
                publicUrl = minioConfig.getEndpoint();
            }
            String url = publicUrl + "/" + minioConfig.getBucketName() + "/" + objectName;

            // 保存文件信息到数据库
            FileInfo fileInfo = new FileInfo();
            fileInfo.setOriginalName(originalFilename);
            fileInfo.setStoredName(storedName);
            fileInfo.setUrl(url);
            fileInfo.setFileSize(file.getSize());
            fileInfo.setFileType(ext);
            fileInfo.setDirectory(directory);
            fileInfo.setUploadUserId(userId);
            fileInfo.setUploadTime(java.time.LocalDateTime.now());
            save(fileInfo);

            return fileInfo;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("文件上传失败", e);
            throw new BusinessException("文件上传失败: " + e.getMessage());
        }
    }

    public void deleteFile(Long fileId) {
        FileInfo fileInfo = getById(fileId);
        if (fileInfo == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        try {
            String objectName = fileInfo.getDirectory() + "/" + fileInfo.getStoredName();
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(objectName)
                    .build());
            removeById(fileId);
        } catch (Exception e) {
            log.error("文件删除失败", e);
            throw new BusinessException("文件删除失败: " + e.getMessage());
        }
    }

    public Page<FileInfo> listFiles(int pageNum, int pageSize, String directory, String keyword) {
        LambdaQueryWrapper<FileInfo> wrapper = new LambdaQueryWrapper<>();
        if (directory != null && !directory.isEmpty()) {
            wrapper.eq(FileInfo::getDirectory, directory);
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(FileInfo::getOriginalName, keyword);
        }
        wrapper.orderByDesc(FileInfo::getUploadTime);
        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    private void ensureBucket() throws Exception {
        boolean exists = minioClient.bucketExists(BucketExistsArgs.builder()
                .bucket(minioConfig.getBucketName())
                .build());
        if (!exists) {
            minioClient.makeBucket(MakeBucketArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .build());
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    private boolean isAllowedType(String ext) {
        for (String allowed : CommonConstant.ALLOWED_IMAGE_TYPES) {
            if (allowed.equalsIgnoreCase(ext)) {
                return true;
            }
        }
        return false;
    }
}
