package chat.shared;

public interface Restorable<T> {
    T restore(T another);
}
