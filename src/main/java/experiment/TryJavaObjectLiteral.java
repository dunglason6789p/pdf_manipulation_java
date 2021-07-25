/*
package experiment;

public class TryJavaObjectLiteral {
    public static void main(String[] args) {
        GenericWrapper<?> genericWrapper = GenericWrapper.create(new Object() {
            final int prop1 = 1;
        });
        genericWrapper.getValue()
    }
    private static <T> T give(T t) {
        return t;
    }
}
class GenericWrapper<T> {
    private T value;
    public T getValue() {return value;}
    public void setValue(T value) {this.value = value;}
    public static <T> GenericWrapper<T> create(T t) {
        GenericWrapper<T> generic = new GenericWrapper<T>();
        generic.value = t;
        return generic;
    }
}
*/
