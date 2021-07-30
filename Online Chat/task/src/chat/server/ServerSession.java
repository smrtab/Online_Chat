package chat.server;

import chat.shared.*;
import chat.command.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class ServerSession extends Thread{

    private int sessionId;
    private Socket socket;
    private User user = null;
    private Thread exchanger;
    
    private DataInputStream inputStream;
    private DataOutputStream outputStream;

    public ServerSession(
        int sessionId,
        Socket socket
    ) {
        this.socket = socket;
        this.sessionId = sessionId;
    }

    @Override
    public void run() {
        try (
            DataInputStream dataInputStream
                = new DataInputStream(this.socket.getInputStream());
            DataOutputStream dataOutputStream
                = new DataOutputStream(this.socket.getOutputStream())
        ) {

            inputStream = dataInputStream;
            outputStream = dataOutputStream;
            
            System.out.printf("Client %d connected!%n", this.sessionId);
            this.output("authorize or register");
            while (!isInterrupted()) {
                try {

                    InputController inputController = new InputController(inputStream.readUTF());
                    inputController.parse();

                    if (inputController.isCommand()) {
                        performCommand(
                            inputController.getCommand(),
                            inputController.getCommandParams()
                        ).invoke();
                    } else {
                        userMustBeAuthorizedAndInChat();
                        Message inputMessage = new Message(
                            inputController.getMessage(),
                            System.currentTimeMillis(),
                            this.user.getName()
                        );
                        user.getActiveChannel().put(inputMessage);
                    }
                } catch (OnlineChatException e) {
                    this.output(e.getMessage());
                } catch (OnlineChatAuthException e) {
                    this.output(e.getMessage());
                    user = null;
                } catch (IOException e) {
                    System.out.printf("Client %d disconnected!%n", this.sessionId);
                    exit();
                }
            }

            throw new RuntimeException("Finished");

        } catch (IOException e) {}
    }

    public void output(String message) throws IOException {
        if (message != null) {
            outputStream.writeUTF(String.format("Server: %s", message));
        }
    }

    public void startMessageExchanger() {
        exchanger = new MessageExchanger(user, outputStream);
        exchanger.start();
    }

    public void exit() throws IOException {
        this.closeUserSession();
        this.interrupt();
    }

    public void kicked() throws IOException {
        OnlineChatService.getBlackListStorage().put(user);
        this.output("you have been kicked out of the server!");
        closeUserSession();
        user = null;
    }

    public void granted() throws IOException {
        user.setRole(UserRole.MODERATOR);
        this.output("you are the new moderator now!");
    }

    public void revoked() throws IOException {
        user.setRole(UserRole.USER);
        this.output("you are no longer a moderator!");
    }

    public void closeUserSession() throws IOException {
        if (user == null) {
            return;
        }
        user.leave();
        if (exchanger != null) {
            exchanger.interrupt();
        }
    }

    public void openUserSession() {
        user = new User((int) System.currentTimeMillis());
        user.setServerSession(this);
    }

    public void showUserList() throws IOException {
        String online = OnlineChatService.getUserStorage().getOnlineUsers(user);
        if (online.isEmpty()) {
            this.output("no one online");
        } else {
            this.output(String.format("online: %s", online));
        }
    }

    public void stats() throws IOException {
        String participant = user.getActiveChannel().getInterlocutorOf(user.getName());
        outputStream.writeUTF(String.format("Server:%n" +
                "Statistics with %s:%n" +
                "Total messages: %d%n" +
                "Messages from %s: %d%n" +
                "Messages from %s: %d",
                participant,
                user.getActiveChannel().getMessagesCount(),
                user.getName(), user.getActiveChannel().getMessagesCount(user.getName()),
                participant, user.getActiveChannel().getMessagesCount(participant)
            )
        );
    }

    public void history(int from) throws IOException {
        List<Message> messages = user.getActiveChannel().sliceMessages(from);
        outputStream.writeUTF("Server:");
        for (Message message : messages) {
            outputStream.writeUTF(message.getMessage());
        }
    }

    public void unread() throws IOException {
        HashSet<String> userNames = user.getUsersWithNewMessages();
        if (userNames.isEmpty()) {
            throw new OnlineChatException("no one unread");
        }
        this.output("unread from: " + String.join(" ", userNames));
    }

    public void checkBlacklist() throws IOException {
        if (OnlineChatService.getBlackListStorage().isBlacklisted(user)) {
            throw new OnlineChatException("you are banned!");
        }
    }

    private void activeChannelMustBeEstablished() {
        if (user.getActiveChannel() == null) {
            throw new OnlineChatException("use /list command to choose a user to text!");
        }
    }

    private void userMustBeAuthorized() {
        if (user == null) {
            throw new OnlineChatException("you are not in the chat!");
        }
    }

    private void userMustBeAuthorizedAndInChat() {
        userMustBeAuthorized();
        activeChannelMustBeEstablished();
    }

    private Controller performCommand(
        ClientCommand command,
        List<String> params
    ) throws IOException {

        Controller controller = new Controller();
        switch (command) {
            case LIST: {
                userMustBeAuthorized();
                controller.addCommand(new ListCommand(this));
                break;
            }
            case CHAT: {
                userMustBeAuthorized();
                controller.addCommand(new ChatCommand(user, params));
                controller.addCommand(new EstablisMessageExchangerCommand(this));
                break;
            }
            case GRANT: {
                controller.addCommand(new GrantCommand(user, params));
                controller.addCommand(new ReportMessageCommand(this, String.format("%s is the new moderator!", params.get(0))));
                break;
            }
            case REVOKE: {
                controller.addCommand(new RevokeCommand(user, params));
                controller.addCommand(new ReportMessageCommand(this, String.format("%s is no longer a moderator!", params.get(0))));
                break;
            }
            case KICK: {
                controller.addCommand(new KickCommand(user, params));
                controller.addCommand(new ReportMessageCommand(this, String.format("%s was kicked!", params.get(0))));
                break;
            }
            case STATS: {
                userMustBeAuthorizedAndInChat();
                controller.addCommand(new StatsCommand(this));
                break;
            }
            case HISTORY: {
                userMustBeAuthorizedAndInChat();
                controller.addCommand(new HistoryCommand(this, params));
                break;
            }
            case UNREAD: {
                controller.addCommand(new UnreadCommand(this));
                break;
            }
            case AUTH: {
                this.closeUserSession();
                this.openUserSession();
                controller.addCommand(new AuthCommand(user, params));
                controller.addCommand(new CacheUserCommand(user));
                controller.addCommand(new CheckBlacklistCommand(this));
                controller.addCommand(new ReportMessageCommand(this, "you are authorized successfully!"));
                break;
            }
            case REGISTRATION: {
                this.closeUserSession();
                this.openUserSession();
                controller.addCommand(new RegistrationCommand(user, params));
                controller.addCommand(new CacheUserCommand(user));
                controller.addCommand(new CheckBlacklistCommand(this));
                controller.addCommand(new ReportMessageCommand(this, "you are registered successfully!"));
                break;
            }
            case UNDEFINED: {
                throw new OnlineChatException("incorrect command!");
            }
        }

        return controller;
    }
}
