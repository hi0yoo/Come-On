package com.comeon.meetingservice.domain.util.fileupload;

public class EmptyFileException extends IllegalArgumentException {

    public EmptyFileException() {
        super("MultipartFile이 null입니다.");
    }
}
