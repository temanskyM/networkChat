package ru.netology.chat.handler;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import ru.netology.chat.Server;
import ru.netology.chat.model.Message;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class ClientHandlerTest {

    @Test
    void update_success() throws IOException {
        Server server = Mockito.mock(Server.class);
        Socket s = Mockito.mock(Socket.class);
        DataInputStream dis = Mockito.mock(DataInputStream.class);
        DataOutputStream dos = Mockito.mock(DataOutputStream.class);
        ClientHandler clientHandler = new ClientHandler(server,s,dis,dos);
        Message msg = Message.builder()
                .from("test")
                .text("test")
                .time(new Date())
                .build();

        clientHandler.update(msg);

        verify(dos,times(1)).writeUTF(any());
    }
}