package view;

import controller.MenuController;
import controller.SettingsController;
import model.user.User;
import util.CommandLine;

import java.util.List;

public class SettingsView {
    private final SettingsController controller;
    private final ConsoleView consoleView;
    private final MenuController menuController;

    public SettingsView(SettingsController controller, ConsoleView consoleView, MenuController menuController) {
        this.controller = controller;
        this.consoleView = consoleView;
        this.menuController = menuController;
    }

    public boolean checkCommand(List<String> t, CommandLine cmd) {
        // الگوی دستور: menu settings change-difficulty -l <difficulty_level>
        if (t.size() >= 3 && t.get(0).equals("menu") && t.get(1).equals("settings") && t.get(2).equals("change-difficulty")) {
            doChangeDifficulty(cmd);
            return true;
        }

        return false;
    }

    private void doChangeDifficulty(CommandLine cmd) {
        User currentUser = menuController.getLoggedInUser();
        if (currentUser == null) {
            consoleView.printError("ابتدا باید وارد حساب کاربری شوید.");
            return;
        }

        try {
            int dl = Integer.parseInt(cmd.get("l"));
            String result = controller.changeDifficulty(currentUser, dl);

            switch (result) {
                case "SUCCESS":
                    consoleView.printMessage("میزان سختی بازی با موفقیت روی " + dl + " تنظیم شد.");
                    break;
                case "ERR_INVALID_DIFFICULTY":
                    consoleView.printError("مقدار میزان سختی (difficulty level) باید عددی بین ۱ تا ۵ باشد.");
                    break;
            }
        } catch (NumberFormatException e) {
            consoleView.printError("میزان سختی باید یک عدد صحیح باشد.");
        }
    }
}