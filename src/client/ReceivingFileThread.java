package client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.StringTokenizer;
import javax.swing.JOptionPane;

public class ReceivingFileThread implements Runnable {

    protected Socket socket;
    protected DataInputStream dis;
    protected DataOutputStream dos;
    protected MainForm main;
    protected StringTokenizer st;

    public ReceivingFileThread(Socket soc, MainForm m) {
        this.socket = soc;
        this.main = m;
        try {
            dos = new DataOutputStream(socket.getOutputStream());
            dis = new DataInputStream(socket.getInputStream());
        } catch (IOException e) {
            System.out.println("[ReceivingFileThread]: " + e.getMessage());
        }
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                String data = dis.readUTF();
                st = new StringTokenizer(data);
                String CMD = st.nextToken();

                switch (CMD) {

                    //   hàm này sẽ xử lý việc nhận một file trong một tiến trình nền xử lý từ một user khác
                    case "CMD_SENDFILE":
                        String consignee = null;
                        DataInputStream dis = null;
                        try {
                            String filename = st.nextToken();
                            int filesize = Integer.parseInt(st.nextToken());

                            // Get the Sender Username
                            consignee = st.nextToken();
                            main.setMyTitle("Downloading File....");
                            System.out.println("Downloading File....");
                            System.out.println("From: " + consignee);
                            System.out.println("file name: " + filename);
                            String path = main.getMyDownloadFolder() + filename;
                            System.out.println("path: " + path);

                            /*  Creat Stream   */
                            FileOutputStream fos = new FileOutputStream(path);
                            dis = new DataInputStream(socket.getInputStream());
                            byte[] buffer = new byte[filesize];
                            int read = 0;
                            int totalRead = 0;
                            int remaining = filesize;
                            while ((read = dis.read(buffer, 0, Math.min(buffer.length, remaining))) > 0) {
                                totalRead += read;
                                remaining -= read;
                                System.out.println("read " + totalRead + " bytes.");
                                System.out.println("remain " + remaining + " bytes.");
                                fos.write(buffer, 0, read);
                            }

                            fos.flush();
                            fos.close();
                            main.setMyTitle("You are logged in as: " + main.getMyUsername());
                            JOptionPane.showMessageDialog(null, "File downloaded to \n'" + path + "'");
                            System.out.println("File saved: " + path);
                        } catch (IOException e) {
                            /*
                             Gửi lại thông báo lỗi đến sender
                             Định dạng: CMD_SENDFILERESPONSE [username] [Message]
                             */
                            DataOutputStream eDos = new DataOutputStream(socket.getOutputStream());
                            eDos.writeUTF("CMD_SENDFILERESPONSE " + consignee + " Cann't connect, Please try again.!");

                            System.out.println(e.getMessage());
                            main.setMyTitle("You are login with name: " + main.getMyUsername());
                            JOptionPane.showMessageDialog(main, e.getMessage(), "Exception", JOptionPane.ERROR_MESSAGE);
                            socket.close();
                        }
                        break;
                }
            }
        } catch (IOException e) {
            System.out.println("[ReceivingFileThread]: " + e.getMessage());
        }
    }
}
