package gdx.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
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
import model.game.GameSession;
import model.game.Grave;
import model.game.Tile;
import model.plant.Plant;
import model.plant.PlantFactory;
import model.projectile.Projectile;
import model.sun.FallingSun;
import model.user.User;
import model.zombie.Zombie;

import java.util.List;

/**
 * صفحه‌ی گرافیکی اصلی گیم‌پلی. این صفحه دقیقاً همان چیزی است که در سند فاز دو
 * زیر بخش‌های «اطلاعات بازی»، «المان‌های محیط بازی» و «تعاملات کاربر با بازی»
 * توضیح داده شده: نمایش تخته‌ی ۵×۹، گیاهان/زامبی‌ها روی خانه‌ها، پرتابه‌ها،
 * خورشیدهای سقوط‌کننده، قبرها، چمن‌زن‌ها، نوار پیشرفت زامبی‌ها، شمارنده‌ی
 * غذای گیاه، و نوار کناری برای کاشت/برداشت/غذادهی/تقلب.
 * <p>
 * منطق بازی (Model) به‌طور کامل توسط {@link GameSession} انجام می‌شود؛ این
 * کلاس فقط وضعیت را می‌خواند و روی صفحه رسم می‌کند، و ورودی کاربر را به
 * متدهای مدل ترجمه می‌کند (دقیقاً هم‌ارز با GameController کنسولی).
 */
public class GameScreen implements Screen {

    public static final float WORLD_WIDTH = 1280f;
    public static final float WORLD_HEIGHT = 720f;

    // ابعاد و مبدأ شبکه‌ی بازی روی صفحه
    private static final float BOARD_LEFT = 260f;
    private static final float BOARD_TOP = 640f;
    private static final float TILE_W = 100f;
    private static final float TILE_H = 96f;

    private final PvZGame game;
    private final Stage stage;
    private final Skin skin;
    private final GameSession session;
    private final int chapter;
    private final int level;

    private final Table sidebarTable = new Table();
    private final Table cardsTable = new Table(); // کارت‌های گیاه انتخابی (یا گیاهان روی نوار نقاله)
    private final Table hudTable = new Table();
    private final Label sunLabel;
    private final Label plantFoodLabel;
    private final Label modeStatusLabel; // نوار وضعیت مُد ویژه (نبرد زمان‌دار، محافظ دانه‌ها و ...)؛ هر فریم آپدیت می‌شود
    private TextButton startWavesButton = null; // فقط برای مُد «هرچه رسد بکار»؛ بعد از شروع موج‌ها مخفی می‌شود
    private final Table zombieProgressBar = new Table();

    private String selectedPlantToPlant = null; // نام گیاهی که کاربر برای کاشت انتخاب کرده (از نوار کناری)
    private boolean shovelSelected = false;      // حالت انتخاب بیلچه برای برداشت گیاه
    private boolean plantFoodSelected = false;   // حالت انتخاب غذای گیاه برای اعمال روی یک گیاه

    private float tickAccumulator = 0f;
    private static final float SECONDS_PER_TICK = 0.1f; // هر تیک = ۰.۱ ثانیه (طبق مدل)

    private boolean announcedGameOver = false;

    public GameScreen(PvZGame game, GameSession session, int chapter, int level) {
        this.game = game;
        this.session = session;
        this.chapter = chapter;
        this.level = level;
        this.skin = game.getSkin();

        Viewport viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT);
        this.stage = new Stage(viewport);

        sunLabel = new Label("0", skin);
        plantFoodLabel = new Label("0", skin);
        modeStatusLabel = new Label("", skin);

        buildHud();
        buildSidebar();
        buildZombieProgressBar();

        SoundManager.playMusic(AssetPaths.MUSIC_MENU);

