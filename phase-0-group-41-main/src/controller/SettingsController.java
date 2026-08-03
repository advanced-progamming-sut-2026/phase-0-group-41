package controller;

import model.user.User;
import model.user.UserManager;

public class SettingsController {
    private final UserManager userManager;

    public SettingsController(UserManager userManager) {
        this.userManager = userManager;
    }

    public String changeDifficulty(User user, int difficultyLevel) {
        // طبق داکیومنت: مقدار difficultyLevel باید مقداری بین ۱ تا ۵ باشد.
        if (difficultyLevel < 1 || difficultyLevel > 5) {
            return "ERR_INVALID_DIFFICULTY";
        }

        user.setDifficultyLevel(difficultyLevel);

        // ذخیره تغییرات در فایل تا در اجرای بعدی برنامه باقی بماند
        userManager.save();

        return "SUCCESS";
    }
}