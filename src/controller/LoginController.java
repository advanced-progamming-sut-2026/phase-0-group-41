package controller;

import network.NetworkManager;
import network.NetworkMessage;

public class LoginController {
    private String pendingForgetUsername;
    private String pendingQuestion;

    public String initiateForgetPassword(String username, String email) {
        NetworkMessage req = new NetworkMessage("INITIATE_FORGET_PASSWORD");
        req.data.put("username", username);
        req.data.put("email", email);
        NetworkMessage res = NetworkManager.sendRequest(req);
        
        if (res != null && "SUCCESS".equals(res.responseBody)) {
            pendingForgetUsername = username;
            pendingQuestion = res.data.get("question");
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

        // طبق رفتار مورد انتظار در LoginView: پاسخ غلط باید کل فرآیند فراموشی
        // رمز عبور را لغو کند (بازگشت به منوی اول)، نه اینکه کاربر بتواند
        // نامحدود حدس بزند در حالی که pendingForgetUsername هنوز معتبر است.
        if (!"SUCCESS".equals(result)) {
            pendingForgetUsername = null;
            pendingQuestion = null;
        }
        return result;
    }

    public String resetPassword(String newPassword, String confirmPassword) {
        if (pendingForgetUsername == null) return "ERR_NOT_AWAITING_RESET";
        if (newPassword == null || confirmPassword == null || !newPassword.equals(confirmPassword)) {
            return "ERR_PASSWORD_MISMATCH";
        }

        NetworkMessage req = new NetworkMessage("RESET_PASSWORD");
        req.data.put("username", pendingForgetUsername);
        req.data.put("newPassword", newPassword);
        NetworkMessage res = NetworkManager.sendRequest(req);
        
        if (res != null && "SUCCESS".equals(res.responseBody)) {
            pendingForgetUsername = null;
            pendingQuestion = null;
            return "SUCCESS";
        }
        return res != null ? res.responseBody : "ERR_CONNECTION";
    }

    public model.user.User getAuthenticatedUser(String username) {
        network.NetworkMessage req = new network.NetworkMessage("GET_USER");
        req.data.put("username", username);
        network.NetworkMessage res = network.NetworkManager.sendRequest(req);
        
        if (res != null && res.payload instanceof model.user.User) {
            return (model.user.User) res.payload;
        }
        return null;
    }
    
    public String authenticate(String username, String password, boolean stayLoggedIn) {
        // نکته‌ی مهمِ باگ‌فیکس: این متد قبلاً یک Socket خام و جدا برای خودش باز
        // می‌کرد (بدون استفاده از NetworkManager)، در حالی که همه‌ی متدهای دیگر
        // از سوکت مشترکِ NetworkManager استفاده می‌کنند. نتیجه‌اش این بود که پاسخ
        // سرور به این سوکتِ اضافی/جدا می‌رفت و هیچ‌وقت با استریم مشترک هماهنگ
        // نمی‌شد و باعث قطع‌شدن یا رفتار غیرقابل‌پیش‌بینیِ درخواست‌های بعدی
        // (مثل GET_USER) روی همان اتصال می‌شد. حالا مثل بقیه‌ی متدها یکپارچه است.
        NetworkMessage req = new NetworkMessage("LOGIN");
        req.data.put("username", username);
        req.data.put("password", password);

        NetworkMessage res = NetworkManager.sendRequest(req);
        return res != null ? res.responseBody : "ERR_CONNECTION_FAILED";
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