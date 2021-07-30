package chat.shared;

import chat.server.OnlineChatAuthException;
import chat.server.OnlineChatException;
import chat.server.OnlineChatService;
import chat.server.ServerSession;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class User implements Serializable, Restorable<User> {

    private final static int MIN_PASSWORD_LENGTH = 6;

    private int id;
    private String name = null;
    private int password;
    private UserRole role = UserRole.USER;
    private Map<String, Long> channels = new HashMap<>();
    private Channel activeChannel;
    private transient ServerSession serverSession;

    public User(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password.hashCode();
    }

    public void setHashPassword(int password) {
        this.password = password;
    }

    public Map<String, Long> getChannels() {
        return channels;
    }

    public void setChannels(Map<String, Long> channels) {
        this.channels = channels;
    }

    public void addChannel(Channel channel) {
        this.channels.putIfAbsent(channel.getName(), 0L);
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public boolean isAdmin() {
        return this.role == UserRole.ADMIN;
    }

    public boolean isModerator() {
        return this.role == UserRole.MODERATOR;
    }

    public boolean isCorrectPassword(String password) {
        return this.password == password.hashCode();
    }

    public ServerSession getServerSession() {
        return serverSession;
    }

    public void setServerSession(ServerSession serverSession) {
        this.serverSession = serverSession;
    }

    public Channel getActiveChannel() {
        return activeChannel;
    }

    public void setActiveChannel(Channel activeChannel) throws IOException {
        this.activeChannel = activeChannel;
        channels.putIfAbsent(activeChannel.getName(), 0L);
        save();
    }

    public long getLastOnlineTimestamp() {
        return channels.get(activeChannel.getName());
    }

    public void setLastOnlineTimestamp(long timestamp) {
        channels.replace(activeChannel.getName(), timestamp);
    }

    private void save() throws IOException {
        FileStorage<User> fileStorage = new FileStorage<>(name, this);
        fileStorage.save();
    }

    private User fetch() {
        FileStorage<User> fileStorage = new FileStorage<>(name);
        return fileStorage.fetch();
    }

    private User fetch(String name) {
        FileStorage<User> fileStorage = new FileStorage<>(name);
        return fileStorage.fetch();
    }

    public void auth(String name, String password) throws IOException {
        User user = fetch(name);

        if (user == null) {
            throw new OnlineChatAuthException("incorrect login!");
        } else if (!user.isCorrectPassword(password)){
            throw new OnlineChatAuthException("incorrect password!");
        } else if (OnlineChatService.getBlackListStorage().isBlacklisted(user)) {
            throw new OnlineChatAuthException("you are banned!");
        } else {
            this.restore(user);
        }
    }

    public void register(String name, String password) throws IOException {
        User user = fetch(name);
        if (user != null) {
            throw new OnlineChatAuthException("this login is already taken! Choose another one.");
        } else {

            if (password.length() < MIN_PASSWORD_LENGTH) {
                throw new OnlineChatAuthException("the password is too short!");
            }

            this.setName(name);
            this.setPassword(password);

            save();
        }
    }

    public void leave() throws IOException {

        OnlineChatService.getUserStorage().remove(this);
        save();

        if (activeChannel == null) {
            return;
        }

        activeChannel.leave(this);
        if (activeChannel.isEmpty()) {
            OnlineChatService.getChannelStorage().remove(activeChannel);
        }
    }

    public void grant(User user) throws IOException {
        this.mustBeAdmin();
        user.mustNotBeModerator();
        user.getServerSession().granted();
    }

    public void revoke(User user) throws IOException {
        this.mustBeAdmin();
        user.mustBeModerator();
        user.getServerSession().revoked();
    }

    public void kick(User user) throws IOException {
        this.mustBeAdminOrModerator();
        this.mustNotBeEqual(user);
        user.getServerSession().kicked();
    }

    private void mustBeAdmin() {
        if (!this.isAdmin()) {
            throw new OnlineChatException("you are not an admin!");
        }
    }

    private void mustBeModerator() {
        if (!this.isModerator()) {
            throw new OnlineChatException("this user is not a moderator!");
        }
    }

    private void mustNotBeModerator() {
        if (this.isModerator()) {
            throw new OnlineChatException("this user is already a moderator!");
        }
    }

    private void mustNotBeEqual(User user) {
        if (this.equals(user)) {
            throw new OnlineChatException("you can't kick yourself!");
        }
    }

    private void mustBeAdminOrModerator() {
        if (!this.isAdmin() && !this.isModerator()) {
            throw new OnlineChatException("you are not a moderator or an admin!");
        }
    }

    public HashSet<String> getUsersWithNewMessages() {
        HashSet<String> names = new HashSet<>();
        Map<String, Long> userChannels = getChannels();
        for (String channelName : userChannels.keySet()) {
            FileStorage<Channel> fileStorage = new FileStorage<>(channelName);
            Channel channel = fileStorage.fetch();
            if (!channel.getNewMessages(userChannels.get(channelName)).isEmpty()) {
                names.add(channel.getInterlocutorOf(getName()));
            }
        }
        return names;
    }

    @Override
    public User restore(User user) {
        this.setId(user.getId());
        this.setName(user.getName());
        this.setHashPassword(user.getPassword());
        this.setChannels(user.getChannels());
        this.setRole(user.getRole());
        return this;
    }
}
