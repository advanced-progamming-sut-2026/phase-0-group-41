package controller;

import model.user.User;
import model.user.UserManager;

public class MainController {
    private final UserManager userManager;

    public MainController(UserManager userManager) {
        this.userManager = userManager;
    }

    public String logout(User currentUser) {
        if (currentUser == null) {
            return "ERR_NOT_LOGGED_IN";
        }

        // قبل از خروج، هرگونه پیشرفت کاربر را در فایل ذخیره می‌کنیم
        // این کار مطابق بند "ذخیره اطلاعات در هنگام بستن/خروج" در داکیومنت است
        userManager.save();

        return "SUCCESS";
    }
}