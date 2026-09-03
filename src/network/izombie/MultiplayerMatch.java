package network.izombie;

import model.minigame.IZombieSession;
import model.user.User;
import model.user.UserManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * یک مسابقه‌ی دونفره‌ی «من، زامبی» تحت شبکه. طبق سند فاز ۳:
 * - یک بازیکن مسئول کاشتن گیاهان و بازیکن دیگر مسئول رهاسازی زامبی‌هاست.
 * - وضعیت زمین بین دو کلاینت باید همگام (Synchronized) باشد؛ به همین دلیل
 *   سرور «مرجع» (authoritative) است: منطق واقعی بازی فقط اینجا (روی همین
 *   شیء IZombieSession مشترک) اجرا می‌شود و هر کلاینت فقط عکس فوری
 *   (BoardSnapshot) دریافت می‌کند، نه اینکه خودش شبیه‌سازی کند.
 * - شرط پیروزی طرف گیاه یا خوردن نشدن همه‌ی مغزها به روش عادی است، یا
 *   مقاومت به مدت ۲ دقیقه (پیش‌فرض قابل‌تنظیم توسط سازنده‌ی این کلاس).
 *
 * این کلاس با یک Thread پس‌زمینه‌ی خودش تیک بازی را جلو می‌برد (هر تیک برابر
 * ۱۰۰ میلی‌ثانیه، یعنی ۱۰ تیک بر ثانیه، هم‌راستا با نسخه‌ی تک‌نفره‌ی گرافیکی).
 */
public class MultiplayerMatch {

    public enum Role { PLANT, ZOMBIE }

    private static final long TICK_MILLIS = 100L;
    private static final int DEFAULT_SURVIVAL_SECONDS = 120; // طبق سند: ۲ دقیقه (قابل تغییر)

    private final String matchId;
    private final String plantUsername;
    private final String zombieUsername;
    private final IZombieSession session;
    private final int survivalTicks;

    private volatile int elapsedTicks = 0;
    private volatile boolean plantSurvived = false; // برنده شدن گیاه به‌خاطر پایان زمان
    private volatile boolean running = true;
    private volatile boolean finished = false;

    // رخدادهای تازه (خوردن مغز، برد/باخت) که هنوز برای کلاینت‌ها ارسال نشده
    private final List<String> pendingEvents = new CopyOnWriteArrayList<>();

    // واکنش‌های در انتظار تحویل، به تفکیک گیرنده
    private final Map<String, List<ReactionMessage>> pendingReactions = new ConcurrentHashMap<>();

    private Thread tickThread;

    public MultiplayerMatch(String matchId, User plantUser, String zombieUsername, int level) {
        this(matchId, plantUser, zombieUsername, level, DEFAULT_SURVIVAL_SECONDS);
    }

    public MultiplayerMatch(String matchId, User plantUser, String zombieUsername, int level, int survivalSeconds) {
        this.matchId = matchId;
        this.plantUsername = plantUser.getUsername();
        this.zombieUsername = zombieUsername;
        this.session = new IZombieSession(plantUser, level);
        this.survivalTicks = survivalSeconds * 10; // ۱۰ تیک بر ثانیه
        pendingReactions.put(this.plantUsername, new CopyOnWriteArrayList<>());
        pendingReactions.put(this.zombieUsername, new CopyOnWriteArrayList<>());
    }

    public void start() {
        tickThread = new Thread(this::runLoop, "izombie-match-" + matchId);
        tickThread.setDaemon(true);
        tickThread.start();
    }

    private void runLoop() {
        while (running) {
            try {
                Thread.sleep(TICK_MILLIS);
            } catch (InterruptedException e) {
                return;
            }
            tick();
        }
    }

    private synchronized void tick() {
        if (finished) {
            return;
        }
        if (!session.isGameOver()) {
            session.advanceOneTick();
            elapsedTicks++;

            List<String> newEvents = session.pollRecentEvents();
            if (!newEvents.isEmpty()) {
                pendingEvents.addAll(newEvents);
            }

            // شرط دوم پیروزی گیاه طبق سند: مقاومت به مدت ۲ دقیقه بدون خورده شدن همه‌ی مغزها
            if (!session.isGameOver() && elapsedTicks >= survivalTicks) {
                plantSurvived = true;
                pendingEvents.add("زمان مسابقه به پایان رسید و همه‌ی مغزها خورده نشدند. طرف گیاه برنده شد!");
            }
        }

        if ((session.isGameOver() || plantSurvived) && !finished) {
            finished = true;
            running = false;
        }
    }

    /** آیا طرف گیاه برنده است؟ (یا با قوانین معمول مینی‌گیم یا با مقاومت زمانی). */
    public synchronized boolean didPlantSideWin() {
        if (plantSurvived) {
            return true;
        }
        return session.isGameOver() && session.isWon();
    }

    public synchronized boolean isFinished() {
        return finished;
    }

