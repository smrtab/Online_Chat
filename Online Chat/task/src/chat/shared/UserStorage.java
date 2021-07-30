package chat.shared;

import chat.server.OnlineChatException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class UserStorage {

    private List<User> list = new ArrayList<>();

    public synchronized int add(User user) {
        return this.put(user);
    }
    public synchronized int put(User user) {
        this.list.add(user);
        return this.list.indexOf(user);
    }

    public synchronized User getByName(String name) {

        List<User> resultUserList = this.list
            .stream()
            .filter(item -> item.getName().equals(name))
            .collect(Collectors.toList());

        User user = null;
        if (resultUserList.size() > 0) {
            user = resultUserList.get(0);
        }

        return user;
    }

    public synchronized void remove(User user) {
        this.list.remove(user);
    }

    public int size() {
        return this.list.size();
    }

    public String getOnlineUsers(User originator) {
        List<String> names = new ArrayList<>();
        list.stream()
            .filter(user -> user.getName() != originator.getName())
            .collect(Collectors.toList())
            .forEach(user -> names.add(user.getName()));

        return String.join(", ", names);
    }
}
