package util;

import java.util.List;

public class ListUtil {
    public static <T> T getOrNull(List<T> list, int index) {
        return (index >= list.size()) ? null : list.get(index);
    }
}
