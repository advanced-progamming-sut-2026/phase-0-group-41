package controller;

import model.user.SecurityQuestions;
import model.user.User;
import model.user.UserManager;

public class LoginController {
    private final UserManager userManager;
    private User pendingForgetPasswordUser;

    public LoginController(UserManager userManager) {
        this.userManager = userManager;
    }

    public User authenticate(String username, String password) {
        User user = userManager.findByUsername(username);
        if (user == null || !userManager.checkPassword(user, password)) {
            return null;
        }
        return user;
    }

    public String initiateForgetPassword(String username, String email) {
        User user = userManager.findByUsername(username);
        if (user == null || !user.getEmail().equalsIgnoreCase(email)) {
            return "NOT_FOUND";
        }
        pendingForgetPasswordUser = user;
        return SecurityQuestions.get(user.getSecurityQuestionId());
    }

    public String answerSecurityQuestion(String answer) {
        if (pendingForgetPasswordUser == null) return "NO_PENDING_USER";

        if (answer != null && answer.equals(pendingForgetPasswordUser.getSecurityAnswer())) {
            pendingForgetPasswordUser = null;
            return "SUCCESS";
        }
        return "WRONG_ANSWER";
    }
}