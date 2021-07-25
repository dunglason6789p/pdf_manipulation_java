package util;

public class CastUtil {
    /**@param exampleInstance E.g: castMagic(x, (List<String>)null)*/
    @Deprecated
    public static <T> T castMagic(Object object, T exampleInstance) {
        return (T)object;
    }
}
