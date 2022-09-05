package com.comeon.meetingservice.web.common.interceptor;

import com.comeon.meetingservice.domain.meeting.entity.MeetingRole;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface MeetingAuth {

    MeetingRole[] meetingRoles() default MeetingRole.PARTICIPANT;

}
