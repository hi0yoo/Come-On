package com.comeon.courseservice.web.course.request;

import com.comeon.courseservice.domain.course.service.dto.CourseDto;
import com.comeon.courseservice.domain.course.service.dto.CoursePlaceDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public class CourseSaveRequest {

    @NotNull
    private MultipartFile imgFile;

    @NotBlank
    private String title;

    @NotBlank
    private String description;

    @NotNull
    List<CoursePlaceSaveRequest> coursePlaces;

    public CourseDto toServiceDto() {
        return CourseDto.builder()
                .title(title)
                .description(description)
                .coursePlaceDtos(
                        coursePlaces.stream()
                                .map(CoursePlaceSaveRequest::toServiceDto)
                                .collect(Collectors.toList())
                )
                .build();
    }


    public static class CoursePlaceSaveRequest {
        private String name;
        private String description;
        private Double lat;
        private Double lng;
        private Integer order;

        @Builder
        public CoursePlaceSaveRequest(String name, String description, Double lat, Double lng, Integer order) {
            this.name = name;
            this.description = description;
            this.lat = lat;
            this.lng = lng;
            this.order = order;
        }

        public CoursePlaceDto toServiceDto() {
            return CoursePlaceDto.builder()
                    .name(name)
                    .description(description)
                    .lat(lat)
                    .lng(lng)
                    .order(order)
                    .build();
        }
    }
}
