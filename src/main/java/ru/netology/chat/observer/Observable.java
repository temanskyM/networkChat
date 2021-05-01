package ru.netology.chat.observer;

public interface Observable<K, T extends Observer<K>> {
    void registerObserver(T obs);

    void unregisterObserver(T obs);

    void notifyObserver(K obj);
}
