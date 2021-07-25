package util;

import java.util.function.Consumer;

public class FunctionalUtil {
    public static <T> T with(T object, Consumer<T> consumer) {
        consumer.accept(object);
        return object;
    }
    public static interface ThrowableRunnable<T extends Throwable> {
        public void run() throws T;
    }
    public static interface ThrowableConsumer<I, T extends Throwable> {
        public void accept(I input) throws T;
    }
    public static interface ThrowableFunction<I, O, T extends Throwable> {
        public O apply(I input) throws T;
    }
    public static interface ThrowableFunctionX<I,O> {
        public O apply(I input) throws Throwable;
    }
}
