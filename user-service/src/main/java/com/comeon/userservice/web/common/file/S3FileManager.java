package com.comeon.userservice.web.common.file;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class S3FileManager implements FileManager {

    private final AmazonS3Client amazonS3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Override
    public UploadFileDto upload(MultipartFile multipartFile, String dirName) {

        if (Objects.isNull(multipartFile) || multipartFile.isEmpty()) {
            // TODO 예외처리
            throw new RuntimeException("파일 없음");
        }

        String originalFileName = multipartFile.getOriginalFilename();
        String storedFileName = createStoredFileName(originalFileName);

        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType(multipartFile.getContentType());

        try (InputStream inputStream = multipartFile.getInputStream()) {
            amazonS3Client.putObject(
                    new PutObjectRequest(bucket, generateStoredPath(dirName, storedFileName), inputStream, objectMetadata)
                            .withCannedAcl(CannedAccessControlList.PublicRead)
            );
        } catch (IOException e) {
            // TODO 예외 처리
            throw new RuntimeException("파일 업로드 예외");
        }

        return new UploadFileDto(originalFileName, storedFileName);
    }

    @Override
    public void delete(String storedFileName, String dirName) {
        amazonS3Client.deleteObject(
                new DeleteObjectRequest(
                        bucket,
                        generateStoredPath(dirName, storedFileName)
                )
        );
    }

    @Override
    public String getFileUrl(String dirName, String storedFileName) {
        return amazonS3Client.getUrl(bucket, generateStoredPath(dirName, storedFileName)).toString();
    }

    private String generateStoredPath(String dirName, String storedFileName) {
        return dirName + "/" + storedFileName;
    }

    private String createStoredFileName(String originalFileName) {
        int pos = originalFileName.lastIndexOf(".");
        String ext = originalFileName.substring(pos + 1);

        return UUID.randomUUID() + "." + ext;
    }
}
