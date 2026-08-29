package network;

import model.user.User;
import model.user.UserManager;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final UserManager userManager;

    public ClientHandler(Socket socket, UserManager userManager) {
        this.socket = socket;
        this.userManager = userManager;
    }

    @Override
    public void run() {
        try (
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream())
        ) {
            while (true) {
                // خواندن پیام از کلاینت
                NetworkMessage request = (NetworkMessage) in.readObject();
                NetworkMessage response = new NetworkMessage(request.command);

                if (request.command.equals("LOGIN")) {
                    String user = request.data.get("username");
                    String pass = request.data.get("password");
                    
                    User foundUser = userManager.findByUsername(user);
                    if (foundUser != null && userManager.checkPassword(foundUser, pass)) {
                        response.success = true;
                        response.responseBody = "SUCCESS";
                    } else {
                        response.success = false;
                        response.responseBody = "ERR_INVALID_CREDENTIALS";
                    }
                } 

                else if (request.command.equals("REGISTER")) {
                    String user = request.data.get("username");
                    if (userManager.usernameExists(user)) {
                        response.responseBody = "ERR_DUPLICATE_USERNAME";
                    } else {
                        userManager.register(
                            user,
                            request.data.get("password"),
                            request.data.get("nickname"),
                            request.data.get("email"),
                            request.data.get("gender")
                        );
                        response.responseBody = "SUCCESS";
                    }
                }

                else if (request.command.equals("INITIATE_FORGET_PASSWORD")) {
                    String user = request.data.get("username");
                    String email = request.data.get("email");
                    model.user.User u = userManager.findByUsername(user);
                    
                    if (u != null && u.getEmail().equalsIgnoreCase(email)) {
                        response.responseBody = "SUCCESS";
                        response.data.put("question", model.user.SecurityQuestions.get(u.getSecurityQuestionId()));
                    } else {
                        response.responseBody = "ERR_NOT_FOUND";
                    }
                }

                else if (request.command.equals("ANSWER_SECURITY_QUESTION")) {
                    String user = request.data.get("username");
                    String answer = request.data.get("answer");
                    model.user.User u = userManager.findByUsername(user);
                    
                    if (u != null && answer.equals(u.getSecurityAnswer())) {
                        response.responseBody = "SUCCESS";
                    } else {
                        response.responseBody = "ERR_WRONG_ANSWER";
                    }
                }

                else if (request.command.equals("RESET_PASSWORD")) {
                    String user = request.data.get("username");
                    String newPass = request.data.get("newPassword");
                    model.user.User u = userManager.findByUsername(user);
                    
                    if (u != null) {
                        userManager.changePassword(u, newPass);
                        response.responseBody = "SUCCESS";
                    } else {
                        response.responseBody = "ERR_USER_NOT_FOUND";
                    }
                }

                else if (request.command.equals("GET_USER")) {
                    String user = request.data.get("username");
                    model.user.User u = userManager.findByUsername(user);
                    if (u != null) {
                        response.payload = u;
                        response.responseBody = "SUCCESS";
                    }
                }

                else if (request.command.equals("PICK_QUESTION")) {
                    String user = request.data.get("username");
                    int qId = Integer.parseInt(request.data.get("qId"));
                    String answer = request.data.get("answer");
                    
                    User pendingUser = userManager.findByUsername(user);
                    if (pendingUser != null) {
                        pendingUser.setSecurityQuestionId(qId);
                        pendingUser.setSecurityAnswer(answer);
                        userManager.save();
                        response.responseBody = "SUCCESS";
                    } else {
                        response.responseBody = "ERR_NO_PENDING_USER";
                    }
                }
                
                // ارسال جواب به کلاینت
                out.writeObject(response);
                out.flush();
            }
        } catch (Exception e) {
            System.out.println("ارتباط با یکی از کلاینت‌ها قطع شد.");
        }
    }
}