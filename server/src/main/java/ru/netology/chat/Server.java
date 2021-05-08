package ru.netology.chat;

import lombok.extern.slf4j.Slf4j;
import ru.netology.chat.handler.ClientHandler;
import ru.netology.chat.model.Message;
import ru.netology.chat.observer.Observable;
import ru.netology.chat.observer.Observer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Slf4j
public class Server implements Runnable, Observable<Message, ClientHandler> {
    private final int PORT;
    private final Object MONITOR = new Object();
    private Set<ClientHandler> clients;

    public Server(int port) {
        this.PORT = port;
    }

    @Override
    public void run() {
        log.info("Server starts");
        try {
            ServerSocket ss = new ServerSocket(PORT);
            Socket s;
            while (true) {
                // Accept the incoming request
                s = ss.accept();

                log.info("New client request received : " + s);

                // obtain input and output streams
                DataInputStream dis = new DataInputStream(s.getInputStream());
                DataOutputStream dos = new DataOutputStream(s.getOutputStream());

                log.debug("Creating a new handler for this client...");

                // Create a new handler object for handling this request.
                ClientHandler clientHandler = new ClientHandler(this, s, dis, dos);

                // Create a new Thread with this object.
                Thread t = new Thread(clientHandler);

                log.debug("Adding this client to active client list");

                // add this client to active clients list
                registerObserver(clientHandler);

                // start the thread.
                t.start();
            }
        }
        catch (IOException e) {
            log.error(Arrays.toString(e.getStackTrace()));
        }
    }

    @Override
    public void registerObserver(ClientHandler obs) {
        if (obs == null) return;
        synchronized (MONITOR) {
            if (clients == null) {
                clients = new HashSet<>(1);
            }
        }
        clients.add(obs);
    }

    @Override
    public void unregisterObserver(ClientHandler obs) {
        if (obs == null) return;
        synchronized (MONITOR) {
            if (clients != null)
                clients.remove(obs);
        }
    }

    @Override
    public void notifyObserver(Message msg) {
        Set<Observer<Message>> observersCopy;
        synchronized (MONITOR) {
            if (clients == null) return;
            observersCopy = new HashSet<>(clients);
        }
        for (Observer<Message> observer : observersCopy) {
            observer.update(msg);
        }
    }
}
