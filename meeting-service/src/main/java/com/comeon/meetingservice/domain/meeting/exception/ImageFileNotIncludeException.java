package com.comeon.meetingservice.domain.meeting.exception;

public class ImageFileNotIncludeException extends IllegalArgumentException {

    public ImageFileNotIncludeException() {
        super("모임 대표 이미지가 첨부되지 않았습니다.");
    }
}
