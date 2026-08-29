package controller;

import model.user.NewsMessage;
import model.user.User;
import java.util.ArrayList;
import java.util.List;

public class NewsController {

    // گرفتن پیام‌های نخوانده و تغییر وضعیت آن‌ها به خوانده شده
    public List<String> getAndMarkUnreadNews(User currentUser) {
        List<String> unread = new ArrayList<>();
        if (currentUser == null) return unread;

        for (NewsMessage msg : currentUser.getNewsList()) {
            if (!msg.isRead()) {
                unread.add(msg.getContent());
                msg.markAsRead(); // طبق داکیومنت: در دفعات بعدی نباید نشان داده شوند
            }
        }
        return unread;
    }

    // گرفتن تمام پیام‌ها
    public List<String> getAllNews(User currentUser) {
        List<String> all = new ArrayList<>();
        if (currentUser == null) return all;

        for (NewsMessage msg : currentUser.getNewsList()) {
            String prefix = msg.isRead() ? "[خوانده شده] " : "[جدید] ";
            all.add(prefix + msg.getContent());
        }
        return all;
    }
}