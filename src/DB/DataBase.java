package DB;

import Client.ViewGuiClient;

import javax.swing.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;

public class DataBase {

    private final String url="jdbc:mysql://localhost:3306/chatdb";
    private final String login="root";
    private final String passw="root";
    private static ViewGuiClient gui;
    private final String password="stumpp";

    private java.sql.Connection connection;

    public Connection getConnection(){
        return connection;
    }

    public Boolean checkReg(String username,String password) throws SQLException     {
        connection = DriverManager.getConnection(url, login, passw);
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("select * from user");
        while(resultSet.next()){
            if(resultSet.getString("username").equals(username)&&resultSet.getString("password").equals(password)){
                return true;
            }
        }
        return false;
    }

    public void getReg(ArrayList<String> f) throws SQLException{
        connection = DriverManager.getConnection(url, login, passw);
        Statement statement = connection.createStatement();
        statement.executeUpdate("INSERT INTO `user` (username,email,age,password) VALUES ('"+ f.get(0) + "','" + f.get(2) + "','"  + f.get(1) + "','" + f.get(3) + "')");
        JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), "Вы успешно зарегестрировались в чате!","Внимание",JOptionPane.INFORMATION_MESSAGE);

    }



}
