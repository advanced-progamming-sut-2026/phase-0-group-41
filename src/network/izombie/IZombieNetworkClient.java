package network.izombie;

import network.NetworkManager;
import network.NetworkMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * لایه‌ی نازک کلاینت برای تمام فرمان‌های شبکه‌ای مینی‌گیم «من، زامبی».
 * هر متد این کلاس دقیقاً یک NetworkManager.sendRequest صدا می‌زند و پاسخ خام
 * سرور را به شکلی راحت برای Screenها برمی‌گرداند. صفحات گرافیکی (Screen) هیچ
 * وقت مستقیماً NetworkMessage نمی‌سازند تا رشته‌های فرمان در یک‌جا متمرکز بمانند.
 */
public final class IZombieNetworkClient {

    private IZombieNetworkClient() {
    }

    public static final class ChallengeResult {
        public boolean sent;
        public String errorCode; // ERR_USER_NOT_FOUND, ERR_USER_OFFLINE, ERR_USER_BUSY, ERR_SELF
    }

    public static ChallengeResult challengeUser(String username, String targetUsername, int level) {
        NetworkMessage req = new NetworkMessage("IZOMBIE_CHALLENGE_USER");
        req.data.put("username", username);
        req.data.put("targetUsername", targetUsername);
        req.data.put("level", String.valueOf(level));
        NetworkMessage res = NetworkManager.sendRequest(req);
        ChallengeResult result = new ChallengeResult();
        result.sent = res.success;
        result.errorCode = res.success ? null : res.responseBody;
        return result;
    }

    public static final class IncomingChallenge {
        public String fromUsername;
        public int level;
    }

    /** null یعنی در حال حاضر چالشی برای این کاربر ثبت نشده. */
    public static IncomingChallenge pollIncomingChallenge(String username) {
        NetworkMessage req = new NetworkMessage("IZOMBIE_POLL_INCOMING_CHALLENGE");
        req.data.put("username", username);
        NetworkMessage res = NetworkManager.sendRequest(req);
        if (!res.success) {
            return null;
        }
        IncomingChallenge challenge = new IncomingChallenge();
        challenge.fromUsername = res.data.get("fromUsername");
        try {
            challenge.level = Integer.parseInt(res.data.get("level"));
        } catch (Exception e) {
            challenge.level = 1;
        }
        return challenge;
    }

    public static void respondToChallenge(String username, boolean accept) {
        NetworkMessage req = new NetworkMessage("IZOMBIE_RESPOND_CHALLENGE");
        req.data.put("username", username);
        req.data.put("accept", String.valueOf(accept));
        NetworkManager.sendRequest(req);
    }

    public enum PollStatus { PENDING, MATCHED, REJECTED, WAITING }

    public static final class PollResult {
        public PollStatus status;
        public String matchId;
    }

    /** برای بازیکنی که کاربر مشخصی را چالش کرده: منتظر تایید/رد او می‌ماند. */
    public static PollResult pollChallengeResult(String username) {
        NetworkMessage req = new NetworkMessage("IZOMBIE_POLL_CHALLENGE_RESULT");
        req.data.put("username", username);
        NetworkMessage res = NetworkManager.sendRequest(req);
        PollResult result = new PollResult();
        if (!res.success) {
            result.status = PollStatus.PENDING;
            return result;
        }
        if ("MATCHED".equals(res.responseBody)) {
            result.status = PollStatus.MATCHED;
            result.matchId = res.data.get("matchId");
        } else {
            result.status = PollStatus.REJECTED;
        }
        return result;
    }

    public static PollResult joinRandomQueue(String username, int level) {
        NetworkMessage req = new NetworkMessage("IZOMBIE_JOIN_RANDOM_QUEUE");
        req.data.put("username", username);
        req.data.put("level", String.valueOf(level));
        NetworkMessage res = NetworkManager.sendRequest(req);
        PollResult result = new PollResult();
        if (res.success && "MATCHED".equals(res.responseBody)) {
            result.status = PollStatus.MATCHED;
            result.matchId = res.data.get("matchId");
        } else {
            result.status = PollStatus.WAITING;
        }
        return result;
    }

