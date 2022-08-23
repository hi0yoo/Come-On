package com.comeon.userservice.web.common.file;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UploadFileDto {

    private String originalFileName;
    private String storedFileName;

}
