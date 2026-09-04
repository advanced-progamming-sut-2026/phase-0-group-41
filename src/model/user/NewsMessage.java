package model.user;

import java.io.Serializable;

public class NewsMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String content;
    private boolean isRead;
    // Timestamp (epoch millis) of when the news item was created. Needed so the
    // News screen can filter to only the last 24 hours by default and still let
    // the user opt in to see the full history.
    private final long timestamp;

    // این همان سازنده‌ای است که ارور "Expected no arguments" را برطرف می‌کند
    public NewsMessage(String content) {
        this(content, System.currentTimeMillis());
    }

    public NewsMessage(String content, long timestamp) {
        this.content = content;
        this.isRead = false; // پیام‌های جدید همیشه نخوانده هستند
        this.timestamp = timestamp;
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

    public long getTimestamp() {
        return timestamp;
    }
}