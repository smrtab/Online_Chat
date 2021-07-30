package chat.command;

import chat.server.ServerSession;
import chat.shared.UserStorage;

import java.io.IOException;

public class ListCommand implements OnlineChatCommand {

    private ServerSession session;

    public ListCommand(ServerSession session) {
        this.session = session;
    }

    @Override
    public void execute() throws IOException {
        session.showUserList();
    }
}
