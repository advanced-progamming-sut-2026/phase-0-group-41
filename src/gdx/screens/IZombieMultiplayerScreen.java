package gdx.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import gdx.PvZGame;
import gdx.assets.AssetPaths;
import gdx.util.ImageUtils;
import gdx.util.SoundManager;

import model.game.Board;

import network.izombie.BoardSnapshot;
import network.izombie.IZombieNetworkClient;
import network.izombie.MultiplayerMatch;
import network.izombie.ReactionMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * صفحه‌ی گرافیکی مسابقه‌ی دونفره‌ی تحت‌شبکه‌ی «من، زامبی» (طبق سند فاز ۳).
 *
 * سرور «مرجع» (authoritative) است: منطق واقعی بازی روی MultiplayerMatch سمت
 * سرور اجرا می‌شود. این کلاس صرفاً هر چند دهم ثانیه یک‌بار یک BoardSnapshot
 * تازه از سرور می‌خواهد و همان را رسم می‌کند؛ هیچ منطق برد/باخت یا فیزیکی در
 * سمت کلاینت اجرا نمی‌شود، تا هر دو بازیکن دقیقاً یک تصویر یکسان از زمین
 * بازی داشته باشند.
 *
 * هر بازیکن فقط می‌تواند نیروهای جبهه‌ی خودش را کنترل کند: نقش PLANT فقط
 * می‌تواند گیاه بکارد و نقش ZOMBIE فقط می‌تواند زامبی قرار دهد؛ سرور هم این
 * محدودیت را دوباره (مستقل از کلاینت) بررسی می‌کند.
 */
public class IZombieMultiplayerScreen implements com.badlogic.gdx.Screen {

    private static final float WORLD_WIDTH = 1280f;
    private static final float WORLD_HEIGHT = 720f;

    private static final float BOARD_LEFT = 260f;
    private static final float BOARD_TOP = 640f;
    private static final float TILE_W = 100f;
    private static final float TILE_H = 96f;

    private static final float STATE_POLL_INTERVAL = 0.15f;
    private static final float REACTION_POLL_INTERVAL = 1.0f;
    private static final String[] ZOMBIE_TYPES = {"normal", "conehead", "buckethead", "imp"};
    private static final String[] PLANT_TYPES = {"peashooter", "sunflower", "wallnut", "potatomine", "squash"};
    // ۴ پیام متنی آماده طبق سند فاز ۳ (بخش «سیستم ارسال واکنش در حین بازی»)
    private static final String[] QUICK_MESSAGES = {
            "Hello!",
            "I'll eat your brain",
            "You're a loser",
            "good game!"
    };
    // ۳ ایموجی: خنده، گریه، دست عضله‌نشان (نماد قدرت/زور بازو)
    private static final String[] EMOJIS = {"\uD83D\uDE02", "\uD83D\uDE22", "\uD83D\uDCAA"}; // 😂 😢 💪
    private static final String[] STICKERS = {"\u2B50", "\uD83D\uDD25", "\uD83D\uDC80"}; // ⭐ 🔥 💀 (بخش امتیازی)

    private final PvZGame game;
    private final Stage stage;
    private final Skin skin;
    private final String matchId;
    private final int level;
    private final String myUsername;

    private MultiplayerMatch.Role myRole;
    private String opponentUsername = "";

    private BoardSnapshot snapshot;
    private float statePollAccumulator = STATE_POLL_INTERVAL; // اولین درخواست بلافاصله انجام شود
    private float reactionPollAccumulator = 0f;
    private boolean navigatedToResult = false;

    private final Label sunLabel;
    private final Label sunRoleLabel;
    private final Label statusLabel;
    private final Label timerLabel;
    private final Label opponentReactionLabel;
    private float opponentReactionTimeLeft = 0f;
    private final List<String> eventLog = new ArrayList<>();

    private String selectedZombieType = null;
    private String selectedPlantType = null;

    private final Table sidebarTable = new Table();

    public IZombieMultiplayerScreen(PvZGame game, String matchId, int level) {
        this.game = game;
        this.matchId = matchId;
        this.level = level;
        this.skin = game.getSkin();
        this.myUsername = game.getLoggedInUser().getUsername();

        Viewport viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT);
        this.stage = new Stage(viewport);

        sunLabel = new Label("0", skin);
        sunRoleLabel = new Label("Sun:", skin);
        statusLabel = new Label("", skin);
        timerLabel = new Label("", skin);
        opponentReactionLabel = new Label("", skin, "title");

