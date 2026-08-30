package network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * لایه‌ی مشترک ارتباط کلاینت با سرور (Server-Client) طبق داکیومنت فاز ۳.
 * تمام کنترلرهایی که نیاز به ارتباط با سرور دارند (Login/Register/...) باید
 * فقط از همین کلاس استفاده کنند تا یک اتصال TCP واحد و قابل اعتماد نگه‌داری شود.
 *
 * نکته‌ی مهم: تمام متدها synchronized شده‌اند چون این کلاس Static/Singleton است
 * و ممکن است از چند بخش برنامه (مثلاً UI Thread و Thread ذخیره‌سازی خودکار)
 * به صورت هم‌زمان صدا زده شود؛ بدون synchronized دو درخواست هم‌زمان می‌توانستند
 * پاسخ همدیگر را بخوانند و باعث قفل‌شدن یا کرش کلاینت شوند.
 */
public class NetworkManager {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 8080;

    private static Socket socket;
    private static ObjectOutputStream out;
    private static ObjectInputStream in;

    private NetworkManager() {
    }

    public static synchronized void connect() {
        try {
            socket = new Socket(SERVER_HOST, SERVER_PORT);
            // ترتیب مهم است: ابتدا OutputStream ساخته و flush شود، سپس InputStream؛
            // در غیر این صورت هر دو طرف (کلاینت و سرور که همین ترتیب را رعایت کرده)
            // در انتظار هدر ObjectInputStream می‌مانند و ارتباط Deadlock می‌شود.
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());
            System.out.println("به سرور بازی متصل شدیم.");
        } catch (Exception e) {
            System.err.println("خطا در اتصال به سرور! مطمئن شوید سرور اجرا شده باشد.");
            closeQuietly();
        }
    }

    public static synchronized boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

    /**
     * یک درخواست را برای سرور می‌فرستد و منتظر پاسخ می‌ماند.
     * در صورت قطع بودن اتصال، ابتدا تلاش می‌کند وصل شود.
     * در صورت بروز هرگونه خطای شبکه، اتصال بسته می‌شود تا درخواست بعدی
     * یک اتصال تازه بسازد (به‌جای تلاش روی یک Socket خراب).
     */
    public static synchronized NetworkMessage sendRequest(NetworkMessage request) {
        try {
            if (!isConnected()) {
                connect();
            }
            if (!isConnected()) {
                return errorMessage(request, "ERR_CONNECTION_FAILED");
            }
            out.writeObject(request);
            out.flush();
            out.reset(); // جلوگیری از کش‌شدن نسخه‌ی قدیمی آبجکت‌ها بین درخواست‌های پیاپی
            Object response = in.readObject();
            if (response instanceof NetworkMessage) {
                return (NetworkMessage) response;
            }
            return errorMessage(request, "ERR_INVALID_RESPONSE");
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("خطا در ارتباط با سرور: " + e.getMessage());
            closeQuietly();
            return errorMessage(request, "ERR_CONNECTION_FAILED");
        }
    }

    private static NetworkMessage errorMessage(NetworkMessage request, String code) {
        NetworkMessage response = new NetworkMessage(request != null ? request.command : "UNKNOWN");
        response.success = false;
        response.responseBody = code;
        return response;
    }

    public static synchronized void disconnect() {
        closeQuietly();
    }

    private static void closeQuietly() {
        try {
            if (socket != null) socket.close();
        } catch (IOException ignored) {
        }
        socket = null;
        out = null;
        in = null;
    }
}