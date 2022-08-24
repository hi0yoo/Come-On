package com.comeon.meetingservice.web.meeting.request;

import com.comeon.meetingservice.domain.meeting.dto.MeetingAddDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static lombok.AccessLevel.*;

@Getter @Setter
@NoArgsConstructor(access = PRIVATE)
public class MeetingAddRequest {

    @NotBlank
    private String title;

    @Pattern(regexp = "^\\d{4}-(0[1-9]|1[012])-(0[1-9]|[12][0-9]|3[01])$")
    @NotBlank
    private String startDate;

    @Pattern(regexp = "^\\d{4}-(0[1-9]|1[012])-(0[1-9]|[12][0-9]|3[01])$")
    @NotBlank
    private String endDate;

    private Long courseId;

    @NotNull
    private MultipartFile image;

    public MeetingAddDto toDto() {
        return MeetingAddDto.builder()
                .courseId(courseId)
                .startDate(LocalDate.parse(startDate, DateTimeFormatter.ISO_DATE))
                .endDate(LocalDate.parse(endDate, DateTimeFormatter.ISO_DATE))
                .title(title)
                .build();
    }

}
