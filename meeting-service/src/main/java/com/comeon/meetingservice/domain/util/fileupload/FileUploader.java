package com.comeon.meetingservice.domain.util.fileupload;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileUploader {

    UploadFileDto upload(MultipartFile multipartFile, String dirName) throws IOException;

}
