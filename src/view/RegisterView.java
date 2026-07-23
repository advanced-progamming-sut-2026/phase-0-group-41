package view;

import controller.MenuController;
import controller.RegisterController;
import model.menu.MenuType;
import model.user.SecurityQuestions;
import util.CommandLine;

import java.util.List;
import java.util.Map;

public class RegisterView {
    private final RegisterController controller;
    private final ConsoleView consoleView;
    private final MenuController menuController;

    public RegisterView(RegisterController controller, ConsoleView consoleView, MenuController menuController) {
        this.controller = controller;
        this.consoleView = consoleView;
        this.menuController = menuController;
    }

    public boolean checkCommand(List<String> t, CommandLine cmd) {
        if (t.get(0).equals("register")) {
            doRegister(cmd);
            return true;
        }
        if (t.get(0).equals("pick") && t.size() >= 2 && t.get(1).equals("question")) {
            doPickQuestion(cmd);
            return true;
        }
        return false;
    }

    private void doRegister(CommandLine cmd) {
        String username = cmd.get("u");
        List<String> passwordParts = cmd.getMulti("p");
        String password = passwordParts.isEmpty() ? null : passwordParts.get(0);
        String passwordConfirm = passwordParts.size() > 1 ? passwordParts.get(1) : null;
        String nickname = cmd.get("n");
        String email = cmd.get("e");
        String gender = cmd.get("g");

        // ارسال داده‌ها به کنترلر و دریافت کد وضعیت
        String result = controller.registerUser(username, password, passwordConfirm, nickname, email, gender);

        switch (result) {
            case "SUCCESS":
                // طبق داکیومنت: در صورت موفقیت باید لیست سوالات نشان داده شود
                StringBuilder qList = new StringBuilder("ثبت‌نام اولیه موفقیت‌آمیز بود. لیست سوالات امنیتی:\n");
                for (Map.Entry<Integer, String> e : SecurityQuestions.all().entrySet()) {
                    qList.append(e.getKey()).append(") ").append(e.getValue()).append('\n');
                }
                consoleView.printMessage(qList.toString());
                consoleView.printMessage("لطفا با دستور 'pick question -q <question_number> -a <answer> -c <answer_confirm>' یک سوال انتخاب کنید.");
                break;
            case "ERR_INVALID_USERNAME":
                consoleView.printError("نام کاربری نامعتبر است (فقط حروف، اعداد و نمادها مجاز هستند).");
                break;
            case "ERR_DUPLICATE_USERNAME":
                consoleView.printError("نام کاربری تکراری: نام کاربری از قبل در سامانه موجود باشد.");
                break;
            case "ERR_PASSWORD_MISMATCH":
                consoleView.printError("عدم تطابق رمز عبور و تکرار آن.");
                break;
            case "ERR_WEAK_PASSWORD":
                consoleView.printError("رمز عبور ضعیف: طول آن حداقل ۸ حرف باشد، از حروف کوچک و بزرگ، اعداد و نمادهای خاص نیز در آن استفاده شده باشد.");
                break;
            case "ERR_INVALID_NICKNAME":
                consoleView.printError("طول غیر مجاز: نام مستعار باید حداقل ۳ کاراکتر و حداکثر ۳۰ کاراکتر باشد.");
                break;
            case "ERR_INVALID_EMAIL":
                consoleView.printError("ایمیل نامعتبر است. فرمت صحیح (example@domain.com) را با رعایت قوانین نقطه و کاراکترها وارد کنید.");
                break;
            case "ERR_INVALID_GENDER":
                consoleView.printError("جنسیت نامعتبر است. باید male یا female باشد.");
                break;
            default:
                consoleView.printError("خطای ناشناخته رخ داد.");
                break;
        }
    }

    private void doPickQuestion(CommandLine cmd) {
        try {
            int qId = Integer.parseInt(cmd.get("q"));
            String answer = cmd.get("a");
            String confirm = cmd.get("c");

            String result = controller.pickQuestion(qId, answer, confirm);

            switch (result) {
                case "SUCCESS":
                    consoleView.printMessage("ثبت‌نام با موفقیت کامل شد.");
                    // طبق داکیومنت: در انتها کاربر به منوی ورود هدایت می‌شود
                    menuController.setCurrentMenu(MenuType.LOGIN);
                    consoleView.printMessage("به صورت خودکار وارد منوی ورود (Login Menu) شدید.");
                    break;
                case "ERR_NO_PENDING_USER":
                    consoleView.printError("ابتدا باید دستور register را به درستی اجرا کنید.");
                    break;
                case "ERR_INVALID_QUESTION_ID":
                    consoleView.printError("شماره سوال نامعتبر است.");
                    break;
                case "ERR_ANSWER_MISMATCH":
                    consoleView.printError("پاسخ و تکرار آن یکسان نیستند.");
                    break;
            }
        } catch (NumberFormatException e) {
            consoleView.printError("شماره سوال باید یک عدد صحیح باشد.");
        }
    }
}