package chat.client;

import chat.server.Server;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class Client {

    public static void main(String[] args) throws InterruptedException {

        TimeUnit.SECONDS.sleep(1);

        try(
            Socket socket = new Socket(InetAddress.getByName(Server.SERVER_HOST), Server.SERVER_PORT);
            DataInputStream input = new DataInputStream(socket.getInputStream());
            DataOutputStream output = new DataOutputStream(socket.getOutputStream());
        ) {

            System.out.println("Client started!");

            Thread outputThread = new Thread(() -> {
                Scanner scanner = new Scanner(System.in);
                while (!socket.isClosed()) {
                    try {
                        if (scanner.hasNextLine()) {
                            String message = scanner.nextLine();
                            if (message.equals("/exit")) {
                                socket.close();
                            }
                            output.writeUTF(message);
                        }
                    } catch (Exception e) {
                        break;
                    }
                }
            });

            Thread inputThread = new Thread(() -> {
                while (!socket.isClosed()) {
                    try {
                        String message = input.readUTF();
                        System.out.println(message);
                    } catch (Exception e) {
                        break;
                    }
                }
            });

            outputThread.start();
            inputThread.start();

            outputThread.join();
            socket.close();

            inputThread.join();

        } catch (Exception e) {
            Thread.currentThread().interrupt();
        }

        System.exit(0);
    }
}
