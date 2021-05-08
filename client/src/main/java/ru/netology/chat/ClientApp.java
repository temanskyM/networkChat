package ru.netology.chat;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class ClientApp {
    public static void main(String[] args) throws IOException {
        Path currentRelativePath = Paths.get("");
        String rootPath = currentRelativePath.toAbsolutePath().toString();
        String appConfigPath = rootPath + File.separator +"app.properties";

        Properties appProps = new Properties();
        appProps.load(new FileInputStream(appConfigPath));

        int port = Integer.parseInt(appProps.getProperty("port"));

        Thread thread = new Thread(new Client(port));
        thread.start();
    }
}
