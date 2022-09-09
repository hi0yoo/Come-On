package com.comeon.meetingservice.web.common.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;

import static lombok.AccessLevel.PRIVATE;

@Getter @Setter
@Builder
@AllArgsConstructor(access = PRIVATE)
public class ListResponse<T> {

    private Integer count;
    private Collection<T> contents;

    public static <T> ListResponse createListResponse(Collection<T> contents) {
        return ListResponse.<T>builder()
                .count(contents.size())
                .contents(contents)
                .build();
    }
}
