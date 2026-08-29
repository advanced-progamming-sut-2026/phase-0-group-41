package network;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class NetworkManager {
    private static Socket socket;
    private static ObjectOutputStream out;
    private static ObjectInputStream in;

    public static void connect() {
        try {
            socket = new Socket("localhost", 8080);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
            System.out.println("به سرور بازی متصل شدیم.");
        } catch (Exception e) {
            System.err.println("خطا در اتصال به سرور!");
        }
    }

    public static NetworkMessage sendRequest(NetworkMessage request) {
        try {
            if (socket == null || socket.isClosed()) {
                connect();
            }
            out.writeObject(request);
            out.flush();
            return (NetworkMessage) in.readObject();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}