package com.keepy.infra.gcs;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.keepy.global.exception.CustomException;
import com.keepy.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class GcsService {

    private final Storage gcsClient;

    @Value("${gcp.storage.bucket}")
    private String bucket;

    public String upload(MultipartFile file, String folder) {
        String key = folder + "/" + UUID.randomUUID() + "_" + file.getOriginalFilename();

        try {
            BlobInfo blobInfo = BlobInfo.newBuilder(BlobId.of(bucket, key))
                    .setContentType(file.getContentType())
                    .build();

            gcsClient.create(blobInfo, file.getBytes());

            return "https://storage.googleapis.com/" + bucket + "/" + key;
        } catch (IOException e) {
            log.error("GCS upload failed", e);
            throw new CustomException(ErrorCode.IMAGE_UPLOAD_FAILED);
        }
    }

    public void delete(String fileUrl) {
        try {
            // https://storage.googleapis.com/{bucket}/{key}
            String prefix = "https://storage.googleapis.com/" + bucket + "/";
            String key = fileUrl.substring(prefix.length());
            gcsClient.delete(BlobId.of(bucket, key));
        } catch (Exception e) {
            log.warn("GCS delete failed for url: {}", fileUrl, e);
        }
    }
}
