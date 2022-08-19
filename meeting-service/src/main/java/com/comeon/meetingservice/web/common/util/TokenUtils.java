package com.comeon.meetingservice.web.common.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Base64;

@Component
public class TokenUtils {

    private static Environment env;

    public TokenUtils(Environment env) {
        this.env = env;
    }

    public static Long getUserId(String token) {
        String payload = getPayload(token);

        JsonObject payloadObject = (JsonObject) JsonParser.parseString(payload);
        return payloadObject.get(env.getProperty("token.claim-name.user-id")).getAsLong();
    }

    private static String getPayload(String token) {
        String[] chunks = token.replace("Bearer ", "").split("\\.");
        Base64.Decoder urlDecoder = Base64.getUrlDecoder();
        String payload = new String(urlDecoder.decode(chunks[1]));
        return payload;
    }
}
