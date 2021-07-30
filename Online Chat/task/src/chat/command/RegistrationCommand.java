package chat.command;

import chat.server.ServerSession;
import chat.shared.User;

import java.io.IOException;
import java.util.List;

public class RegistrationCommand  implements OnlineChatCommand {

    private User user;
    private List<String> params;

    public RegistrationCommand(User user, List<String> params) {
        this.user = user;
        this.params = params;
    }

    @Override
    public void execute() throws IOException {
        user.register(params.get(0), params.get(1));
    }
}