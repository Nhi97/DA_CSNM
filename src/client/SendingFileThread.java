package client;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import javax.swing.JOptionPane;

public class SendingFileThread implements Runnable {
    
    protected Socket socket;
    private DataOutputStream dos;
    protected FormSendFile form;
    protected String pathfile;
    protected String receiver;
    protected String sender;
    private final int BUFFER_SIZE = 1000;
    
    public SendingFileThread(Socket soc, String pathfile, String receiver, String sender, FormSendFile frm){
        this.socket = soc;
        this.pathfile = pathfile;
        this.receiver = receiver;
        this.sender = sender;
        this.form = frm;
    }

    @Override
    public void run() {
        FileInputStream fileInputStream = null;
        try {
            //form.disableGUI(true);
            System.out.println("Give File..!");
            dos = new DataOutputStream(socket.getOutputStream());
            
            /** Write filename, recipient, username  **/
            File file = new File(pathfile);
            byte[] buffer = new byte[(int) file.length()];
            int filesize = buffer.length;
            
            String filename = file.getName();
            dos.writeUTF("CMD_SENDFILE "+ filename.replace(" ", "_") +" "+ filesize +" "+ receiver +" "+ sender);
            System.out.println("From: "+ sender);
            System.out.println("To: "+ receiver);
            
            fileInputStream = new FileInputStream(file);
            while(fileInputStream.read(buffer) >0){
                dos.write(buffer, 0, buffer.length);
            }
//            /** Create an stream **/
//            InputStream input = new FileInputStream(filename);
//            OutputStream output = socket.getOutputStream();
//            /*  Các tiến trình trên màn hình  */
// 
//            // Đọc file 
//            BufferedInputStream bis = new BufferedInputStream(input);
//            /** Tạo một chỗ để chứa file **/
//            byte[] buffer = new byte[BUFFER_SIZE];
//            
//            while((bis.read(buffer)) > 0){
//                //form.updateProgress(p);
//                output.write(buffer, 0, buffer.length);
//            }
            /* Cập nhật AttachmentForm GUI */
            form.setTitle("File downloaded.!");
            form.updateAttachment(false); //  Cập nhật Attachment 
            JOptionPane.showMessageDialog(form, "File sent successful.!", "Successful", JOptionPane.INFORMATION_MESSAGE);
            form.closeThis();
            /* Đóng gửi file */
            dos.flush();
            fileInputStream.close();
            System.out.println("File downloaded..!");
        } catch (IOException e) {
            form.updateAttachment(false); //  Cập nhật Attachment
            System.out.println("[SendFile]: "+ e.getMessage());
        }
    }
}