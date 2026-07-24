package controller;

import model.user.SecurityQuestions;
import model.user.User;
import model.user.UserManager;

public class LoginController {
    private final UserManager userManager;
    private User pendingForgetPasswordUser;
    private boolean isAwaitingNewPassword = false;

    public LoginController(UserManager userManager) {
        this.userManager = userManager;
    }

    public String authenticate(String username, String password, boolean stayLoggedIn) {
        User user = userManager.findByUsername(username);

        // ==========================================
        // نکته مهم: برای چک کردن پسورد از متد آماده‌ی
        // checkPassword در UserManager استفاده می‌کنیم
        // ==========================================
        if (user == null || !userManager.checkPassword(user, password)) {
            return "ERR_INVALID_CREDENTIALS";
        }

        // قابلیت Stay Logged In (در صورت نیاز بعداً تکمیل می‌شود)
        if (stayLoggedIn) {
            // e.g., userManager.setStayLoggedIn(user);
        }

        // ==================================================
        // --- اضافه شده برای سیستم کوئست و پاداش روزانه ---
        // ==================================================
        java.time.LocalDate today = java.time.LocalDate.now();
        if (user.getLastLoginDate() == null || !user.getLastLoginDate().equals(today)) {
            user.getQuestManager().resetDailyQuests();
        }
        user.updateLastLoginDate(); // آپدیت تاریخ آخرین ورود به امروز
        userManager.save(); // ذخیره تغییرات تاریخ و ریست کوئست‌ها در فایل
        // ==================================================

        return "SUCCESS";
    }

    public User getAuthenticatedUser(String username) {
        return userManager.findByUsername(username);
    }

    public String initiateForgetPassword(String username, String email) {
        User user = userManager.findByUsername(username);
        if (user == null || !user.getEmail().equalsIgnoreCase(email)) {
            return "ERR_NOT_FOUND";
        }
        pendingForgetPasswordUser = user;
        return "SUCCESS";
    }

    public String getPendingQuestion() {
        if (pendingForgetPasswordUser == null) return null;
        return SecurityQuestions.get(pendingForgetPasswordUser.getSecurityQuestionId());
    }

    public String answerSecurityQuestion(String answer) {
        if (pendingForgetPasswordUser == null) {
            return "ERR_NO_PENDING_USER";
        }

        if (answer != null && answer.equals(pendingForgetPasswordUser.getSecurityAnswer())) {
            isAwaitingNewPassword = true;
            return "SUCCESS";
        }

        pendingForgetPasswordUser = null;
        isAwaitingNewPassword = false;
        return "ERR_WRONG_ANSWER";
    }

    public String resetPassword(String newPassword, String confirmPassword) {
        if (!isAwaitingNewPassword || pendingForgetPasswordUser == null) {
            return "ERR_NOT_AWAITING_RESET";
        }
        if (!newPassword.equals(confirmPassword)) {
            return "ERR_PASSWORD_MISMATCH";
        }
        if (!isPasswordStrong(newPassword)) {
            return "ERR_WEAK_PASSWORD";
        }

        // ==========================================
        // استفاده از متد changePassword   
        // ==========================================
        userManager.changePassword(pendingForgetPasswordUser, newPassword);

        pendingForgetPasswordUser = null;
        isAwaitingNewPassword = false;
        return "SUCCESS";
    }

    private boolean isPasswordStrong(String password) {
        String regex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{}|;':\",./<>?]).{8,}$";
        return password.matches(regex);
    }
}