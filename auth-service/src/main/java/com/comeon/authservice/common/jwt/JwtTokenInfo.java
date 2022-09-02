package com.comeon.authservice.common.jwt;

import lombok.Getter;

import java.time.Instant;

@Getter
public class JwtTokenInfo {

    private String value;
    private Instant expiry;

    public JwtTokenInfo(String value, Instant expiry) {
        this.value = value;
        this.expiry = expiry;
    }
}
