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
        if (chapterName.equalsIgnoreCase("tutorial") || chapterName.equalsIgnoreCase("beginner")) {
            int level = Math.min(user.getBeginnerLastCompletedLevel() + 1, model.game.ChapterPlan.LEVELS_PER_CHAPTER);
            return "SUCCESS_0_" + level;
        }

        int requestedChapter;
        try {
            requestedChapter = Integer.parseInt(chapterName);
        } catch (NumberFormatException e) {
            return "ERR_INVALID_CHAPTER";
        }

        if (!model.game.ChapterPlan.isValidChapter(requestedChapter) || requestedChapter == 0) {
            // شماره‌ی ۰ فقط از طریق نام "beginner"/"tutorial" قابل دسترسی است، نه عدد مستقیم
            return "ERR_INVALID_CHAPTER";
        }

        // بررسی قفل بودن: بازیکن فقط می‌تواند نهایتاً به فصلی برود که یکی از آخرین فصل تکمیل‌شده‌اش بالاتر است
        int maxUnlockedChapter = Math.max(1, user.getLastCompletedChapter() + 1);
        if (requestedChapter > maxUnlockedChapter) {
            return "ERR_LOCKED_CHAPTER";
        }

        int levelToPlay;
        if (requestedChapter == user.getLastCompletedChapter()) {
            // این فصل قبلاً کامل شده؛ اجازه‌ی تکرار مرحله‌ی اول را می‌دهیم
            levelToPlay = 1;
        } else {
            // فصل جاری (هنوز کامل نشده): مرحله‌ی بعدی بازی می‌شود
            levelToPlay = Math.min(user.getLastCompletedLevel() + 1, model.game.ChapterPlan.LEVELS_PER_CHAPTER);
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