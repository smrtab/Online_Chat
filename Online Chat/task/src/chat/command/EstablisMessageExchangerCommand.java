package chat.command;

import chat.server.ServerSession;

public class EstablisMessageExchangerCommand implements OnlineChatCommand {

    private ServerSession session;

    public EstablisMessageExchangerCommand(ServerSession session) {
        this.session = session;
    }

    @Override
    public void execute() {
        session.startMessageExchanger();
    }
}
