package com.comeon.meetingservice.web.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.Base64;

public class TokenUtils {

    public static Long getUserId(String token) {
        String payload = getPayload(token);

        JsonObject payloadObject = (JsonObject) JsonParser.parseString(payload);
        return payloadObject.get("sub").getAsLong();
    }

    private static String getPayload(String token) {
        String[] chunks = token.replace("Bearer ", "").split("\\.");
        Base64.Decoder urlDecoder = Base64.getUrlDecoder();
        String payload = new String(urlDecoder.decode(chunks[1]));
        return payload;
    }
}
