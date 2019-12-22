package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import server.MainForm;

public class ServerThread implements Runnable {

    ServerSocket server;
    MainForm main;
    boolean keepGoing = true;

    public ServerThread(int port, MainForm main) {
        main.appendMessage("[Server]: Server is activing at port " + port);
        try {
            this.main = main;
            server = new ServerSocket(port);
            main.appendMessage("[Server]: Server is active.!");
        } catch (IOException e) {
            main.appendMessage("[IOException]: " + e.getMessage());
        }
    }

    @Override
    public void run() {
        try {
            while (keepGoing) {
                Socket socket = server.accept();

                /*SOcket thread*/
                new Thread(new SocketThread(socket, main)).start();
            }
        } catch (IOException e) {
        }
    }

    public void stop() {
        try {
            server.close();
            keepGoing = false;
            System.out.println("Server close..!");
            System.exit(0);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

}
