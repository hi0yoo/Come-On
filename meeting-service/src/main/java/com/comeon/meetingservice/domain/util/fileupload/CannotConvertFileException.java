package com.comeon.meetingservice.domain.util.fileupload;

public class CannotConvertFileException extends IllegalStateException {

    public CannotConvertFileException() {
        super("MultipartFile을 File로 변환하지 못했습니다.");
    }
}
