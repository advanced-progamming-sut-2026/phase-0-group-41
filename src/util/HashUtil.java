package util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class HashUtil {

    private HashUtil() {
    }

    /** رمز عبور را با SHA-256(بخش امتیازی داک) */
    public static String sha256(String input) {
        try {
            //کلاس MessageDigest ابزار بومی جاوا برای توابع هش است. با متد getInstance به جاوا می‌گوییم موتور هشر را بر اساس الگوریتم "SHA-256" آماده و فعال کند.
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                //این بخشِ کلیدی، بایت عددی را به مبنای ۱۶ تبدیل می‌کند
                sb.append(String.format("%02x", b));
                /** x یعنی حروف کوچک هگزادسیمال استفاده شود.
2 یعنی خروجی حتماً ۲ کاراکتر باشد.                  
0 یعنی اگر عدد بایت کوچک بود و خروجی ۱ کاراکتر شد (مثلاً f)، یک صفر پشت آن بگذارد تا تبدیل به 0f شود */               
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 در دسترس نیست", e);
        }
    }
}
