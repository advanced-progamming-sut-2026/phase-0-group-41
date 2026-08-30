package network;

import model.user.User;
import model.user.UserManager;
import java.io.EOFException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.regex.Pattern;

public class ClientHandler implements Runnable {

    // همان قانون قدرت رمز عبور که سمت کلاینت در RegisterController چک می‌شود؛
    // چون کلاینت قابل اعتماد نیست (مثلاً می‌تواند دستکاری شود)، سرور هم باید
    // همین قانون را مستقل بررسی کند؛ این نبود، باعث می‌شد RESET_PASSWORD
    // بدون هیچ اعتبارسنجی‌ای رمزهای ضعیف را قبول کند.
    private static final Pattern STRONG_PASSWORD = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{}|;':\",./<>?]).{8,}$");

    private final Socket socket;
    private final UserManager userManager;

    // نام کاربری متصل به این کلاینت خاص، پس از LOGIN موفق ثبت می‌شود.
    // این برای دستور SAVE_USER لازم است تا کلاینت مجبور نباشد هر بار نام کاربری
    // را جداگانه بفرستد و همچنین برای علامت‌گذاری آنلاین/آفلاین کاربر استفاده می‌شود.
    private String authenticatedUsername;

    public ClientHandler(Socket socket, UserManager userManager) {
        this.socket = socket;
        this.userManager = userManager;
    }

