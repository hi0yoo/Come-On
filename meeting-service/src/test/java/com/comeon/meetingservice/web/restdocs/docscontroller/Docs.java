package com.comeon.meetingservice.web.restdocs.docscontroller;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class Docs {

    Map<String, String> apiResponseCodes;
    Map<Integer, String> errorCodes;
    Map<String, String> placeCategories;

}
