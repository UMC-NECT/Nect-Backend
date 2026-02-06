package com.nect.api.global.infra;

import com.amazonaws.HttpMethod;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.nect.api.global.code.StorageErrorCode;
import com.nect.api.global.infra.exception.StorageException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.UUID;

@Service
public class S3Service {
    private static final long PRESIGNED_EXPIRE_MILLIS = 5 * 60 * 1000; // 5분
    private final AmazonS3 amazonS3;
    @Value("${spring.cloud.cloud-flare.r2.bucket}")
    private String bucket;

    public S3Service(AmazonS3 amazonS3) {
        this.amazonS3 = amazonS3;
    }

    public String uploadFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new StorageException(StorageErrorCode.EMPTY_FILE);
        }

        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename(); // 고유한 파일 이름 생성

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(file.getContentType());
        metadata.setContentLength(file.getSize());

        PutObjectRequest putObjectRequest = new PutObjectRequest(bucket, fileName, file.getInputStream(), metadata);

        amazonS3.putObject(putObjectRequest);

        return fileName; // DB 저장 값 (Key)
    }

    // 다운로드, 조회 전용
    public String getPresignedGetUrl(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            throw new StorageException(StorageErrorCode.EMPTY_FILE_NAME);
        }

        Date expiration = new Date(System.currentTimeMillis() + PRESIGNED_EXPIRE_MILLIS);

        URL url;
        try {
            GeneratePresignedUrlRequest request =
                    new GeneratePresignedUrlRequest(bucket, fileName)
                            .withMethod(HttpMethod.GET)
                            .withExpiration(expiration);

            url = amazonS3.generatePresignedUrl(request);
        } catch (AmazonS3Exception e) {
            if (e.getStatusCode() == 404) {
                throw new StorageException(StorageErrorCode.FILE_NOT_FOUND);
            }
            throw new StorageException(StorageErrorCode.FILE_DOWNLOAD_FAILED);

        } catch (SdkClientException | IllegalArgumentException e) {
            throw new StorageException(StorageErrorCode.FILE_DOWNLOAD_FAILED);
        } catch (Exception e) {
            throw new StorageException(StorageErrorCode.S3_EXCEPTION);
        }

        return url.toString();
    }

    public void deleteByFileName(String fileName){
        amazonS3.deleteObject(bucket, fileName);
    }
}