package chat.command;

import chat.server.ServerSession;

import java.io.IOException;

public class UnreadCommand implements OnlineChatCommand {

    private  ServerSession session;

    public UnreadCommand(
        ServerSession session
    ) {
        this.session = session;
    }

    @Override
    public void execute() throws IOException {
        session.unread();
    }
}
