package ru.netology.chat.observer;

public interface Observer<T> {
    void update(T obj);
}
