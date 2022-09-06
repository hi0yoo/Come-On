package com.comeon.courseservice.web.common.response;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Slice;

import java.util.List;

@Getter
public class SliceResponse<T> {

    private int currentSlice;
    private int sizePerSlice;
    private int numberOfElements;
    private boolean hasPrevious;
    private boolean hasNext;
    private boolean isFirst;
    private boolean isLast;
    private List<T> contents;

    @Builder
    private SliceResponse(int currentSlice, int sizePerSlice, int numberOfElements,
                          boolean hasPrevious, boolean hasNext, boolean isFirst, boolean isLast, List<T> contents) {
        this.currentSlice = currentSlice;
        this.sizePerSlice = sizePerSlice;
        this.numberOfElements = numberOfElements;
        this.hasPrevious = hasPrevious;
        this.hasNext = hasNext;
        this.isFirst = isFirst;
        this.isLast = isLast;
        this.contents = contents;
    }

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
