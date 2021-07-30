package chat.command;

import chat.server.OnlineChatService;
import chat.server.ServerSession;
import chat.shared.User;

import java.io.IOException;

public class ReportMessageCommand implements OnlineChatCommand {

    private ServerSession session;
    private String message;

    public ReportMessageCommand(ServerSession session, String message) {
        this.session = session;
        this.message = message;
    }

    @Override
    public void execute() {
        try {
            session.output(message);
        } catch (IOException e) {}
    }
}
