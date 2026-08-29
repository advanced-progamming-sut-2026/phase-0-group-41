package view;

import controller.ProfileController;
import model.user.User;
import util.CommandLine;
import java.util.List;

public class ProfileView {
    private final ProfileController controller;
    private final ConsoleView consoleView;

    public ProfileView(ProfileController controller, ConsoleView consoleView) {
        this.controller = controller;
        this.consoleView = consoleView;
    }

    public boolean checkCommand(User user, List<String> t, CommandLine cmd) {
        if (user == null) {
            consoleView.printError("شما وارد حساب کاربری نشده‌اید.");
            return true;
        }

        if (t.size() < 2 || !t.get(0).equals("menu") || !t.get(1).equals("profile")) {
            return false;
        }

        String action = t.size() >= 3 ? t.get(2) : "";

        // ۱. تغییر نام کاربری
        if (action.equals("change-username")) {
            String newUsername = cmd.get("u");
            if (newUsername == null) {
                consoleView.printError("فرمت دستور اشتباه است. الگو: menu profile change-username -u <username>");
                return true;
            }
            String result = controller.changeUsername(user, newUsername);
            if (result.equals("ERR_SAME_USERNAME")) {
                consoleView.printError("نام کاربری جدید با نام کاربری فعلی شما برابر است.");
            } else if (result.equals("SUCCESS")) {
                consoleView.printMessage("نام کاربری شما با موفقیت تغییر یافت.");
            }
            return true;
        }

        // ۲. تغییر نام مستعار (در داکیومنت هم سوییچ -u نوشته شده است)
        if (action.equals("change-nickname")) {
            String newNickname = cmd.get("u");
            if (newNickname == null) {
                consoleView.printError("فرمت دستور اشتباه است. الگو: menu profile change-nickname -u <nickname>");
                return true;
            }
            String result = controller.changeNickname(user, newNickname);
            if (result.equals("ERR_SAME_NICKNAME")) {
                consoleView.printError("نام مستعار جدید با نام مستعار فعلی شما برابر است.");
            } else if (result.equals("SUCCESS")) {
                consoleView.printMessage("نام مستعار شما با موفقیت تغییر یافت.");
            }
            return true;
        }

        // ۳. تغییر ایمیل
        if (action.equals("change-email")) {
            String newEmail = cmd.get("e");
            if (newEmail == null) {
                consoleView.printError("فرمت دستور اشتباه است. الگو: menu profile change-email -e <email>");
                return true;
            }
            String result = controller.changeEmail(user, newEmail);
            if (result.equals("ERR_SAME_EMAIL")) {
                consoleView.printError("ایمیل جدید با ایمیل فعلی شما برابر است.");
            } else if (result.equals("SUCCESS")) {
                consoleView.printMessage("ایمیل شما با موفقیت تغییر یافت.");
            }
            return true;
        }

        // ۴. تغییر رمز عبور
        if (action.equals("change-password")) {
            String newPassword = cmd.get("p");
            String oldPassword = cmd.get("o");
            if (newPassword == null || oldPassword == null) {
                consoleView.printError("فرمت دستور اشتباه است. الگو: menu profile change-password -p <new_password> -o <old_password>");
                return true;
            }
            String result = controller.changePassword(user, oldPassword, newPassword);
            if (result.equals("ERR_WRONG_OLD_PASSWORD")) {
                consoleView.printError("رمز عبور قبلی اشتباه است.");
            } else if (result.equals("ERR_SAME_PASSWORD")) {
                consoleView.printError("رمز عبور جدید با رمز عبور فعلی برابر است.");
            } else if (result.equals("SUCCESS")) {
                consoleView.printMessage("رمز عبور شما با موفقیت تغییر یافت.");
            }
            return true;
        }

        // ۵. نمایش اطلاعات کاربر
        if (action.equals("show-info")) {
            consoleView.printMessage("--- اطلاعات کاربری ---");
            consoleView.printMessage("نام کاربری: " + user.getUsername());
            consoleView.printMessage("نام مستعار: " + user.getNickname());
            consoleView.printMessage("تعداد بازی‌های انجام شده: " + user.getGamesPlayed());
            consoleView.printMessage("میزان سکه‌های کسب شده: " + user.getCoins());
            consoleView.printMessage("میزان الماس‌های کسب شده: " + user.getDiamonds());
            consoleView.printMessage("تعداد مراحل گذرانده شده: " + user.getLevelsCompleted());
            consoleView.printMessage("بیشترین میوپوینت: " + user.getMaxMowPoints());
            consoleView.printMessage("----------------------");
            return true;
        }

        return false;
    }
}