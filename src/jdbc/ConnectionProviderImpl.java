package jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionProviderImpl implements ConnectionProvider {

    @Override
    public Connection getConnection() {

        Connection conn = null;

        final String user = "root";
        final String password = "Thanh144";
        // hard code
        final String url = "jdbc:mysql://localhost:3306/user_chatroom?useSSL=false&autoReconnect=true";
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(url, user, password);
            System.out.println("Da ket noi thanh cong");
        } catch (ClassNotFoundException | SQLException e) {
            System.out.println("Loi ko ket noi database");
        }
        return conn;
    }
}
