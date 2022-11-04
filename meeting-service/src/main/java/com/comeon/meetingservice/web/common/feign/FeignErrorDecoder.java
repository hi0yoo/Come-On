package com.comeon.meetingservice.web.common.feign;

import com.comeon.meetingservice.common.exception.CustomException;
import com.comeon.meetingservice.common.exception.ErrorCode;
import com.comeon.meetingservice.web.common.feign.courseservice.response.CourseServiceApiResponse;
import com.comeon.meetingservice.web.common.response.ErrorResponse;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class FeignErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {
        switch (response.status()) {
            case 400:
                if (methodKey.contains("getCoursePlaces")) {
                    Gson gson = new Gson();
                    try {
                        CourseServiceApiResponse<ErrorResponse<String>> courseServiceApiResponse
                                = gson.fromJson(response.body().asReader(StandardCharsets.UTF_8),
                                new TypeToken<CourseServiceApiResponse<ErrorResponse<String>>>() {
                                }.getType());

                        ErrorResponse<String> data = courseServiceApiResponse.getData();
                        Integer errorCode = data.getErrorCode();
                        switch (errorCode) {
                            case 906:
                                throw new CustomException(data.getMessage(), ErrorCode.COURSE_NOT_AVAILABLE);
                            case 904:
                                throw new CustomException(data.getMessage(), ErrorCode.COURSE_NOT_FOUND);
                            default:
                                throw new CustomException(data.getMessage(), ErrorCode.COURSE_SERVICE_ERROR);
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            default:
                throw new CustomException("Course Service 이용 불가", ErrorCode.COURSE_NOT_AVAILABLE);
        }
    }
}
