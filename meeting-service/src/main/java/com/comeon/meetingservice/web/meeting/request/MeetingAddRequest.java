package com.comeon.meetingservice.web.meeting.request;

import com.comeon.meetingservice.domain.meeting.dto.MeetingAddDto;
import com.comeon.meetingservice.domain.meeting.dto.MeetingAddPlaceDto;
import com.comeon.meetingservice.web.common.feign.courseservice.response.CourseListResponse;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

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

    public MeetingAddDto toDto(
            Long userId,
            String originalFileName,
            String storedFileName,
            List<CourseListResponse> courseListResponses) {

        return MeetingAddDto.builder()
                .userId(userId)
                .startDate(LocalDate.parse(startDate, DateTimeFormatter.ISO_DATE))
                .endDate(LocalDate.parse(endDate, DateTimeFormatter.ISO_DATE))
                .title(title)
                .originalFileName(originalFileName)
                .storedFileName(storedFileName)
                .meetingAddPlaceDtos(convertToPlaceAddDtoList(courseListResponses))
                .build();
    }

    private List<MeetingAddPlaceDto> convertToPlaceAddDtoList(List<CourseListResponse> courseListResponses) {
        List<MeetingAddPlaceDto> meetingAddPlaceDtos = courseListResponses.stream()
                .map(clr ->
                        MeetingAddPlaceDto.builder()
                                .apiId(clr.getApiId())
                                .category(clr.getPlaceCategory())
                                .name(clr.getName())
                                .memo(clr.getDescription())
                                .lat(clr.getLat())
                                .lng(clr.getLng())
                                .order(clr.getOrder())
                                .build())
                .collect(Collectors.toList());
        return meetingAddPlaceDtos;
    }

}
