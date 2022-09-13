package com.comeon.meetingservice.web.common.response;

import lombok.*;
import org.springframework.data.domain.Slice;

import java.util.List;

import static lombok.AccessLevel.*;

@Getter @Setter
@Builder
@AllArgsConstructor(access = PRIVATE)
public class SliceResponse<T> {

    private int currentSlice;
    private int sizePerSlice;
    private int numberOfElements;
    private boolean hasPrevious;
    private boolean hasNext;
    private boolean isFirst;
    private boolean isLast;
    private List<T> contents;

    // contents 를 조작한 경우 사용
    public static<T> SliceResponse<T> toSliceResponse(Slice slice, List<T> contents) {
        return SliceResponse.<T>builder()
                .currentSlice(slice.getNumber())
                .sizePerSlice(slice.getSize())
                .numberOfElements(slice.getNumberOfElements())
                .hasPrevious(slice.hasPrevious())
                .hasNext(slice.hasNext())
                .isFirst(slice.isFirst())
                .isLast(slice.isLast())
                .contents(contents)
                .build();
    }

    // contents 를 조작하지 않은 경우 사용
    public static SliceResponse toSliceResponse(Slice slice) {
        return SliceResponse.builder()
                .currentSlice(slice.getNumber())
                .sizePerSlice(slice.getSize())
                .numberOfElements(slice.getNumberOfElements())
                .hasPrevious(slice.hasPrevious())
                .hasNext(slice.hasNext())
                .isFirst(slice.isFirst())
                .isLast(slice.isLast())
                .contents(slice.getContent())
                .build();
    }
}
