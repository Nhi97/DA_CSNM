package client;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.Iterator;
import java.util.Vector;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class MainForm extends javax.swing.JFrame {

    String username;
    String password;
    String host;
    int port;
    Socket socket;
    DataOutputStream dos;
    public boolean attachmentOpen = false;
    private boolean isConnected = false;
    private static String mydownloadfolder = "/home/nhile/NetBeansProjects/Download";

    /**
     * Creates new form MainForm
     */
    public MainForm() {
        initComponents();
        initEvents();
    }

    public void initFrame(String username, String password, String host, int port) {
        this.username = username;
        this.password = password;
        this.host = host;
        this.port = port;
        setTitle("Xin chào: " + username);
        //Kết nối 
        connect();
    }

    public void connect() {
        try {
            socket = new Socket(host, port);
            dos = new DataOutputStream(socket.getOutputStream());

            // gửi username va password den server de kiem tra
            dos.writeUTF("CMD_JOIN " + username);
            //dos.writeUTF(password);

            appendMessage(" Có thể thực hiện các thao tác chat !!!", "Trạng thái", Color.GREEN, Color.GREEN);

            // Khởi động Client Thread 
            new Thread(new ClientThread(socket, this)).start();
            btSend.setEnabled(true);
            // đã được kết nối
            isConnected = true;

        } catch (IOException e) {
            isConnected = false;
            JOptionPane.showMessageDialog(this, "Không thể kết nối đến máy chủ, vui lòng thử lại sau.!", "Kết nối thất bại", JOptionPane.ERROR_MESSAGE);
            appendMessage("[IOException]: " + e.getMessage(), "Lỗi", Color.RED, Color.RED);
        }
    }

    /*Được kết nối*/
    public boolean isConnected() {
        return this.isConnected;
    }

    /*Hiển thị Message khi tuong tac voi server*/
    public void appendMessage(String msg, String header, Color headerColor, Color contentColor) {
        tpChat.setEditable(true);
        getMsgHeader(header, headerColor);
        getMsgContent(msg, contentColor);
        tpChat.setEditable(false);
    }

    /*Tin nhắn chat cua chinh client*/
    public void appendMyMessage(String msg, String header) {
        tpChat.setEditable(true);
        getMsgHeader(header, Color.ORANGE);
        getMsgContent(msg, Color.LIGHT_GRAY);
        tpChat.setEditable(false);
    }

    /*Tiêu đề tin nhắn(Ten cua client)*/
    public void getMsgHeader(String header, Color color) {
        int len = tpChat.getDocument().getLength();
        tpChat.setCaretPosition(len);
        tpChat.setCharacterAttributes(MessageStyle.styleMessageContent(color, "Impact", 13), false);
        tpChat.replaceSelection(header + ":");
    }

    /*Nội dung tin nhắn*/
    public void getMsgContent(String msg, Color color) {
        int len = tpChat.getDocument().getLength();
        tpChat.setCaretPosition(len);
        tpChat.setCharacterAttributes(MessageStyle.styleMessageContent(color, "Arial", 12), false);
        tpChat.replaceSelection(msg + "\n\n");
    }

    public void appendOnlineList(Vector list) {
        sampleOnlineList(list);
    }

    /*
     Hiển thị danh sách đang online
     */
    public void showOnLineList(Vector list) {
        try {
            tpOnlineFriend.setEditable(true);
            tpOnlineFriend.setContentType("text/html");
            StringBuilder sb = new StringBuilder();
            Iterator it = list.iterator();
            sb.append("<html><table>");
            while (it.hasNext()) {
                Object e = it.next();
                URL url = getImageFile();
                Icon icon = new ImageIcon(this.getClass().getResource("/images/online.png"));
                sb.append("<tr><td><b>></b></td><td>").append(e).append("</td></tr>");
                System.out.println("Online: " + e);
            }
            sb.append("</table></body></html>");
            tpOnlineFriend.removeAll();
            tpOnlineFriend.setText(sb.toString());
            tpOnlineFriend.setEditable(false);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /*
     ************************************  Hiển thị danh sách online  *********************************************
     */
    private void sampleOnlineList(Vector list) {
        tpOnlineFriend.setEditable(true);
        tpOnlineFriend.removeAll();
        tpOnlineFriend.setText("");
        Iterator i = list.iterator();
        while (i.hasNext()) {
            Object e = i.next();
            /*  Hiển thị Username Online   */
            JPanel panel = new JPanel();
            panel.setLayout(new FlowLayout(FlowLayout.LEFT));
            panel.setBackground(Color.white);

            Icon icon = new ImageIcon(this.getClass().getResource("/images/online.png"));
            JLabel label = new JLabel(icon);
            label.setText(" " + e);
            panel.add(label);
            int len = tpOnlineFriend.getDocument().getLength();
            tpOnlineFriend.setCaretPosition(len);
            tpOnlineFriend.insertComponent(panel);

            /*  Append Next Line   */
            sampleAppend();
        }
      //  tpOnlineFriend.setEditable(false);
    }

    private void sampleAppend() {
        int len = tpOnlineFriend.getDocument().getLength();
        tpOnlineFriend.setCaretPosition(len);
        tpOnlineFriend.replaceSelection("\n");
    }
    /*
     ************************************  Show Online Sample  *********************************************
     */

    /*
     Get image file path
     */
    public URL getImageFile() {
        URL url = this.getClass().getResource("/images/online.png");
        return url;
    }

    /*
     Set myTitle
     */
    public void setMyTitle(String s) {
        setTitle(s);
    }

    /*
     Phương thức tải get download
     */
    public String getMyDownloadFolder() {
        return this.mydownloadfolder;
    }

    /*
     Phương thức get host
     */
    public String getMyHost() {
        return this.host;
    }

    /*
     Phương thức get Port
     */
    public int getMyPort() {
        return this.port;
    }

    /*
     Phương thức nhận My Username
     */
    public String getMyUsername() {
        return this.username;
    }

    /*
     Cập nhật Attachment 
     */
    public void updateAttachment(boolean b) {
        this.attachmentOpen = b;
    }

    private void initEvents() {
        sendFileEvent();
        logoutEvent();
        downloadEvent();
    }

    private void sendFileEvent() {
        sendFileMenu.addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                if (!attachmentOpen) {
                    FormSendFile sendFile = new FormSendFile();
                    if (sendFile.prepare(username, host, port, MainForm.this)) {
                        sendFile.setLocationRelativeTo(null);
                        sendFile.setVisible(true);
                        attachmentOpen = true;
                    } else {
                        JOptionPane.showMessageDialog(sendFileMenu, "Không thể thiết lập Chia sẻ File tại thời điểm này, xin vui lòng thử lại sau.!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }

        });
    }

    private void logoutEvent() {
        LogoutMenu.addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                int confirm = JOptionPane.showConfirmDialog(null, "Bạn có chắc đăng xuất không ?");
                if (confirm == 0) {

                    try {
                        socket.close();
                        setVisible(false);

                        /*Login Form **/
                        new LoginForm().setVisible(true);

                    } catch (IOException ex) {
                        System.out.println(ex.getMessage());
                    }

                }
            }

        });

    }

    private void downloadEvent() {
        mniDownLoad.addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {

                JFileChooser chooser = new JFileChooser();
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int browse = chooser.showOpenDialog(MainForm.this.mniDownLoad);
                if (browse == chooser.APPROVE_OPTION) {
                    MainForm.mydownloadfolder = chooser.getSelectedFile().toString() + "/";
                }
            }
        });
    }

    /*
     Hàm này sẽ mở 1 file chooser
     */
    public void openFolder() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int open = chooser.showDialog(this, "Save");
        if (open == chooser.APPROVE_OPTION) {
            mydownloadfolder = chooser.getSelectedFile().toString() + "/";
        } else {
            mydownloadfolder = "/home/nhile/NetBeansProjects/Download";
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        tpChat = new javax.swing.JTextPane();
        tfChat = new javax.swing.JTextField();
        jScrollPane3 = new javax.swing.JScrollPane();
        tpOnlineFriend = new javax.swing.JTextPane();
        jLabel1 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        btSend = new javax.swing.JButton();
        btPicture = new javax.swing.JButton();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu3 = new javax.swing.JMenu();
        sendFileMenu = new javax.swing.JMenuItem();
        mniDownLoad = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        LogoutMenu = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(new java.awt.Color(204, 255, 255));

        tpChat.setFont(new java.awt.Font("Arial", 0, 16)); // NOI18N
        jScrollPane1.setViewportView(tpChat);

        tfChat.setFont(new java.awt.Font("Dialog", 0, 16)); // NOI18N
        tfChat.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tfChatActionPerformed(evt);
            }
        });

        tpOnlineFriend.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        tpOnlineFriend.setForeground(new java.awt.Color(120, 14, 3));
        tpOnlineFriend.setAutoscrolls(false);
        tpOnlineFriend.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jScrollPane3.setViewportView(tpOnlineFriend);

        jLabel1.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jLabel1.setText("List Friends:");

        btSend.setFont(new java.awt.Font("Dialog", 1, 16)); // NOI18N
        btSend.setText("Send");
        btSend.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btSendActionPerformed(evt);
            }
        });

        btPicture.setFont(new java.awt.Font("Dialog", 1, 16)); // NOI18N
        btPicture.setText("Send");
        btPicture.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btPictureActionPerformed(evt);
            }
        });

        jMenu3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/sharing.png"))); // NOI18N
        jMenu3.setText("File");
        jMenu3.setFont(new java.awt.Font("Dialog", 1, 16)); // NOI18N
        jMenu3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenu3ActionPerformed(evt);
            }
        });

        sendFileMenu.setFont(new java.awt.Font("Dialog", 1, 16)); // NOI18N
        sendFileMenu.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/sendfile.png"))); // NOI18N
        sendFileMenu.setText("Send File");
        sendFileMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sendFileMenuActionPerformed(evt);
            }
        });
        jMenu3.add(sendFileMenu);

        mniDownLoad.setFont(new java.awt.Font("Dialog", 1, 16)); // NOI18N
        mniDownLoad.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/process.png"))); // NOI18N
        mniDownLoad.setText("DownLoad");
        mniDownLoad.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mniDownLoadActionPerformed(evt);
            }
        });
        jMenu3.add(mniDownLoad);

        jMenuBar1.add(jMenu3);

        jMenu2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/check.png"))); // NOI18N
        jMenu2.setText("Account");
        jMenu2.setFont(new java.awt.Font("Dialog", 1, 16)); // NOI18N

        LogoutMenu.setFont(new java.awt.Font("Dialog", 1, 18)); // NOI18N
        LogoutMenu.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/loggoff.png"))); // NOI18N
        LogoutMenu.setText("Logout");
        LogoutMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                LogoutMenuActionPerformed(evt);
            }
        });
        jMenu2.add(LogoutMenu);

        jMenuBar1.add(jMenu2);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(52, 52, 52)
                        .addComponent(jLabel3))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 169, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 37, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(tfChat, javax.swing.GroupLayout.PREFERRED_SIZE, 379, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btPicture)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btSend))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 379, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(178, 178, 178))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(4, 4, 4)
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 260, Short.MAX_VALUE)
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 171, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 385, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btSend)
                            .addComponent(btPicture)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(tfChat, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void sendFileMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sendFileMenuActionPerformed
        // TODO add your handling code here:

    }//GEN-LAST:event_sendFileMenuActionPerformed

    private void mniDownLoadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mniDownLoadActionPerformed
        // TODO add your handling code here:

    }//GEN-LAST:event_mniDownLoadActionPerformed

    private void jMenu3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenu3ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jMenu3ActionPerformed

    private void LogoutMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_LogoutMenuActionPerformed
        // TODO add your handling code here:

    }//GEN-LAST:event_LogoutMenuActionPerformed

    private void tfChatActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfChatActionPerformed
        // TODO add your handling code here:
        try {
            String content = username + " " + evt.getActionCommand();
            dos.writeUTF("CMD_CHATALL " + content);
            appendMyMessage(" " + evt.getActionCommand(), username);
            tfChat.setText("");
        } catch (IOException e) {
            appendMessage(" Không thể gửi tin nhắn đi bây giờ, không thể kết nối đến Máy Chủ tại thời điểm này, xin vui lòng thử lại sau hoặc khởi động lại ứng dụng này.!", "Lỗi", Color.RED, Color.RED);
        }
    }//GEN-LAST:event_tfChatActionPerformed

    private void btSendActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btSendActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btSendActionPerformed

    private void btPictureActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btPictureActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btPictureActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(MainForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MainForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MainForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MainForm().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem LogoutMenu;
    private javax.swing.JButton btPicture;
    private javax.swing.JButton btSend;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenu jMenu3;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JMenuItem mniDownLoad;
    private javax.swing.JMenuItem sendFileMenu;
    private javax.swing.JTextField tfChat;
    private javax.swing.JTextPane tpChat;
    private javax.swing.JTextPane tpOnlineFriend;
    // End of variables declaration//GEN-END:variables
}
