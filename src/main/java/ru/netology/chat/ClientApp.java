package ru.netology.chat;

public class ClientApp {
    public static void main(String[] args) {
        Thread thread = new Thread(new Client());
        thread.start();
    }
}