        Gdx.input.setInputProcessor(stage);
    }

    // ==================== ساخت رابط کاربری ثابت (HUD) ====================

    private void buildHud() {
        // --- منوی توقف (دکمه‌ی Pause بالای صفحه، طبق بخش «توقف بازی») ---
        TextButton pauseButton = new TextButton("II", skin);
        pauseButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                openPauseMenu();
            }
        });

        // --- شمارنده‌ی خورشید (تعداد خورشیدهای جمع‌آوری‌شده) ---
        Table sunBox = new Table();
        sunBox.add(new Image(ImageUtils.loadRegion(AssetPaths.ICON_SUN))).size(36f).padRight(4f);
        sunBox.add(sunLabel).padRight(16f);

        // --- شمارنده‌ی غذای گیاه (Plant Food) ---
        Table foodBox = new Table();
        foodBox.add(new Image(ImageUtils.loadRegion(AssetPaths.ICON_PLANT_FOOD_LEAF))).size(30f).padRight(4f);
        foodBox.add(plantFoodLabel).padRight(16f);

        // --- سکه و الماس (طبق سند: در تمامی منوها حتی حین بازی قابل مشاهده باشد) ---
        User user = session.getUser();
        Table coinBox = new Table();
        coinBox.add(new Image(ImageUtils.loadRegion(AssetPaths.ICON_COIN))).size(28f).padRight(4f);
        coinBox.add(new Label(String.valueOf(user.getCoins()), skin)).padRight(16f);

        Table diamondBox = new Table();
        diamondBox.add(new Image(ImageUtils.loadRegion(AssetPaths.ICON_DIAMOND))).size(28f).padRight(4f);
        diamondBox.add(new Label(String.valueOf(user.getDiamonds()), skin)).padRight(16f);

        hudTable.setFillParent(true);
        hudTable.top();
        Table topRow = new Table();
        topRow.add(pauseButton).size(48f).padRight(16f);
        topRow.add(sunBox);
        topRow.add(foodBox);
        topRow.add(coinBox);
        topRow.add(diamondBox);

        // --- خانه‌های دیباگ: نمایش فقط وقتی حالت دیباگ کاربر فعال باشد ---
        if (user.isDebugMode()) {
            TextButton addSunCheat = new TextButton("+ Sun", skin);
            addSunCheat.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    session.getSunManager().addSun(50);
                }
            });
            TextButton addFoodCheat = new TextButton("+ Plant Food", skin);
            addFoodCheat.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    session.addPlantFood();
                }
            });
            TextButton removeCooldownCheat = new TextButton("No Cooldown", skin);
            removeCooldownCheat.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    session.clearAllCooldowns();
                }
            });
            TextButton addCoinCheat = new TextButton("+ Coin", skin);
            addCoinCheat.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    user.setCoins(user.getCoins() + 500);
                }
            });
            TextButton addDiamondCheat = new TextButton("+ Diamond", skin);
            addDiamondCheat.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    user.setDiamonds(user.getDiamonds() + 5);
                }
            });
            TextButton spawnZombieCheat = new TextButton("Spawn Zombie", skin);
            spawnZombieCheat.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    int dl = session.getUser().getDifficultyLevel();
                    Zombie z = model.zombie.ZombieFactory.create("normal", dl);
                    int row = new java.util.Random().nextInt(Board.ROWS);
                    z.spawn(row, Board.COLS - 1);
                    z.setSpawnTick((int) session.getTickCount());
                    session.getAliveZombies().add(z);
                }
            });
            TextButton nukeCheat = new TextButton("Nuke", skin);
            nukeCheat.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    session.getAliveZombies().clear();
                }
            });
            topRow.add(addSunCheat).padLeft(8f);
            topRow.add(addFoodCheat).padLeft(8f);
            topRow.add(removeCooldownCheat).padLeft(8f);
            topRow.add(addCoinCheat).padLeft(8f);
            topRow.add(addDiamondCheat).padLeft(8f);
            topRow.add(spawnZombieCheat).padLeft(8f);
            topRow.add(nukeCheat).padLeft(8f);
        }

        hudTable.add(topRow).padTop(8f).row();

        // --- نوار وضعیت مُد ویژه‌ی مرحله (طبق سند: وضعیت مأموریت‌ها/محدودیت‌ها
        //     باید حین بازی به‌طریقی نمایش داده شود). متن این برچسب هر فریم در
        //     render() به‌روزرسانی می‌شود (چون مثلاً شمارش معکوس نبرد زمان‌دار زنده است). ---
        modeStatusLabel.setText(session.getLevelRules().getHudStatusText(session));
        hudTable.add(modeStatusLabel).padTop(4f).row();

        // --- مُد «هرچه رسد بکار»: دکمه‌ی شروع دستیِ موج‌ها بعد از اتمام کاشت ---
        if (session.getLevelRules() instanceof model.levelrules.PlantWhatYouGetRules) {
            model.levelrules.PlantWhatYouGetRules pwyg = (model.levelrules.PlantWhatYouGetRules) session.getLevelRules();
            startWavesButton = new TextButton("Let's Rock! (Start Waves)", skin);
            startWavesButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    pwyg.startWaves(session);
                }
            });
            hudTable.add(startWavesButton).padTop(6f).row();
        }

        stage.addActor(hudTable);
    }

    private void buildZombieProgressBar() {
        // نوار پیشرفت زامبی‌ها: نوار در ابتدای مرحله خالی و در انتهای آن پر است
        // (طبق بخش «پیشروی زامبی‌ها»). این پیاده‌سازی ساده، بر اساس شماره‌ی موج فعلی
        // نسبت به تعداد کل موج‌ها است.
        zombieProgressBar.setFillParent(false);
        Table wrapper = new Table();
        wrapper.setFillParent(true);
        wrapper.top();
        wrapper.add(zombieProgressBar).width(500f).height(24f).padTop(8f);
        stage.addActor(wrapper);
    }

    private void refreshZombieProgressBar() {
        zombieProgressBar.clear();
        int totalWaves = session.getWaveManager().getTotalWaves();
        int currentWave = session.getWaveManager().getCurrentWave();
        float ratio = totalWaves <= 0 ? 0f : Math.min(1f, currentWave / (float) totalWaves);

        Table filled = new Table();
        filled.setBackground(skin.newDrawable("white", Color.RED));
        Table empty = new Table();
        empty.setBackground(skin.newDrawable("white", Color.DARK_GRAY));

        zombieProgressBar.add(filled).width(500f * ratio).height(24f);
        zombieProgressBar.add(empty).width(500f * (1 - ratio)).height(24f);
    }

    private void buildSidebar() {
        sidebarTable.setFillParent(true);
        sidebarTable.top().left();
        sidebarTable.padTop(60f).padLeft(8f);

        refreshPlantCards();
        sidebarTable.add(cardsTable).row();

        // --- دکمه‌ی برداشت گیاه (بیلچه) ---
        TextButton shovelButton = new TextButton("Shovel", skin);
        shovelButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                SoundManager.playSound(AssetPaths.SFX_SHOVEL);
                shovelSelected = !shovelSelected;
                selectedPlantToPlant = null;
                plantFoodSelected = false;
            }
        });
        sidebarTable.add(shovelButton).padTop(10f).width(70f).row();

        // --- دکمه‌ی استفاده از غذای گیاه ---
        TextButton foodButton = new TextButton("Plant Food", skin);
        foodButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (session.getPlantFoodCount() > 0) {
                    SoundManager.playSound(AssetPaths.SFX_MINT);
                    plantFoodSelected = !plantFoodSelected;
                    selectedPlantToPlant = null;
                    shovelSelected = false;
                }
            }
        });
        sidebarTable.add(foodButton).padTop(6f).width(70f).row();

        stage.addActor(sidebarTable);
    }

    /**
     * کارت‌های گیاه سمت چپ صفحه را می‌سازد. در حالت عادی، همان گیاهانی که
     * بازیکن قبل از شروع مرحله انتخاب کرده نمایش داده می‌شود. در مُد «نوار
     * نقاله»، به‌جای آن، فقط گیاهانی که همین الان روی نوار هستند نشان داده
     * می‌شوند (طبق سند: بدون نیاز به انیمیشن، صرفاً گیاهان موجود روی نوار).
     */
    private void refreshPlantCards() {
        cardsTable.clear();
        model.levelrules.ILevelRules rules = session.getLevelRules();
        List<String> plantsToShow;
        if (rules instanceof model.levelrules.ConveyorBeltRules) {
            plantsToShow = ((model.levelrules.ConveyorBeltRules) rules).getBeltPlants();
        } else if (rules instanceof model.levelrules.LockedPlantsRules) {
            // فقط گیاهان مجاز این مُد نمایش داده شوند (طبق سند: بعضی اسلات‌ها/گیاهان از ابتدا قفل‌اند)
            plantsToShow = ((model.levelrules.LockedPlantsRules) rules).getAllowedPlants();
        } else {
            plantsToShow = game.getPlantSelectionController().getSelectedPlants();
        }
        for (String plantName : plantsToShow) {
            cardsTable.add(buildPlantCard(plantName)).size(70f, 90f).pad(2f).row();
        }
    }

    private Stack buildPlantCard(String plantName) {
        Stack stack = new Stack();
        Image bg = new Image(ImageUtils.loadRegion(AssetPaths.CARD_BACKGROUND));
        Image icon = new Image(ImageUtils.loadRegion(AssetPaths.plantSeedPacket(plantName)));
        Label nameLabel = new Label(plantName, skin);
        nameLabel.setFontScale(0.5f);

        stack.add(bg);
        stack.add(icon);
        Table overlay = new Table();
        overlay.bottom();
        overlay.add(nameLabel);
        stack.add(overlay);

        stack.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (session.isPlantOnCooldown(plantName)) {
                    return; // در حال شارژ (Cooldown) - طبق سند، قابل انتخاب نیست
                }
                selectedPlantToPlant = plantName;
                shovelSelected = false;
                plantFoodSelected = false;
            }
        });
        return stack;
    }

    // ==================== منوی توقف ====================

    private void openPauseMenu() {
        game.setScreen(new PauseScreen(game, this, this::restartLevel));
    }

    private void restartLevel() {
        GameSession fresh = new GameSession(session.getUser(), session.getWaveManager().getTotalWaves());
        game.setScreen(new GameScreen(game, fresh, chapter, level));
    }

    // ==================== حلقه‌ی اصلی رندر ====================

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.05f, 0.05f, 0.05f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (!session.isGameOver()) {
            tickAccumulator += delta * session.getUser().getGameSpeed();
            while (tickAccumulator >= SECONDS_PER_TICK) {
                tickAccumulator -= SECONDS_PER_TICK;
                session.advanceOneTick();

                FallingSun newSun = session.getSunManager().tick(session.getBoard());
                if (newSun != null) {
                    session.getFallingSuns().add(newSun);
                }
                for (FallingSun fs : session.getFallingSuns()) {
                    fs.tick();
                }
            }
        } else if (!announcedGameOver) {
            announcedGameOver = true;
            int score = session.getUser().getHighScore();
            game.setScreen(new WinLossScreen(game, session.isWon(), chapter, level, score, this::restartLevel));
            return;
        }

        // --- رسم پس‌زمینه‌ی صحنه بر اساس فصل مرحله (طبق بخش «فصل‌ها») ---
        stage.getBatch().begin();
        String bg = seasonBackground();
        if (!bg.isEmpty()) {
            stage.getBatch().draw(ImageUtils.loadRegion(bg), 0, 0, WORLD_WIDTH, WORLD_HEIGHT);
        }
        drawBoard(stage.getBatch());
        drawTerrainOverlays(stage.getBatch());
        drawSpecialLevelOverlays(stage.getBatch());
        drawGraves(stage.getBatch());
        drawLawnMowers(stage.getBatch());
        drawPlants(stage.getBatch());
        drawZombies(stage.getBatch());
        drawProjectiles(stage.getBatch());
        drawFallingSuns(stage.getBatch());
        stage.getBatch().end();

        // --- به‌روزرسانی برچسب‌های HUD ---
        sunLabel.setText(String.valueOf(session.getSunManager().getCurrentSun()));
        plantFoodLabel.setText(String.valueOf(session.getPlantFoodCount()));
        modeStatusLabel.setText(session.getLevelRules().getHudStatusText(session));
        if (session.getLevelRules() instanceof model.levelrules.ConveyorBeltRules) {
            refreshPlantCards(); // لیست نوار نقاله مدام تغییر می‌کند
        }
        if (startWavesButton != null) {
            boolean wavesStarted = ((model.levelrules.PlantWhatYouGetRules) session.getLevelRules()).isWavesStarted();
            startWavesButton.setVisible(!wavesStarted);
        }
        refreshZombieProgressBar();

        stage.act(delta);
        stage.draw();

        handleBoardClick();
    }

    private String seasonBackground() {
        switch (session.getSeason()) {
            case ANCIENT_EGYPT: return AssetPaths.BG_LAWN_ANCIENT_EGYPT;
            case FROSTBITE_CAVES: return AssetPaths.BG_LAWN_FROSTBITE_CAVES;
            case BIG_WAVE_BEACH: return AssetPaths.BG_LAWN_BIG_WAVE_BEACH;
            case DARK_AGES: return AssetPaths.BG_LAWN_DARK_AGES;
            default: return AssetPaths.BG_LAWN_NORMAL;
        }
    }

    /** چمن‌زن مخصوص فصل فعلی مرحله (طبق بخش «پس‌زمینه‌ی هر فصل باید همان چمن‌زن مخصوص را داشته باشد»). */
    private String seasonMowerPath(boolean used) {
        switch (session.getSeason()) {
            case ANCIENT_EGYPT: return used ? AssetPaths.LAWN_MOWER_USED_EGYPT : AssetPaths.LAWN_MOWER_IDLE_EGYPT;
            case FROSTBITE_CAVES: return used ? AssetPaths.LAWN_MOWER_USED_ICEAGE : AssetPaths.LAWN_MOWER_IDLE_ICEAGE;
            case BIG_WAVE_BEACH: return used ? AssetPaths.LAWN_MOWER_USED_BEACH : AssetPaths.LAWN_MOWER_IDLE_BEACH;
            case DARK_AGES: return used ? AssetPaths.LAWN_MOWER_USED_DARK : AssetPaths.LAWN_MOWER_IDLE_DARK;
            default: return used ? AssetPaths.LAWN_MOWER_USED_NORMAL : AssetPaths.LAWN_MOWER_IDLE_NORMAL;
        }
    }

    // ==================== رسم تخته و موجودیت‌ها ====================

    private float tileX(int col) {
        return BOARD_LEFT + col * TILE_W;
    }

    private float tileY(int row) {
        return BOARD_TOP - (row + 1) * TILE_H;
    }

    private void drawBoard(com.badlogic.gdx.graphics.g2d.Batch batch) {
        // پس‌زمینه‌ی خود تخته (اگر بخواهیم مجزا از پس‌زمینه‌ی کلی رسم شود) در همین‌جا اضافه می‌شود.
        // فعلاً چون BG_LAWN_* کل صفحه را می‌پوشاند، چیزی اضافه نمی‌کنیم.
    }

    /**
     * جلوه‌های بصری اختصاصی مراحل ویژه‌ی بخش ماجراجویی: خط ددلاین، نشانگر
     * خانه‌های محافظت‌شده (محافظ دانه‌ها). سایر مُدهای ویژه (نوار نقاله، نبرد
     * زمان‌دار، از دست نده، هرچه رسد بکار) در نوار وضعیت بالای صفحه (buildHud)
     * یا نوار کناری (buildSidebar) نمایش داده می‌شوند، نه روی خودِ خانه‌ها.
     */
    private void drawSpecialLevelOverlays(com.badlogic.gdx.graphics.g2d.Batch batch) {
        model.levelrules.ILevelRules rules = session.getLevelRules();

        if (rules instanceof model.levelrules.DeadLineRules) {
            int col = ((model.levelrules.DeadLineRules) rules).getDeadLineColumn();
            float lineX = tileX(col) + TILE_W / 2f - 6f;
            batch.draw(ImageUtils.loadRegion(AssetPaths.DEAD_LINE_MARKER),
                    lineX, tileY(Board.ROWS - 1), 12f, TILE_H * Board.ROWS);
        }

        if (rules instanceof model.levelrules.SaveOurSeedsRules) {
            for (model.levelrules.SaveOurSeedsRules.ProtectedTile pt
                    : ((model.levelrules.SaveOurSeedsRules) rules).getProtectedTiles()) {
                batch.draw(ImageUtils.loadRegion(AssetPaths.PROTECTED_TILE_MARKER),
                        tileX(pt.col), tileY(pt.row), TILE_W, TILE_H);
            }
        }
    }

    /**
     * جلوه‌های بصری اختصاصی هر فصل که روی خودِ خانه‌ها می‌نشینند: آب و خط
     * حداکثر پیشروی آب و خانه‌ی ساحل پست (ساحل موج بزرگ)، زمین لغزنده به بالا/
     * پایین (غارهای یخی). طبق سند فاز دو صفحه‌ی ۲۸-۲۹.
     */
    private void drawTerrainOverlays(com.badlogic.gdx.graphics.g2d.Batch batch) {
        Board board = session.getBoard();

        // --- ساحل موج بزرگ: آب روی خانه‌های زیرِ سطح فعلی + خط مشخص‌کننده‌ی
        //     حداکثر پیشروی آب در انتهای راست‌ترین ستونی که آب می‌تواند برسد ---
        if (session.getSeason() == model.game.Season.BIG_WAVE_BEACH) {
            int maxWaterCol = -1;
            for (int r = 0; r < Board.ROWS; r++) {
                for (int c = 0; c < Board.COLS; c++) {
                    Tile tile = board.getTile(r, c);
                    if (tile.isWater()) {
                        batch.draw(ImageUtils.loadRegion(AssetPaths.WATER_TILE), tileX(c), tileY(r), TILE_W, TILE_H);
                        maxWaterCol = Math.max(maxWaterCol, c);
                    }
                    if (tile.isLowTideBeach()) {
                        batch.draw(ImageUtils.loadRegion(AssetPaths.LOW_TIDE_BEACH_MARKER),
                                tileX(c) + 20f, tileY(r) + 20f, TILE_W - 40f, TILE_H - 40f);
                    }
                }
            }
            // خط سرتاسری روی مرز چپِ ستون‌های پوشیده‌شده با آب (طبق سند: مشخص می‌کند
            // آب حداکثر تا چه ستونی پیشروی می‌کند). خط باریک است، پس روی مرز کاشته می‌شود.
            if (maxWaterCol >= 0) {
                float lineX = tileX(maxWaterCol) - 6f;
                batch.draw(ImageUtils.loadRegion(AssetPaths.WATER_LEVEL_LINE),
                        lineX, tileY(Board.ROWS - 1), 12f, TILE_H * Board.ROWS);
            }
        }

        // --- غارهای یخی: زمین‌های لغزنده به بالا/پایین ---
        if (session.getSeason() == model.game.Season.FROSTBITE_CAVES) {
            for (int r = 0; r < Board.ROWS; r++) {
                for (int c = 0; c < Board.COLS; c++) {
                    Tile.SliderDirection dir = board.getTile(r, c).getSliderDirection();
                    if (dir == Tile.SliderDirection.UP) {
                        batch.draw(ImageUtils.loadRegion(AssetPaths.ICE_SLIDER_UP), tileX(c), tileY(r), TILE_W, TILE_H);
                    } else if (dir == Tile.SliderDirection.DOWN) {
                        batch.draw(ImageUtils.loadRegion(AssetPaths.ICE_SLIDER_DOWN), tileX(c), tileY(r), TILE_W, TILE_H);
                    }
                }
            }
        }
    }

    private void drawGraves(com.badlogic.gdx.graphics.g2d.Batch batch) {
        Board board = session.getBoard();
        for (int r = 0; r < Board.ROWS; r++) {
            for (int c = 0; c < Board.COLS; c++) {
                Tile tile = board.getTile(r, c);
                if (tile.hasGrave()) {
                    String path = graveTexturePath(tile.getGrave());
                    batch.draw(ImageUtils.loadRegion(path), tileX(c), tileY(r), TILE_W, TILE_H);
                }
                if (tile.isNecromancyTile() && !AssetPaths.NECROMANCY_TILE_MARKER.isEmpty()) {
                    batch.draw(ImageUtils.loadRegion(AssetPaths.NECROMANCY_TILE_MARKER),
                            tileX(c) + 24f, tileY(r) + 24f, TILE_W - 48f, TILE_H - 48f);
                }
            }
        }
    }

    private String graveTexturePath(Grave grave) {
        // سه نوع واقعاً متفاوت طبق سند: قبر معمولی، قبر حاوی خورشید، قبر حاوی غذای
        // گیاه. در فصل مصر باستان از قبر مخصوص همان فصل (هیروگلیف) استفاده می‌شود،
        // چون در آن فصل قبرها هیچ محتوایی ندارند (فقط مانع تیر مستقیم‌اند).
        if (session.getSeason() == model.game.Season.ANCIENT_EGYPT) {
            return AssetPaths.GRAVE_ANCIENT_EGYPT;
        }
        if (grave.hasSun()) return AssetPaths.GRAVE_TYPE_SUN;
        if (grave.hasPlantFood()) return AssetPaths.GRAVE_TYPE_PLANT_FOOD;
        return AssetPaths.GRAVE_TYPE_PLAIN;
    }

    private void drawLawnMowers(com.badlogic.gdx.graphics.g2d.Batch batch) {
        Board board = session.getBoard();
        for (int r = 0; r < Board.ROWS; r++) {
            if (!board.isLawnMowerAvailable(r)) {
                continue; // برای مراحلی که ماشین چمن‌زنی ندارند
            }
            String path = seasonMowerPath(board.isLawnMowerUsed(r));
            batch.draw(ImageUtils.loadRegion(path), BOARD_LEFT - 50f, tileY(r), 44f, TILE_H);
        }
    }

    private void drawPlants(com.badlogic.gdx.graphics.g2d.Batch batch) {
        Board board = session.getBoard();
        // ترتیب رسم: سطرهای پایین‌تر روی سطرهای بالاتر نمایش داده شوند (طبق بخش «ترتیب درست نمایش موجودیت‌ها»)
        for (int r = Board.ROWS - 1; r >= 0; r--) {
            for (int c = 0; c < Board.COLS; c++) {
                Tile tile = board.getTile(r, c);
                Plant plant = tile.getPlant();
                if (plant == null) {
                    continue;
                }
                String path = AssetPaths.plantIcon(plant.getName());
                TextureRegion tex = ImageUtils.loadRegion(path);
                batch.draw(tex, tileX(c) + 8f, tileY(r) + 8f, TILE_W - 16f, TILE_H - 16f);

                // گیاه یخ‌زده: طبق سند، خود گیاه باید داخل بلوک یخ دیده شود (نه محو
                // یا فقط رنگ‌عوض‌شده)؛ پس روی همان اسپرایت گیاه، پوسته‌ی نیمه‌شفاف
                // یخ کشیده می‌شود.
                if (plant.isFrozenSolid()) {
                    TextureRegion iceOverlay = ImageUtils.loadRegion(AssetPaths.FROZEN_PLANT_ICE_OVERLAY);
                    batch.draw(iceOverlay, tileX(c) + 2f, tileY(r) + 2f, TILE_W - 4f, TILE_H - 4f);
                }

                // نوار سلامتی ساده‌ی گیاه در صورت آسیب‌دیدگی
                if (plant.getHealth() < plant.getMaxHealth()) {
                    drawHealthBar(batch, tileX(c) + 8f, tileY(r) + TILE_H - 6f, TILE_W - 16f,
                            plant.getHealth() / (float) plant.getMaxHealth());
                }
            }
        }
    }

    private void drawZombies(com.badlogic.gdx.graphics.g2d.Batch batch) {
        for (Zombie z : session.getAliveZombies()) {
            float x = tileX(0) + (float) z.getXPosition() * TILE_W;
            float y = tileY(z.getRow());

            // زامبی کاملاً یخ‌زده: طبق سند فقط نمایش یک بلوک یخ کافی است و نیازی به
            // نمایش خود زامبی زیر آن نیست؛ پس اینجا کلاً به‌جای اسپرایت زامبی، بلوک
            // یخ رسم می‌شود و بقیه‌ی جلوه‌های زیر (نوار سلامتی و هشدار خط پایان) رد می‌شود.
            if (z.getFrozenTicks() > 0) {
                TextureRegion iceBlock = ImageUtils.loadRegion(AssetPaths.FROZEN_ZOMBIE_ICE_BLOCK);
                batch.draw(iceBlock, x, y, TILE_W - 10f, TILE_H - 10f);
                continue;
            }

            String path = AssetPaths.zombieIcon(z.getTypeName());
            TextureRegion tex = ImageUtils.loadRegion(path);

            // تغییر رنگ زامبی هنگام چیل شدن (سرعت کم شده ولی هنوز کاملاً یخ نزده)
            if (z.getChilledTicks() > 0) {
                batch.setColor(0.75f, 0.9f, 1f, 1f);
            }
            batch.draw(tex, x, y, TILE_W - 10f, TILE_H - 10f);
            batch.setColor(Color.WHITE);

            // جلوه‌ی گردباد (مصر باستان): برای مدت کوتاهی بعد از ورود زامبیِ گردباد-زده
            // به زمین، یک ابر گردوغبار روی آن نمایش داده می‌شود تا کاربر متوجه شود
            // چرا این زامبی چند خانه جلوتر ظاهر شده است.
            if (z.isSpawnedByTornado() && session.getTickCount() - z.getSpawnTick() < 15) {
                TextureRegion tornado = ImageUtils.loadRegion(AssetPaths.TORNADO_EFFECT);
                batch.draw(tornado, x - 10f, y - 10f, TILE_W + 10f, TILE_H + 10f);
            }

            drawHealthBar(batch, x, y + TILE_H - 16f, TILE_W - 10f, z.getHealth() / (float) z.getMaxHealth());

            // جلوه‌ی هشدار برای زامبی‌های نزدیک به خط پایان (طبق بخش «جلوه زامبی‌های نزدیک خط پایان»)
            if (z.getXPosition() < 1.0) {
                batch.setColor(1f, 0.4f, 0.4f, 1f);
                batch.draw(tex, x, y, TILE_W - 10f, TILE_H - 10f);
                batch.setColor(Color.WHITE);
            }
        }
    }

    private void drawProjectiles(com.badlogic.gdx.graphics.g2d.Batch batch) {
        for (Projectile p : session.getActiveProjectiles()) {
            if (p.isDead()) {
                continue;
            }
            String path = p.isFire() ? AssetPaths.PROJECTILE_FIRE
                    : (p.isIce() ? AssetPaths.PROJECTILE_ICE : AssetPaths.PROJECTILE_NORMAL);
            TextureRegion tex = ImageUtils.loadRegion(path);
            float x = tileX(0) + (float) p.getX() * TILE_W;
            float y = tileY(p.getRow()) + TILE_H / 2f - 8f;
            batch.draw(tex, x, y, 20f, 20f);
        }
    }

    private void drawFallingSuns(com.badlogic.gdx.graphics.g2d.Batch batch) {
        for (FallingSun fs : session.getFallingSuns()) {
            String path;
            switch (fs.getKind()) {
                case SPECIAL: path = AssetPaths.SUN_SPECIAL; break;
                case RADIOACTIVE: path = AssetPaths.SUN_RADIOACTIVE; break;
                default: path = AssetPaths.SUN_NORMAL; break;
            }
            TextureRegion tex = ImageUtils.loadRegion(path);
            float x = tileX(fs.getCol()) + TILE_W / 2f - 16f;
            float y = fs.isLanded() ? tileY(fs.getRow()) + TILE_H / 2f - 16f
                    : tileY(fs.getRow()) + TILE_H + 40f; // قبل از فرود، بالاتر از خانه نمایش داده می‌شود
            batch.draw(tex, x, y, 32f, 32f);
        }
    }

    private void drawHealthBar(com.badlogic.gdx.graphics.g2d.Batch batch, float x, float y, float width, float ratio) {
        // TODO: می‌توان به‌جای رسم دستی، از یک تکسچر ساده (white pixel) رنگ‌شده استفاده کرد.
    }

    // ==================== ورودی کاربر روی تخته (کاشت/برداشت/غذادهی/برداشت خورشید) ====================

    private void handleBoardClick() {
        if (!Gdx.input.justTouched()) {
            return;
        }
        com.badlogic.gdx.math.Vector2 touch = stage.screenToStageCoordinates(
                new com.badlogic.gdx.math.Vector2(Gdx.input.getX(), Gdx.input.getY()));

        // اول بررسی می‌کنیم که کلیک روی یک خورشید سقوط‌کرده باشد (برداشت خورشید آسمانی)
        for (int i = 0; i < session.getFallingSuns().size(); i++) {
            FallingSun fs = session.getFallingSuns().get(i);
            float x = tileX(fs.getCol()) + TILE_W / 2f - 16f;
            float y = fs.isLanded() ? tileY(fs.getRow()) + TILE_H / 2f - 16f : tileY(fs.getRow()) + TILE_H + 40f;
            if (touch.x >= x && touch.x <= x + 32f && touch.y >= y && touch.y <= y + 32f) {
                collectSunAt(fs.getRow(), fs.getCol());
                return;
            }
        }

        int col = (int) ((touch.x - BOARD_LEFT) / TILE_W);
        int row = (int) ((BOARD_TOP - touch.y) / TILE_H) - 1;
        if (col < 0 || col >= Board.COLS || row < 0 || row >= Board.ROWS) {
            return; // کلیک خارج از تخته بوده (مثلاً روی نوار کناری)
        }

        Tile tile = session.getBoard().getTile(row, col);

        if (shovelSelected) {
            if (tile.getPlant() != null) {
                tile.setPlant(null);
            }
            shovelSelected = false;
            return;
        }

        if (plantFoodSelected) {
            if (tile.getPlant() != null && session.getPlantFoodCount() > 0) {
                tile.getPlant().feed(session);
            }
            plantFoodSelected = false;
            return;
        }

        if (selectedPlantToPlant != null) {
            plantSelectedAt(row, col);
            return;
        }

        // اگر گیاه تولیدکننده‌ی خورشید آماده باشد، کلیک روی خودش هم خورشید را جمع می‌کند
        collectSunAt(row, col);
    }

    private void collectSunAt(int row, int col) {
        List<FallingSun> suns = session.getFallingSuns();
        for (int i = 0; i < suns.size(); i++) {
            FallingSun fs = suns.get(i);
            if (fs.getRow() == row && fs.getCol() == col) {
                int amount = fs.getKind().getValue();
                suns.remove(i);
                session.getSunManager().addSun(amount);
                SoundManager.playSound(AssetPaths.SFX_SUN);
                return;
            }
        }
        Tile tile = session.getBoard().getTile(row, col);
        if (tile != null && tile.getPlant() instanceof model.plant.interfaces.ISunProducer) {
            model.plant.interfaces.ISunProducer producer = (model.plant.interfaces.ISunProducer) tile.getPlant();
            if (producer.isSunReady()) {
                session.getSunManager().addSun(producer.getReadySunAmount());
                producer.collectSun();
                SoundManager.playSound(AssetPaths.SFX_SUN);
            }
        }
    }

    private void plantSelectedAt(int row, int col) {
        String type = selectedPlantToPlant;
        model.levelrules.ILevelRules rules = session.getLevelRules();

        // --- مُد «گیاهان زندانی»: فقط گیاهان مجاز قابل کاشت‌اند ---
        if (rules instanceof model.levelrules.LockedPlantsRules
                && !((model.levelrules.LockedPlantsRules) rules).getAllowedPlants().contains(type)) {
            selectedPlantToPlant = null;
            return;
        }

        boolean isConveyorBelt = rules instanceof model.levelrules.ConveyorBeltRules;
        if (isConveyorBelt) {
            // در این مُد، وجود گیاه روی نوار جایگزین چک cooldown عادی می‌شود
            if (!((model.levelrules.ConveyorBeltRules) rules).getBeltPlants().contains(type)) {
                selectedPlantToPlant = null;
                return;
            }
        } else if (session.isPlantOnCooldown(type)) {
            selectedPlantToPlant = null;
            return;
        }

        Tile tile = session.getBoard().getTile(row, col);
        if (tile == null || !tile.canPlant(PlantFactory.create(type))) {
            return;
        }
        Plant plant = PlantFactory.create(type);
        User user = session.getUser();
        int currentPlantLevel = user.getPlantLevel(type);
        plant.applyUpgradeLevel(currentPlantLevel);

        if (user.hasGreenhouseBoost(type)) {
            plant.setGreenhouseBoosted(true);
            user.consumeGreenhouseBoost(type);
        }
        if (!session.getSunManager().spendSun(plant.getSunCost())) {
            return; // خورشید کافی نیست
        }
        plant.place(row, col);
        tile.setPlant(plant);
        if (isConveyorBelt) {
            // گیاه از روی نوار برداشته می‌شود و دیگر نمایش داده نمی‌شود (به‌جای cooldown عادی)
            ((model.levelrules.ConveyorBeltRules) rules).takeFromBelt(type);
        } else {
            session.startPlantCooldown(type, plant.getCooldownTicks());
        }
        selectedPlantToPlant = null;
        SoundManager.playSound(AssetPaths.SFX_PLANT);
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
