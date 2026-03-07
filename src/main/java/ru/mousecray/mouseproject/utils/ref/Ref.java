package ru.mousecray.mouseproject.utils.ref;

public interface Ref<T> {
    public void $(T val);
    public T $$();
}
