package chat.shared;

import chat.server.OnlineChatException;
import chat.server.OnlineChatService;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;

public class ChannelStorage  implements Serializable {

    private List<Channel> list = new ArrayList<>();

    public synchronized void put(Channel channel) {
        this.list.add(channel);
    }

    public synchronized Channel load(User originator, User target) throws IOException, ClassNotFoundException {
        String channelName = Channel.createChannelName(originator.getName(), target.getName());
        List<Channel> channels = this.list
            .stream()
            .filter(item -> item.channelEstablished(channelName))
            .collect(Collectors.toList());

        Channel channel;
        if (channels.size() == 0) {

            FileStorage<Channel> fileStorage = new FileStorage<>(channelName);
            channel = fileStorage.fetch();

            if (channel == null) {
                Set<String> participants = new HashSet<>(Set.of(originator.getName(), target.getName()));
                channel = new Channel(channelName, participants);
            } else {
                channel.wakeup();
            }

            this.put(channel);
        } else {
            channel = channels.get(0);
        }

        return channel;
    }

    public void join(
        User originator,
        User target
    ) throws IOException {
        try {
            if (target != null) {
                Channel channel = this.load(originator, target);
                originator.setActiveChannel(channel);
                target.addChannel(channel);
            } else {
                throw new OnlineChatException("the user is not online!");
            }
        } catch (ClassNotFoundException e) {}
    }

    public void remove(Channel channel) {
        this.list.remove(channel);
    }
}
