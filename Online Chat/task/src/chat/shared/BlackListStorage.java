package chat.shared;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class BlackListStorage implements Serializable {

    private volatile Map<String, Long> map = new HashMap<>();

    public synchronized void add(User user) throws IOException {
        this.put(user);
    }
    public synchronized void put(User user) throws IOException {
        this.map.put(user.getName(), System.currentTimeMillis());
        FileStorage<BlackListStorage> fileStorage = new FileStorage<>("black_list", this);
        fileStorage.save();
    }

    public boolean isBlacklisted(User user) throws IOException {
        return this.map.containsKey(user.getName()) && !isExpired(user);
    }

    public synchronized boolean remove(User user) throws IOException {
        if (!this.map.containsKey(user.getName())) {
            return false;
        }
        this.map.remove(user.getName());
        FileStorage<BlackListStorage> fileStorage = new FileStorage<>("black_list", this);
        fileStorage.save();
        return true;
    }

    private synchronized boolean isExpired(User user) throws IOException {
        Long diff = System.currentTimeMillis() - this.map.get(user.getName());
        if (TimeUnit.MILLISECONDS.toMinutes(diff) >= 5) {
            remove(user);
            return true;
        }
        return false;
    }
}
