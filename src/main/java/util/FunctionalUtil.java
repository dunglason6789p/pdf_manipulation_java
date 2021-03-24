package util;

import java.util.function.Consumer;

public class FunctionalUtil {
    public static <T> T with(T object, Consumer<T> consumer) {
        consumer.accept(object);
        return object;
    }
}
