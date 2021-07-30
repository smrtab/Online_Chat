package chat.server;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InputController {

    private String input;

    private boolean isCommand = false;
    private boolean isMessage = false;
    private ClientCommand command;
    private String message;
    private List<String> commandParams = new ArrayList<>();

    public InputController(String input) {
        this.input = input;
    }

    public InputController parse() {
        if (input.toCharArray()[0] == '/') {

            List<String> words = new ArrayList<>(Arrays.asList(input.split("\\s+")));
            isCommand = true;

            try {
                command = ClientCommand.getByName(words.get(0).replace("/", ""));
            } catch (IllegalArgumentException e) {
                command = ClientCommand.UNDEFINED;
            }

            words.remove(0);
            commandParams.addAll(words);
        } else {
            isMessage = true;
            message = input;
        }

        return this;
    }

    public boolean isCommand() {
        return isCommand;
    }
    public boolean isMessage() {
        return isMessage;
    }
    public ClientCommand getCommand() {
        return command;
    }
    public String getMessage() {
        return message;
    }
    public List<String> getCommandParams() {
        return commandParams;
    }
}
