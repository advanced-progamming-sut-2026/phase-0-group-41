package view;

import controller.LeaderboardController;
import model.user.User;
import util.CommandLine;

import java.util.List;

public class LeaderboardView {

    private final LeaderboardController controller;
    private final ConsoleView consoleView;

    public LeaderboardView(LeaderboardController controller, ConsoleView consoleView) {
        this.controller = controller;
        this.consoleView = consoleView;
    }

    public boolean checkCommand(CommandLine cmd) {
        List<String> t = cmd.getTokens();
        if (t.isEmpty()) return false;

        String first = t.get(0);

        // پشتیبانی از هر دو دستور: show leaderboard و sort
        if ((first.equals("show") && t.size() >= 2 && t.get(1).equals("leaderboard")) || first.equals("sort")) {
            String sortBy = cmd.get("s");
            if (sortBy == null) {
                sortBy = "score"; // پیش‌فرض
            }
            
            String order = cmd.get("o");
            boolean ascending = "asc".equalsIgnoreCase(order);

            displayLeaderboard(sortBy, ascending);
            return true;
        }

        return false;
    }

    private void displayLeaderboard(String sortBy, boolean ascending) {
        // دریافت دیتای خالص از کنترلر
        List<User> sortedUsers = controller.getSortedLeaderboard(sortBy, ascending);

        if (sortedUsers.isEmpty()) {
            consoleView.printMessage("هیچ کاربری در سیستم ثبت‌نام نکرده است.");
            return;
        }

        // بخش نمایشی و چاپ
        System.out.println("\n================================ 🏆 جدول امتیازات (Leaderboard) 🏆 ================================");
        System.out.println("مرتب‌شده بر اساس: " + sortBy + " | جهت: " + (ascending ? "صعودی (Ascending)" : "کاهشی (Descending)"));
        System.out.println("---------------------------------------------------------------------------------------------------");
        System.out.printf("| %-4s | %-15s | %-20s | %-10s | %-12s | %-15s | %-8s |\n",
                "رتبه", "نام کاربری", "آخرین مرحله", "مینی‌گیم‌ها", "کوئست روزانه", "کوئست غیرروزانه", "امتیاز");
        System.out.println("---------------------------------------------------------------------------------------------------");

        int rank = 1;
        for (User u : sortedUsers) {
            // ساخت استرینگ نمایشی فقط در لایه View
            String stageInfo;
            if (u.getLastCompletedChapter() == 0 && u.getLastCompletedLevel() == 0) {
                stageInfo = "انجام نشده";
            } else {
                stageInfo = "مرحله " + u.getLastCompletedLevel() + " فصل " + u.getLastCompletedChapter();
            }

            System.out.printf("| %-4d | %-15s | %-20s | %-10d | %-12d | %-15d | %-8d |\n",
                    rank++,
                    u.getUsername(),
                    stageInfo,
                    u.getMiniGamesCompleted(),
                    u.getDailyQuestsCompleted(),
                    u.getNonDailyQuestsCompleted(),
                    u.getHighScore());
        }
        System.out.println("---------------------------------------------------------------------------------------------------\n");
    }
}