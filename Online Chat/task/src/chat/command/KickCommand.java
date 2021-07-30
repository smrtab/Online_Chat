package chat.command;

import chat.server.OnlineChatService;
import chat.shared.User;

import java.io.IOException;
import java.util.List;

public class KickCommand implements OnlineChatCommand {

    private User user;
    private List<String> params;

    public KickCommand(
        User user,
        List<String> params
    ) {
        this.user = user;
        this.params = params;
    }

    @Override
    public void execute() throws IOException {
        User target = OnlineChatService.getUserStorage().getByName(params.get(0));
        user.kick(target);
    }
}
