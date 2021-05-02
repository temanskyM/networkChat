package ru.netology.chat;

public class ServerApp {
    public static void main(String[] args) {
       Thread thread = new Thread(new Server());
       thread.start();
    }
}

