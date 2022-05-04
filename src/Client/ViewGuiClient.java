package Client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ViewGuiClient {
    private final Client client;
    private JFrame frame = new JFrame("Веб-чат!");
    private JTextArea messages = new JTextArea(30, 20);
    private JTextArea users = new JTextArea(30, 15);
    private JPanel panel = new JPanel();
    private JTextField textField = new JTextField(40);
    private JButton buttonDisable = new JButton("Отключиться");
    private JButton buttonConnect = new JButton("Подключиться");
    private JButton buttonReg=new JButton("Зарегестирроваться");
    private JButton buttonAdm=new JButton("Админ");

    public ViewGuiClient(Client client) {
        this.client = client;
    }



    protected void initFrameClient() {
        messages.setEditable(false);
        users.setEditable(false);
        frame.add(new JScrollPane(messages), BorderLayout.CENTER);
        frame.add(new JScrollPane(users), BorderLayout.EAST);
        panel.add(textField);
        panel.add(buttonConnect);
        panel.add(buttonDisable);
        panel.add(buttonReg);
        frame.add(panel, BorderLayout.SOUTH);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (client.isConnect()) {
                    client.disableClient();
                }
                System.exit(0);
            }
        });
        frame.setVisible(true);
        buttonDisable.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                client.disableClient();
            }
        });
        buttonReg.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    client.getReg();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        });
        buttonConnect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                client.connectToServer();
            }
        });
        textField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                client.sendMessageOnServer(textField.getText());
                textField.setText("");
            }
        });
    }

    protected void addMessage(String text) {
        messages.append(text);
    }

    protected void refreshListUsers(Set<String> listUsers) {
        users.setText("");
        if (client.isConnect()) {
            StringBuilder text = new StringBuilder("Список пользователей:\n");
            for (String user : listUsers) {
                text.append(user + "\n");
            }
            users.append(text.toString());
        }
    }

    protected String getServerAddressFromOptionPane() {
        while (true) {
            String addressServer = JOptionPane.showInputDialog(
                    frame, "Введите адрес сервера:",
                    "Ввод адреса",
                    JOptionPane.QUESTION_MESSAGE
            );
            return addressServer.trim();
        }
    }

    protected int getPortServerFromOptionPane() {
        while (true) {
            String port = JOptionPane.showInputDialog(
                    frame, "Введите порт сервера:",
                    "Ввод порта сервера",
                    JOptionPane.QUESTION_MESSAGE
            );
            try {
                return Integer.parseInt(port.trim());
            } catch (Exception e) {
                JOptionPane.showMessageDialog(
                        frame, "Введен неккоректный порт сервера. Попробуйте еще раз.",
                        "Ошибка ввода порта сервера", JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }

    protected String getNameUser() {
        return JOptionPane.showInputDialog(
                frame, "Введите имя пользователя:",
                "Ввод имени пользователя",
                JOptionPane.QUESTION_MESSAGE
        );
    }
    protected String getEmail() {
        return JOptionPane.showInputDialog(
                frame, "Введите вашу почту:",
                "Ввод почты пользователя",
                JOptionPane.QUESTION_MESSAGE
        );
    }
    protected String getAge() {
        return JOptionPane.showInputDialog(
                frame, "Введите ваш возраст:",
                "Ввод возраста пользователя",
                JOptionPane.QUESTION_MESSAGE
        );
    }

    protected String getID() {
        return JOptionPane.showInputDialog(
                frame, "Введите айди пользователя:",
                "УДАЛЕНИЯ ПОЛЬЗОВАТЕЛЯ ИЗ ЧАТА",
                JOptionPane.QUESTION_MESSAGE
        );
    }

    protected String getUserPassword(){
        return JOptionPane.showInputDialog(frame,"Введите пароль: ","Ввод пароля",JOptionPane.QUESTION_MESSAGE );
    }

    protected void errorDialogWindow(String text) {
        JOptionPane.showMessageDialog(
                frame, text,
                "Ошибка", JOptionPane.ERROR_MESSAGE
        );
    }

    protected ArrayList<String> reg(){
        ArrayList<String> val=new ArrayList<>();
        val.add(getNameUser());
        val.add(getAge());
        val.add(getEmail());
        val.add(getUserPassword());
        return val;
    }


}
