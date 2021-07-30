package chat.command;

import chat.server.OnlineChatService;
import chat.server.ServerSession;
import chat.shared.Channel;
import chat.shared.ChannelStorage;
import chat.shared.User;

import java.io.IOException;
import java.util.List;

public class ChatCommand implements OnlineChatCommand {

    private User originator;
    private List<String> params;

    public ChatCommand(
        User originator,
        List<String> params
    ) {
        this.originator = originator;
        this.params = params;
    }

    @Override
    public void execute() throws IOException {
        User target = OnlineChatService.getUserStorage().getByName(params.get(0));
        OnlineChatService.getChannelStorage().join(
            originator,
            target
        );
    }
}
