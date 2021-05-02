package ru.netology.chat;

import ru.netology.chat.handler.ClientHandler;
import ru.netology.chat.model.Message;
import ru.netology.chat.observer.Observable;
import ru.netology.chat.observer.Observer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

public class Server implements Runnable, Observable<Message, ClientHandler> {
    private final int PORT = 8000;
    // this is the object we will be synchronizing on ("the monitor")
    private final Object MONITOR = new Object();
    private Set<ClientHandler> clients;

    @Override
    public void run() {
        System.out.println("ru.netology.chat.chat.Server starts.");
        try {
            ServerSocket ss = new ServerSocket(PORT);
            Socket s;
            while (true) {
                // Accept the incoming request
                s = ss.accept();

                System.out.println("New client request received : " + s);

                // obtain input and output streams
                DataInputStream dis = new DataInputStream(s.getInputStream());
                DataOutputStream dos = new DataOutputStream(s.getOutputStream());

                System.out.println("Creating a new ru.netology.chat.chat.handler for this client...");

                // Create a new ru.netology.chat.chat.handler object for handling this request.
                ClientHandler clientHandler = new ClientHandler(this, s, dis, dos);

                // Create a new Thread with this object.
                Thread t = new Thread(clientHandler);

                System.out.println("Adding this client to active client list");

                // add this client to active clients list
                registerObserver(clientHandler);

                // start the thread.
                t.start();
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public void registerObserver(ClientHandler obs) {
        if (obs == null) return;
        synchronized (MONITOR) {
            if (clients == null) {
                clients = new HashSet<>(1);
            }
//            if (clientHandlerList.add(ru.netology.chat.chat.observer) && clientHandlerList.size() == 1) {
//                performInit(); // some initialization when first ru.netology.chat.chat.observer added
//            }
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
