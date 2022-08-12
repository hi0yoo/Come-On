package com.comeon.meetingservice.domain.util.fileupload;

import lombok.*;

import static lombok.AccessLevel.*;

@Getter @Setter
@Builder
@AllArgsConstructor(access = PROTECTED)
public class UploadFileDto {

    private String originalFileName;
    private String storedFileName;

}
