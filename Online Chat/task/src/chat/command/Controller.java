package chat.command;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Controller {

    private List<OnlineChatCommand> commands = new ArrayList<>();

    public void addCommand(OnlineChatCommand command) {
        this.commands.add(command);
    }

    public void invoke() throws IOException {
        for (OnlineChatCommand command: commands) {
            command.execute();
        }
    }
}
