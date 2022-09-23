package com.comeon.userservice.web.common.response;

import lombok.Getter;

import java.util.List;

@Getter
public class ListResponse<T> {

    private int count;
    private List<T> contents;

    public ListResponse(List<T> contents) {
        this.count = contents.size();
        this.contents = contents;
    }

    public static<T> ListResponse<T> toListResponse(List<T> contents) {
        return new ListResponse<>(contents);
    }
}
