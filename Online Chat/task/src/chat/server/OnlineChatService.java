package chat.server;

import chat.shared.BlackListStorage;
import chat.shared.ChannelStorage;
import chat.shared.UserStorage;

public class OnlineChatService {

    private static UserStorage userStorage;
    private static ChannelStorage channelStorage;
    private static BlackListStorage blackListStorage;

    public static UserStorage getUserStorage() {
        return userStorage;
    }

    public static void setUserStorage(UserStorage userStorage) {
        OnlineChatService.userStorage = userStorage;
    }

    public static ChannelStorage getChannelStorage() {
        return channelStorage;
    }

    public static void setChannelStorage(ChannelStorage channelStorage) {
        OnlineChatService.channelStorage = channelStorage;
    }

    public static BlackListStorage getBlackListStorage() {
        return blackListStorage;
    }

    public static void setBlackListStorage(BlackListStorage blackListStorage) {
        OnlineChatService.blackListStorage = blackListStorage;
    }
}
