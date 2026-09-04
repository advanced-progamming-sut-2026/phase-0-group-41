package gdx.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
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
import model.game.Tile;
import model.minigame.IZombieSession;
import model.plant.Plant;
import model.plant.PlantFactory;
import model.projectile.Projectile;
import model.zombie.Zombie;

/**
 * بخش امتیازی: بازی دونفره‌ی «من، زامبی» به‌صورت Couch Play روی یک دستگاه،
 * بدون نیاز به شبکه (طبق سند فاز ۳، بخش امتیازی).
 *
 * بازیکن نقش گیاهان با ماوس (کلیک روی خانه‌ها) بازی می‌کند و بازیکن نقش
 * زامبی‌ها با صفحه‌کلید: کلیدهای جهت‌دار برای حرکت مکان‌نما، کلیدهای ۱ تا ۴
 * برای انتخاب نوع زامبی، و Space/Enter برای قرار دادن زامبی در مکان‌نما.
 */
public class IZombieCouchScreen implements Screen {

    private static final float WORLD_WIDTH = 1280f;
    private static final float WORLD_HEIGHT = 720f;

    // === رفع باگ: بک‌گراند و مختصات زمین با هم منطبق نبودند ===
    // قبلاً این صفحه از BG_MINIGAME_IZOMBIE استفاده می‌کرد که یک تصویر کاملاً
    // متفاوت (برندِ «Big Brainz») است، در حالی که مقادیر BOARD_LEFT/BOARD_TOP/
    // TILE_W/TILE_H به‌صورت دستی و جدا از آن تصویر تنظیم شده بودند. نتیجه این
    // بود که شبکه‌ی خانه‌ها روی چمن واقعیِ تصویر نمی‌نشست و گیاه/زامبی نسبت به
    // بک‌گراند جابه‌جا به نظر می‌رسید (دقیقاً همان چیزی که در عکس مشخص بود).
    // راه‌حل: دقیقاً همان بک‌گراند و همان مختصاتِ کالیبره‌شده‌ی GameScreen
    // (بازی اصلی) استفاده می‌شود؛ چون WORLD_WIDTH/HEIGHT و Board.ROWS/COLS در
    // هر دو صفحه یکسان است، این مقادیر بدون هیچ تغییری روی این زمین هم درست
    // می‌نشینند.
    private static final float BOARD_LEFT = 325f;
    private static final float BOARD_TOP = 518f;
    private static final float TILE_W = 98.75f;
    private static final float TILE_H = 87.9f;
    private static final float SECONDS_PER_TICK = 0.1f;

    private static final String[] ZOMBIE_TYPES = {"normal", "conehead", "buckethead", "imp"};
    private static final String[] PLANT_TYPES = {"peashooter", "sunflower", "wallnut", "potatomine", "squash"};

    private final PvZGame game;
    private final Stage stage;
    private final Skin skin;
    private final IZombieSession session;
    private final int level;

    private final Label plantSunLabel;
    private final Label zombieSunLabel;
    private final Label statusLabel;
    private final Image cursorImage;

    private float tickAccumulator = 0f;
    private boolean announcedGameOver = false;

    private String selectedPlantType = null;
    private String selectedZombieType = ZOMBIE_TYPES[0];
    private int cursorRow = 0;
    private int cursorCol = Board.COLS - 1;

    public IZombieCouchScreen(PvZGame game, int level) {
        this.game = game;
        this.level = level;
        this.skin = game.getSkin();
        this.session = new IZombieSession(game.getLoggedInUser(), level);

        Viewport viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT);
        this.stage = new Stage(viewport);

        plantSunLabel = new Label("0", skin);
        zombieSunLabel = new Label("0", skin);
        statusLabel = new Label("", skin);
        cursorImage = new Image(skin.newDrawable("white", new Color(1f, 1f, 0f, 0.35f)));
        cursorImage.setSize(TILE_W - 6f, TILE_H - 6f);
        stage.addActor(cursorImage);

        buildHud();
        buildSidebars();