    @Override
    public void run() {
        try (
            // ترتیب ساخت استریم‌ها باید با NetworkManager سمت کلاینت یکی باشد
            // (اول Output و flush، بعد Input)، وگرنه هر دو طرف منتظر هدر
            // ObjectInputStream می‌مانند و اتصال از همان ابتدا Deadlock می‌شود.
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        ) {
            out.flush();
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            while (true) {
                // خواندن پیام از کلاینت
                NetworkMessage request = (NetworkMessage) in.readObject();
                NetworkMessage response = new NetworkMessage(request.command);
                response.success = false;

                switch (request.command) {

                    case "LOGIN": {
                        String user = request.data.get("username");
                        String pass = request.data.get("password");

                        User foundUser = userManager.findByUsername(user);
                        if (foundUser != null && userManager.checkPassword(foundUser, pass)) {
                            response.success = true;
                            response.responseBody = "SUCCESS";
                            authenticatedUsername = user;
                            foundUser.updateLastLoginDate();
                            userManager.save();
                            OnlineUsers.setOnline(user, this);
                        } else {
                            response.responseBody = "ERR_INVALID_CREDENTIALS";
                        }
                        break;
                    }

                    case "REGISTER": {
                        String user = request.data.get("username");
                        User created = userManager.register(
                                user,
                                request.data.get("password"),
                                request.data.get("nickname"),
                                request.data.get("email"),
                                request.data.get("gender")
                        );
                        // register خودش به‌صورت atomic (synchronized) یکتا بودن نام کاربری
                        // را بررسی می‌کند؛ اگر از قبل وجود داشت null برمی‌گرداند.
                        if (created != null) {
                            response.success = true;
                            response.responseBody = "SUCCESS";
                        } else {
                            response.responseBody = "ERR_DUPLICATE_USERNAME";
                        }
                        break;
                    }

                    case "INITIATE_FORGET_PASSWORD": {
                        String user = request.data.get("username");
                        String email = request.data.get("email");
                        User u = userManager.findByUsername(user);

                        if (u != null && email != null && email.equalsIgnoreCase(u.getEmail())) {
                            response.success = true;
                            response.responseBody = "SUCCESS";
                            response.data.put("question", model.user.SecurityQuestions.get(u.getSecurityQuestionId()));
                        } else {
                            response.responseBody = "ERR_NOT_FOUND";
                        }
                        break;
                    }

                    case "ANSWER_SECURITY_QUESTION": {
                        String user = request.data.get("username");
                        String answer = request.data.get("answer");
                        User u = userManager.findByUsername(user);

                        if (u != null && u.getSecurityAnswer() != null && u.getSecurityAnswer().equals(answer)) {
                            response.success = true;
                            response.responseBody = "SUCCESS";
                        } else {
                            response.responseBody = "ERR_WRONG_ANSWER";
                        }
                        break;
                    }

                    case "RESET_PASSWORD": {
                        String user = request.data.get("username");
                        String newPass = request.data.get("newPassword");
                        User u = userManager.findByUsername(user);

                        if (u == null) {
                            response.responseBody = "ERR_USER_NOT_FOUND";
                        } else if (newPass == null || !STRONG_PASSWORD.matcher(newPass).matches()) {
                            response.responseBody = "ERR_WEAK_PASSWORD";
                        } else {
                            userManager.changePassword(u, newPass);
                            response.success = true;
                            response.responseBody = "SUCCESS";
                        }
                        break;
                    }

                    case "GET_USER": {
                        String user = request.data.get("username");
                        User u = userManager.findByUsername(user);
                        if (u != null) {
                            response.payload = u;
                            response.success = true;
                            response.responseBody = "SUCCESS";
                        } else {
                            response.responseBody = "ERR_USER_NOT_FOUND";
                        }
                        break;
                    }

                    case "PICK_QUESTION": {
                        String user = request.data.get("username");
                        String answer = request.data.get("answer");
                        User pendingUser = userManager.findByUsername(user);

                        if (pendingUser == null) {
                            response.responseBody = "ERR_NO_PENDING_USER";
                            break;
                        }
                        int qId;
                        try {
                            qId = Integer.parseInt(request.data.get("qId"));
                        } catch (NumberFormatException e) {
                            response.responseBody = "ERR_INVALID_QUESTION_ID";
                            break;
                        }
                        if (!model.user.SecurityQuestions.exists(qId)) {
                            response.responseBody = "ERR_INVALID_QUESTION_ID";
                            break;
                        }
                        pendingUser.setSecurityQuestionId(qId);
                        pendingUser.setSecurityAnswer(answer);
                        userManager.save();
                        response.success = true;
                        response.responseBody = "SUCCESS";
                        break;
                    }

                    // === فرمان جدید: ذخیره‌ی نسخه‌ی به‌روزشده‌ی کاربر روی سرور. ===
                    // طبق سند فاز ۳، «داده‌های مرتبط با کاربر در بازی باید در سرور
                    // ذخیره شوند»؛ این فرمان همان چیزی است که هر بار سکه/الماس/کوئست/
                    // پیشرفت مرحله تغییر می‌کند (خروج از بازی، لاگ‌اوت، خروج از برنامه)
                    // باید به سرور ارسال شود تا از دستگاه دیگر هم قابل مشاهده باشد.
                    case "SAVE_USER": {
                        Object payload = request.payload;
                        String username = request.data.get("username");
                        if (payload instanceof User && username != null && username.equals(authenticatedUsername)) {
                            userManager.updateUser((User) payload);
                            response.success = true;
                            response.responseBody = "SUCCESS";
                        } else if (!(payload instanceof User)) {
                            response.responseBody = "ERR_INVALID_PAYLOAD";
                        } else {
                            response.responseBody = "ERR_NOT_AUTHENTICATED";
                        }
                        break;
                    }

                    // === فرمان جدید: لیدربورد باید از داده‌های واقعی سرور بیاید ===
                    // (طبق سند: «اطلاعات لیدربورد باید از داده‌های ذخیره‌شده کاربران
                    // در سرور دریافت شود»)، نه از یک UserManager محلی و خالی روی کلاینت.
                    case "GET_ALL_USERS": {
                        response.payload = new java.util.ArrayList<>(userManager.getAllUsers());
                        response.success = true;
                        response.responseBody = "SUCCESS";
                        break;
                    }

                    case "LOGOUT": {
                        String username = request.data.get("username");
                        if (username != null) {
                            OnlineUsers.setOffline(username);
                        }
                        response.success = true;
                        response.responseBody = "SUCCESS";
                        break;
                    }

                    default:
                        response.responseBody = "ERR_UNKNOWN_COMMAND";
                        break;
                }

                // ارسال جواب به کلاینت
                out.writeObject(response);
                out.flush();
                out.reset();
            }
        } catch (EOFException e) {
            // کلاینت اتصال را به‌صورت عادی بست
        } catch (Exception e) {
            System.out.println("ارتباط با یکی از کلاینت‌ها قطع شد: " + e.getMessage());
        } finally {
            if (authenticatedUsername != null) {
                OnlineUsers.setOffline(authenticatedUsername);
            }
            try {
                socket.close();
            } catch (Exception ignored) {
            }
        }
    }
}