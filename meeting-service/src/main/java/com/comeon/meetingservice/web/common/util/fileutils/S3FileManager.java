package com.comeon.meetingservice.web.common.util.fileutils;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.util.IOUtils;
import com.comeon.meetingservice.web.common.exception.EmptyFileException;
import com.comeon.meetingservice.web.common.exception.UploadFailException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Component
public class S3FileManager implements FileManager {

    private final AmazonS3Client amazonS3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Override
    public UploadFileDto upload(MultipartFile multipartFile, String dirName) {
        if (Objects.isNull(multipartFile) || multipartFile.isEmpty()) {
            throw new EmptyFileException();
        }

        // DB에 저장할 파일명과 기존 파일명 추출
        String originalFileName = multipartFile.getOriginalFilename();
        String storedFileName = createStoredFileName(originalFileName);

        // S3에 해당 파일 업로드, dirName 폴더 하위에 저장 파일명으로 Put 함
        try {
            uploadToS3(multipartFile, dirName + "/" + storedFileName);
        } catch (IOException e) {
            log.error("S3 File Uploader IO Exception", e);
            throw new UploadFailException(e.getMessage());
        }

        return UploadFileDto.builder()
                .storedFileName(storedFileName)
                .originalFileName(originalFileName)
                .build();
    }

    @Override
    public void delete(String storedFileName, String dirName) {
        deleteFromS3(dirName + "/" + storedFileName);
    }

    @Override
    public String getFileUrl(String dirName, String fileName) {
        return amazonS3Client.getUrl(bucket, dirName + "/" + fileName).toString();
    }

    public void deleteFromS3(String fileName) {
        amazonS3Client.deleteObject(new DeleteObjectRequest(bucket, fileName));
    }

    private void uploadToS3(MultipartFile uploadFile, String fileName) throws IOException {
        byte[] fileBytes = IOUtils.toByteArray(uploadFile.getInputStream());
        ByteArrayInputStream fileByteArrayInputStream = new ByteArrayInputStream(fileBytes);

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(uploadFile.getContentType());
        metadata.setContentLength(fileBytes.length);

        amazonS3Client.putObject(new PutObjectRequest(bucket, fileName, fileByteArrayInputStream, metadata)
                .withCannedAcl(CannedAccessControlList.PublicRead));

        log.info("S3 {} 버킷에 put 작업을 수행했습니다. URI: {}",
                bucket, amazonS3Client.getUrl(bucket, fileName).toString());
    }

    private String createStoredFileName(String originalFilename) {
        String extension = extractExtension(originalFilename);
        String uuid = UUID.randomUUID().toString();
        return uuid + "." + extension;
    }

    private String extractExtension(String originalFilename) {
        int extensionPosition = originalFilename.lastIndexOf(".");
        String extension = originalFilename.substring(extensionPosition + 1);
        return extension;
    }
}