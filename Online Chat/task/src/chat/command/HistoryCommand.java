package chat.command;

import chat.server.OnlineChatException;
import chat.server.ServerSession;

import java.io.IOException;
import java.util.List;

public class HistoryCommand implements OnlineChatCommand {

    private ServerSession session;
    private List<String> params;

    public HistoryCommand(
        ServerSession session,
        List<String> params
    ) {
        this.session = session;
        this.params = params;
    }

    @Override
    public void execute() throws IOException {
        try {
            session.history(Integer.parseInt(params.get(0)));
        } catch (NumberFormatException e) {
            throw new OnlineChatException(String.format("%s is not a number!", params.get(0)));
        }
    }
}
