package network.izombie;

import model.user.User;
import model.user.UserManager;
import network.OnlineUsers;

import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * سرور، هماهنگ‌کننده‌ی مرکزی «سیستم انتخاب رقیب» برای مینی‌گیم «من، زامبی» است
 * (طبق سند فاز ۳). دو مسیر پشتیبانی می‌شود:
 *
 *  ۱) بازی با کاربر مشخص: چون معماری کلاینت-سرور این پروژه بر پایه‌ی
 *     درخواست/پاسخ همزمان (blocking request/response روی یک Socket) است، نه
 *     یک کانال push واقعی، «نمایش پاپ‌آپ به کاربر مقصد» با یک صف انتظار پیاده
 *     شده: وقتی کاربر A کاربر B را چالش می‌کند، یک PendingChallenge برای B
 *     ثبت می‌شود. کلاینت B با یک تایمر کوتاه (Poll) دستور POLL_CHALLENGE را
 *     صدا می‌زند و اگر چالشی برایش ثبت شده باشد، پاپ‌آپ تایید/رد را نشان
 *     می‌دهد؛ نتیجه با RESPOND_CHALLENGE به سرور برمی‌گردد و کلاینت A هم با
 *     Poll دوره‌ای (POLL_MATCH_FOUND) از نتیجه باخبر می‌شود.
 *
 *  ۲) بازی تصادفی: کاربر وارد یک صف انتظار (Queue) می‌شود؛ اگر از قبل کاربر
 *     دیگری منتظر باشد، بلافاصله جفت می‌شوند و مسابقه ساخته می‌شود؛ در غیر
 *     این صورت در صف می‌ماند تا نفر بعدی با Poll دوره‌ای جفتش پیدا شود.
 *
 * نقش‌ها (گیاه/زامبی) هنگام ساخت مسابقه به‌صورت تصادفی تعیین می‌شوند تا هیچ
 * کدام از دو حالت (چالش مشخص / تصادفی) نسبت به دیگری امتیاز اضافه‌ای نداشته
 * باشد.
 */
public final class MatchmakingManager {

    public static final class PendingChallenge {
        public final String fromUsername;
        public final int level;
        public volatile Boolean accepted; // null = هنوز پاسخ داده نشده

        PendingChallenge(String fromUsername, int level) {
            this.fromUsername = fromUsername;
            this.level = level;
        }
    }

    private static final Map<String, PendingChallenge> INCOMING_CHALLENGES = new ConcurrentHashMap<>(); // targetUsername -> challenge
    private static final Map<String, String> CHALLENGE_RESULT_MATCH = new ConcurrentHashMap<>(); // challengerUsername -> matchId (وقتی قبول شد)
    private static final Map<String, String> CHALLENGE_RESULT_REJECTED = new ConcurrentHashMap<>(); // challengerUsername -> targetUsername (وقتی رد شد)

    private static final Queue<QueueEntry> RANDOM_QUEUE = new ConcurrentLinkedQueue<>();
    private static final Map<String, String> RANDOM_MATCH_FOUND = new ConcurrentHashMap<>(); // username -> matchId

    private static final Map<String, MultiplayerMatch> ACTIVE_MATCHES = new ConcurrentHashMap<>(); // matchId -> match
    private static final Map<String, String> USER_TO_MATCH = new ConcurrentHashMap<>(); // username -> matchId

    private static final class QueueEntry {
        final String username;
        final int level;
        QueueEntry(String username, int level) { this.username = username; this.level = level; }
    }

    private MatchmakingManager() {
    }

    // ==================== چالش با کاربر مشخص ====================

    public enum ChallengeRequestResult { SENT, ERR_USER_NOT_FOUND, ERR_USER_OFFLINE, ERR_USER_BUSY, ERR_SELF }

    public static synchronized ChallengeRequestResult challenge(String fromUsername, String targetUsername, int level) {
        if (fromUsername.equals(targetUsername)) {
            return ChallengeRequestResult.ERR_SELF;
        }
        if (!OnlineUsers.isOnline(targetUsername)) {
            return ChallengeRequestResult.ERR_USER_OFFLINE;
        }
        if (USER_TO_MATCH.containsKey(targetUsername) || USER_TO_MATCH.containsKey(fromUsername)) {
            return ChallengeRequestResult.ERR_USER_BUSY;
        }
        INCOMING_CHALLENGES.put(targetUsername, new PendingChallenge(fromUsername, level));
        return ChallengeRequestResult.SENT;
    }

    /** کلاینت مقصد این را به‌صورت دوره‌ای صدا می‌زند تا ببیند چالشی برایش رسیده یا نه. */
    public static synchronized PendingChallenge pollIncomingChallenge(String username) {
        return INCOMING_CHALLENGES.get(username);
    }

