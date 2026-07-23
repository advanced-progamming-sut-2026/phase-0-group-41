package model.user;

import java.io.Serializable;

public class NewsMessage implements Serializable {
    private final String content;
    private boolean isRead;

    // این همان سازنده‌ای است که ارور "Expected no arguments" را برطرف می‌کند
    public NewsMessage(String content) {
        this.content = content;
        this.isRead = false; // پیام‌های جدید همیشه نخوانده هستند
    }

    public String getContent() {
        return content;
    }

    // این همان متدی است که ارور "Cannot resolve method 'isRead'" را برطرف می‌کند
    public boolean isRead() {
        return isRead;
    }

    public void markAsRead() {
        this.isRead = true;
    }
}