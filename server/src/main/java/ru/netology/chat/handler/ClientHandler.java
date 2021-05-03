package ru.netology.chat.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import ru.netology.chat.Server;
import ru.netology.chat.model.Message;
import ru.netology.chat.observer.Observer;
import ru.netology.chat.model.Registration;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.Date;

public class ClientHandler implements Runnable, Observer<Message> {
    private ObjectMapper objectMapper = new ObjectMapper();
    private String name;
    private final DataInputStream dis;
    private final DataOutputStream dos;
    private Socket s;

    private Server server;

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
            registration();
            String received;
            while (true) {
                // receive the string
                received = dis.readUTF();

                Message recvMessage = objectMapper.readValue(received, Message.class);
                recvMessage.setTime(new Date());
                System.out.println("Received from " + this.name + ":" + recvMessage.getText());

                //Рассылаем всем
                server.notifyObserver(recvMessage);

                if (recvMessage.getText().equals("logout")) {
                    this.s.close();
                    break;
                }
            }

            this.dis.close();
            this.dos.close();

        }
        catch (SocketException socketException){
            server.unregisterObserver(this);
            exitAnnounce();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                s.close();
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
                .text("Пользователь " + this.name + " вошел в чат.")
                .build();

        //Рассылаем всем
        this.server.notifyObserver(announce);
    }

    private void exitAnnounce() {
        Message announce = Message.builder()
                .from("Server")
                .time(new Date())
                .text("Пользователь " + this.name + " вышел из чата.")
                .build();

        //Рассылаем всем
        this.server.notifyObserver(announce);
    }

    @Override
    public void update(Message message) {
        try {
            dos.writeUTF(objectMapper.writeValueAsString(message));
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }
}
