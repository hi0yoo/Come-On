package com.comeon.meetingservice.web.common.util.fileutils;

import org.springframework.web.multipart.MultipartFile;

public interface FileManager {

    UploadFileDto upload(MultipartFile multipartFile, String dirName);


    void delete(String storedFileName, String dirName);
}
