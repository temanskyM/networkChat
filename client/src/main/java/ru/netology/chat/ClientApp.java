package ru.netology.chat;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ClientApp {
    public static void main(String[] args) throws IOException {
        String rootPath = Thread.currentThread().getContextClassLoader().getResource("").getPath();
        String appConfigPath = rootPath + "app.properties";

        Properties appProps = new Properties();
        appProps.load(new FileInputStream(appConfigPath));

        int port = Integer.parseInt(appProps.getProperty("port"));

        Thread thread = new Thread(new Client(port));
        thread.start();
    }
}
