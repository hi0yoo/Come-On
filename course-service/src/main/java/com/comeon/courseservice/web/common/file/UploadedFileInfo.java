package com.comeon.courseservice.web.common.file;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UploadedFileInfo {

    private String originalFileName;
    private String storedFileName;

}
