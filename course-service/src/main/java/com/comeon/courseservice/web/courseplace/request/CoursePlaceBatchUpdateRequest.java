package com.comeon.courseservice.web.courseplace.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.Valid;
import java.util.List;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class CoursePlaceBatchUpdateRequest {

    @Valid
    private List<CoursePlaceSaveRequest> toSave;

    @Valid
    private List<CoursePlaceModifyRequest> toModify;

    @Valid
    private List<CoursePlaceDeleteRequest> toDelete;

}
