package controller;

import model.user.User;
import model.user.UserManager;
import util.HashUtil;

public class ProfileController {
    private final UserManager userManager;

    public ProfileController(UserManager userManager) {
        this.userManager = userManager;
    }

    public String changeUsername(User user, String newUsername) {
        if (user.getUsername().equals(newUsername)) {
            return "ERR_SAME_USERNAME";
        }

        // استفاده از متدی که خودت از قبل در UserManager داشتی
        if (userManager.usernameExists(newUsername)) {
            return "ERR_USERNAME_TAKEN";
        }

        String oldUsername = user.getUsername();
        user.setUsername(newUsername);

        // جابجایی کلید در مپ تا سیستم لاگین خراب نشود
        userManager.updateUsernameKey(oldUsername, newUsername);

        return "SUCCESS";
    }

    public String changeNickname(User user, String newNickname) {
        if (user.getNickname().equals(newNickname)) {
            return "ERR_SAME_NICKNAME";
        }
        user.setNickname(newNickname);
        userManager.save();
        return "SUCCESS";
    }

    public String changeEmail(User user, String newEmail) {
        if (user.getEmail().equals(newEmail)) {
            return "ERR_SAME_EMAIL";
        }
        user.setEmail(newEmail);
        userManager.save();
        return "SUCCESS";
    }

    public String changePassword(User user, String oldPassword, String newPassword) {
        // رمز عبور ذخیره‌شده هش‌شده است (طبق UserManager.registerUser)، پس برای
        // مقایسه‌ی درست، رمز خام ورودی کاربر هم باید قبل از مقایسه هش شود؛
        // مقایسه‌ی مستقیم رمز خام با هش هیچ‌وقت درست نمی‌شد (باگ قبلی).
        String oldHashed = HashUtil.sha256(oldPassword);
        if (!user.getPasswordHash().equals(oldHashed)) {
            return "ERR_WRONG_OLD_PASSWORD";
        }
        String newHashed = HashUtil.sha256(newPassword);
        if (user.getPasswordHash().equals(newHashed)) {
            return "ERR_SAME_PASSWORD";
        }
        user.setPasswordHash(newHashed);
        userManager.save();
        return "SUCCESS";
    }
}