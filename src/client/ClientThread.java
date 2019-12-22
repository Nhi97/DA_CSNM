package client;

import java.awt.Color;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.swing.JOptionPane;

public class ClientThread implements Runnable {

    Socket socket;
    DataInputStream dis;
    DataOutputStream dos;
    MainForm main;
    StringTokenizer st;

    public ClientThread(Socket socket, MainForm main) {
        this.main = main;
        this.socket = socket;
        try {
            dis = new DataInputStream(socket.getInputStream());
        } catch (IOException e) {
            main.appendMessage("[IOException]: " + e.getMessage(), "Error", Color.RED, Color.RED);
        }
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                String data = dis.readUTF();
                st = new StringTokenizer(data);
                /**
                 * Get Message CMD *
                 */
                String CMD = st.nextToken();
                switch (CMD) {

                    //tin nhan server gui ve cho nhung client con lai trong list
                    case "CMD_MESSAGE":
                        String msg = "";
                        String frm = st.nextToken();
                        while (st.hasMoreTokens()) {
                            msg = msg + " " + st.nextToken();
                        }
                        main.appendMessage(msg, frm, Color.MAGENTA, Color.BLUE);
                        break;

                    //list nhung client co dang nhap
                    case "CMD_ONLINE":
                        Vector online = new Vector();
                        while (st.hasMoreTokens()) {
                            String list = st.nextToken();
                            if (!list.equalsIgnoreCase(main.username)) {
                                online.add(list);
                            }
                        }
                        main.appendOnlineList(online);
                        break;

                    //  hàm này sẽ thông báo đến client rằng có một file nhận, Chấp nhận hoặc từ chối file  
                    case "CMD_FILE_XD":  // Format:  CMD_FILE_XD [sender] [receiver] [filename]
                        String sender = st.nextToken();
                        String receiver = st.nextToken();
                        String fname = st.nextToken();
                        int confirm = JOptionPane.showConfirmDialog(main, "From: " + sender + "\nFile name: " + fname + "\nDo you want to receive this file.?");

                        // client chấp nhận yêu cầu, sau đó thông báo đến sender để gửi file
                        if (confirm == 0) {
                            /* chọn chỗ lưu file   */
                            main.openFolder();
                            try {
                                dos = new DataOutputStream(socket.getOutputStream());
                                // Format:  CMD_SEND_FILE_ACCEPT [ToSender] [Message]
                                String format = "CMD_SEND_FILE_ACCEPT " + sender + " Accept";
                                dos.writeUTF(format);

                                /*  hàm này sẽ tạo một socket filesharing  để tạo một luồng xử lý file đi vào và socket này sẽ tự động đóng khi hoàn thành.  */
                                Socket fSoc = new Socket(main.getMyHost(), main.getMyPort());
                                DataOutputStream fdos = new DataOutputStream(fSoc.getOutputStream());
                                fdos.writeUTF("CMD_SHARINGSOCKET " + main.getMyUsername());

                                /*  Run Thread for this   */
                                new Thread(new ReceivingFileThread(fSoc, main)).start();

                                // Write message send file success
                                String extension = "";

                                int i = fname.lastIndexOf('.');
                                if (i > 0) {
                                    extension = fname.substring(i + 1);
                                }

                                String pathFile = main.getMyDownloadFolder() + fname;
                                System.out.println("Extension file: " + extension);

                                if (extension.equals("jpg")) {
                                    main.appendImage("HIHIHI", sender, Color.yellow, Color.yellow, pathFile);
                                } else {
                                    main.appendMessage("Give you 1 file: " + fname, sender, Color.MAGENTA, Color.BLUE);
                                }

                            } catch (IOException e) {
                                System.out.println("[CMD_FILE_XD]: " + e.getMessage());
                            }
                        } else { // client từ chối yêu cầu, sau đó gửi kết quả tới sender
                            try {
                                dos = new DataOutputStream(socket.getOutputStream());
                                // Format:  CMD_SEND_FILE_ERROR [ToSender] [Message]
                                String format = "CMD_SEND_FILE_ERROR " + sender + " Other person refuse your request or cann't connect.!";
                                dos.writeUTF(format);
                            } catch (IOException e) {
                                System.out.println("[CMD_FILE_XD]: " + e.getMessage());
                            }
                        }
                        break;

                    default:
                        main.appendMessage("[CMDException]: Don't understand statement " + CMD, "CMDException", Color.RED, Color.RED);
                        break;
                }
            }
        } catch (IOException e) {
            main.appendMessage(" Not connect to server, Please try again.!", "Error", Color.RED, Color.RED);
        }
    }
}
