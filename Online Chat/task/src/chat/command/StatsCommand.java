package chat.command;

import chat.server.OnlineChatService;
import chat.server.ServerSession;
import chat.shared.User;

import java.io.IOException;
import java.util.List;

public class StatsCommand implements OnlineChatCommand {

    private  ServerSession session;

    public StatsCommand(
        ServerSession session
    ) {
        this.session = session;
    }

    @Override
    public void execute() throws IOException {
        session.stats();
    }
}
