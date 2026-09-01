package gdx.screens;

import com.badlogic.gdx.scenes.scene2d.ui.Label;

import gdx.PvZGame;
import gdx.assets.AssetPaths;
import network.izombie.IZombieNetworkClient;

/**
 * صفحه‌ی انتظار برای مینی‌گیم دونفره‌ی «من، زامبی».
 *
 * چون ارتباط کلاینت-سرور این پروژه بر پایه‌ی درخواست/پاسخ همزمان است (نه یک
 * کانال push واقعی)، این صفحه هر چند ثانیه یک‌بار (POLL_INTERVAL) از سرور
 * می‌پرسد که آیا وضعیت عوض شده یا نه:
 *  - حالت WAITING_FOR_CHALLENGE_RESPONSE: منتظر تایید/رد چالش از طرف رقیب مشخص
 *  - حالت RANDOM_QUEUE: منتظر پیدا شدن یک حریف تصادفی
 * به‌محض پیدا شدن matchId، مستقیم وارد صفحه‌ی بازی چندنفره می‌شویم.
 */
public class IZombieWaitingScreen extends BaseMenuScreen {

    public enum Mode { WAITING_FOR_CHALLENGE_RESPONSE, RANDOM_QUEUE }

    private static final float POLL_INTERVAL = 1.5f;

    private final Mode mode;
    private final int level;
    private final String opponentUsername; // فقط برای WAITING_FOR_CHALLENGE_RESPONSE

    private final Label statusLabel;
    private float pollAccumulator = 0f;
    private boolean resolved = false;
    private boolean leftQueue = false;

    public IZombieWaitingScreen(PvZGame game, int level, Mode mode, String opponentUsername) {
        super(game);
        this.mode = mode;
        this.level = level;
        this.opponentUsername = opponentUsername;

        String headline = mode == Mode.RANDOM_QUEUE
                ? "Searching for an opponent..."
                : "Waiting for " + opponentUsername + " to respond...";

        rootTable.add(title("I, Zombie")).padBottom(16f).row();
        statusLabel = new Label(headline, skin);
        statusLabel.setWrap(true);
        rootTable.add(statusLabel).width(520f).padBottom(30f).row();

        addButton(rootTable, "Cancel", this::cancel);
    }

    private void cancel() {
        if (resolved) {
            return;
        }
        resolved = true;
        String username = game.getLoggedInUser().getUsername();
        if (mode == Mode.RANDOM_QUEUE) {
            IZombieNetworkClient.leaveRandomQueue(username);
        }
        // برای چالش مشخص، سرور خودش با یک پاسخ منفی یا timeout (که در این
        // پیاده‌سازی وجود ندارد) هماهنگ می‌شود؛ کاربر صرفا صفحه را ترک می‌کند.
        game.goToMiniGames();
    }

    @Override
    public void render(float delta) {
        super.render(delta);
        if (resolved) {
            return;
        }

        pollAccumulator += delta;
        if (pollAccumulator < POLL_INTERVAL) {
            return;
        }
        pollAccumulator = 0f;

        String username = game.getLoggedInUser().getUsername();
        IZombieNetworkClient.PollResult result = (mode == Mode.RANDOM_QUEUE)
                ? IZombieNetworkClient.pollRandomMatch(username)
                : IZombieNetworkClient.pollChallengeResult(username);

        if (result.status == IZombieNetworkClient.PollStatus.MATCHED) {
            resolved = true;
            game.setScreen(new IZombieMultiplayerScreen(game, result.matchId, level));
        } else if (result.status == IZombieNetworkClient.PollStatus.REJECTED) {
            resolved = true;
            game.setScreen(new IZombieChallengeRejectedScreen(game, opponentUsername));
        }
    }

    @Override
    protected String backgroundPath() {
        return AssetPaths.BG_QUEST;
    }
}
