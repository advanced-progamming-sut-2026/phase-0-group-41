package gdx.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
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
import model.minigame.BeghouledSession;
import model.minigame.IZombieSession;
import model.minigame.MiniGameSession;
import model.minigame.VasebreakerSession;
import model.minigame.WallnutBowlingSession;
import model.plant.Plant;
import model.zombie.Zombie;

/**
 * صفحه‌ی گرافیکی مشترک هر چهار مینی‌گیم. چون هر مینی‌گیم مدل نمونه‌سازی و روش
 * تعامل متفاوتی دارد (شکستن کوزه / کاشتن گردو / گذاشتن زامبی / جابجایی گیاه)،
 * این کلاس صرفاً روی همان تخته‌ی ۵×۹ استاندارد (که MiniGameSession از
 * GameSession به ارث می‌برد) کلیک کاربر را به متد مناسب مدل مربوطه وصل می‌کند.
 */
public class MiniGameScreen implements Screen {

    private static final float WORLD_WIDTH = 1280f;
    private static final float WORLD_HEIGHT = 720f;

    private static final float BOARD_LEFT = 260f;
    private static final float BOARD_TOP = 640f;
    private static final float TILE_W = 100f;
    private static final float TILE_H = 96f;
    private static final float SECONDS_PER_TICK = 0.1f;

    private final PvZGame game;
    private final Stage stage;
    private final Skin skin;
    private final MiniGameSession session;
    private final String gameName;
    private final int level;

    private final Label sunLabel;
    private final Label statusLabel;
    private final Table sidebarTable = new Table();

    private float tickAccumulator = 0f;
    private boolean announcedGameOver = false;

    // برای بولینگ گردویی: نوع گردوی انتخاب‌شده از نوار نقاله برای کاشت بعدی
    private WallnutBowlingSession.NutType selectedNutType = null;
    // برای من زامبی: نوع زامبی انتخاب‌شده برای قرار دادن
    private String selectedZombieType = null;
    // برای کوزه‌شکنی: بذر افتاده‌ی انتخاب‌شده از نوار کناری برای کاشت بعدی
    private VasebreakerSession.DroppedSeedPacket selectedDroppedSeed = null;
    // برای Beghouled: خانه‌ی اول انتخاب‌شده برای جابجایی
    private int[] firstSwapTile = null;

    // === رفع باگ اصلی: قبلاً sidebarTable هر فریم (۶۰ بار در ثانیه) کاملاً
    // پاک و از نو ساخته می‌شد (refreshSidebar در render). چون هر بار Actor/
    // ClickListener کاملاً جدیدی ساخته می‌شد، هیچ کلیکی (که همیشه چند فریم طول
    // می‌کشد: یک فریم touchDown و فریم(های) بعدی touchUp) هرگز کامل نمی‌شد، چون
    // touchUp روی یک Actor کاملاً متفاوت از Actor مربوط به touchDown می‌رسید.
    // در نتیجه انتخاب گردو/زامبی/بذر از نوار کناری عملاً غیرممکن بود و کاربر
    // نمی‌توانست چیزی بکارد. راه‌حل: کارت‌ها فقط وقتی واقعاً لازم است (تغییر
    // محتوای نوار نقاله/زامبی‌های در دسترس/بذرهای افتاده) از نو ساخته می‌شوند؛
    // در غیر این صورت فقط برچسب/رنگ همان Actor موجود آپدیت می‌شود تا هویت
    // Actor (و در نتیجه وضعیت داخلی ClickListener) بین فریم‌ها حفظ شود.
    private String lastSidebarSignature = null;
    private final java.util.Map<Object, Label> sidebarLabels = new java.util.HashMap<>();
    private final java.util.Map<Object, com.badlogic.gdx.scenes.scene2d.ui.Stack> sidebarCardStacks = new java.util.HashMap<>();

