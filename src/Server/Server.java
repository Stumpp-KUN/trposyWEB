package Server;

import Connection.*;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class
Server {
    private ServerSocket serverSocket;
    private static ViewGuiServer gui;
    private static ModelGuiServer model;
    private static volatile boolean ServerStatus = false;

    protected void startServer(int port) {
        try {
            serverSocket = new ServerSocket(port);
            ServerStatus = true;
            gui.refreshDialogWindowServer("Сервер запущен.\n");
        } catch (Exception e) {
            gui.refreshDialogWindowServer("Не удалось запустить сервер.\n");
        }
    }

    protected void stopServer() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                for (Map.Entry<String, Connection> user : model.getAllUsersMultiChat().entrySet()) {
                    user.getValue().close();
                }
                serverSocket.close();
                model.getAllUsersMultiChat().clear();
                gui.refreshDialogWindowServer("Сервер остановлен.\n");
            } else gui.refreshDialogWindowServer("Сервер не запущен - останавливать нечего!\n");
        } catch (Exception e) {
            gui.refreshDialogWindowServer("Остановить сервер не удалось.\n");
        }
    }

    protected void acceptServer() {
        while (true) {
            try {
                Socket socket = serverSocket.accept();
                new ServerThread(socket).start();
            } catch (Exception e) {
                gui.refreshDialogWindowServer("Связь с сервером потеряна.\n");
                break;
            }
        }
    }

    protected void sendMessageAllUsers(Message message) {
        for (Map.Entry<String, Connection> user : model.getAllUsersMultiChat().entrySet()) {
            try {
                user.getValue().send(message);
            } catch (Exception e) {
                gui.refreshDialogWindowServer("Ошибка отправки сообщения всем пользователям!\n");
            }
        }
    }

    public static void main(String[] args) {
        Server server = new Server();
        gui = new ViewGuiServer(server);
        model = new ModelGuiServer();
        gui.initFrameServer();
        while (true) {
            if (ServerStatus) {
                server.acceptServer();
                ServerStatus = false;
            }
        }
    }

    private class ServerThread extends Thread {
        private Socket socket;

        public ServerThread(Socket socket) {
            this.socket = socket;
        }
        private String requestAndAddingUser(Connection connection) {
            while (true) {
                try {
                    //посылаем клиенту сообщение-запрос имени
                    connection.send(new Message(MessageType.REQUEST_NAME_USER));
                    Message responseMessage = connection.receive();
                    String userName = responseMessage.getTextMessage();
                    //получили ответ с именем и проверяем не занято ли это имя другим клиентом
                    if (responseMessage.getTypeMessage() == MessageType.USER_NAME && userName != null && !userName.isEmpty() && !model.getAllUsersMultiChat().containsKey(userName)) {
                        //добавляем имя в мапу
                        model.addUser(userName, connection);
                        Set<String> listUsers = new HashSet<>();
                        for (Map.Entry<String, Connection> users : model.getAllUsersMultiChat().entrySet()) {
                            listUsers.add(users.getKey());
                        }
                        connection.send(new Message(MessageType.NAME_ACCEPTED, listUsers));
                        sendMessageAllUsers(new Message(MessageType.USER_ADDED, userName));
                        return userName;
                    }
                    else connection.send(new Message(MessageType.NAME_USED));
                } catch (Exception e) {
                    gui.refreshDialogWindowServer("Возникла ошибка при запросе и добавлении нового пользователя\n");
                }
            }
        }

        private void messagingBetweenUsers(Connection connection, String userName) {
            while (true) {
                try {
                    Message message = connection.receive();
                    if (message.getTypeMessage() == MessageType.TEXT_MESSAGE) {
                        String textMessage = String.format("%s: %s\n", userName, message.getTextMessage());
                        sendMessageAllUsers(new Message(MessageType.TEXT_MESSAGE, textMessage));
                    }
                    if (message.getTypeMessage() == MessageType.DISABLE_USER) {
                        sendMessageAllUsers(new Message(MessageType.REMOVED_USER, userName));
                        model.removeUser(userName);
                        connection.close();
                        gui.refreshDialogWindowServer(String.format("Пользователь с удаленным доступом %s отключился.\n", socket.getRemoteSocketAddress()));
                        break;
                    }
                } catch (Exception e) {
                    gui.refreshDialogWindowServer(String.format("Произошла ошибка при рассылке сообщения от пользователя %s, либо отключился!\n", userName));
                    break;
                }
            }
        }

        @Override
        public void run() {
            gui.refreshDialogWindowServer(String.format("Подключился новый пользователь с удаленным сокетом - %s.\n", socket.getRemoteSocketAddress()));
            try {
                Connection connection = new Connection(socket);
                String nameUser = requestAndAddingUser(connection);
                messagingBetweenUsers(connection, nameUser);
            } catch (Exception e) {
                gui.refreshDialogWindowServer(String.format("Произошла ошибка при рассылке сообщения от пользователя!\n"));
            }
        }
    }
}