    public static synchronized void respondChallenge(UserManager userManager, String targetUsername, boolean accept) {
        PendingChallenge challenge = INCOMING_CHALLENGES.remove(targetUsername);
        if (challenge == null) {
            return;
        }
        if (!accept) {
            CHALLENGE_RESULT_REJECTED.put(challenge.fromUsername, targetUsername);
            return;
        }
        User challenger = userManager.findByUsername(challenge.fromUsername);
        User target = userManager.findByUsername(targetUsername);
        if (challenger == null || target == null) {
            CHALLENGE_RESULT_REJECTED.put(challenge.fromUsername, targetUsername);
            return;
        }
        MultiplayerMatch match = createMatch(challenger, target, challenge.level);
        CHALLENGE_RESULT_MATCH.put(challenge.fromUsername, match.getMatchId());
    }

    /** کلاینتِ چالش‌دهنده این را دوره‌ای صدا می‌زند تا بفهمد رقیب قبول/رد کرده. */
    public static synchronized String pollChallengeMatchFound(String fromUsername) {
        return CHALLENGE_RESULT_MATCH.remove(fromUsername);
    }

    public static synchronized boolean pollChallengeRejected(String fromUsername) {
        return CHALLENGE_RESULT_REJECTED.remove(fromUsername) != null;
    }

    // ==================== بازی تصادفی ====================

    public static synchronized String joinRandomQueue(UserManager userManager, String username, int level) {
        if (USER_TO_MATCH.containsKey(username)) {
            return null; // از قبل داخل یک مسابقه است
        }
        for (QueueEntry entry : RANDOM_QUEUE) {
            if (entry.username.equals(username)) {
                return null; // از قبل در صف است
            }
        }
        // اگر نفر دیگری منتظر باشد، بلافاصله جفت شوند
        QueueEntry waiting = RANDOM_QUEUE.poll();
        if (waiting != null) {
            User first = userManager.findByUsername(waiting.username);
            User second = userManager.findByUsername(username);
            if (first == null || second == null) {
                return null;
            }
            MultiplayerMatch match = createMatch(first, second, Math.max(waiting.level, level));
            RANDOM_MATCH_FOUND.put(waiting.username, match.getMatchId());
            RANDOM_MATCH_FOUND.put(username, match.getMatchId());
            return match.getMatchId();
        }
        RANDOM_QUEUE.add(new QueueEntry(username, level));
        return null; // منتظر بمان
    }

    public static synchronized void leaveRandomQueue(String username) {
        RANDOM_QUEUE.removeIf(e -> e.username.equals(username));
    }

    /** کلاینت این را دوره‌ای صدا می‌زند تا ببیند حریفی برایش پیدا شده یا نه. */
    public static synchronized String pollRandomMatchFound(String username) {
        return RANDOM_MATCH_FOUND.remove(username);
    }

    // ==================== مسابقه‌ها ====================

    private static MultiplayerMatch createMatch(User a, User b, int level) {
        String matchId = UUID.randomUUID().toString();
        boolean aIsPlant = new java.util.Random().nextBoolean(); // تعیین تصادفی نقش‌ها
        User plantUser = aIsPlant ? a : b;
        String zombieUsername = aIsPlant ? b.getUsername() : a.getUsername();

        MultiplayerMatch match = new MultiplayerMatch(matchId, plantUser, zombieUsername, level);
        ACTIVE_MATCHES.put(matchId, match);
        USER_TO_MATCH.put(a.getUsername(), matchId);
        USER_TO_MATCH.put(b.getUsername(), matchId);
        match.start();
        return match;
    }

    public static MultiplayerMatch getMatch(String matchId) {
        return ACTIVE_MATCHES.get(matchId);
    }

    public static MultiplayerMatch getMatchForUser(String username) {
        String matchId = USER_TO_MATCH.get(username);
        return matchId == null ? null : ACTIVE_MATCHES.get(matchId);
    }

    /** کاربر مسابقه را ترک می‌کند (خروج دستی یا قطع اتصال). */
    public static synchronized void leaveMatch(String username) {
        String matchId = USER_TO_MATCH.remove(username);
        if (matchId == null) {
            return;
        }
        MultiplayerMatch match = ACTIVE_MATCHES.get(matchId);
        if (match == null) {
            return;
        }
        String opponent = match.opponentOf(username);
        // اگر حریف هنوز داخل مسابقه است، مسابقه را برایش هم آزاد می‌کنیم؛ منطق
        // پایان‌دادن واقعی (که کدام‌طرف برنده باشد) در سمت کلاینت با پیام مناسب
        // مدیریت می‌شود؛ اینجا فقط رفرنس‌ها را پاک می‌کنیم تا مسابقه حافظه
        // اشغال نکند.
        if (opponent != null) {
            USER_TO_MATCH.remove(opponent);
        }
        match.stop();
        ACTIVE_MATCHES.remove(matchId);
    }
}
