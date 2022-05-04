package Server;

import Connection.Connection;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ModelGuiServer {

    private Map<String, Connection> allUsersMultiChat = new HashMap<>();


    protected Map<String, Connection> getAllUsersMultiChat() {
        return allUsersMultiChat;
    }

    protected void addUser(String nameUser, Connection connection) {
        allUsersMultiChat.put(nameUser, connection);
    }

    protected void removeUser(String nameUser) {
        allUsersMultiChat.remove(nameUser);
    }

}