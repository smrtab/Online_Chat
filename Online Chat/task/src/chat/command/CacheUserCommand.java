package chat.command;

import chat.server.OnlineChatService;
import chat.server.ServerSession;
import chat.shared.User;
import chat.shared.UserStorage;

public class CacheUserCommand implements OnlineChatCommand {

    private User user;

    public CacheUserCommand(User user) {
        this.user = user;
    }

    @Override
    public void execute() {
        OnlineChatService.getUserStorage().add(user);
    }
}
