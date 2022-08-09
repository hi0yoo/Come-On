package com.comeon.meetingservice.domain.meeting.dto;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

import static lombok.AccessLevel.*;

@Getter @Setter
@Builder
@AllArgsConstructor(access = PRIVATE)
public class MeetingDto {

    private Long courseId;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long hostId;
    private String title;
    private MultipartFile image;

}
