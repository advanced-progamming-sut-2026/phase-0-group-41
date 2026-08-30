package network;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ثبت‌کننده‌ی ساده و Thread-Safe کاربرانِ آنلاین.
 *
 * طبق سند فاز ۳ («اگر کاربر مقصد آنلاین باشد، یک پنجره Pop-up برای او نمایش
 * داده شود») سرور باید بداند در هر لحظه چه کسی آنلاین است. این کلاس فقط
 * زیرساخت پایه (نگاشت نام‌کاربری -> ClientHandler فعال) را فراهم می‌کند؛
 * قابلیت کامل «انتخاب رقیب» و صف بازی تصادفی برای مینی‌گیم «من، زامبی»
 * یک ویژگی بزرگ‌تر و جداگانه است که باید روی همین پایه ساخته شود.
 */
public final class OnlineUsers {

    private static final Map<String, ClientHandler> ONLINE = new ConcurrentHashMap<>();

    private OnlineUsers() {
    }

    public static void setOnline(String username, ClientHandler handler) {
        if (username == null) return;
        ONLINE.put(username, handler);
    }

    public static void setOffline(String username) {
        if (username == null) return;
        ONLINE.remove(username);
    }

    public static boolean isOnline(String username) {
        return username != null && ONLINE.containsKey(username);
    }

    public static ClientHandler getHandler(String username) {
        return ONLINE.get(username);
    }
}