    public static void leaveRandomQueue(String username) {
        NetworkMessage req = new NetworkMessage("IZOMBIE_LEAVE_RANDOM_QUEUE");
        req.data.put("username", username);
        NetworkManager.sendRequest(req);
    }

    /** کاربری که وارد صف تصادفی شده، این را دوره‌ای صدا می‌زند. */
    public static PollResult pollRandomMatch(String username) {
        NetworkMessage req = new NetworkMessage("IZOMBIE_POLL_RANDOM_MATCH");
        req.data.put("username", username);
        NetworkMessage res = NetworkManager.sendRequest(req);
        PollResult result = new PollResult();
        if (res.success && "MATCHED".equals(res.responseBody)) {
            result.status = PollStatus.MATCHED;
            result.matchId = res.data.get("matchId");
        } else {
            result.status = PollStatus.WAITING;
        }
        return result;
    }

    public static final class MatchInfo {
        public boolean found;
        public MultiplayerMatch.Role role;
        public String opponentUsername;
    }

    public static MatchInfo getMatchInfo(String username, String matchId) {
        NetworkMessage req = new NetworkMessage("IZOMBIE_MATCH_INFO");
        req.data.put("username", username);
        req.data.put("matchId", matchId);
        NetworkMessage res = NetworkManager.sendRequest(req);
        MatchInfo info = new MatchInfo();
        info.found = res.success;
        if (res.success) {
            info.role = MultiplayerMatch.Role.valueOf(res.data.get("role"));
            info.opponentUsername = res.data.get("opponentUsername");
        }
        return info;
    }

    public static BoardSnapshot fetchState(String username, String matchId) {
        NetworkMessage req = new NetworkMessage("IZOMBIE_STATE");
        req.data.put("username", username);
        req.data.put("matchId", matchId);
        NetworkMessage res = NetworkManager.sendRequest(req);
        if (!res.success || !(res.payload instanceof BoardSnapshot)) {
            return null;
        }
        return (BoardSnapshot) res.payload;
    }

    public static String placeZombie(String username, String matchId, String zombieType, int row, int col) {
        NetworkMessage req = new NetworkMessage("IZOMBIE_PLACE_ZOMBIE");
        req.data.put("username", username);
        req.data.put("matchId", matchId);
        req.data.put("zombieType", zombieType);
        req.data.put("row", String.valueOf(row));
        req.data.put("col", String.valueOf(col));
        NetworkMessage res = NetworkManager.sendRequest(req);
        return res.responseBody;
    }

    public static String plantPlant(String username, String matchId, String plantType, int row, int col) {
        NetworkMessage req = new NetworkMessage("IZOMBIE_PLANT");
        req.data.put("username", username);
        req.data.put("matchId", matchId);
        req.data.put("plantType", plantType);
        req.data.put("row", String.valueOf(row));
        req.data.put("col", String.valueOf(col));
        NetworkMessage res = NetworkManager.sendRequest(req);
        return res.responseBody;
    }

    public static void sendReaction(String username, String matchId, String kind, String content) {
        NetworkMessage req = new NetworkMessage("IZOMBIE_SEND_REACTION");
        req.data.put("username", username);
        req.data.put("matchId", matchId);
        req.data.put("kind", kind);
        req.data.put("content", content);
        NetworkManager.sendRequest(req);
    }

    @SuppressWarnings("unchecked")
    public static List<ReactionMessage> pollReactions(String username, String matchId) {
        NetworkMessage req = new NetworkMessage("IZOMBIE_POLL_REACTIONS");
        req.data.put("username", username);
        req.data.put("matchId", matchId);
        NetworkMessage res = NetworkManager.sendRequest(req);
        if (res.success && res.payload instanceof List) {
            return (List<ReactionMessage>) res.payload;
        }
        return new ArrayList<>();
    }

    public static String pollMyCurrentMatch(String username) {
        NetworkMessage req = new NetworkMessage("IZOMBIE_MY_MATCH");
        req.data.put("username", username);
        NetworkMessage res = NetworkManager.sendRequest(req);
        if (res.success) {
            return res.data.get("matchId");
        }
        return null;
    }

    public static void leaveMatch(String username) {
        NetworkMessage req = new NetworkMessage("IZOMBIE_LEAVE_MATCH");
        req.data.put("username", username);
        NetworkManager.sendRequest(req);
    }
}