    public synchronized BoardSnapshot snapshotFor() {
        List<String> events = new ArrayList<>(pendingEvents);
        pendingEvents.clear();
        BoardSnapshot snap = BoardSnapshot.capture(session, Math.max(0, survivalTicks - elapsedTicks), events);
        if (finished) {
            snap.gameOver = true;
            snap.plantSideWon = didPlantSideWin();
        }
        return snap;
    }

    public synchronized IZombieSession.PlaceZombieResult placeZombie(String username, String type, int row, int col) {
        if (!zombieUsername.equals(username) || finished) {
            return IZombieSession.PlaceZombieResult.INVALID_LOCATION;
        }
        return session.placeZombie(type, row, col);
    }

    /** نتیجه‌ی تلاش برای کاشت گیاه: null یعنی موفق. مقدار غیر null پیام خطا برای کلاینت است. */
    public synchronized String plantAt(String username, String plantType, int row, int col) {
        if (!plantUsername.equals(username) || finished) {
            return "ERR_INVALID";
        }
        model.game.Board board = session.getBoard();
        if (row < 0 || row >= model.game.Board.ROWS || col < 0 || col >= model.game.Board.COLS) {
            return "ERR_INVALID_LOCATION";
        }
        model.game.Tile tile = board.getTile(row, col);
        model.plant.Plant plant;
        try {
            plant = model.plant.PlantFactory.create(plantType);
        } catch (IllegalArgumentException e) {
            return "ERR_INVALID_PLANT";
        }
        if (tile == null || !tile.canPlant(plant)) {
            return "ERR_INVALID_LOCATION";
        }
        if (session.isPlantOnCooldown(plantType)) {
            return "ERR_COOLDOWN";
        }
        if (!session.getSunManager().spendSun(plant.getSunCost())) {
            return "ERR_NOT_ENOUGH_SUN";
        }
        plant.place(row, col);
        tile.setPlant(plant);
        session.startPlantCooldown(plantType, plant.getCooldownTicks());
        return null;
    }

    /**
     * برداشت خورشیدِ آماده از یک آفتابگردان (یا هر گیاه خورشیدزای دیگر) روی
     * زمین. فقط بازیکن نقش PLANT اجازه‌ی این کار را دارد؛ سرور دوباره (مستقل
     * از کلاینت) بررسی می‌کند که واقعاً خورشیدی برای برداشت آماده باشد.
     * null یعنی موفق، در غیر این صورت پیام خطا برای کلاینت است.
     */
    public synchronized String collectSun(String username, int row, int col) {
        if (!plantUsername.equals(username) || finished) {
            return "ERR_INVALID";
        }
        model.game.Board board = session.getBoard();
        if (row < 0 || row >= model.game.Board.ROWS || col < 0 || col >= model.game.Board.COLS) {
            return "ERR_INVALID_LOCATION";
        }
        model.game.Tile tile = board.getTile(row, col);
        model.plant.Plant plant = (tile == null) ? null : tile.getPlant();
        if (!(plant instanceof model.plant.interfaces.ISunProducer)) {
            return "ERR_INVALID_LOCATION";
        }
        model.plant.interfaces.ISunProducer producer = (model.plant.interfaces.ISunProducer) plant;
        if (!producer.isSunReady()) {
            return "ERR_NOT_READY";
        }
        session.getSunManager().addSun(producer.getReadySunAmount());
        producer.collectSun();
        return null;
    }

    public void sendReaction(String fromUsername, String kind, String content) {
        String targetUsername = fromUsername.equals(plantUsername) ? zombieUsername : plantUsername;
        List<ReactionMessage> queue = pendingReactions.get(targetUsername);
        if (queue != null) {
            ReactionMessage msg = new ReactionMessage();
            msg.fromUsername = fromUsername;
            msg.kind = kind;
            msg.content = content;
            queue.add(msg);
        }
    }

    public List<ReactionMessage> pollReactionsFor(String username) {
        List<ReactionMessage> queue = pendingReactions.get(username);
        if (queue == null) {
            return new ArrayList<>();
        }
        List<ReactionMessage> copy = new ArrayList<>(queue);
        queue.clear();
        return copy;
    }

    public String getMatchId() {
        return matchId;
    }

    public String getPlantUsername() {
        return plantUsername;
    }

    public String getZombieUsername() {
        return zombieUsername;
    }

    public Role roleOf(String username) {
        if (plantUsername.equals(username)) return Role.PLANT;
        if (zombieUsername.equals(username)) return Role.ZOMBIE;
        return null;
    }

    public boolean involves(String username) {
        return plantUsername.equals(username) || zombieUsername.equals(username);
    }

    public String opponentOf(String username) {
        if (plantUsername.equals(username)) return zombieUsername;
        if (zombieUsername.equals(username)) return plantUsername;
        return null;
    }

    public void stop() {
        running = false;
        if (tickThread != null) {
            tickThread.interrupt();
        }
    }
}
