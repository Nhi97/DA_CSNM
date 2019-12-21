package client;

import client.MainForm;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.StringTokenizer;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

public class FormSendFile extends javax.swing.JFrame {

    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;
    private String myusername;
    private String host;
    private int port;
    private StringTokenizer st;
    private String sendTo;
    private String file;
    private MainForm main;
    private Object chooser;

    // Tạo một form SendFile
    public FormSendFile() {
        initComponents();
        initEvents();

    }

    /*
     Phương thức này được gọi đến khi người dùng click vào menu “Gửi File”, 
     sau đó kết nối đến Server với từ khoá "CMD_SHARINGSOCKET" + username
     */
    public boolean prepare(String username, String host, int port, MainForm m) {
        this.host = host;
        this.myusername = username;
        this.port = port;
        this.main = m;
        /*  Kết nối đến Server  */
        try {
            socket = new Socket(host, port);
            dos = new DataOutputStream(socket.getOutputStream());
            dis = new DataInputStream(socket.getInputStream());

            //  Format: CMD_SHARINGSOCKET [sender]
            String format = "CMD_SHARINGSOCKET " + myusername;
            dos.writeUTF(format);
            System.out.println("format: " + format);

            /*  Khởi động SendFile Thread    */
            new Thread(new SendFileThread(this)).start();
            return true;
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return false;
    }

    /*Phương thức SendFileThread này sẽ gửi yêu cầu chuyển dữ liệu đến Server
     (Khi nhấn nút "Gửi File") để kiểm tra file gửi lên có hợp lệ hay ko*/
    class SendFileThread implements Runnable {

        private FormSendFile form;

        public SendFileThread(FormSendFile form) {
            this.form = form;
        }

        private void closeMe() {
            try {
                socket.close();
            } catch (IOException e) {
                System.out.println("[closeMe]: " + e.getMessage());
            }
            dispose();
        }

        @Override
        public void run() {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    // Đọc nội dung của dữ liệu được nhận từ Server
                    String data = dis.readUTF();
                    st = new StringTokenizer(data);

                    //  Lấy chữ đầu tiên từ dữ liệu
                    String cmd = st.nextToken();
                    switch (cmd) {
                        // Định dạng: CMD_RECEIVE_FILE_ERROR [Message]
                        case "CMD_RECEIVE_FILE_ERROR":
                            String msg = "";
                            while (st.hasMoreTokens()) {
                                msg = msg + " " + st.nextToken();
                            }
                            form.updateAttachment(false);
                            JOptionPane.showMessageDialog(FormSendFile.this, msg, "Lỗi", JOptionPane.ERROR_MESSAGE);
                            this.closeMe();
                            break;

                        case "CMD_RECEIVE_FILE_ACCEPT":  // Định dạng: CMD_RECEIVE_FILE_ACCEPT [Message]

                            /*Server đã chấp nhận file muốn gửi. Bắt đầu khởi động thread File đính kèm*/
                            new Thread(new SendingFileThread(socket, file, sendTo, myusername, FormSendFile.this)).start();
                            break;

                        case "CMD_SENDFILEERROR":
                            String emsg = "";
                            while (st.hasMoreTokens()) {
                                emsg = emsg + " " + st.nextToken();
                            }
                            System.out.println(emsg);
                            JOptionPane.showMessageDialog(FormSendFile.this, emsg, "Lỗi", JOptionPane.ERROR_MESSAGE);
                            form.updateAttachment(false);
//                            form.disableGUI(false);
//                            form.updateBtn("Gửi File");
                            break;

                        case "CMD_SENDFILERESPONSE":

                            //Format: CMD_SENDFILERESPONSE [username] [Message]
                            String rReceiver = st.nextToken();
                            String rMsg = "";
                            while (st.hasMoreTokens()) {
                                rMsg = rMsg + " " + st.nextToken();
                            }
                            form.updateAttachment(false);
                            JOptionPane.showMessageDialog(FormSendFile.this, rMsg, "Lỗi", JOptionPane.ERROR_MESSAGE);
                            dispose();
                            break;
                    }
                }
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private void initEvents() {
        btBrowerEvent();
        btSendFileEvent();
    }

    private void btBrowerEvent() {
        btnBrowse.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                showOpenDialog();
            }
        });
    }

    public void showOpenDialog() {
        JFileChooser chooser = new JFileChooser();
        int intval = chooser.showOpenDialog(this);
        if (intval == chooser.APPROVE_OPTION) {
            txtFile.setText(chooser.getSelectedFile().toString());
        } else {
            txtFile.setText("");
        }
    }

    private void btSendFileEvent() {
        
        btnSendFile.addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                sendTo = txtSendTo.getText();
                file = txtFile.getText();
                if ((sendTo.length() > 0) && (file.length() > 0)) {

                    try {
                        // Format: CMD_SEND_FILE_XD [sender] [receiver] [filename]
                        //txtFile.setText("");
                        String filename = getThisFilename(file);
                        String format = "CMD_SEND_FILE_XD " + myusername + " " + sendTo + " " + filename;
                        dos.writeUTF(format);
                        System.out.println(format);

                    } catch (IOException ex) {
                        System.out.println(ex.getMessage());
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "Không để trống.!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }

        });
    }

    /*  Hàm này sẽ nhận Filename */
    public String getThisFilename(String path) {
        File p = new File(path);
        String fname = p.getName();
        return fname.replace(" ", "_");
    }

    /*   Hàm này sẽ đóng Form  */
    protected void closeThis() {
        dispose();
    }

    /*  Cập nhật file đính kèm   */
    public void updateAttachment(boolean b) {
        main.updateAttachment(b);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        txtFile = new javax.swing.JTextField();
        btnBrowse = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        txtSendTo = new javax.swing.JTextField();
        btnSendFile = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Dịch vụ Gửi File - QchatApp");

        jLabel1.setFont(new java.awt.Font("Dialog", 1, 18)); // NOI18N
        jLabel1.setText("Choose File :");

        txtFile.setEditable(false);
        txtFile.setBackground(new java.awt.Color(255, 255, 255));
        txtFile.setFont(new java.awt.Font("Dialog", 0, 16)); // NOI18N
        txtFile.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        txtFile.setPreferredSize(new java.awt.Dimension(366, 50));
        txtFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtFileActionPerformed(evt);
            }
        });

        btnBrowse.setFont(new java.awt.Font("Dialog", 1, 18)); // NOI18N
        btnBrowse.setText("Brower");
        btnBrowse.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBrowseActionPerformed(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font("Dialog", 1, 18)); // NOI18N
        jLabel2.setText("Send to :");

        txtSendTo.setFont(new java.awt.Font("Dialog", 0, 16)); // NOI18N
        txtSendTo.setPreferredSize(new java.awt.Dimension(366, 50));

        btnSendFile.setBackground(new java.awt.Color(132, 6, 6));
        btnSendFile.setFont(new java.awt.Font("Dialog", 1, 18)); // NOI18N
        btnSendFile.setForeground(new java.awt.Color(255, 255, 255));
        btnSendFile.setText("Send File");
        btnSendFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSendFileActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addComponent(btnSendFile))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(txtSendTo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(txtFile, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnBrowse)))
                        .addGap(37, 37, 37))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addComponent(jLabel1)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtFile, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnBrowse))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 30, Short.MAX_VALUE)
                .addComponent(jLabel2)
                .addGap(26, 26, 26)
                .addComponent(txtSendTo, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(38, 38, 38)
                .addComponent(btnSendFile, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void txtFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtFileActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtFileActionPerformed

    private void btnBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBrowseActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnBrowseActionPerformed

    private void btnSendFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSendFileActionPerformed

    }//GEN-LAST:event_btnSendFileActionPerformed

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
            java.util.logging.Logger.getLogger(FormSendFile.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(FormSendFile.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(FormSendFile.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(FormSendFile.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new FormSendFile().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBrowse;
    private javax.swing.JButton btnSendFile;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JTextField txtFile;
    private javax.swing.JTextField txtSendTo;
    // End of variables declaration//GEN-END:variables
}
