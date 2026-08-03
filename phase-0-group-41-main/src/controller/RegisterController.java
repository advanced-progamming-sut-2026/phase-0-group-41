package controller;

import model.user.SecurityQuestions;
import model.user.User;
import model.user.UserManager;

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

        // ۴. بررسی ایمیل
        if (email == null || !isValidEmail(email)) {
            return "ERR_INVALID_EMAIL";
        }

        // ۵. بررسی جنسیت
        if (gender == null || !(gender.equalsIgnoreCase("male") || gender.equalsIgnoreCase("female"))) {
            return "ERR_INVALID_GENDER";
        }

        // ==========================================
        // نکته مهم: ما پسورد خام را می‌فرستیم،
        // چون UserManager.register خودش زحمت Hash کردن را می‌کشد.
        // ==========================================
        pendingRegisteredUser = userManager.register(username, password, nickname, email, gender);
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

        userManager.save();
        pendingRegisteredUser = null;
        return "SUCCESS";
    }

    // متدهای کمکی بدون تغییر باقی می‌مانند
    private boolean isPasswordStrong(String password) {
        String regex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{}|;':\",./<>?]).{8,}$";
        return password.matches(regex);
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9](?!.*\\.\\.)[a-zA-Z0-9._-]*[a-zA-Z0-9]@[a-zA-Z0-9][a-zA-Z0-9-]*[a-zA-Z0-9]\\.[a-zA-Z]{2,}$";
        return email.matches(emailRegex);
    }
}