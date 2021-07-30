package chat.server;

public enum ClientCommand {
    LIST("list"),
    CHAT("chat"),
    AUTH("auth"){
        @Override
        public String messageSuccess() {
            return "you are authorized successfully!";
        }
    },
    REGISTRATION("registration"){
        @Override
        public String messageSuccess() {
            return "you are registered successfully!";
        }
    },
    GRANT("grant"),
    KICK("kick"),
    REVOKE("revoke"),
    STATS("stats"),
    HISTORY("history"),
    UNREAD("unread"),
    UNDEFINED("undefined");

    private String name;
    ClientCommand(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static ClientCommand getByName(String name) {
        return ClientCommand.valueOf(name.toUpperCase());
    }

    public String messageSuccess() {
        return null;
    }
}
