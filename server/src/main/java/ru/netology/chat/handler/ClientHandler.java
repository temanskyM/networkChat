package ru.netology.chat.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import ru.netology.chat.Server;
import ru.netology.chat.model.Message;
import ru.netology.chat.observer.Observer;
import ru.netology.chat.model.Registration;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Date;

@Slf4j
public class ClientHandler implements Runnable, Observer<Message> {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final DataInputStream dis;
    private final DataOutputStream dos;
    private final Socket s;
    private final Server server;
    private boolean start;
    private String name;

    // constructor
    public ClientHandler(Server server, Socket s,
                         DataInputStream dis, DataOutputStream dos) {
        this.server = server;
        this.dis = dis;
        this.dos = dos;
        this.s = s;
    }

    @Override
    public void run() {
        try {
            start = true;
            registration();
            while (start) {
                String received = dis.readUTF();

                Message recvMessage = objectMapper.readValue(received, Message.class);
                recvMessage.setTime(new Date());
                log.info("Received from user \"" + this.name + "\" message \"" + recvMessage.getText() + "\" at " + recvMessage.getTime());

                server.notifyObserver(recvMessage);

                if (recvMessage.getText().equals("/exit")) {
                    this.s.close();
                    exitProcess();
                    start = false;
                }
            }
        } catch (SocketException socketException) {
            exitProcess();
        } catch (IOException e) {
            log.error(Arrays.toString(e.getStackTrace()));
        } finally {
            try {
                dis.close();
                dos.close();
            } catch (IOException ignored) {
            }
        }
    }

    private void registration() throws IOException {
        String received;
        // receive the string
        received = dis.readUTF();
        //Преобразуем в класс регистрации
        Registration registration = objectMapper.readValue(received, Registration.class);
        //Достаем имя и присваеваем потоку
        this.name = registration.getName();

        registrationAnnounce();
    }

    private void registrationAnnounce() {
        Message announce = Message.builder()
                .from("Server")
                .time(new Date())
                .text("User " + this.name + " join to chat.")
                .build();

        //Рассылаем всем
        this.server.notifyObserver(announce);

        log.info("User " + this.name + " join to chat.");
    }

    private void exitProcess() {
        server.unregisterObserver(this);
        Message announce = Message.builder()
                .from("Server")
                .time(new Date())
                .text("User " + this.name + " leave from chat.")
                .build();

        //Рассылаем всем
        this.server.notifyObserver(announce);

        log.info("User " + this.name + " leave from chat.");
    }

    @Override
    public void update(Message message) {
        try {
            dos.writeUTF(objectMapper.writeValueAsString(message));
        } catch (IOException e) {
            log.error(Arrays.toString(e.getStackTrace()));
        }
    }
}
