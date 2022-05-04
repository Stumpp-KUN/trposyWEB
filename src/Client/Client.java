package Client;

import Connection.*;
import DB.DataBase;

import javax.xml.crypto.Data;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Socket;
import java.sql.SQLException;
import java.util.*;

public class Client {
    private DataBase db;
    private Connection connection;
    private static ModelGuiClient model;
    private static ViewGuiClient gui;
    private volatile boolean isConnect = false;

    public boolean isConnect() {
        return isConnect;
    }

    public void setConnect(boolean connect) {
        isConnect = connect;
    }

    public static void main(String[] args) {
        Client client = new Client();
        model = new ModelGuiClient();
        gui = new ViewGuiClient(client);
        gui.initFrameClient();
        while (true) {
            if (client.isConnect()) {
                client.nameUserRegistration();
                client.receiveMessageFromServer();
                client.setConnect(false);
            }
        }
    }


    protected void connectToServer() {
        if (!isConnect) {
            while (true) {
                try{
                    String addressServer = gui.getServerAddressFromOptionPane();
                    int port = gui.getPortServerFromOptionPane();
                    Socket socket = new Socket(addressServer, port);
                    connection = new Connection(socket);
                    isConnect = true;
                    gui.addMessage("Сервисное сообщение: Вы подключились к серверу.\n");
                    break;
                } catch (Exception e) {
                    gui.errorDialogWindow("Произошла ошибка! Возможно Вы ввели не верный адрес сервера или порт. Попробуйте еще раз");
                    break;
                }
            }
        } else gui.errorDialogWindow("Вы уже подключены!");
    }

    protected void nameUserRegistration() {
        while (true) {
            try {
                Message message = connection.receive();
                if (message.getTypeMessage() == MessageType.REQUEST_NAME_USER) {
                    String nameUser = gui.getNameUser();
                      String passUser=gui.getUserPassword();
                      DataBase db=new DataBase();
                      Boolean access=db.checkReg(nameUser,passUser);
                      if(access==true){
                          connection.send(new Message(MessageType.USER_NAME, nameUser));
                          connection.send(new Message(MessageType.USER_PASSWORD,passUser));
                          try(FileWriter writer = new FileWriter("C:\\Users\\Asus\\Desktop\\zxc.txt"))
                          {
                              writer.write(nameUser);
                              writer.write(" ");
                          }
                          catch(IOException ex){
                              System.out.println(ex.getMessage());
                          }
                      }
                      else{
                          gui.errorDialogWindow("Данного пользователя нету");
                          message=null;
                          connection.close();
                      }
                }

                if (message.getTypeMessage() == MessageType.NAME_USED) {
                    gui.errorDialogWindow("Данное имя уже используется, введите другое");
                    String nameUser = gui.getNameUser();
                    connection.send(new Message(MessageType.USER_NAME, nameUser));
                }
                if (message.getTypeMessage() == MessageType.NAME_ACCEPTED) {
                    gui.addMessage("Сервисное сообщение: ваше имя принято!\n");
                    model.setUsers(message.getListUsers());
                    break;
                }
            } catch (Exception e) {
                e.printStackTrace();
                gui.errorDialogWindow("Произошла ошибка при регистрации имени. Попробуйте переподключиться");
                try {
                    connection.close();
                    isConnect = false;
                    break;
                } catch (IOException ex) {
                    gui.errorDialogWindow("Ошибка при закрытии соединения");
                }
            }

        }
    }

    protected void getReg()throws SQLException{
        ArrayList<String> val=gui.reg();
            DataBase vd=new DataBase();
            vd.getReg(val);
    }


    protected void sendMessageOnServer(String text) {
        try {
            connection.send(new Message(MessageType.TEXT_MESSAGE, text));
        } catch (Exception e) {
            gui.errorDialogWindow("Ошибка при отправки сообщения");
        }
    }

    protected void receiveMessageFromServer() {
        while (isConnect) {
            try {
                Message message = connection.receive();
                if (message.getTypeMessage() == MessageType.TEXT_MESSAGE) {
                    gui.addMessage(message.getTextMessage());
                }
                if (message.getTypeMessage() == MessageType.USER_ADDED) {
                    model.addUser(message.getTextMessage());
                    gui.refreshListUsers(model.getUsers());
                    gui.addMessage(String.format("Пользователь %s присоединился к чату.\n", message.getTextMessage()));
                }

                if (message.getTypeMessage() == MessageType.REMOVED_USER) {
                    model.removeUser(message.getTextMessage());
                    gui.refreshListUsers(model.getUsers());
                    gui.addMessage(String.format("Пользователь %s покинул чат.\n", message.getTextMessage()));
                }
            } catch (Exception e) {
                gui.errorDialogWindow("Ошибка при приеме сообщения от сервера.");
                setConnect(false);
                gui.refreshListUsers(model.getUsers());
                break;
            }
        }
    }


    protected void disableClient() {
        try {
            if (isConnect) {
                connection.send(new Message(MessageType.DISABLE_USER));
                model.getUsers().clear();
                isConnect = false;
                gui.refreshListUsers(model.getUsers());
            } else gui.errorDialogWindow("Вы уже отключены.");
        } catch (Exception e) {
            gui.errorDialogWindow("Сервисное сообщение: произошла ошибка при отключении.");
        }
    }
}
