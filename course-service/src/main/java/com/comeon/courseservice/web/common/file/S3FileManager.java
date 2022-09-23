package com.comeon.courseservice.web.common.file;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.util.IOUtils;
import com.comeon.courseservice.common.exception.CustomException;
import com.comeon.courseservice.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class S3FileManager implements FileManager {

    private final AmazonS3 amazonS3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Override
    public UploadedFileInfo upload(MultipartFile multipartFile, String dirName) {

        if (Objects.isNull(multipartFile) || multipartFile.isEmpty()) {
            throw new CustomException("파일이 비어있습니다.", ErrorCode.EMPTY_FILE);
        }

        String originalFileName = multipartFile.getOriginalFilename();
        String storedFileName = createStoredFileName(originalFileName);

        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType(multipartFile.getContentType());

        try (InputStream inputStream = multipartFile.getInputStream()) {
            byte[] bytes = IOUtils.toByteArray(inputStream);
            objectMetadata.setContentLength(bytes.length);
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
            amazonS3Client.putObject(
                    new PutObjectRequest(bucket, generateStoredPath(storedFileName, dirName), byteArrayInputStream, objectMetadata)
                            .withCannedAcl(CannedAccessControlList.PublicRead)
            );
        } catch (IOException e) {
            throw new CustomException("파일 업로드 중 예외 발생", e, ErrorCode.UPLOAD_FAIL);
        }

        return new UploadedFileInfo(originalFileName, storedFileName);
    }

    @Override
    public void delete(String storedFileName, String dirName) {
        amazonS3Client.deleteObject(
                new DeleteObjectRequest(
                        bucket,
                        generateStoredPath(storedFileName, dirName)
                )
        );
    }

    @Override
    public String getFileUrl(String storedFileName, String dirName) {
        return amazonS3Client.getUrl(bucket, generateStoredPath(storedFileName, dirName)).toString();
    }

    private String generateStoredPath(String storedFileName, String dirName) {
        return dirName + "/" + storedFileName;
    }

    private String createStoredFileName(String originalFileName) {
        int pos = originalFileName.lastIndexOf(".");
        String ext = originalFileName.substring(pos + 1);

        return UUID.randomUUID() + "." + ext;
    }
}

