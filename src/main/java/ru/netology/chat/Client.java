package ru.netology.chat;

import com.fasterxml.jackson.databind.ObjectMapper;
import ru.netology.chat.model.Message;
import ru.netology.chat.model.Registration;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

public class Client implements Runnable {
    private final int ServerPort = 8000;
    private final Scanner scn = new Scanner(System.in);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private String name;

    @Override
    public void run() {
        try {
            // getting localhost ip
            InetAddress ip = InetAddress.getByName("localhost");

            // establish the connection
            Socket s = new Socket(ip, ServerPort);

            // obtaining input and out streams
            DataInputStream dis = new DataInputStream(s.getInputStream());
            DataOutputStream dos = new DataOutputStream(s.getOutputStream());

            registration(dos);

            // sendMessage thread
            Thread sendMessage = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        // read the message to deliver.
                        String msg = scn.nextLine();

                        try {
                            sendMessage(dos, msg);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

            // readMessage thread
            Thread readMessage = new Thread(() -> {
                while (true) {
                    try {
                        // read the message sent to this client
                        String received = dis.readUTF();
                        Message message = objectMapper.readValue(received, Message.class);

                        System.out.println(message.toString());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

            sendMessage.start();
            readMessage.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void registration(DataOutputStream dos) throws IOException {
        System.out.println("Введите имя: ");
        String name = scn.nextLine();
        this.name = name;

        Registration registration = new Registration();
        registration.setName(name);

        String msg = objectMapper.writeValueAsString(registration);
        dos.writeUTF(msg);
        System.out.println("Успешная регистрация.");
    }

    private void sendMessage(DataOutputStream dos, String text) throws IOException {
        Message message = Message.builder()
                .from(name)
                .text(text)
                .build();

        dos.writeUTF(objectMapper.writeValueAsString(message));
    }
}
