package com.comeon.courseservice.web.courseplace.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class CoursePlaceBatchUpdateRequest {

    /*
    저장하는 데이터는 전체 필드가 다 필요함
    수정하는 데이터는 id와 수정할 필드, 순서 필드만 필요함
    삭제하는 데이터는 id만 있으면 됨

    저장 데이터는 id를 제외한 전체 필드 검증 필요.
    수정 데이터는 id와 순서 검증 필요.
    삭제 데이터는 id 검증 필요

    Action 필드를 넣는다면, 검증이 힘들어진다.

    id 중복 검증
    order 중복 검증
     */

    @Valid
    private List<CoursePlaceSaveRequest> toSave = new ArrayList<>();

    @Valid
    private List<CoursePlaceModifyRequest> toModify = new ArrayList<>();

    @Valid
    private List<CoursePlaceDeleteRequest> toDelete = new ArrayList<>();

}
