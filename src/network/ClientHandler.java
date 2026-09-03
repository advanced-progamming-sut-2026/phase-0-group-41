package network;

import model.minigame.IZombieSession;
import model.user.User;
import model.user.UserManager;
import network.izombie.MatchmakingManager;
import network.izombie.MultiplayerMatch;
import network.izombie.ReactionMessage;

import java.io.EOFException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
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

                    // بخش امتیازی فاز ۳: پس از هر دور بازی امتیازی، امتیاز کاربر
                    // به سرور ارسال می‌شود؛ رکورد فقط در صورتی که امتیاز جدید
                    // بیشتر باشد به‌روزرسانی می‌شود (منطق مقایسه داخل User است تا
                    // کلاینت نتواند با ارسال مستقیم مقدار بزرگ تقلب کند).
                    case "SUBMIT_SCORE_GAME_RESULT": {
                        String username = request.data.get("username");
                        User user = userManager.findByUsername(username);
                        if (user == null) {
                            response.responseBody = "ERR_USER_NOT_FOUND";
                            break;
                        }
                        int score = parseIntOr(request.data.get("score"), -1);
                        if (score < 0) {
                            response.responseBody = "ERR_INVALID_SCORE";
                            break;
                        }
                        user.updateMaxMowPoints(score);
                        userManager.save();
                        response.success = true;
                        response.responseBody = "SUCCESS";
                        response.data.put("myPoint", String.valueOf(user.getMaxMowPoints()));
                        break;
                    }

                    case "LOGOUT": {
                        String username = request.data.get("username");
                        if (username != null) {
                            OnlineUsers.setOffline(username);
                            MatchmakingManager.leaveMatch(username);
                            MatchmakingManager.leaveRandomQueue(username);
                        }
                        response.success = true;
                        response.responseBody = "SUCCESS";
                        break;
                    }

                    // ==================== بازی چندنفره: «من، زامبی» ====================
                    // پیاده‌سازی «سیستم انتخاب رقیب» طبق سند فاز ۳. چون معماری این پروژه
                    // بر پایه‌ی درخواست/پاسخ همزمان است (نه یک کانال push واقعی)، «نمایش
                    // پاپ‌آپ» با یک صف انتظار روی سرور (MatchmakingManager) و Poll دوره‌ای
                    // از سمت کلاینت شبیه‌سازی می‌شود؛ به همین دلیل هیچ‌کدام از فرمان‌های
                    // زیر نیاز به authenticatedUsername محلی این ClientHandler ندارند
                    // (کاربر می‌تواند نام کاربری خودش را در هر درخواست بفرستد) چون ممکن
                    // است چند اتصال TCP مختلف (چند تب/تماس هم‌زمان) برای یک کاربر باز باشد.

                    case "IZOMBIE_CHALLENGE_USER": {
                        String from = request.data.get("username");
                        String target = request.data.get("targetUsername");
                        int level = parseIntOr(request.data.get("level"), 1);
                        if (target == null || userManager.findByUsername(target) == null) {
                            response.responseBody = "ERR_USER_NOT_FOUND";
                            break;
                        }
                        MatchmakingManager.ChallengeRequestResult result =
                                MatchmakingManager.challenge(from, target, level);
                        response.success = (result == MatchmakingManager.ChallengeRequestResult.SENT);
                        response.responseBody = result.name();
                        break;
                    }

                    // کلاینت مقصد یک چالش، این را دوره‌ای صدا می‌زند تا پاپ‌آپ تایید/رد را نشان دهد
                    case "IZOMBIE_POLL_INCOMING_CHALLENGE": {
                        String username = request.data.get("username");
                        MatchmakingManager.PendingChallenge challenge =
                                MatchmakingManager.pollIncomingChallenge(username);
                        if (challenge != null) {
                            response.success = true;
                            response.responseBody = "SUCCESS";
                            response.data.put("fromUsername", challenge.fromUsername);
                            response.data.put("level", String.valueOf(challenge.level));
                        } else {
                            response.responseBody = "NONE";
                        }
                        break;
                    }

                    case "IZOMBIE_RESPOND_CHALLENGE": {
                        String username = request.data.get("username");
                        boolean accept = "true".equals(request.data.get("accept"));
                        MatchmakingManager.respondChallenge(userManager, username, accept);
                        response.success = true;
                        response.responseBody = "SUCCESS";
                        break;
                    }

                    // کلاینتِ چالش‌دهنده، این را دوره‌ای صدا می‌زند تا بفهمد رقیب چه پاسخی داده
                    case "IZOMBIE_POLL_CHALLENGE_RESULT": {
                        String username = request.data.get("username");
                        String matchId = MatchmakingManager.pollChallengeMatchFound(username);
                        if (matchId != null) {
                            response.success = true;
                            response.responseBody = "MATCHED";
                            response.data.put("matchId", matchId);
                        } else if (MatchmakingManager.pollChallengeRejected(username)) {
                            response.success = true;
                            response.responseBody = "REJECTED";
                        } else {
                            response.responseBody = "PENDING";
                        }
                        break;
                    }

                    case "IZOMBIE_JOIN_RANDOM_QUEUE": {
                        String username = request.data.get("username");
                        int level = parseIntOr(request.data.get("level"), 1);
                        String matchId = MatchmakingManager.joinRandomQueue(userManager, username, level);
                        if (matchId != null) {
                            response.success = true;
                            response.responseBody = "MATCHED";
                            response.data.put("matchId", matchId);
                        } else {
                            response.success = true;
                            response.responseBody = "WAITING";
                        }
                        break;
                    }

                    case "IZOMBIE_LEAVE_RANDOM_QUEUE": {
                        MatchmakingManager.leaveRandomQueue(request.data.get("username"));
                        response.success = true;
                        response.responseBody = "SUCCESS";
                        break;
                    }

                    case "IZOMBIE_POLL_RANDOM_MATCH": {
                        String username = request.data.get("username");
                        String matchId = MatchmakingManager.pollRandomMatchFound(username);
                        if (matchId != null) {
                            response.success = true;
                            response.responseBody = "MATCHED";
                            response.data.put("matchId", matchId);
                        } else {
                            response.responseBody = "WAITING";
                        }
                        break;
                    }

                    // اطلاعات اولیه‌ی مسابقه (نقش هر بازیکن و نام کاربری حریف) پس از پیدا شدن matchId
                    case "IZOMBIE_MATCH_INFO": {
                        String username = request.data.get("username");
                        String matchId = request.data.get("matchId");
                        MultiplayerMatch match = MatchmakingManager.getMatch(matchId);
                        if (match == null || !match.involves(username)) {
                            response.responseBody = "ERR_MATCH_NOT_FOUND";
                            break;
                        }
                        response.success = true;
                        response.responseBody = "SUCCESS";
                        response.data.put("role", match.roleOf(username).name());
                        response.data.put("opponentUsername", match.opponentOf(username));
                        break;
                    }

                    case "IZOMBIE_STATE": {
                        String username = request.data.get("username");
                        String matchId = request.data.get("matchId");
                        MultiplayerMatch match = MatchmakingManager.getMatch(matchId);
                        if (match == null || !match.involves(username)) {
                            response.responseBody = "ERR_MATCH_NOT_FOUND";
                            break;
                        }
                        response.success = true;
                        response.responseBody = "SUCCESS";
                        response.payload = match.snapshotFor();
                        break;
                    }

                    case "IZOMBIE_PLACE_ZOMBIE": {
                        String username = request.data.get("username");
                        String matchId = request.data.get("matchId");
                        String type = request.data.get("zombieType");
                        MultiplayerMatch match = MatchmakingManager.getMatch(matchId);
                        if (match == null || !match.involves(username)) {
                            response.responseBody = "ERR_MATCH_NOT_FOUND";
                            break;
                        }
                        int row = parseIntOr(request.data.get("row"), -1);
                        int col = parseIntOr(request.data.get("col"), -1);
                        IZombieSession.PlaceZombieResult result = match.placeZombie(username, type, row, col);
                        response.success = (result == IZombieSession.PlaceZombieResult.SUCCESS);
                        response.responseBody = result.name();
                        break;
                    }

                    case "IZOMBIE_PLANT": {
                        String username = request.data.get("username");
                        String matchId = request.data.get("matchId");
                        String plantType = request.data.get("plantType");
                        MultiplayerMatch match = MatchmakingManager.getMatch(matchId);
                        if (match == null || !match.involves(username)) {
                            response.responseBody = "ERR_MATCH_NOT_FOUND";
                            break;
                        }
                        int row = parseIntOr(request.data.get("row"), -1);
                        int col = parseIntOr(request.data.get("col"), -1);
                        String error = match.plantAt(username, plantType, row, col);
                        response.success = (error == null);
                        response.responseBody = (error == null) ? "SUCCESS" : error;
                        break;
                    }

                    case "IZOMBIE_COLLECT_SUN": {
                        String username = request.data.get("username");
                        String matchId = request.data.get("matchId");
                        MultiplayerMatch match = MatchmakingManager.getMatch(matchId);
                        if (match == null || !match.involves(username)) {
                            response.responseBody = "ERR_MATCH_NOT_FOUND";
                            break;
                        }
                        int row = parseIntOr(request.data.get("row"), -1);
                        int col = parseIntOr(request.data.get("col"), -1);
                        String error = match.collectSun(username, row, col);
                        response.success = (error == null);
                        response.responseBody = (error == null) ? "SUCCESS" : error;
                        break;
                    }

                    case "IZOMBIE_SEND_REACTION": {
                        String username = request.data.get("username");
                        String matchId = request.data.get("matchId");
                        String kind = request.data.get("kind");
                        String content = request.data.get("content");
                        MultiplayerMatch match = MatchmakingManager.getMatch(matchId);
                        if (match == null || !match.involves(username)) {
                            response.responseBody = "ERR_MATCH_NOT_FOUND";
                            break;
                        }
                        match.sendReaction(username, kind, content);
                        response.success = true;
                        response.responseBody = "SUCCESS";
                        break;
                    }

                    case "IZOMBIE_POLL_REACTIONS": {
                        String username = request.data.get("username");
                        String matchId = request.data.get("matchId");
                        MultiplayerMatch match = MatchmakingManager.getMatch(matchId);
                        if (match == null || !match.involves(username)) {
                            response.responseBody = "ERR_MATCH_NOT_FOUND";
                            break;
                        }
                        List<ReactionMessage> reactions = match.pollReactionsFor(username);
                        response.success = true;
                        response.responseBody = "SUCCESS";
                        response.payload = new java.util.ArrayList<>(reactions);
                        break;
                    }

                    // برای کاربری که چالش را همین الان پذیرفته: matchId مسابقه‌ای که سرور
                    // بلافاصله بعد از RESPOND_CHALLENGE ساخته را برمی‌گرداند
                    case "IZOMBIE_MY_MATCH": {
                        String username = request.data.get("username");
                        network.izombie.MultiplayerMatch match = MatchmakingManager.getMatchForUser(username);
                        if (match != null) {
                            response.success = true;
                            response.responseBody = "SUCCESS";
                            response.data.put("matchId", match.getMatchId());
                        } else {
                            response.responseBody = "NONE";
                        }
                        break;
                    }

                    case "IZOMBIE_LEAVE_MATCH": {
                        String username = request.data.get("username");
                        MatchmakingManager.leaveMatch(username);
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
                // اگر کلاینت به‌طور ناگهانی (کرش/قطع اینترنت) قطع شود، نباید در
                // صف تصادفی گیر بماند یا مسابقه‌ی نیمه‌کاره‌اش برای همیشه فعال بماند
                MatchmakingManager.leaveRandomQueue(authenticatedUsername);
                MatchmakingManager.leaveMatch(authenticatedUsername);
            }
            try {
                socket.close();
            } catch (Exception ignored) {
            }
        }
    }

    private static int parseIntOr(String value, int fallback) {
        if (value == null) return fallback;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}