package com.comeon.meetingservice.domain.util.fileupload;

import org.springframework.web.multipart.MultipartFile;

public interface FileUploader {

    UploadFileDto upload(MultipartFile multipartFile, String dirName);

}
