package chat.shared;

import chat.shared.User;

import java.io.Serializable;

public final class Message implements Serializable {

    private String message;
    private long timestamp;
    private volatile String owner;

    public Message(
        String message,
        long timestamp,
        String owner
    ) {
        this.owner = owner;
        this.timestamp = timestamp;
        this.message = new StringBuilder()
            .append(this.owner)
            .append(": ")
            .append(message)
            .toString();
    }

    public String getMessage() {
        return message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }
}