        IZombieNetworkClient.MatchInfo info = IZombieNetworkClient.getMatchInfo(myUsername, matchId);
        if (info.found) {
            myRole = info.role;
            opponentUsername = info.opponentUsername;
        }

        buildHud();
        buildSidebar();
        buildReactionBar();

        SoundManager.playMusic(AssetPaths.MUSIC_MINIGAME);
        Gdx.input.setInputProcessor(stage);
    }

    // ==================== ساخت رابط کاربری ثابت ====================

    private void buildHud() {
        Table top = new Table();
        top.setFillParent(true);
        top.top();

        TextButton pauseButton = new TextButton("II", skin);
        pauseButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                openPauseMenu();
            }
        });

        TextButton exitButton = new TextButton("Exit", skin);
        exitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                leaveAndGoTo(() -> game.goToMiniGames());
            }
        });

        Table sunBox = new Table();
        sunBox.add(new Image(ImageUtils.loadRegion(AssetPaths.ICON_SUN))).size(32f).padRight(4f);
        sunBox.add(sunRoleLabel).padRight(4f);
        sunBox.add(sunLabel).padRight(20f);

        String roleText = myRole == null ? "" : (myRole == MultiplayerMatch.Role.PLANT ? "You: Plants" : "You: Zombies");
        Label title = new Label("I, Zombie Online - " + roleText + " vs " + opponentUsername, skin, "title");

        Table row = new Table();
        row.add(pauseButton).size(56f, 44f).padRight(8f);
        row.add(exitButton).size(70f, 44f).padRight(16f);
        row.add(sunBox);
        row.add(title).padLeft(20f);

        top.add(row).padTop(8f).row();
        top.add(timerLabel).padTop(2f).row();
        top.add(statusLabel).padTop(2f).row();
        stage.addActor(top);

        // نمایش واکنش دریافتی از حریف در گوشه‌ی صفحه
        Table cornerTable = new Table();
        cornerTable.setFillParent(true);
        cornerTable.top().right().padTop(90f).padRight(20f);
        cornerTable.add(opponentReactionLabel);
        stage.addActor(cornerTable);
    }

    private void buildSidebar() {
        sidebarTable.setFillParent(true);
        sidebarTable.top().left();
        sidebarTable.padTop(80f).padLeft(8f);
        stage.addActor(sidebarTable);

        if (myRole == MultiplayerMatch.Role.ZOMBIE) {
            sidebarTable.add(new Label("Choose zombie:", skin)).left().row();
            for (String t : ZOMBIE_TYPES) {
                sidebarTable.add(buildZombieCard(t)).size(80f, 80f).padBottom(6f).row();
            }
        } else if (myRole == MultiplayerMatch.Role.PLANT) {
            sidebarTable.add(new Label("Choose plant:", skin)).left().row();
            for (String p : PLANT_TYPES) {
                sidebarTable.add(buildPlantCard(p)).size(80f, 80f).padBottom(6f).row();
            }
        }
    }

    private Stack buildZombieCard(String zombieType) {
        Stack stack = new Stack();
        stack.add(new Image(ImageUtils.loadRegion(AssetPaths.CARD_BACKGROUND)));
        stack.add(new Image(ImageUtils.loadRegion(AssetPaths.zombieIcon(zombieType))));
        stack.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                selectedZombieType = zombieType;
                SoundManager.playSound(AssetPaths.SFX_CLICK);
            }
        });
        return stack;
    }

    private Stack buildPlantCard(String plantName) {
        Stack stack = new Stack();
        stack.add(new Image(ImageUtils.loadRegion(AssetPaths.CARD_BACKGROUND)));
        stack.add(new Image(ImageUtils.loadRegion(AssetPaths.plantSeedPacket(plantName))));
        stack.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                selectedPlantType = plantName;
                SoundManager.playSound(AssetPaths.SFX_CLICK);
            }
        });
        return stack;
    }

    /** نوار پایینی واکنش‌ها: ۳ پیام متنی، ۳ ایموجی و (بخش امتیازی) ۳ استیکر متحرک. */
    private void buildReactionBar() {
        Table bottom = new Table();
        bottom.setFillParent(true);
        bottom.bottom().padBottom(10f);

        Table row = new Table();
        for (String msg : QUICK_MESSAGES) {
            TextButton b = new TextButton(msg, skin);
            b.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    IZombieNetworkClient.sendReaction(myUsername, matchId, ReactionMessage.KIND_TEXT, msg);
                }
            });
            row.add(b).pad(4f);
        }
        for (String emoji : EMOJIS) {
            TextButton b = new TextButton(emoji, skin);
            b.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    IZombieNetworkClient.sendReaction(myUsername, matchId, ReactionMessage.KIND_EMOJI, emoji);
                }
            });
            row.add(b).pad(4f);
        }
        // بخش امتیازی: استیکرهای متحرک
        for (String sticker : STICKERS) {
            TextButton b = new TextButton(sticker, skin);
            b.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    IZombieNetworkClient.sendReaction(myUsername, matchId, ReactionMessage.KIND_STICKER, sticker);
                }
            });
            row.add(b).pad(4f);
        }
        bottom.add(row);
        stage.addActor(bottom);
    }

    // ==================== حلقه‌ی اصلی ====================

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.05f, 0.05f, 0.05f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        pollServer(delta);

        stage.getBatch().begin();
        drawBoard(stage.getBatch());
        if (snapshot != null) {
            drawPlants(stage.getBatch());
            drawZombies(stage.getBatch());
        }
        stage.getBatch().end();

        if (snapshot != null) {
            int mySun = (myRole == MultiplayerMatch.Role.ZOMBIE) ? snapshot.zombieSun : snapshot.plantSun;
            sunRoleLabel.setText(myRole == MultiplayerMatch.Role.ZOMBIE ? "Zombie sun:" : "Plant sun:");
            sunLabel.setText(String.valueOf(mySun));
            timerLabel.setText("Time left: " + (snapshot.timeRemainingTicks / 10) + "s");
        }
        if (opponentReactionTimeLeft > 0f) {
            opponentReactionTimeLeft -= delta;
            if (opponentReactionTimeLeft <= 0f) {
                opponentReactionLabel.setText("");
            }
        }

        stage.act(delta);
        stage.draw();

        handleBoardClick();
    }

    private void pollServer(float delta) {
        statePollAccumulator += delta;
        if (statePollAccumulator >= STATE_POLL_INTERVAL) {
            statePollAccumulator = 0f;
            BoardSnapshot latest = IZombieNetworkClient.fetchState(myUsername, matchId);
            if (latest != null) {
                snapshot = latest;
                for (String event : latest.events) {
                    eventLog.add(event);
                }
                if (!latest.events.isEmpty()) {
                    statusLabel.setText(latest.events.get(latest.events.size() - 1));
                }
                if (latest.gameOver && !navigatedToResult) {
                    navigatedToResult = true;
                    boolean iWon = (myRole == MultiplayerMatch.Role.PLANT) == latest.plantSideWon;
                    game.setScreen(new IZombieMultiplayerResultScreen(game, level, iWon));
                    return;
                }
            }
        }

        reactionPollAccumulator += delta;
        if (reactionPollAccumulator >= REACTION_POLL_INTERVAL) {
            reactionPollAccumulator = 0f;
            List<ReactionMessage> reactions = IZombieNetworkClient.pollReactions(myUsername, matchId);
            for (ReactionMessage r : reactions) {
                showIncomingReaction(r);
            }
        }
    }

    private void showIncomingReaction(ReactionMessage r) {
        opponentReactionLabel.setText(opponentUsername + ": " + r.content);
        opponentReactionLabel.setColor(Color.WHITE);
        // برای استیکر متحرک، اندازه‌ی متن بزرگ‌تر و ماندگاری کمی بیشتر است تا حس متحرک/برجسته بودن منتقل شود
        boolean isSticker = ReactionMessage.KIND_STICKER.equals(r.kind);
        opponentReactionLabel.setFontScale(isSticker ? 2.2f : 1.4f);
        opponentReactionTimeLeft = isSticker ? 2.5f : 2.0f;
        SoundManager.playSound(AssetPaths.SFX_CLICK);
    }

    // ==================== رسم ====================

    private float tileX(int col) {
        return BOARD_LEFT + col * TILE_W;
    }

    private float tileY(int row) {
        return BOARD_TOP - (row + 1) * TILE_H;
    }

    private void drawBoard(com.badlogic.gdx.graphics.g2d.Batch batch) {
        TextureRegion bg = ImageUtils.loadRegion(AssetPaths.BG_MINIGAME_IZOMBIE);
        batch.draw(bg, 0, 0, WORLD_WIDTH, WORLD_HEIGHT);
    }

    private void drawPlants(com.badlogic.gdx.graphics.g2d.Batch batch) {
        for (BoardSnapshot.PlantDto p : snapshot.plants) {
            TextureRegion tex = ImageUtils.loadRegion(AssetPaths.plantIcon(p.name));
            batch.draw(tex, tileX(p.col) + 8f, tileY(p.row) + 8f, TILE_W - 16f, TILE_H - 16f);
        }
    }

    private void drawZombies(com.badlogic.gdx.graphics.g2d.Batch batch) {
        for (BoardSnapshot.ZombieDto z : snapshot.zombies) {
            TextureRegion tex = ImageUtils.loadRegion(AssetPaths.zombieIcon(z.typeName));
            float x = tileX(0) + (float) z.xPosition * TILE_W;
            float y = tileY(z.row);
            batch.draw(tex, x, y, TILE_W - 10f, TILE_H - 10f);
        }
    }

    // ==================== ورودی کاربر ====================

    private void handleBoardClick() {
        if (!Gdx.input.justTouched() || myRole == null) {
            return;
        }
        Vector2 touch = stage.screenToStageCoordinates(new Vector2(Gdx.input.getX(), Gdx.input.getY()));

        int col = (int) ((touch.x - BOARD_LEFT) / TILE_W);
        int row = (int) ((BOARD_TOP - touch.y) / TILE_H) - 1;
        if (col < 0 || col >= Board.COLS || row < 0 || row >= Board.ROWS) {
            return;
        }

        if (myRole == MultiplayerMatch.Role.ZOMBIE) {
            placeZombieAt(row, col);
        } else if (myRole == MultiplayerMatch.Role.PLANT) {
            plantAt(row, col);
        }
    }

    private void placeZombieAt(int row, int col) {
        if (selectedZombieType == null) {
            return;
        }
        String result = IZombieNetworkClient.placeZombie(myUsername, matchId, selectedZombieType, row, col);
        statusLabel.setText(describeZombieResult(result));
        SoundManager.playSound(AssetPaths.SFX_CLICK);
    }

    private void plantAt(int row, int col) {
        if (selectedPlantType == null) {
            return;
        }
        String result = IZombieNetworkClient.plantPlant(myUsername, matchId, selectedPlantType, row, col);
        statusLabel.setText(describePlantResult(result));
        SoundManager.playSound(AssetPaths.SFX_CLICK);
    }

    private String describeZombieResult(String result) {
        if (result == null) return "";
        switch (result) {
            case "SUCCESS": return selectedZombieType + " placed!";
            case "BEYOND_RED_LINE": return "You can only place zombies right of the red line.";
            case "NOT_ENOUGH_SUN": return "Not enough sun for " + selectedZombieType + ".";
            case "INVALID_ZOMBIE": return "Unknown zombie type.";
            default: return "Invalid location.";
        }
    }

    private String describePlantResult(String result) {
        if (result == null) return "";
        switch (result) {
            case "SUCCESS": return selectedPlantType + " planted!";
            case "ERR_NOT_ENOUGH_SUN": return "Not enough sun for " + selectedPlantType + ".";
            case "ERR_COOLDOWN": return selectedPlantType + " is still on cooldown.";
            case "ERR_INVALID_LOCATION": return "You can't plant there.";
            case "ERR_INVALID_PLANT": return "Unknown plant type.";
            default: return "Invalid move.";
        }
    }

    // ==================== خروج ====================

    private void leaveAndGoTo(Runnable next) {
        IZombieNetworkClient.leaveMatch(myUsername);
        next.run();
    }

    /** === رفع باگ: نبود دکمه‌ی توقف در حالت آنلاین ===
     *  چون سرور «مرجع» است و مسابقه‌ی زنده‌ی حریف مستقل از کلاینت شما ادامه
     *  پیدا می‌کند، توقف واقعی/فریز کردن کل بازی برای هر دو طرف اینجا معنا
     *  ندارد (این کار بازی حریف را هم متوقف می‌کرد که غیرمنصفانه است). به
     *  همین دلیل دکمه‌ی توقف یک منوی محلی (تنظیمات صدا / ادامه / خروج با
     *  اطلاع‌رسانی) باز می‌کند؛ خود ارتباط با سرور و ساعت مسابقه متوقف
     *  نمی‌شود، اما حداقل کاربر - درست مثل مرحله‌ی عادی - راهی برای توقف و
     *  دیدن منو دارد و می‌تواند با اطمینان از آن خارج شود. */
    private void openPauseMenu() {
        SoundManager.playSound(AssetPaths.SFX_CLICK);
        game.setScreen(new PauseScreen(game, this, () -> game.setScreen(this),
                () -> leaveAndGoTo(() -> game.goToMiniGames())));
    }

    // ==================== متدهای استاندارد Screen ====================

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}
