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
        return false;
    }

    private void doLogin(CommandLine cmd) {
        String username = cmd.get("u");
        String password = cmd.get("p");

        User user = controller.authenticate(username, password);
        if (user == null) {
            consoleView.printError("نام کاربری یا رمز عبور اشتباه است.");
        } else {
            menuController.setLoggedInUser(user);
            menuController.setCurrentMenu(MenuType.MAIN);
            consoleView.printMessage("خوش آمدید " + user.getNickname() + "!");
        }
    }

    private void doForgetPassword(CommandLine cmd) {
        String username = cmd.get("u");
        String email = cmd.get("e");

        String question = controller.initiateForgetPassword(username, email);
        if (question.equals("NOT_FOUND")) {
            consoleView.printError("کاربر با این نام کاربری/ایمیل یافت نشد.");
        } else {
            consoleView.printMessage(question);
            consoleView.printMessage("پاسخ را با دستور 'answer -a <پاسخ>' وارد کنید.");
        }
    }

    private void doAnswerSecurityQuestion(CommandLine cmd) {
        String answer = cmd.get("a");
        String result = controller.answerSecurityQuestion(answer);

        if (result.equals("NO_PENDING_USER")) {
            consoleView.printError("ابتدا 'forget password' را اجرا کنید.");
        } else if (result.equals("SUCCESS")) {
            consoleView.printMessage("پاسخ صحیح بود. لطفا رمز عبور جدید را با 'menu profile change-password ...' تنظیم کنید (یا در این اسکلت با ورود مجدد رمز فعلی را نگه دارید).");
        } else {
            consoleView.printError("پاسخ نادرست بود.");
        }
    }
}