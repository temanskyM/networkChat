package ru.netology.chat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.internal.matchers.Any;
import ru.netology.chat.handler.ClientHandler;
import ru.netology.chat.model.Message;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class ServerTest {

    @Test
    void registerObserver_whenNull_thenNoError() {
        Server server = new Server(8000);
        server.registerObserver(null);
    }

    @Test
    void registerObserver_whenNotNull_thenNoError() {
        Server server = new Server(8000);
        ClientHandler clientHandler = Mockito.mock(ClientHandler.class);
        server.registerObserver(clientHandler);
    }

    @Test
    void unregisterObserver_whenNotNull_thenNoError() {
        Server server = new Server(8000);
        ClientHandler clientHandler = Mockito.mock(ClientHandler.class);
        server.registerObserver(clientHandler);
        server.unregisterObserver(clientHandler);
    }

    @Test
    void unregisterObserver_whenNull_thenNoError() {
        Server server = new Server(8000);
        server.unregisterObserver(null);
    }

    @Test
    void notifyObserver_whenNull_thenNoError() {
        Server server = new Server(8000);
        ClientHandler clientHandler = Mockito.mock(ClientHandler.class);
        server.registerObserver(clientHandler);
        Message msg = Message.builder()
                .from("test")
                .text("test")
                .time(new Date())
                .build();
        server.notifyObserver(msg);
        verify(clientHandler,times(1)).update(any());
    }


}