    public MiniGameScreen(PvZGame game, MiniGameSession session, String gameName, int level) {
        this.game = game;
        this.session = session;
        this.gameName = gameName;
        this.level = level;
        this.skin = game.getSkin();

        Viewport viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT);
        this.stage = new Stage(viewport);

        sunLabel = new Label("0", skin);
        statusLabel = new Label("", skin);

        buildHud();
        buildSidebar();

        SoundManager.playMusic(AssetPaths.MUSIC_MINIGAME);
        Gdx.input.setInputProcessor(stage);
    }

    private void buildHud() {
        Table top = new Table();
        top.setFillParent(true);
        top.top();

        // دکمه‌ی توقف بازی (طبق سند: دقیقاً مثل مرحله‌ی عادی باید بتوان مینی‌گیم
        // را هم وسط بازی متوقف کرد و منوی تنظیمات/توقف را دید).
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
                game.goToMiniGames();
            }
        });

        Table sunBox = new Table();
        sunBox.add(new com.badlogic.gdx.scenes.scene2d.ui.Image(ImageUtils.loadRegion(AssetPaths.ICON_SUN))).size(32f).padRight(4f);
        sunBox.add(sunLabel).padRight(20f);

        Label title = new Label(displayNameFor(gameName) + " - Level " + level, skin, "title");

        Table row = new Table();
        row.add(pauseButton).size(56f, 44f).padRight(8f);
        row.add(exitButton).size(70f, 44f).padRight(16f);
        row.add(sunBox);
        row.add(title).padLeft(20f);

        top.add(row).padTop(8f).row();
        top.add(statusLabel).padTop(4f).row();
        stage.addActor(top);
    }

    /** منوی توقف مینی‌گیم؛ دقیقاً هم‌ارز با openPauseMenu در GameScreen (همان
     *  PauseScreen با گزینه‌های ادامه/شروع دوباره/ذخیره و خروج). چون تعویض
     *  Screen باعث می‌شود render این کلاس دیگر صدا زده نشود، تیک بازی هم
     *  کاملاً متوقف می‌ماند - دقیقاً همان رفتاری که در مرحله‌ی عادی وجود دارد. */
    private void openPauseMenu() {
        game.setScreen(new PauseScreen(game, this, () -> game.startMiniGame(gameName, level)));
    }

    /** نام قابل‌نمایش هر مینی‌گیم؛ قبلاً همان شناسه‌ی خام (کوچک، بدون فاصله،
     *  مثل "wallnutbowling") مستقیماً در تیتر نمایش داده می‌شد. */
    private static String displayNameFor(String id) {
        switch (id.toLowerCase()) {
            case "vasebreaker": return "Vasebreaker";
            case "wallnutbowling": return "Wall-nut Bowling";
            case "izombie": return "I, Zombie";
            case "beghouled": return "Beghouled";
            default: return id;
        }
    }

    private void buildSidebar() {
        sidebarTable.setFillParent(true);
        sidebarTable.top().left();
        sidebarTable.padTop(80f).padLeft(8f);
        stage.addActor(sidebarTable);
        refreshSidebar();
    }

    /** فراخوانی هر فریم: فقط وقتی محتوای واقعی نوار کناری عوض شده (نوار نقاله
     *  پر/خالی شده، بذر تازه‌ای افتاده یا مصرف شده) کارت‌ها را از نو می‌سازد؛
     *  در غیر این صورت فقط برچسب‌ها/رنگ‌ها را روی همان Actorهای قبلی آپدیت
     *  می‌کند تا کلیک/انتخاب کاربر هرگز از وسط قطع نشود. */
    private void refreshSidebar() {
        String signature = computeSidebarSignature();
        if (!signature.equals(lastSidebarSignature)) {
            rebuildSidebar();
            lastSidebarSignature = signature;
        }
        updateSidebarOverlays();
    }

    private String computeSidebarSignature() {
        if (session instanceof WallnutBowlingSession) {
            return "bowling:" + ((WallnutBowlingSession) session).getConveyorBelt().toString();
        } else if (session instanceof IZombieSession) {
            // چهار نوع زامبی همیشه ثابت‌اند؛ فقط کول‌داون/قیمت روی برچسب عوض می‌شود.
            return "izombie";
        } else if (session instanceof BeghouledSession) {
            // دکمه‌های ارتقا همیشه ثابت‌اند.
            return "beghouled";
        } else if (session instanceof VasebreakerSession) {
            StringBuilder sb = new StringBuilder("vasebreaker:");
            for (VasebreakerSession.DroppedSeedPacket seed : ((VasebreakerSession) session).getDroppedSeeds()) {
                sb.append(seed.plantType).append(',').append(seed.row).append(',').append(seed.col).append(';');
            }
            return sb.toString();
        }
        return "";
    }

    private void rebuildSidebar() {
        sidebarTable.clear();
        sidebarLabels.clear();
        sidebarCardStacks.clear();

        if (session instanceof WallnutBowlingSession) {
            WallnutBowlingSession bowling = (WallnutBowlingSession) session;
            sidebarTable.add(new Label("Conveyor:", skin)).left().row();
            java.util.List<WallnutBowlingSession.NutType> belt = bowling.getConveyorBelt();
            for (int i = 0; i < belt.size(); i++) {
                com.badlogic.gdx.scenes.scene2d.ui.Stack stack = buildNutCard(belt.get(i));
                sidebarCardStacks.put(i, stack);
                sidebarTable.add(stack).size(80f, 80f).padBottom(6f).row();
            }
        } else if (session instanceof IZombieSession) {
            sidebarTable.add(new Label("Choose zombie:", skin)).left().row();
            String[] types = {"normal", "conehead", "buckethead", "imp"};
            for (String t : types) {
                com.badlogic.gdx.scenes.scene2d.ui.Stack stack = buildZombieCard(t);
                sidebarCardStacks.put(t, stack);
                sidebarTable.add(stack).size(80f, 80f).padBottom(6f).row();
            }
        } else if (session instanceof BeghouledSession) {
            sidebarTable.add(new Label("Tap two adjacent", skin)).left().row();
            sidebarTable.add(new Label("plants to swap.", skin)).left().padBottom(10f).row();
            addUpgradeButton("peashooter", 500);
            addUpgradeButton("wallnut", 500);
            addUpgradeButton("puffshroom", 250);
            addUpgradeButton("cabbagepult", 1000);
        } else if (session instanceof VasebreakerSession) {
            VasebreakerSession vb = (VasebreakerSession) session;
            sidebarTable.add(new Label("Tap a vase to break it.", skin)).left().row();
            java.util.List<VasebreakerSession.DroppedSeedPacket> seeds = vb.getDroppedSeeds();
            if (!seeds.isEmpty()) {
                sidebarTable.add(new Label("Plants collected:", skin)).left().padTop(10f).row();
                for (VasebreakerSession.DroppedSeedPacket seed : seeds) {
                    com.badlogic.gdx.scenes.scene2d.ui.Stack stack = buildDroppedSeedCard(seed);
                    sidebarCardStacks.put(seed, stack);
                    sidebarTable.add(stack).size(80f, 80f).padBottom(6f).row();
                }
            }
        }
    }

    /** آپدیت هر فریمِ برچسب‌های متغیر (کول‌داون/زمان باقی‌مانده) و هایلایت
     *  انتخاب فعلی، بدون ساختن هیچ Actor جدیدی. */
    private void updateSidebarOverlays() {
        if (session instanceof WallnutBowlingSession) {
            java.util.List<WallnutBowlingSession.NutType> belt = ((WallnutBowlingSession) session).getConveyorBelt();
            for (int i = 0; i < belt.size(); i++) {
                com.badlogic.gdx.scenes.scene2d.ui.Stack stack = sidebarCardStacks.get(i);
                if (stack == null) {
                    continue;
                }
                boolean selected = belt.get(i) == selectedNutType;
                stack.setColor(1f, 1f, selected ? 0.6f : 1f, 1f);
            }
        } else if (session instanceof IZombieSession) {
            IZombieSession iz = (IZombieSession) session;
            String[] types = {"normal", "conehead", "buckethead", "imp"};
            for (String t : types) {
                Label label = sidebarLabels.get(t);
                com.badlogic.gdx.scenes.scene2d.ui.Stack stack = sidebarCardStacks.get(t);
                if (label == null || stack == null) {
                    continue;
                }
                boolean onCooldown = iz.isZombieOnCooldown(t);
                String caption = onCooldown
                        ? (iz.getZombieCooldownRemaining(t) / 10) + "s"
                        : String.valueOf(iz.getZombieCost(t));
                label.setText(caption);
                boolean selected = t.equals(selectedZombieType);
                float alpha = onCooldown ? 0.5f : 1f;
                stack.setColor(1f, 1f, selected ? 0.6f : 1f, alpha);
            }
        } else if (session instanceof VasebreakerSession) {
            for (VasebreakerSession.DroppedSeedPacket seed : ((VasebreakerSession) session).getDroppedSeeds()) {
                Label label = sidebarLabels.get(seed);
                if (label != null) {
                    label.setText((seed.decayTicks / 10) + "s");
                }
                com.badlogic.gdx.scenes.scene2d.ui.Stack stack = sidebarCardStacks.get(seed);
                if (stack != null) {
                    boolean selected = selectedDroppedSeed == seed;
                    stack.setColor(1f, 1f, selected ? 0.6f : 1f, 1f);
                }
            }
        }
    }

    /** کارت یک گیاهِ به‌دست‌آمده از کوزه‌ی سبز که هنوز کاشته نشده؛ طبق سند باید
     *  در بخش مناسبی نمایش داده شود تا کاربر بتواند آن را انتخاب و بکارد. عدد
     *  روی کارت، زمان باقی‌مانده (بر حسب ثانیه) تا محو شدن بذر است. */
    private com.badlogic.gdx.scenes.scene2d.ui.Stack buildDroppedSeedCard(VasebreakerSession.DroppedSeedPacket seed) {
        com.badlogic.gdx.scenes.scene2d.ui.Stack stack = new com.badlogic.gdx.scenes.scene2d.ui.Stack();
        stack.add(new com.badlogic.gdx.scenes.scene2d.ui.Image(ImageUtils.loadRegion(AssetPaths.CARD_BACKGROUND)));
        stack.add(new com.badlogic.gdx.scenes.scene2d.ui.Image(ImageUtils.loadRegion(AssetPaths.plantIcon(seed.plantType))));
        Table overlay = new Table();
        overlay.bottom();
        Label label = new Label((seed.decayTicks / 10) + "s", skin);
        label.setFontScale(0.6f);
        overlay.add(label);
        stack.add(overlay);
        sidebarLabels.put(seed, label);

        // توجه: قبلاً isSelected فقط در لحظه‌ی ساخت کارت محاسبه می‌شد و داخل
        // کلیک‌لیستنر «منجمد» می‌ماند؛ چون الان همین Actor بین فریم‌ها زنده
        // می‌ماند، باید هر بار وضعیت واقعی فعلی selectedDroppedSeed را بخوانیم
        // نه مقدار قدیمی زمان ساخت را.
        stack.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                selectedDroppedSeed = (selectedDroppedSeed == seed) ? null : seed; // کلیک دوباره یعنی لغو انتخاب
                SoundManager.playSound(AssetPaths.SFX_CLICK);
            }
        });
        return stack;
    }

    /** کارت یک گردو در نوار نقاله، با آیکون واقعی همان گردو روی پس‌زمینه‌ی استاندارد کارت. */
    private com.badlogic.gdx.scenes.scene2d.ui.Stack buildNutCard(WallnutBowlingSession.NutType type) {
        com.badlogic.gdx.scenes.scene2d.ui.Stack stack = new com.badlogic.gdx.scenes.scene2d.ui.Stack();
        stack.add(new com.badlogic.gdx.scenes.scene2d.ui.Image(ImageUtils.loadRegion(AssetPaths.CARD_BACKGROUND)));
        stack.add(new com.badlogic.gdx.scenes.scene2d.ui.Image(ImageUtils.loadRegion(nutTexturePath(type))));
        stack.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                selectedNutType = type;
                selectedZombieType = null;
                SoundManager.playSound(AssetPaths.SFX_CLICK);
            }
        });
        return stack;
    }

    /** کارت یک زامبی قابل‌انتخاب در «من زامبی»، با آیکون واقعی، قیمت و زمان
     *  باقی‌مانده‌ی cooldown (دقیقاً مثل کارت گیاهان در مراحل عادی). */
    private com.badlogic.gdx.scenes.scene2d.ui.Stack buildZombieCard(String zombieType) {
        IZombieSession iz = (IZombieSession) session;
        com.badlogic.gdx.scenes.scene2d.ui.Stack stack = new com.badlogic.gdx.scenes.scene2d.ui.Stack();
        stack.add(new com.badlogic.gdx.scenes.scene2d.ui.Image(ImageUtils.loadRegion(AssetPaths.CARD_BACKGROUND)));
        stack.add(new com.badlogic.gdx.scenes.scene2d.ui.Image(ImageUtils.loadRegion(AssetPaths.zombieIcon(zombieType))));

        Table overlay = new Table();
        overlay.bottom();
        Label label = new Label("", skin);
        label.setFontScale(0.6f);
        overlay.add(label);
        stack.add(overlay);
        sidebarLabels.put(zombieType, label);

        // توجه: قبلاً وقتی زامبی در حال cooldown بود اصلاً هیچ ClickListener‌ای
        // اضافه نمی‌شد؛ چون الان همین کارت (به‌جای ساخت دوباره) تا پایان
        // cooldown روی صفحه می‌ماند، لیستنر همیشه باید وجود داشته باشد و خودش
        // در لحظه‌ی کلیک وضعیت فعلی cooldown را چک کند.
        stack.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (iz.isZombieOnCooldown(zombieType)) {
                    return;
                }
                selectedZombieType = zombieType;
                selectedNutType = null;
                SoundManager.playSound(AssetPaths.SFX_CLICK);
            }
        });
        return stack;
    }

    private void addUpgradeButton(String plantName, int cost) {
        com.badlogic.gdx.scenes.scene2d.ui.Stack stack = new com.badlogic.gdx.scenes.scene2d.ui.Stack();
        stack.add(new com.badlogic.gdx.scenes.scene2d.ui.Image(ImageUtils.loadRegion(AssetPaths.CARD_BACKGROUND)));
        stack.add(new com.badlogic.gdx.scenes.scene2d.ui.Image(ImageUtils.loadRegion(AssetPaths.plantSeedPacket(plantName))));
        stack.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                BeghouledSession.UpgradeResult result = ((BeghouledSession) session).upgradePlant(plantName);
                if (result == BeghouledSession.UpgradeResult.NOT_ENOUGH_SUN) {
                    statusLabel.setText("Not enough sun to upgrade " + plantName + ".");
                } else if (result == BeghouledSession.UpgradeResult.INVALID_UPGRADE) {
                    statusLabel.setText(plantName + " is not on the field.");
                } else {
                    statusLabel.setText(plantName + " upgraded!");
                }
                SoundManager.playSound(AssetPaths.SFX_CLICK);
            }
        });
        sidebarTable.add(stack).size(80f, 80f).padBottom(6f).row();
    }

    // ==================== حلقه‌ی اصلی رندر ====================

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.05f, 0.05f, 0.05f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (!session.isGameOver()) {
            tickAccumulator += delta;
            while (tickAccumulator >= SECONDS_PER_TICK) {
                tickAccumulator -= SECONDS_PER_TICK;
                session.advanceOneTick();
            }
        } else if (!announcedGameOver) {
            announcedGameOver = true;
            game.setScreen(new MiniGameResultScreen(game, gameName, level, session.isWon()));
            return;
        }

        stage.getBatch().begin();
        drawBoard(stage.getBatch());
        drawCraters(stage.getBatch());
        drawVases(stage.getBatch());
        drawPlants(stage.getBatch());
        drawRollingNuts(stage.getBatch());
        drawZombies(stage.getBatch());
        stage.getBatch().end();

        sunLabel.setText(String.valueOf(session.getSunManager().getCurrentSun()));
        refreshSidebar();

        stage.act(delta);
        stage.draw();

        handleBoardClick();
    }

    private float tileX(int col) {
        return BOARD_LEFT + col * TILE_W;
    }

    private float tileY(int row) {
        return BOARD_TOP - (row + 1) * TILE_H;
    }

    private void drawBoard(com.badlogic.gdx.graphics.g2d.Batch batch) {
        // پس‌زمینه‌ی واقعی حیاط (همان دارایی رسمی که در GameScreen/QuestScreen و بقیه‌ی
        // منوها استفاده شده) - بدون رنگ خاکستری یا مستطیل دستی.
        TextureRegion bg = ImageUtils.loadRegion(backgroundPath());
        batch.draw(bg, 0, 0, WORLD_WIDTH, WORLD_HEIGHT);
    }

    private String backgroundPath() {
        if (session instanceof IZombieSession) {
            return AssetPaths.BG_MINIGAME_IZOMBIE;
        }
        return AssetPaths.BG_MINIGAME_VASEBREAKER; // frontlawn معمولی؛ برای بولینگ و Beghouled هم یکسان است
    }

    private void drawVases(com.badlogic.gdx.graphics.g2d.Batch batch) {
        if (!(session instanceof VasebreakerSession)) {
            return;
        }
        VasebreakerSession vb = (VasebreakerSession) session;
        for (int r = 0; r < Board.ROWS; r++) {
            for (int c = 0; c < Board.COLS; c++) {
                VasebreakerSession.VaseType type = vb.getVaseAt(r, c);
                if (type == null) {
                    continue;
                }
                String path;
                switch (type) {
                    case GREEN: path = AssetPaths.VASE_GREEN; break;
                    case PURPLE: path = AssetPaths.VASE_PURPLE; break;
                    default: path = AssetPaths.VASE_NORMAL; break;
                }
                TextureRegion tex = ImageUtils.loadRegion(path);
                batch.draw(tex, tileX(c) + 14f, tileY(r), TILE_W - 28f, TILE_H + 4f);
            }
        }
    }

    private void drawCraters(com.badlogic.gdx.graphics.g2d.Batch batch) {
        if (!(session instanceof BeghouledSession)) {
            return;
        }
        Board board = session.getBoard();
        for (int r = 0; r < Board.ROWS; r++) {
            for (int c = 0; c < Board.COLS; c++) {
                if (board.getTile(r, c).getTerrainType() == model.game.TerrainType.CRATER) {
                    TextureRegion tex = ImageUtils.loadRegion(AssetPaths.BEGHOULED_CRATER);
                    batch.draw(tex, tileX(c) + 10f, tileY(r) + 6f, TILE_W - 20f, TILE_H - 12f);
                }
            }
        }
    }

    private void drawRollingNuts(com.badlogic.gdx.graphics.g2d.Batch batch) {
        if (!(session instanceof WallnutBowlingSession)) {
            return;
        }
        WallnutBowlingSession bowling = (WallnutBowlingSession) session;
        for (WallnutBowlingSession.RollingNut nut : bowling.getActiveNuts()) {
            TextureRegion tex = ImageUtils.loadRegion(nutTexturePath(nut.type));
            float x = tileX(0) + (float) nut.x * TILE_W;
            float y = tileY(nut.row);
            batch.draw(tex, x, y, TILE_W - 12f, TILE_H - 12f);
        }
    }

    private String nutTexturePath(WallnutBowlingSession.NutType type) {
        switch (type) {
            case EXPLOSIVE: return AssetPaths.NUT_BOWLING_EXPLOSIVE;
            case GIANT: return AssetPaths.NUT_BOWLING_GIANT;
            default: return AssetPaths.NUT_BOWLING_NORMAL;
        }
    }

    private void drawPlants(com.badlogic.gdx.graphics.g2d.Batch batch) {
        Board board = session.getBoard();
        for (int r = Board.ROWS - 1; r >= 0; r--) {
            for (int c = 0; c < Board.COLS; c++) {
                Plant plant = board.getTile(r, c).getPlant();
                if (plant == null) {
                    continue;
                }
                TextureRegion tex = ImageUtils.loadRegion(AssetPaths.plantIcon(plant.getName()));
                batch.draw(tex, tileX(c) + 8f, tileY(r) + 8f, TILE_W - 16f, TILE_H - 16f);
            }
        }
    }

    private void drawZombies(com.badlogic.gdx.graphics.g2d.Batch batch) {
        for (Zombie z : session.getAliveZombies()) {
            // در کوزه‌شکنی، غول‌پیکرِ خارج‌شده از کوزه‌ی بنفش با اسپرایت اختصاصی
            // «غول کوزه‌ای» رسم می‌شود؛ برای بقیه‌ی موارد، آیکون معمولی همان نوع زامبی.
            boolean isVaseGargantuar = session instanceof VasebreakerSession
                    && "gargantuar".equalsIgnoreCase(z.getTypeName());
            String path = isVaseGargantuar ? AssetPaths.VASE_GARGANTUAR : AssetPaths.zombieIcon(z.getTypeName());
            TextureRegion tex = ImageUtils.loadRegion(path);
            float x = tileX(0) + (float) z.getXPosition() * TILE_W;
            float y = tileY(z.getRow());
            batch.draw(tex, x, y, TILE_W - 10f, TILE_H - 10f);
        }
    }

    // ==================== ورودی کاربر ====================

    private void handleBoardClick() {
        if (!Gdx.input.justTouched()) {
            return;
        }
        Vector2 touch = stage.screenToStageCoordinates(new Vector2(Gdx.input.getX(), Gdx.input.getY()));

        int col = (int) ((touch.x - BOARD_LEFT) / TILE_W);
        int row = (int) ((BOARD_TOP - touch.y) / TILE_H);
        if (col < 0 || col >= Board.COLS || row < 0 || row >= Board.ROWS) {
            return;
        }

        if (session instanceof VasebreakerSession) {
            handleVasebreakerClick(row, col);
        } else if (session instanceof WallnutBowlingSession) {
            handleBowlingClick(row, col);
        } else if (session instanceof IZombieSession) {
            handleIZombieClick(row, col);
        } else if (session instanceof BeghouledSession) {
            handleBeghouledClick(row, col);
        }
    }

    private void handleVasebreakerClick(int row, int col) {
        VasebreakerSession vb = (VasebreakerSession) session;

        // اگر خانه‌ی کلیک‌شده کوزه نداشته باشد و کاربر یک بذر از نوار کناری
        // انتخاب کرده باشد، تلاش برای کاشت همان بذر در این خانه انجام می‌شود
        // (طبق سند: بذرهای به‌دست‌آمده باید در بخش مناسبی قابل‌انتخاب و کاشت باشند).
        if (vb.getVaseAt(row, col) == null && selectedDroppedSeed != null) {
            VasebreakerSession.PlantSeedResult result = vb.plantDroppedSeed(
                    selectedDroppedSeed.row, selectedDroppedSeed.col, row, col);
            if (result == VasebreakerSession.PlantSeedResult.SUCCESS) {
                statusLabel.setText(selectedDroppedSeed.plantType + " planted!");
                selectedDroppedSeed = null;
            } else if (result == VasebreakerSession.PlantSeedResult.INVALID_TARGET) {
                statusLabel.setText("Can't plant there.");
            } else {
                statusLabel.setText("That seed is no longer available.");
                selectedDroppedSeed = null;
            }
            return;
        }

        VasebreakerSession.VaseBreakResult result = vb.breakVase(row, col);
        switch (result.status) {
            case NO_VASE:
                statusLabel.setText("Nothing here. Select a plant from the sidebar to plant it here.");
                break;
            case GREEN_SEED:
                statusLabel.setText("Green vase broke! Seed packet: " + result.contentName);
                break;
            case PURPLE_GARGANTUAR:
                statusLabel.setText("Purple vase broke! A Gargantuar appeared!");
                break;
            case NORMAL_ZOMBIE:
                statusLabel.setText("Vase broke! Zombie: " + result.contentName);
                break;
            case NORMAL_SEED:
                statusLabel.setText("Vase broke! Seed: " + result.contentName);
                break;
            case NORMAL_EMPTY:
                statusLabel.setText("Vase was empty.");
                break;
            default:
                break;
        }
        SoundManager.playSound(AssetPaths.SFX_CLICK);
    }

    private void handleBowlingClick(int row, int col) {
        if (selectedNutType == null) {
            return;
        }
        WallnutBowlingSession bowling = (WallnutBowlingSession) session;
        String typeStr;
        switch (selectedNutType) {
            case EXPLOSIVE: typeStr = "explodeonut"; break;
            case GIANT: typeStr = "giantwallnut"; break;
            default: typeStr = "bowlingwallnut"; break;
        }
        WallnutBowlingSession.PlantNutResult result = bowling.plantNut(typeStr, row, col);
        if (result == WallnutBowlingSession.PlantNutResult.SUCCESS) {
            statusLabel.setText("Nut planted and rolling!");
            selectedNutType = null;
        } else if (result == WallnutBowlingSession.PlantNutResult.BEYOND_RED_LINE) {
            statusLabel.setText("You can only plant behind the red line.");
        } else {
            statusLabel.setText("That nut is not on the conveyor belt.");
        }
    }

    private void handleIZombieClick(int row, int col) {
        if (selectedZombieType == null) {
            return;
        }
        IZombieSession iz = (IZombieSession) session;
        IZombieSession.PlaceZombieResult result = iz.placeZombie(selectedZombieType, row, col);
        switch (result) {
            case SUCCESS:
                statusLabel.setText(selectedZombieType + " placed!");
                selectedZombieType = null;
                break;
            case BEYOND_RED_LINE:
                statusLabel.setText("You can only place zombies right of the red line.");
                break;
            case NOT_ENOUGH_SUN:
                statusLabel.setText("Not enough sun for " + selectedZombieType + ".");
                break;
            case ON_COOLDOWN:
                statusLabel.setText(selectedZombieType + " is still recharging.");
                break;
            default:
                statusLabel.setText("Invalid zombie type.");
        }
    }

    private void handleBeghouledClick(int row, int col) {
        BeghouledSession beghouled = (BeghouledSession) session;
        if (firstSwapTile == null) {
            firstSwapTile = new int[]{row, col};
            statusLabel.setText("Tile selected. Tap an adjacent tile to swap.");
            return;
        }
        BeghouledSession.SwapResult result = beghouled.swapPlants(firstSwapTile[0], firstSwapTile[1], row, col);
        firstSwapTile = null;
        switch (result) {
            case SUCCESS:
                statusLabel.setText("Match made!");
                break;
            case NOT_ADJACENT:
                statusLabel.setText("Tiles must be adjacent.");
                break;
            case NO_MATCH:
                statusLabel.setText("No match — swap undone.");
                break;
            case EMPTY_TILE:
                statusLabel.setText("One of the tiles is a crater.");
                break;
            default:
                statusLabel.setText("Invalid tiles.");
        }
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
