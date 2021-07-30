package chat.shared;

import chat.server.OnlineChatService;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class Channel implements Serializable, Restorable<Channel> {

    private String name;
    protected volatile List<Message> messages = new ArrayList<>();
    private volatile Set<String> participants;
    transient private volatile FileStorage<Channel> fileStorage;
    transient private Thread fileSynchronizer;

    public Channel(String name, Set<String> participants) {
        this.name = name;
        this.participants = participants;
        this.wakeup();
    }

    public synchronized void wakeup() {
        this.fileStorage = new FileStorage<>(this.name, this);

        AtomicLong lastMessageTimestamp = new AtomicLong(System.currentTimeMillis());
        Channel that = this;
        this.fileSynchronizer = new Thread(() -> {
            while (!fileSynchronizer.isInterrupted()) {
                try {
                    List<Message> newMessages = that.getNewMessages(lastMessageTimestamp.get());
                    if (newMessages.size() > 0) {
                        lastMessageTimestamp.set(newMessages.get(newMessages.size() - 1).getTimestamp());
                        this.fileStorage.save();
                    }
                } catch (IOException e) {}
            }
        });
        this.fileSynchronizer.start();
    }

    public static String createChannelName(String... names) {
        Set<String> userSet = new TreeSet<>();
        for (String name: names) {
            userSet.add(name);
        }

        StringBuilder channelNameBuilder = new StringBuilder();
        channelNameBuilder.append("chat");
        userSet.forEach(name -> channelNameBuilder.append("_").append(name));

        return channelNameBuilder.toString();
    }

    public synchronized void put(Message message) {
        this.messages.add(message);
    }

    public synchronized List<Message> getNewMessages(long timestamp) {
        return this.messages
            .stream()
            .filter(item -> item.getTimestamp() > timestamp)
            .collect(Collectors.toList());
    }

    public boolean channelEstablished(String channelName) {
        return this.name.equals(channelName);
    }

    public void leave(User user) throws IOException {
        this.participants.remove(user.getName());
        if (this.isEmpty()) {
            this.fileSynchronizer.interrupt();
            this.fileStorage.save();
        }
    }

    public boolean isEmpty() {
        return this.participants
            .stream()
            .filter(name -> OnlineChatService.getUserStorage().getByName(name) != null)
            .collect(Collectors.toList())
            .isEmpty();
    }

    @Override
    public Channel restore(Channel another) {
        this.setParticipants(another.getParticipants());
        this.setMessages(another.getMessages());
        this.setName(another.getName());
        return this;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<String> getParticipants() {
        return participants;
    }

    public void setParticipants(Set<String> participants) {
        this.participants = participants;
    }

    public synchronized List<Message> getMessages() {
        return messages;
    }

    public synchronized String getInterlocutorOf(String name) {
        return this.participants
            .stream()
            .filter(userName -> !userName.equals(name))
            .collect(Collectors.toList())
            .get(0);
    }

    public synchronized List<Message> getMessages(int count) {
        return this.messages
            .stream()
            .skip(
                this.messages.size() < count
                    ? 0 : this.messages.size() - count
            )
            .limit(25)
            .collect(Collectors.toList());
    }

    public synchronized List<Message> sliceMessages(int count) {
        int lastIndex = this.messages.size();
        int startIndex = lastIndex - count;
        int limit = lastIndex - startIndex >= 25 ? 25 : (lastIndex - startIndex);
        return this.messages.subList(startIndex, startIndex + limit);
    }

    public int getMessagesCount() {
        return this.messages.size();
    }

    public synchronized int getMessagesCount(String name) {
        return this.messages
            .stream()
            .filter(message -> message.getOwner().equals(name))
            .collect(Collectors.toList())
            .size();
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }
}
