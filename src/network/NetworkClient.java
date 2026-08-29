package network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class NetworkClient {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int PORT = 8080;

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_ADDRESS, PORT);
             DataOutputStream out = new DataOutputStream(socket.getOutputStream());
             DataInputStream in = new DataInputStream(socket.getInputStream())) {

            System.out.println("به سرور متصل شدیم!");

            // ارسال پیام به سرور
            out.writeUTF("سلام سرور! تست ارتباط شبکه.");

            // دریافت پاسخ از سرور
            String serverResponse = in.readUTF();
            System.out.println("پاسخ سرور: " + serverResponse);

        } catch (IOException e) {
            System.out.println("خطا در اتصال به سرور. مطمئن شو سرور ران شده باشد.");
        }
    }
}