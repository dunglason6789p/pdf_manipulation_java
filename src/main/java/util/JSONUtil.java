package util;

import com.google.gson.Gson;

public class JSONUtil {
    private static final Gson jsonizer = new Gson();
    public static <T> String toJsonString(T object) {
        return jsonizer.toJson(object);
    }
}
