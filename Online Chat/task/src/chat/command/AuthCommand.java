package chat.command;

import chat.server.ServerSession;
import chat.shared.User;

import java.io.IOException;
import java.util.List;

public class AuthCommand  implements OnlineChatCommand {

    private User user;
    private List<String> params;

    public AuthCommand(User user, List<String> params) {
        this.user = user;
        this.params = params;
    }

    @Override
    public void execute() throws IOException {
        user.auth(params.get(0), params.get(1));
    }
}