        SoundManager.playMusic(AssetPaths.MUSIC_MINIGAME);
        Gdx.input.setInputProcessor(stage);
    }

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
                game.goToMiniGames();
            }
        });

        // === رفع باگ بودجه‌ی مشترک: نمایش جداگانه‌ی خورشید هر طرف ===
        // قبلاً فقط یک عدد خورشید (که در واقع مال SunManager مشترک بود) نشان
        // داده می‌شد و هیچ‌کدام از دو بازیکن نمی‌دانستند دقیقاً بودجه‌ی خودشان
        // (که جداگانه از تعداد آفتابگردان‌ها/زامبی‌های خورشیدزا تولید می‌شود)
        // چقدر است.
        Table plantSunBox = new Table();
        plantSunBox.add(new Label("Plants:", skin)).padRight(4f);
        plantSunBox.add(new Image(ImageUtils.loadRegion(AssetPaths.ICON_SUN))).size(28f).padRight(4f);
        plantSunBox.add(plantSunLabel).padRight(20f);

        Table zombieSunBox = new Table();
        zombieSunBox.add(new Label("Zombies:", skin)).padRight(4f);
        zombieSunBox.add(new Image(ImageUtils.loadRegion(AssetPaths.ICON_SUN))).size(28f).padRight(4f);
        zombieSunBox.add(zombieSunLabel).padRight(20f);

        Label title = new Label("I, Zombie - Couch Play (Level " + level + ")", skin, "title");

        Table row = new Table();
        row.add(pauseButton).size(56f, 44f).padRight(8f);
        row.add(exitButton).size(70f, 44f).padRight(16f);
        row.add(plantSunBox);
        row.add(zombieSunBox);
        row.add(title).padLeft(20f);

        top.add(row).padTop(8f).row();
        // === رفع باگ: راهنمای نامشخص برای بازیکن سمت زامبی ===
        // قبلاً فقط «arrows to move, 1-4 to pick, Space to place» نوشته شده
        // بود بدون توضیح اینکه اصلاً هدف بازی چیست (خوردن مغزها، فقط سمت راست
        // خط قرمز، بودجه‌ی جداگانه). این خط راهنما را کامل‌تر می‌کنیم.
        top.add(new Label("Goal: reach the house on the left and eat all 5 brains before you run out of zombie sun!", skin))
                .padTop(2f).row();
        top.add(new Label("Plants (left player): click a card, then click a tile to plant.", skin))
                .padTop(2f).row();
        top.add(new Label("Zombies (right player): arrows to move cursor, 1-4 to pick a zombie type, Space/Enter to place (only right of the red line).", skin))
                .padTop(2f).row();
        top.add(statusLabel).padTop(4f).row();
        stage.addActor(top);
    }

    /** منوی توقف بازی؛ دقیقاً هم‌ارز با openPauseMenu در MiniGameScreen. چون
     *  تعویض Screen باعث می‌شود render این کلاس دیگر صدا زده نشود، تیک بازی
     *  کاملاً متوقف می‌ماند تا کاربر Resume بزند. */
    private void openPauseMenu() {
        SoundManager.playSound(AssetPaths.SFX_CLICK);
        game.setScreen(new PauseScreen(game, this, () -> game.setScreen(new IZombieCouchScreen(game, level))));
    }

    private void buildSidebars() {
        Table plantSidebar = new Table();
        plantSidebar.setFillParent(true);
        plantSidebar.top().left();
        plantSidebar.padTop(110f).padLeft(8f);
        plantSidebar.add(new Label("Plants:", skin)).left().row();
        for (String p : PLANT_TYPES) {
            plantSidebar.add(buildPlantCard(p)).size(64f, 64f).padBottom(4f).row();
        }
        stage.addActor(plantSidebar);

        Table zombieSidebar = new Table();
        zombieSidebar.setFillParent(true);
        zombieSidebar.top().right();
        zombieSidebar.padTop(110f).padRight(8f);
        zombieSidebar.add(new Label("Zombies (1-4):", skin)).right().row();
        for (int i = 0; i < ZOMBIE_TYPES.length; i++) {
            Label l = new Label((i + 1) + ". " + ZOMBIE_TYPES[i], skin);
            zombieSidebar.add(l).right().padBottom(4f).row();
        }
        stage.addActor(zombieSidebar);
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

    // ==================== حلقه‌ی اصلی ====================

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
            handleZombieKeyboardInput();
        } else if (!announcedGameOver) {
            announcedGameOver = true;
            game.setScreen(new IZombieMultiplayerResultScreen(game, level, !session.isWon()));
            // نکته: در Couch Play کاربر واحدی که وارد سیستم است هر دو نقش را بازی می‌کند،
            // پس این صفحه‌ی نتیجه صرفاً برد/باخت کلی مینی‌گیم را نشان می‌دهد.
            return;
        }

        for (String event : session.pollRecentEvents()) {
            statusLabel.setText(event);
        }

        stage.getBatch().begin();
        drawBoard(stage.getBatch());
        drawPlants(stage.getBatch());
        // === اضافه‌شده: رسم پرتابه‌ها (تیر نخودی و مشابه) ===
        // منطق پرتابه‌ها همیشه سمت مدل کار می‌کرد (peashooter واقعاً شلیک
        // می‌کرد)، اما این صفحه اصلاً هیچ کدی برای رسم پرتابه‌ها نداشت، پس
        // بازیکن هیچ تیری روی صفحه نمی‌دید. اینجا دقیقاً مطابق
        // GameScreen.drawProjectiles اضافه شد.
        drawProjectiles(stage.getBatch());
        drawZombies(stage.getBatch());
        stage.getBatch().end();

        plantSunLabel.setText(String.valueOf(session.getSunManager().getCurrentSun()));
        zombieSunLabel.setText(String.valueOf(session.getZombieSunManager().getCurrentSun()));
        cursorImage.setPosition(tileX(cursorCol) + 3f, tileY(cursorRow) + 3f);

        stage.act(delta);
        stage.draw();

        handlePlantMouseInput();
    }

    private void handleZombieKeyboardInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT) && cursorCol > 0) cursorCol--;
        if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT) && cursorCol < Board.COLS - 1) cursorCol++;
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP) && cursorRow > 0) cursorRow--;
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN) && cursorRow < Board.ROWS - 1) cursorRow++;

        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) selectedZombieType = ZOMBIE_TYPES[0];
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) selectedZombieType = ZOMBIE_TYPES[1];
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) selectedZombieType = ZOMBIE_TYPES[2];
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_4)) selectedZombieType = ZOMBIE_TYPES[3];

        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) || Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            IZombieSession.PlaceZombieResult result = session.placeZombie(selectedZombieType, cursorRow, cursorCol);
            switch (result) {
                case SUCCESS:
                    statusLabel.setText(selectedZombieType + " placed!");
                    break;
                case BEYOND_RED_LINE:
                    statusLabel.setText("Zombies can only go right of the red line.");
                    break;
                case NOT_ENOUGH_SUN:
                    statusLabel.setText("Not enough sun for " + selectedZombieType + ".");
                    break;
                default:
                    statusLabel.setText("Invalid zombie placement.");
            }
            SoundManager.playSound(AssetPaths.SFX_CLICK);
        }
    }

    private void handlePlantMouseInput() {
        // === رفع باگ: امکان برداشت خورشید حتی بدون انتخاب گیاه ===
        // قبلاً این متد اگر هیچ گیاهی انتخاب نشده بود کلاً زودتر برمی‌گشت، پس
        // کلیک روی آفتابگردانِ آماده‌ی برداشت (بدون اینکه کارت گیاه دیگری هم
        // انتخاب شده باشد) بی‌اثر بود. حالا فقط کلیک‌نشدن باعث خروج زودهنگام
        // می‌شود؛ خالی بودن selectedPlantType داخل خودِ منطق کاشتن چک می‌شود.
        if (!Gdx.input.justTouched()) {
            return;
        }
        Vector2 touch = stage.screenToStageCoordinates(new Vector2(Gdx.input.getX(), Gdx.input.getY()));
        int col = (int) ((touch.x - BOARD_LEFT) / TILE_W);
        // === رفع باگ: کم کردن اضافی ۱ از سطر ===
        // قبلاً اینجا «- 1» اضافه بود که باعث می‌شد فرمول با tileY() (که خودش
        // «(row + 1) * TILE_H» را از BOARD_TOP کم می‌کند) هم‌خوان نباشد؛
        // در نتیجه ردیف بالایی همیشه row=-1 (خارج از بازه) حساب می‌شد و بقیه‌ی
        // ردیف‌ها هم یکی جابه‌جا محاسبه می‌شدند، پس عملاً کاشتن گیاه ممکن
        // نبود یا در خانه‌ی اشتباه انجام می‌شد. همین فرمول در MiniGameScreen
        // (بدون "- 1") درست کار می‌کند.
        int row = (int) ((BOARD_TOP - touch.y) / TILE_H);
        if (col < 0 || col >= Board.COLS || row < 0 || row >= Board.ROWS) {
            return;
        }

        Tile tile = session.getBoard().getTile(row, col);

        // === رفع باگ: برداشت خورشید از آفتابگردان‌ها ===
        // قبلاً کلیک روی یک آفتابگردانِ آماده‌ی برداشت هیچ اثری نداشت (چون این
        // متد فقط منطق کاشتن را داشت)، پس تنها منبع خورشید طرف گیاه همان
        // خورشید اولیه‌ی شروع بازی بود و هیچ‌وقت افزایش پیدا نمی‌کرد. حالا
        // دقیقاً مطابق الگوی MiniGameScreen.handleIZombieClick، اول بررسی
        // می‌کنیم آیا خانه‌ی کلیک‌شده گیاهِ خورشیدزای آماده دارد.
        if (tile != null && tile.getPlant() instanceof model.plant.interfaces.ISunProducer) {
            model.plant.interfaces.ISunProducer producer =
                    (model.plant.interfaces.ISunProducer) tile.getPlant();
            if (producer.isSunReady()) {
                session.getSunManager().addSun(producer.getReadySunAmount());
                producer.collectSun();
                statusLabel.setText("+" + producer.getReadySunAmount() + " sun collected!");
                SoundManager.playSound(AssetPaths.SFX_SUN);
                return;
            }
        }

        if (selectedPlantType == null) {
            return;
        }
        if (session.isPlantOnCooldown(selectedPlantType)) {
            statusLabel.setText(selectedPlantType + " is on cooldown.");
            return;
        }
        Plant plant = PlantFactory.create(selectedPlantType);
        if (tile == null || !tile.canPlant(plant)) {
            statusLabel.setText("Can't plant there.");
            return;
        }
        if (!session.getSunManager().spendSun(plant.getSunCost())) {
            statusLabel.setText("Not enough sun for " + selectedPlantType + ".");
            return;
        }
        plant.place(row, col);
        tile.setPlant(plant);
        session.startPlantCooldown(selectedPlantType, plant.getCooldownTicks());
        statusLabel.setText(selectedPlantType + " planted!");
        SoundManager.playSound(AssetPaths.SFX_PLANT);
    }

    // ==================== رسم ====================

    private float tileX(int col) {
        return BOARD_LEFT + col * TILE_W;
    }

    private float tileY(int row) {
        return BOARD_TOP - (row + 1) * TILE_H;
    }

    private void drawBoard(com.badlogic.gdx.graphics.g2d.Batch batch) {
        // === رفع باگ: بک‌گراند اشتباه ===
        // قبلاً BG_MINIGAME_IZOMBIE استفاده می‌شد که تصویر برندِ «Big Brainz»
        // است، نه چمنِ معمولی بازی؛ به همین دلیل شبکه‌ی خانه‌ها روی آن تصویر
        // جا نمی‌افتاد. حالا از همان BG_LAWN_NORMAL بازی اصلی استفاده می‌شود
        // (دقیقاً هم‌راستا با BOARD_LEFT/TOP/TILE_W/H که از GameScreen گرفته شد).
        TextureRegion bg = ImageUtils.loadRegion(AssetPaths.BG_LAWN_NORMAL);
        batch.draw(bg, 0, 0, WORLD_WIDTH, WORLD_HEIGHT);
        drawRedLine(batch);
    }

    /**
     * === اضافه‌شده: رسم خط قرمز (مرز کاشتن زامبی) ===
     * قبلاً هیچ نشانه‌ی بصری‌ای برای خط قرمز روی زمین وجود نداشت؛ فقط وقتی
     * بازیکن زامبی سعی می‌کرد سمت چپِ خط قرمز زامبی بگذارد، یک پیام متنی
     * نشانش داده می‌شد. حالا یک خط قرمزِ ضخیم دقیقاً روی مرز ستونِ
     * RED_LINE_COL رسم می‌شود تا هر دو بازیکن از قبل بدانند کجا مجاز است.
     */
    private void drawRedLine(com.badlogic.gdx.graphics.g2d.Batch batch) {
        int redLineCol = session.getRedLineCol();
        float x = tileX(redLineCol) - 3f;
        float y = tileY(Board.ROWS - 1);
        Color prev = batch.getColor().cpy();
        batch.setColor(Color.RED);
        batch.draw(skin.getRegion("white"), x, y, 6f, TILE_H * Board.ROWS);
        batch.setColor(prev);
    }

    /**
     * === اضافه‌شده: رسم با حفظ نسبت ابعاد (aspect ratio) ===
     * قبلاً هر اسپرایت گیاه/زامبی به‌زور داخل کل مستطیل تایل کش/فشرده
     * می‌شد (batch.draw(tex, x, y, TILE_W-16, TILE_H-16))، بدون توجه به
     * ابعاد واقعی تصویر (که برای هر گیاه فرق می‌کند). این متد دقیقاً همان
     * راه‌حلی است که در GameScreen.drawFitted (بازی اصلی) استفاده شده:
     * تصویر با حفظ نسبت ابعاد داخل جعبه جا می‌شود، وسط‌چین افقی و چسبیده
     * به کف تایل — دقیقاً مثل عکسی که از بازی اصلی فرستادید.
     */
    private void drawFitted(com.badlogic.gdx.graphics.g2d.Batch batch, TextureRegion tex,
                             float x, float y, float w, float h) {
        float texW = tex.getRegionWidth();
        float texH = tex.getRegionHeight();
        if (texW <= 0 || texH <= 0) {
            batch.draw(tex, x, y, w, h);
            return;
        }
        float scale = Math.min(w / texW, h / texH);
        float drawW = texW * scale;
        float drawH = texH * scale;
        float drawX = x + (w - drawW) / 2f;
        batch.draw(tex, drawX, y, drawW, drawH);
    }

    private void drawPlants(com.badlogic.gdx.graphics.g2d.Batch batch) {
        Board board = session.getBoard();
        for (int r = Board.ROWS - 1; r >= 0; r--) {
            for (int c = 0; c < Board.COLS; c++) {
                Plant plant = board.getTile(r, c).getPlant();
                if (plant == null) continue;
                TextureRegion tex = ImageUtils.loadRegion(AssetPaths.plantIcon(plant.getName()));
                drawFitted(batch, tex, tileX(c) + 8f, tileY(r) + 6f, TILE_W - 16f, TILE_H - 12f);

                // === اضافه‌شده: نشانگر خورشیدِ آماده‌ی برداشت روی آفتابگردان‌ها ===
                // بدون این نشانگر بازیکن سمت گیاه اصلاً نمی‌دانست کدام
                // آفتابگردان خورشید آماده دارد که باید کلیک کند (مطابق همان
                // الگوی MiniGameScreen).
                if (plant instanceof model.plant.interfaces.ISunProducer
                        && ((model.plant.interfaces.ISunProducer) plant).isSunReady()) {
                    TextureRegion sunTex = ImageUtils.loadRegion(AssetPaths.SUN_NORMAL);
                    batch.draw(sunTex, tileX(c) + TILE_W - 30f, tileY(r) + TILE_H - 30f, 32f, 32f);
                }
            }
        }
    }

    /**
     * === اضافه‌شده: رسم پرتابه‌ها ===
     * منطق مدل (Peashooter و مشابه) از قبل کاملاً کار می‌کرد و
     * session.getActiveProjectiles() پر می‌شد، اما این صفحه هیچ کدی برای
     * رسم آن‌ها نداشت، پس بازیکن هیچ تیری روی صفحه نمی‌دید. دقیقاً مطابق
     * GameScreen.drawProjectiles/projectileTexturePath اضافه شد.
     */
    private void drawProjectiles(com.badlogic.gdx.graphics.g2d.Batch batch) {
        for (Projectile p : session.getActiveProjectiles()) {
            if (p.isDead()) {
                continue;
            }
            TextureRegion tex = ImageUtils.loadRegion(projectileTexturePath(p));
            float x = tileX(0) + (float) p.getX() * TILE_W;
            float y = tileY(p.getRow()) + TILE_H / 2f - 8f;
            batch.draw(tex, x, y, 20f, 20f);
        }
    }

    private String projectileTexturePath(Projectile p) {
        if (p instanceof model.projectile.StrikeThroughProjectile) {
            return AssetPaths.PROJECTILE_PIERCING;
        }
        if (p instanceof model.projectile.LobbedProjectile && ((model.projectile.LobbedProjectile) p).hasSplash()) {
            if (p.isFire()) {
                return AssetPaths.PROJECTILE_PEPPER;
            }
            return p.isIce() ? AssetPaths.PROJECTILE_FROZEN_WATERMELON : AssetPaths.PROJECTILE_WATERMELON;
        }
        if (p.isFire()) {
            return AssetPaths.PROJECTILE_FIRE;
        }
        if (p.isIce()) {
            return AssetPaths.PROJECTILE_ICE;
        }
        return AssetPaths.PROJECTILE_NORMAL;
    }

    private void drawZombies(com.badlogic.gdx.graphics.g2d.Batch batch) {
        for (Zombie z : session.getAliveZombies()) {
            TextureRegion tex = ImageUtils.loadRegion(AssetPaths.zombieIcon(z.getTypeName()));
            float x = tileX(0) + (float) z.getXPosition() * TILE_W;
            float y = tileY(z.getRow());
            drawFitted(batch, tex, x + 5f, y + 2f, TILE_W - 10f, TILE_H - 4f);
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
