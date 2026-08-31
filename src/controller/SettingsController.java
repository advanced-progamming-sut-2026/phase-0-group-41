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


    public String changeGameSettings(User user, float gameSpeed, boolean showHitboxes, boolean debugMode) {
        if (gameSpeed < 1f || gameSpeed > 3f) {
            return "ERR_INVALID_SPEED";
        }

        user.setGameSpeed(gameSpeed);
        user.setShowHitboxes(showHitboxes);
        user.setDebugMode(debugMode);

        userManager.save();

        return "SUCCESS";
    }

    public String changeNetworkGridVisibility(User user, boolean showNetworkGrid) {
        user.setShowNetworkGrid(showNetworkGrid);
        userManager.save();
        return "SUCCESS";
    }

    // متد اعمال کد تقلب (Cheat) در حالت دیباگ - افزایش سکه/الماس
    public String applyCheat(User user, int amount, String type) {
        if (!user.isDebugMode()) {
            return "ERR_DEBUG_MODE_DISABLED";
        }
        if (amount <= 0) {
            return "ERR_INVALID_AMOUNT";
        }

        if (type.equalsIgnoreCase("coin") || type.equalsIgnoreCase("coins")) {
            user.addCoins(amount);
            userManager.save();
            return "SUCCESS_COIN";
        } else if (type.equalsIgnoreCase("diamond") || type.equalsIgnoreCase("diamonds")) {
            user.addDiamonds(amount);
            userManager.save();
            return "SUCCESS_DIAMOND";
        }

        return "ERR_INVALID_CHEAT_TYPE";
    }

}