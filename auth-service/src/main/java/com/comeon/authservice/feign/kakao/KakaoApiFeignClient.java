package com.comeon.authservice.feign.kakao;

import com.comeon.authservice.feign.kakao.response.UserUnlinkResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "kakao-api", url = "https://kapi.kakao.com")
public interface KakaoApiFeignClient {

    // come-on 애플리케이션에 등록되지 않은 target_id로 unlink 하면 400 Error
    // 아래 3가지 파라미터 중 하나라도 입력되지 않으면 401 Error 응답받음.
    @PostMapping("/v1/user/unlink")
    UserUnlinkResponse userUnlink(@RequestHeader(HttpHeaders.AUTHORIZATION) String kakaoAdminKey,
                                  @RequestParam("target_id") Long targetId,
                                  @RequestParam("target_id_type") String targetIdType);
}
