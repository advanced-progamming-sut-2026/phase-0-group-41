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
        // در داکیومنت نوشته شده: مراحلی که هنوز Unlock نشده‌اند را نمی‌توانید ادامه دهید.
        // در اینجا یک شبیه‌سازی ساده از بررسی قفل مراحل انجام می‌دهیم.
        // در فازهای بعدی که کلاس Chapter ساخته شد، این منطق پیچیده‌تر می‌شود.

        if (chapterName == null || chapterName.trim().isEmpty()) {
            return "ERR_INVALID_CHAPTER";
        }

        // فرض می‌کنیم مرحله "tutorial" همیشه باز است
        if (chapterName.equalsIgnoreCase("tutorial") || user.getLevelsCompleted() > 0) {
            return "SUCCESS"; // مرحله باز است
        } else {
            return "ERR_LOCKED_CHAPTER"; // مرحله قفل است
        }
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