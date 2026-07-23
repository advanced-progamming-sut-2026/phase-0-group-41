package controller;

import model.user.SecurityQuestions;
import model.user.User;
import model.user.UserManager;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Pattern;

public class RegisterController {
    private final UserManager userManager;
    private User pendingRegisteredUser;

    public RegisterController(UserManager userManager) {
        this.userManager = userManager;
    }

    public String registerUser(String username, String password, String passwordConfirm, String nickname, String email, String gender) {
        // ۱. بررسی نام کاربری
        if (username == null || !username.matches("^[a-zA-Z0-9\\p{Punct}]+$")) {
            return "ERR_INVALID_USERNAME";
        }
        if (userManager.usernameExists(username)) {
            return "ERR_DUPLICATE_USERNAME";
        }

        // ۲. بررسی رمز عبور
        if (!password.equals(passwordConfirm)) {
            return "ERR_PASSWORD_MISMATCH";
        }
        if (!isPasswordStrong(password)) {
            return "ERR_WEAK_PASSWORD";
        }

        // ۳. بررسی نام مستعار
        if (nickname == null || nickname.length() < 3 || nickname.length() > 30) {
            return "ERR_INVALID_NICKNAME";
        }

        // ۴. بررسی ایمیل (دقیقاً طبق قوانین داکیومنت تصویر image_dd3080.png)
        if (email == null || !isValidEmail(email)) {
            return "ERR_INVALID_EMAIL";
        }

        // ۵. بررسی جنسیت
        if (gender == null || !(gender.equalsIgnoreCase("male") || gender.equalsIgnoreCase("female"))) {
            return "ERR_INVALID_GENDER";
        }

        // هش کردن رمز عبور با SHA-256 قبل از ذخیره‌سازی
        String hashedPassword = hashSHA256(password);

        // ساخت کاربر موقت (هنوز سوال امنیتی جواب داده نشده)
        pendingRegisteredUser = userManager.register(username, hashedPassword, nickname, email, gender);
        return "SUCCESS";
    }

    public String pickQuestion(int qId, String answer, String confirm) {
        if (pendingRegisteredUser == null) {
            return "ERR_NO_PENDING_USER";
        }
        if (!SecurityQuestions.exists(qId)) {
            return "ERR_INVALID_QUESTION_ID";
        }
        if (answer == null || !answer.equals(confirm)) {
            return "ERR_ANSWER_MISMATCH";
        }

        pendingRegisteredUser.setSecurityQuestionId(qId);
        pendingRegisteredUser.setSecurityAnswer(answer);

        // ذخیره اطلاعات پس از تکمیل موفقیت‌آمیز ثبت‌نام
        userManager.save();
        pendingRegisteredUser = null;
        return "SUCCESS";
    }

    // ================= متدهای کمکی (Helper Methods) =================

    private boolean isPasswordStrong(String password) {
        // حداقل ۸ کاراکتر، شامل حروف کوچک، بزرگ، عدد و نماد خاص
        String regex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{}|;':\",./<>?]).{8,}$";
        return password.matches(regex);
    }

    private boolean isValidEmail(String email) {
        // دقیق‌ترین Regex ممکن برای پوشش تمام قوانین داکیومنت شما
        String emailRegex = "^[a-zA-Z0-9](?!.*\\.\\.)[a-zA-Z0-9._-]*[a-zA-Z0-9]@[a-zA-Z0-9][a-zA-Z0-9-]*[a-zA-Z0-9]\\.[a-zA-Z]{2,}$";
        return email.matches(emailRegex);
    }

    private String hashSHA256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder(2 * encodedhash.length);
            for (byte b : encodedhash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }
}