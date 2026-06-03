package com.nipun.media.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.InputStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class MediaStorageService {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket:nipun-educational-assets}")
    private String bucketName;

    public String uploadFile(String key, InputStream inputStream, long contentLength, String contentType) {
        log.info("Uploading file to S3: {} (size={})", key, contentLength);
        
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(contentType)
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(inputStream, contentLength));
        log.info("Successfully uploaded file {} to S3 bucket {}", key, bucketName);
        
        return key;
    }
}
