package com.comeon.meetingservice.web.common.response;

public enum ErrorCode {

    EMPTY_FILE(101, "업로드 파일이 없는 경우 발생"),
    UPLOAD_FAIL(102, "파일 업로드에 실패할 경우 발생"),
    VALIDATION_FAIL(103, "요청 데이터 검증에 실패했을 경우 발생"),
    ENTITY_NOT_FOUND(104, "해당 식별자를 가진 리소스가 없을 경우 발생");

    private final Integer code;
    private final String instruction;


    ErrorCode(Integer errorCode, String instruction) {
        this.code = errorCode;
        this.instruction = instruction;
    }

    public String getInstruction() {
        return instruction;
    }

    public Integer getCode() {
        return code;
    }

    public static Integer findCode(Throwable throwable) {
        String exceptionName = throwable.getClass().getSimpleName().replace("Exception", "");
        String regex = "([a-z])([A-Z]+)";
        String replacement = "$1_$2";

        return ErrorCode.valueOf(exceptionName.replaceAll(regex, replacement).toUpperCase()).getCode();
    }
}
