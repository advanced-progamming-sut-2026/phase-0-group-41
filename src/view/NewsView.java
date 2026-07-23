package view;

import controller.NewsController;
import model.user.User;
import util.CommandLine;
import java.util.List;

public class NewsView {
    private final NewsController controller;
    private final ConsoleView consoleView;

    public NewsView(NewsController controller, ConsoleView consoleView) {
        this.controller = controller;
        this.consoleView = consoleView;
    }

    // دقت کن که User به عنوان ورودی اضافه شد
    public boolean checkCommand(User loggedInUser, List<String> t, CommandLine cmd) {
        if (t.size() == 3 && t.get(0).equals("menu") && t.get(1).equals("news") && t.get(2).equals("show-unread")) {
            List<String> unread = controller.getAndMarkUnreadNews(loggedInUser);
            if (unread.isEmpty()) {
                consoleView.printMessage("خبر جدیدی وجود ندارد.");
            } else {
                consoleView.printMessage("--- اخبار جدید ---");
                for (String news : unread) {
                    consoleView.printMessage("- " + news);
                }
            }
            return true;
        }

        if (t.size() == 3 && t.get(0).equals("menu") && t.get(1).equals("news") && t.get(2).equals("show-all")) {
            List<String> allNews = controller.getAllNews(loggedInUser);
            if (allNews.isEmpty()) {
                consoleView.printMessage("صندوق اخبار شما خالی است.");
            } else {
                consoleView.printMessage("--- تمام اخبار ---");
                for (String news : allNews) {
                    consoleView.printMessage(news);
                }
            }
            return true;
        }

        return false;
    }
}