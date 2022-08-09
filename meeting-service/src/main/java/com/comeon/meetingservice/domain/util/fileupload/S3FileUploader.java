package com.comeon.meetingservice.domain.util.fileupload;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Component
public class S3FileUploader implements FileUploader {

    private final AmazonS3Client amazonS3Client;
    private final ResourceLoader resourceLoader;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.s3.tempStoragePath}")
    private String tempStoragePath;

    @Override
    public UploadFileDto upload(MultipartFile multipartFile, String dirName) throws IOException {
        if (multipartFile.isEmpty()) {
            throw new IllegalArgumentException("MultipartFile이 null입니다.");
        }

        // DB에 저장할 파일명과 기존 파일명 추출
        String originalFileName = multipartFile.getOriginalFilename();
        String storedFileName = createStoredFileName(originalFileName);

        // S3에 업로드 하기 위해 File 객체로 변환, tempStoragePath 하위에 파일 임시 저장
        File uploadFile = convert(multipartFile)
                .orElseThrow(CannotConvertFileException::new);

        // S3에 해당 파일 업로드, dirName 폴더 하위에 저장 파일명으로 Put 함
        uploadToS3(uploadFile, dirName + "/" + storedFileName);

        // S3에 올리기 위해 임시로 저장했던 파일 삭제
        removeTempFile(uploadFile);

        return UploadFileDto.builder()
                .storedFileName(storedFileName)
                .originalFileName(originalFileName)
                .build();
    }

    private void uploadToS3(File uploadFile, String fileName) {
        amazonS3Client.putObject(new PutObjectRequest(bucket, fileName, uploadFile)
                .withCannedAcl(CannedAccessControlList.PublicRead));
        log.info("S3 {} 버킷에 put 작업을 수행했습니다. URI: {}",
                bucket, amazonS3Client.getUrl(bucket, fileName).toString());
    }

    private void removeTempFile(File targetFile) {
        if (targetFile.delete()) {
            log.info("임시 파일 삭제 완료");
        } else {
            log.error("임시 파일 삭제 오류");
        }
    }

    private Optional<File> convert(MultipartFile file) throws IOException {
        // 지정된 임시 경로를 가지는 File 객체 생성
        File convertFile = new File(
                resourceLoader.getResource(tempStoragePath).getFile(),
                file.getOriginalFilename());

        // 임시 경로에 File 실제 생성 및 클라이언트 파일의 바이트를 변환 파일의 바이트 스트림으로 저장
        if(convertFile.createNewFile()) {
            try (FileOutputStream fos = new FileOutputStream(convertFile)) {
                fos.write(file.getBytes());
            }
            return Optional.of(convertFile);
        }

        return Optional.empty();
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