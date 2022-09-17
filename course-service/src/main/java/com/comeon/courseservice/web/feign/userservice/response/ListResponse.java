package com.comeon.courseservice.web.feign.userservice.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
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