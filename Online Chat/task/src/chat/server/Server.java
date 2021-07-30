package chat.server;

import chat.shared.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;

public class Server {

    public static final int SERVER_PORT = 23456;
    public static final String SERVER_HOST = "127.0.0.1";

    private static List<ServerSession> clients = new ArrayList<>();
    public static int clientCounter = 1;

    public static void main(String[] args) {
        try(
            ServerSocket serverSocket = new ServerSocket(
                SERVER_PORT,
                0,
                InetAddress.getByName(SERVER_HOST)
            )
        ){
            serverSocket.setSoTimeout(7000);
            System.out.println("Server started!");

            Thread.UncaughtExceptionHandler handler = (th, ex) -> clients.remove(th);

            FileStorage<User> fileStorage = new FileStorage<>("admin");
            User admin = fileStorage.fetch();
            if (admin == null) {
                admin = new User((int) System.currentTimeMillis());
                admin.setName("admin");
                admin.setPassword("12345678");
                admin.setRole(UserRole.ADMIN);
                fileStorage.save(admin);
            }

            OnlineChatService.setUserStorage(new UserStorage());
            OnlineChatService.setChannelStorage(new ChannelStorage());

            FileStorage<BlackListStorage> blackListStorageFileStorage = new FileStorage<>("black_list");
            BlackListStorage blackListStorage = blackListStorageFileStorage.fetch();

            if (blackListStorage == null) {
                blackListStorage = new BlackListStorage();
            }
            OnlineChatService.setBlackListStorage(blackListStorage);

            while (true) {
                ServerSession serverSession = new ServerSession(
                    clientCounter++,
                    serverSocket.accept()
                );
                serverSession.start();
                serverSession.setUncaughtExceptionHandler(handler);
                clients.add(serverSession);
            }
        } catch (IOException e) {
            Thread.currentThread().interrupt();
        }
        System.exit(0);
    }
}
