package controller;

import model.user.SecurityQuestions;
import model.user.UserManager;
import network.NetworkMessage;
import network.NetworkManager;

public class RegisterController {
    
    private String pendingUsername; 

    public RegisterController(UserManager userManager) {
        // برای جلوگیری از ارور در MenuController نگه داشته شده، اما کارها سمت سرور است
    } 

    public String registerUser(String username, String password, String passwordConfirm, String nickname, String email, String gender) {
        if (username == null || !username.matches("^[a-zA-Z0-9\\p{Punct}]+$")) return "ERR_INVALID_USERNAME";
        if (!password.equals(passwordConfirm)) return "ERR_PASSWORD_MISMATCH";
        if (!isPasswordStrong(password)) return "ERR_WEAK_PASSWORD";
        if (nickname == null || nickname.length() < 3 || nickname.length() > 30) return "ERR_INVALID_NICKNAME";
        if (email == null || !isValidEmail(email)) return "ERR_INVALID_EMAIL";
        if (gender == null || !(gender.equalsIgnoreCase("male") || gender.equalsIgnoreCase("female"))) return "ERR_INVALID_GENDER";

        NetworkMessage req = new NetworkMessage("REGISTER");
        req.data.put("username", username);
        req.data.put("password", password);
        req.data.put("nickname", nickname);
        req.data.put("email", email);
        req.data.put("gender", gender);

        NetworkMessage res = NetworkManager.sendRequest(req);
        
        if (res != null && "SUCCESS".equals(res.responseBody)) {
            pendingUsername = username;
            return "SUCCESS";
        }
        return res != null ? res.responseBody : "ERR_CONNECTION";
    }

    public String pickQuestion(int qId, String answer, String confirm) {
        if (pendingUsername == null) return "ERR_NO_PENDING_USER";
        if (!SecurityQuestions.exists(qId)) return "ERR_INVALID_QUESTION_ID";
        if (answer == null || !answer.equals(confirm)) return "ERR_ANSWER_MISMATCH";

        NetworkMessage req = new NetworkMessage("PICK_QUESTION");
        req.data.put("username", pendingUsername);
        req.data.put("qId", String.valueOf(qId));
        req.data.put("answer", answer);

        NetworkMessage res = NetworkManager.sendRequest(req);
        if (res != null && "SUCCESS".equals(res.responseBody)) {
            pendingUsername = null;
            return "SUCCESS";
        }
        return res != null ? res.responseBody : "ERR_CONNECTION";
    }

    private boolean isPasswordStrong(String password) {
        String regex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{}|;':\",./<>?]).{8,}$";
        return password.matches(regex);
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9](?!.*\\.\\.)[a-zA-Z0-9._-]*[a-zA-Z0-9]@[a-zA-Z0-9][a-zA-Z0-9-]*[a-zA-Z0-9]\\.[a-zA-Z]{2,}$";
        return email.matches(emailRegex);
    }
}