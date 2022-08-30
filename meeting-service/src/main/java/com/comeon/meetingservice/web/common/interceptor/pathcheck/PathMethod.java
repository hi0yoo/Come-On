package com.comeon.meetingservice.web.common.interceptor.pathcheck;

public enum PathMethod {

    GET, HEAD, POST, PUT, PATCH, DELETE, OPTIONS, TRACE, ANY;

    public boolean matches(String method) {
        return name().equals(method);
    }

    public boolean isAny() {
        return this.equals(ANY);
    }
}
