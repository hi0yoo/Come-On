package com.comeon.userservice.web.common.file;

import org.springframework.web.multipart.MultipartFile;

public interface FileManager {

    UploadFileDto upload(MultipartFile multipartFile, String dirName);

    void delete(String storedFileName, String dirName);

    String getFileUrl(String dirName, String storedFileName);
}
