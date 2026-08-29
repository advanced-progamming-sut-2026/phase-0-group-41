package network;

import model.user.UserManager;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private static final int PORT = 8080;

    public static void main(String[] args) {
        // ایجاد یک نمونه مرکزی از سیستم مدیریت کاربران
        UserManager userManager = new UserManager(); 
        
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("سرور روی پورت " + PORT + " راه‌اندازی شد. منتظر اتصال کلاینت‌ها...");
            
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("یک کلاینت جدید متصل شد: " + clientSocket.getInetAddress());
                
                // پاس دادن سوکت و userManager به Thread اختصاصی این کلاینت
                ClientHandler handler = new ClientHandler(clientSocket, userManager);
                new Thread(handler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}