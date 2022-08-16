package com.comeon.meetingservice.web.common.exception;

public class EmptyFileException extends IllegalArgumentException {

    public EmptyFileException() {
        super("MultipartFile이 null입니다.");
    }
}
