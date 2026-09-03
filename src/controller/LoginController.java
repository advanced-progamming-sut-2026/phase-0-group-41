package controller;

import model.user.User;
import model.user.UserManager;
import network.NetworkManager;
import network.NetworkMessage;

public class LoginController {
    // ادغام فیلدهای هر دو نسخه
    private final UserManager userManager; // برای قابلیت‌های کلاینت‌ساید مثل stayLoggedIn
    private String pendingForgetUsername;
    private String pendingQuestion;
    private boolean isAwaitingNewPassword = false; // از نسخه اول برای امنیت مراحل ریست پسورد

    // کانستراکتور نسخه اول حفظ شد تا به فایل‌های لوکال دسترسی داشته باشیم
    public LoginController(UserManager userManager) {
        this.userManager = userManager;
    }

    public String authenticate(String username, String password, boolean stayLoggedIn) {
        // نکته‌ی مهمِ باگ‌فیکس: یکپارچه شدن با NetworkManager به جای سوکت خام
        NetworkMessage req = new NetworkMessage("LOGIN");
        req.data.put("username", username);
        req.data.put("password", password);

        NetworkMessage res = NetworkManager.sendRequest(req);
        String result = res != null ? res.responseBody : "ERR_CONNECTION_FAILED";

        if ("SUCCESS".equals(result)) {
            // ==================================================
            // --- قابلیت Stay Logged In (ادغام از نسخه اول) ---
            // ==================================================
            if (userManager != null) {
                if (stayLoggedIn) {
                    userManager.rememberUser(username);
                } else {
                    userManager.forgetRememberedUser();
                }
            }

            // ==================================================
            // --- سیستم کوئست و پاداش روزانه (ادغام از نسخه اول) ---
            // دریافت اطلاعات یوزر برای آپدیت‌های محلی سمت کلاینت
            // ==================================================
            User user = getAuthenticatedUser(username);
            if (user != null) {
                java.time.LocalDate today = java.time.LocalDate.now();
                if (user.getLastLoginDate() == null || !user.getLastLoginDate().equals(today)) {
                    if (user.getQuestManager() != null) user.getQuestManager().resetDailyQuests();
                    if (user.getQuestContext() != null) user.getQuestContext().resetDailyCounters();
                }
                user.updateLastLoginDate(); // آپدیت تاریخ آخرین ورود
                if (userManager != null) {
                    userManager.save(); // ذخیره تغییرات در فایل محلی
                }
            }
        }

        return result;
    }

    public User getAuthenticatedUser(String username) {
        NetworkMessage req = new NetworkMessage("GET_USER");
        req.data.put("username", username);
        NetworkMessage res = NetworkManager.sendRequest(req);
        
        if (res != null && res.payload instanceof User) {
            return (User) res.payload;
        }
        
        // در صورت عدم دریافت از شبکه، سعی در خواندن از لوکال منیجر
        return userManager != null ? userManager.findByUsername(username) : null;
    }

    public String initiateForgetPassword(String username, String email) {
        NetworkMessage req = new NetworkMessage("INITIATE_FORGET_PASSWORD");
        req.data.put("username", username);
        req.data.put("email", email);
        NetworkMessage res = NetworkManager.sendRequest(req);
        
        if (res != null && "SUCCESS".equals(res.responseBody)) {
            pendingForgetUsername = username;
            pendingQuestion = res.data.get("question");
            isAwaitingNewPassword = false;
            return "SUCCESS";
        }
        return res != null ? res.responseBody : "ERR_CONNECTION";
    }

    public String getPendingQuestion() {
        return pendingQuestion;
    }

    public String answerSecurityQuestion(String answer) {
        NetworkMessage req = new NetworkMessage("ANSWER_SECURITY_QUESTION");
        req.data.put("username", pendingForgetUsername);
        req.data.put("answer", answer);
        NetworkMessage res = NetworkManager.sendRequest(req);
        String result = res != null ? res.responseBody : "ERR_CONNECTION";

        // طبق رفتار مورد انتظار در LoginView: پاسخ غلط باید کل فرآیند را لغو کند
        if ("SUCCESS".equals(result)) {
            isAwaitingNewPassword = true; // اضافه شده از نسخه اول برای امنیت مرحله بعد
        } else {
            pendingForgetUsername = null;
            pendingQuestion = null;
            isAwaitingNewPassword = false;
        }
        return result;
    }

    public String resetPassword(String newPassword, String confirmPassword) {
        // ادغام اعتبارسنجی وضعیت از هر دو نسخه
        if (pendingForgetUsername == null || !isAwaitingNewPassword) {
            return "ERR_NOT_AWAITING_RESET";
        }
        if (newPassword == null || confirmPassword == null || !newPassword.equals(confirmPassword)) {
            return "ERR_PASSWORD_MISMATCH";
        }
        
        // بررسی قوی بودن رمز عبور پیش از ارسال به سرور (از نسخه اول)
        if (!isPasswordStrong(newPassword)) {
            return "ERR_WEAK_PASSWORD";
        }

        NetworkMessage req = new NetworkMessage("RESET_PASSWORD");
        req.data.put("username", pendingForgetUsername);
        req.data.put("newPassword", newPassword);
        NetworkMessage res = NetworkManager.sendRequest(req);
        
        if (res != null && "SUCCESS".equals(res.responseBody)) {
            pendingForgetUsername = null;
            pendingQuestion = null;
            isAwaitingNewPassword = false;
            return "SUCCESS";
        }
        return res != null ? res.responseBody : "ERR_CONNECTION";
    }

    // متد چک کردن قدرت پسورد از نسخه اول
    private boolean isPasswordStrong(String password) {
        String regex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{}|;':\",./<>?]).{8,}$";
        return password.matches(regex);
    }

    /**
     * پس از لاگ‌اوت یا خروج از برنامه فراخوانی می‌شود تا سرور کاربر را
     * از لیست کاربران آنلاین خارج کند (لازم برای بخش «انتخاب رقیب» در
     * مینی‌گیم «من، زامبی»).
     */
    public void logout(String username) {
        if (username == null) return;
        NetworkMessage req = new NetworkMessage("LOGOUT");
        req.data.put("username", username);
        NetworkManager.sendRequest(req);
    }
}