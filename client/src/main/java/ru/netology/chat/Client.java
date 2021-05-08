package ru.netology.chat;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import ru.netology.chat.model.Message;
import ru.netology.chat.model.Registration;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.Scanner;

@Slf4j
public class Client implements Runnable {
    private final int PORT;
    private final Scanner scn = new Scanner(System.in);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private DataInputStream dis;
    private DataOutputStream dos;
    private String name;

    public Client(int port) {
        this.PORT = port;
    }

    @Override
    public void run() {
        try {
            // getting localhost ip
            InetAddress ip = InetAddress.getByName("localhost");

            // establish the connection
            Socket s = new Socket(ip, PORT);

            // obtaining input and out streams
            dis = new DataInputStream(s.getInputStream());
            dos = new DataOutputStream(s.getOutputStream());

            registration();

            // sendMessage thread
            Thread sendMessage = new Thread(() -> {
                while (true) {
                    try {
                        // read the message to deliver.
                        String msg = scn.nextLine();
                        sendMessage(msg);

                        if(msg.equals("/exit"))
                            return;
                    } catch (IOException e) {
                        log.error(Arrays.toString(e.getStackTrace()));
                    }
                }
            });

            // readMessage thread
            Thread readMessage = new Thread(() -> {
                while (true) {
                    try {
                        // read the message sent to this client
                        if(Thread.currentThread().isInterrupted())
                            return;
                        String received = dis.readUTF();
                        Message message = objectMapper.readValue(received, Message.class);
                        printMessage(message);
                    } catch (IOException e) {
                        log.error(Arrays.toString(e.getStackTrace()));
                    }
                }
            });

            sendMessage.start();
            readMessage.start();

            sendMessage.join();

            readMessage.interrupt();
            s.close();
            log.info("Close client");
        } catch (IOException | InterruptedException e) {
            log.error(Arrays.toString(e.getStackTrace()));
        }

    }

    private void registration() throws IOException {
        System.out.println("Input name: ");
        String name = scn.nextLine();
        this.name = name;

        Registration registration = new Registration();
        registration.setName(name);

        String msg = objectMapper.writeValueAsString(registration);
        dos.writeUTF(msg);
        log.info("Success registration");
    }

    private void sendMessage(String text) throws IOException {
        Message message = Message.builder()
                .from(name)
                .text(text)
                .build();

        dos.writeUTF(objectMapper.writeValueAsString(message));
    }

    private void printMessage(Message message) {
        System.out.printf("[%tT] %s: %s\n", message.getTime(), message.getFrom(), message.getText());

        log.info("Received from \""+ message.getFrom() + "\" message \"" +message.getText() +"\"");
    }
}
