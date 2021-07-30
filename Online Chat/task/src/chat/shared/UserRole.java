package chat.shared;

import java.io.Serializable;

public enum UserRole implements Serializable {
    ADMIN("admin"),
    MODERATOR("moderator"),
    USER("user");

    private String name;
    UserRole(String name) {
        this.name = name;
    }
}
