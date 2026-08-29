package controller;

import network.NetworkManager;
import network.NetworkMessage;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

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
        return res != null ? res.responseBody : "ERR_CONNECTION";
    }

    public String resetPassword(String newPassword, String confirmPassword) {
        if (!newPassword.equals(confirmPassword)) return "ERR_PASSWORD_MISMATCH";
        
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
        try (Socket socket = new Socket("localhost", 8080);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
             
            NetworkMessage req = new NetworkMessage("LOGIN");
            req.data.put("username", username);
            req.data.put("password", password);
            
            out.writeObject(req);
            
            NetworkMessage res = (NetworkMessage) in.readObject();
            return res.responseBody; // برگرداندن "SUCCESS" یا "ERR_INVALID_CREDENTIALS"
            
        } catch (Exception e) {
            return "ERR_CONNECTION_FAILED";
        }
    }
}