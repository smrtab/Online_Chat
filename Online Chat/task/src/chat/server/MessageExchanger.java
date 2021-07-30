package chat.server;

import chat.shared.Channel;
import chat.shared.Message;
import chat.shared.ChannelStorage;
import chat.shared.User;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

public class MessageExchanger extends Thread {

    private final int MAX_MESSAGES = 25;
    private volatile User user;
    private volatile DataOutputStream dataOutputStream;

    MessageExchanger(User user, DataOutputStream dataOutputStream) {
        this.user = user;
        this.dataOutputStream = dataOutputStream;
    }

    private void handleMessages(List<Message> messagesList, boolean init) {
        List<Message> messages = new ArrayList<>(messagesList);
        Stack<Message> stack = new Stack<>();
        Collections.reverse(messages);
        if (!messages.isEmpty()) {
            int newMessagesCount = 0;
            int oldMessagesCount = 0;
            for (Message message: messages) {
                if (user.getLastOnlineTimestamp() < message.getTimestamp()) {
                    newMessagesCount++;
                } else {
                    oldMessagesCount++;
                }

                stack.add(message);

                if (newMessagesCount + oldMessagesCount >= MAX_MESSAGES || oldMessagesCount == 10) {
                    break;
                }
            }
        }

        while (!stack.empty()) {
            Message message = stack.pop();
            try {
                if (init && (user.getLastOnlineTimestamp() < message.getTimestamp())) {
                    outputNew(message.getMessage());
                } else {
                    output(message.getMessage());
                }
            } catch (IOException e) {}
            if (user.getLastOnlineTimestamp() < message.getTimestamp()) {
                user.setLastOnlineTimestamp(message.getTimestamp());
            }
        }
    }

    @Override
    public void run() {
        handleMessages(user.getActiveChannel().getMessages(), true);
        while (!isInterrupted()) {
            handleMessages(user.getActiveChannel().getNewMessages(user.getLastOnlineTimestamp()), false);
        }
    }

    private void output(String message) throws IOException {
        dataOutputStream.writeUTF(message);
    }

    private void outputNew(String message) throws IOException {
        dataOutputStream.writeUTF("(new)" + " " + message);
    }
}