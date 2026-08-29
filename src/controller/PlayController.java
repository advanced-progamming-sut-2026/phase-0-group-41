package controller;

import model.user.User;
import model.user.UserManager;

public class PlayController {
    private final UserManager userManager;

    public PlayController(UserManager userManager) {
        this.userManager = userManager;
    }

    // بررسی و ورود به یک Chapter
    public String enterChapter(User user, String chapterName) {
        if (chapterName.equalsIgnoreCase("tutorial")) return "SUCCESS_0_1";

        int requestedChapter;
        try {
            requestedChapter = Integer.parseInt(chapterName);
        } catch (NumberFormatException e) {
            return "ERR_INVALID_CHAPTER";
        }

        if (requestedChapter < 1 || requestedChapter > 4) return "ERR_INVALID_CHAPTER";

        // بررسی قفل بودن: بازیکن فقط می‌تواند نهایتاً به فصلی برود که یکی از آخرین فصل تکمیل‌شده‌اش بالاتر است
        int maxUnlockedChapter = user.getLastCompletedChapter() + 1;
        if (requestedChapter > maxUnlockedChapter) {
            return "ERR_LOCKED_CHAPTER";
        }

        int levelToPlay = 1;
        if (requestedChapter == maxUnlockedChapter) {
            // اگر در فصل جدید یا فصل جاری است، مرحله بعدی را بازی می‌کند
            levelToPlay = user.getLastCompletedLevel() + 1;
            if (levelToPlay > 4) levelToPlay = 4; // سقف ۴ مرحله برای هر فصل
        } else {
            // اگر فصلی را قبلاً تمام کرده، می‌تواند آزادانه مرحله آخر آن را تکرار کند (یا هر منطق دلخواه دیگر)
            levelToPlay = 1; 
        }

        // بازگرداندن یک پکیج دیتای استرینگ شامل وضعیت، شماره فصل و شماره مرحله
        return "SUCCESS_" + requestedChapter + "_" + levelToPlay;
    }
    // متد اعمال کد تقلب (Cheat)
    public String applyCheat(User user, int amount, String type) {
        if (amount <= 0) {
            return "ERR_INVALID_AMOUNT";
        }

        if (type.equalsIgnoreCase("coin") || type.equalsIgnoreCase("coins")) {
            user.addCoins(amount); // استفاده از متد کلاس User
            userManager.save();    // ذخیره آنی در فایل
            return "SUCCESS_COIN";
        } else if (type.equalsIgnoreCase("diamond") || type.equalsIgnoreCase("diamonds")) {
            user.addDiamonds(amount); // استفاده از متد کلاس User
            userManager.save();       // ذخیره آنی در فایل
            return "SUCCESS_DIAMOND";
        }

        return "ERR_INVALID_CHEAT_TYPE";
    }

    // دریافت موجودی سکه
    public int getCoinBalance(User user) {
        return user.getCoins();
    }

    // دریافت موجودی الماس
    public int getDiamondBalance(User user) {
        return user.getDiamonds();
    }
}