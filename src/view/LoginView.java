package view;

import controller.LoginController;
import controller.MenuController;
import model.menu.MenuType;
import model.user.User;
import util.CommandLine;

import java.util.List;

public class LoginView {
    private final LoginController controller;
    private final ConsoleView consoleView;
    private final MenuController menuController;

    public LoginView(LoginController controller, ConsoleView consoleView, MenuController menuController) {
        this.controller = controller;
        this.consoleView = consoleView;
        this.menuController = menuController;
    }

    public boolean checkCommand(List<String> t, CommandLine cmd) {
        if (t.get(0).equals("login")) {
            doLogin(cmd);
            return true;
        }
        if (t.get(0).equals("forget") && t.size() >= 2 && t.get(1).equals("password")) {
            doForgetPassword(cmd);
            return true;
        }
        if (t.get(0).equals("answer")) {
            doAnswerSecurityQuestion(cmd);
            return true;
        }
        if (t.get(0).equals("reset-password")) {
            doResetPassword(cmd);
            return true;
        }
        return false;
    }

    private void doLogin(CommandLine cmd) {
        String username = cmd.get("u");
        String password = cmd.get("p");

        // بررسی وجود تگ stay-logged-in در دستور کاربر
        boolean stayLoggedIn = cmd.getTokens().contains("-stay-logged-in");

        String result = controller.authenticate(username, password, stayLoggedIn);

        if (result.equals("SUCCESS")) {
            User user = controller.getAuthenticatedUser(username);
            menuController.setLoggedInUser(user);
            menuController.setCurrentMenu(MenuType.MAIN); // هدایت به منوی اصلی
            consoleView.printMessage("ورود موفقیت‌آمیز بود. به منوی اصلی خوش آمدید، " + user.getNickname() + "!");
            if (stayLoggedIn) {
                consoleView.printMessage("[سیستم]: حالت Stay Logged In فعال شد.");
            }
        } else {
            consoleView.printError("نام کاربری یا رمز عبور اشتباه است.");
        }
    }

    private void doForgetPassword(CommandLine cmd) {
        String username = cmd.get("u");
        String email = cmd.get("e");

        String result = controller.initiateForgetPassword(username, email);
        if (result.equals("SUCCESS")) {
            String question = controller.getPendingQuestion();
            consoleView.printMessage("سوال امنیتی شما: " + question);
            consoleView.printMessage("لطفاً با دستور 'answer -a <answer>' پاسخ دهید.");
        } else {
            consoleView.printError("کاربری با این نام کاربری و ایمیل در سیستم یافت نشد.");
        }
    }

    private void doAnswerSecurityQuestion(CommandLine cmd) {
        String answer = cmd.get("a");
        String result = controller.answerSecurityQuestion(answer);

        switch (result) {
            case "SUCCESS":
                consoleView.printMessage("پاسخ صحیح بود!");
                consoleView.printMessage("لطفاً رمز عبور جدید خود را با این فرمت وارد کنید: reset-password -p <new_password> <confirm_password>");
                break;
            case "ERR_WRONG_ANSWER":
                consoleView.printError("پاسخ امنیتی نادرست بود. فرآیند لغو شد و به اول منو بازگشتید.");
                break;
            case "ERR_NO_PENDING_USER":
                consoleView.printError("شما در فرآیند بازیابی رمز عبور نیستید. ابتدا از دستور forget password استفاده کنید.");
                break;
        }
    }

    private void doResetPassword(CommandLine cmd) {
        List<String> passwordParts = cmd.getMulti("p");
        String newPassword = passwordParts.isEmpty() ? null : passwordParts.get(0);
        String confirmPassword = passwordParts.size() > 1 ? passwordParts.get(1) : null;

        String result = controller.resetPassword(newPassword, confirmPassword);

        switch (result) {
            case "SUCCESS":
                consoleView.printMessage("رمز عبور با موفقیت تغییر کرد! حالا می‌توانید با رمز جدید login کنید.");
                break;
            case "ERR_PASSWORD_MISMATCH":
                consoleView.printError("عدم تطابق رمز عبور و تکرار آن.");
                break;
            case "ERR_WEAK_PASSWORD":
                consoleView.printError("رمز عبور ضعیف: طول آن حداقل ۸ حرف باشد، از حروف کوچک و بزرگ، اعداد و نمادهای خاص نیز در آن استفاده شده باشد.");
                break;
            case "ERR_NOT_AWAITING_RESET":
                consoleView.printError("شما مجوز تغییر رمز را ندارید. باید ابتدا به سوال امنیتی پاسخ دهید.");
                break;
        }
    }
}