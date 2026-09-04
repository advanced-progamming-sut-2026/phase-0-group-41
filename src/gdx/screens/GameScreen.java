package gdx.screens;
import gdx.render.LawnMowerVisualManager;
import gdx.render.PamAssets;
import gdx.render.ZombieVisualManager;
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

    // ابعاد و مبدأ شبکه‌ی بازی روی صفحه — این اعداد قبلاً به‌صورت گرد و حدسی
    // انتخاب شده بودند و با موقعیت واقعی زمین خاکی/کاشی در تصویر پس‌زمینه
    // (IMAGE_BACKGROUNDS_FRONTLAWN_TEXTURE، ابعاد اصلی ۱۰۲۴×۷۶۸) مطابقت نداشتند؛
    // به همین دلیل گیاهان/زامبی‌ها یک سطر بالاتر و کمی جابه‌جا نمایش داده
    // می‌شدند. این اعداد با اندازه‌گیری پیکسلی مستقیمِ ناحیه‌ی واقعی کاشت در
    // پس‌زمینه (که از x=260 تا x=970 و y=215 تا y=684 در تصویر اصلی است) و
    // تبدیل به مقیاس بوم ۱۲۸۰×۷۲۰ محاسبه شده‌اند.
    private static final float BOARD_LEFT = 325f;
    private static final float BOARD_TOP = 518f;
    private static final float TILE_W = 98.75f;
    private static final float TILE_H = 87.9f;

    private final PvZGame game;
    private final Stage stage;
    private final Skin skin;
    private final GameSession session;
    private final int chapter;
    private final int level;

    private final Table sidebarTable = new Table();
    private final ZombieVisualManager zombieVisuals = new ZombieVisualManager();
    private final LawnMowerVisualManager lawnMowers = new LawnMowerVisualManager();
    private final Table cardsTable = new Table(); // کارت‌های گیاه انتخابی (یا گیاهان روی نوار نقاله)
    // نگاشت نام گیاه به المان‌های گرافیکیِ کارتش (برای به‌روزرسانی سریعِ
    // نمایش cooldown هر فریم، بدون نیاز به ساختن دوباره‌ی کل جدول کارت‌ها)
    private final java.util.Map<String, Stack> plantCardsByName = new java.util.LinkedHashMap<>();
    private final java.util.Map<String, com.badlogic.gdx.scenes.scene2d.ui.Image> dimOverlaysByName = new java.util.LinkedHashMap<>();
    private final java.util.Map<String, Label> cooldownLabelsByName = new java.util.LinkedHashMap<>();
    private final Table hudTable = new Table();
    private final Label sunLabel;
    private final Label plantFoodLabel;
    private final Label modeStatusLabel; // نوار وضعیت مُد ویژه (نبرد زمان‌دار، محافظ دانه‌ها و ...)؛ هر فریم آپدیت می‌شود
    // عنوان بالای صفحه که فصل، مرحله و شماره‌ی موج فعلی را نشان می‌دهد (طبق
    // درخواست کاربر: «جزئیات مرحله و شماره موج فعلی مشخص نیست»)؛ هر فریم
    // به‌روزرسانی می‌شود چون شماره‌ی موج در حین بازی تغییر می‌کند.
    private final Label chapterLevelWaveLabel;
    private TextButton startWavesButton = null; // فقط برای مُد «هرچه رسد بکار»؛ بعد از شروع موج‌ها مخفی می‌شود
    private final Table zombieProgressBar = new Table();

    private String selectedPlantToPlant = null; // نام گیاهی که کاربر برای کاشت انتخاب کرده (از نوار کناری)
    private boolean shovelSelected = false;      // حالت انتخاب بیلچه برای برداشت گیاه
    private boolean plantFoodSelected = false;   // حالت انتخاب غذای گیاه برای اعمال روی یک گیاه

    private float tickAccumulator = 0f;
    private static final float SECONDS_PER_TICK = 0.1f; // هر تیک = ۰.۱ ثانیه (طبق مدل)

    private boolean announcedGameOver = false;

    // === اطلاع‌رسانی جمع‌آوری غذای گیاه/سکه/گلدان/الماس (طبق سند: «کاربر باید
    // به طریقی مطلع شود») - یک پیام کوتاه چند ثانیه‌ای بالای صفحه نمایش داده می‌شود ===
    private final Label toastLabel;
    private float toastTimeLeft = 0f;
    private int lastKnownPlantFood;
    private int lastKnownCoins;
    private int lastKnownDiamonds;
    private int lastKnownPots;

    // === اطلاع‌رسانی «خورشید آماده‌ی برداشت» روی گیاهان تولیدکننده (مثل
    // آفتابگردان): طبق گزارش، چون تنها نشانگرِ این وضعیت یک بج کوچک ۳۲×۳۲
    // روی گوشه‌ی کاشی بود، کاربر متوجه نمی‌شد که گیاه اصلاً کار می‌کند یا نه.
    // اینجا با ردیابی این‌که کدام گیاه‌ها همین الان به‌تازگی «آماده» شده‌اند
    // (نسبت به فریم قبل)، یک toast واضح + یک صدای زنگ نمایش داده می‌شود. ===
    private final java.util.Set<Plant> announcedReadyProducers = new java.util.HashSet<>();

    // === اعلان قرمز وسط صفحه (طبق سند: قبل از شروع بازی/موج بعدی/نکرومنسی/
    // ظهور زامبی از ساحل پست). صرفاً یک نوشته‌ی ساده وسط صفحه، بدون جلوه‌ی خاص. ===
    private final Label announcementLabel;
    private float announcementTimeLeft = 0f;
    private int lastKnownWave = 0;

    public GameScreen(PvZGame game, GameSession session, int chapter, int level) {
        this.game = game;
        this.session = session;
        this.chapter = chapter;
        this.level = level;
        this.skin = game.getSkin();

        Viewport viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT);
        this.stage = new Stage(viewport);

        // پیام‌های کوتاه رویدادی (جمع‌آوری سکه/غذای گیاه/گلدان و ...) با فونت
        // بازیگوش BrianneTod ("toast") نمایش داده می‌شوند.
        toastLabel = new Label("", skin, "toast");
        toastLabel.setFontScale(1.1f);
        lastKnownPlantFood = session.getPlantFoodCount();
        lastKnownCoins = session.getUser().getCoins();
        lastKnownDiamonds = session.getUser().getDiamonds();
        lastKnownPots = session.getUser().getPendingGreenhousePots();

        // اعلان قرمز وسط صفحه (شروع موج/موج نهایی و ...) با فونت وحشت
        // "House of Terror" ("horror") نمایش داده می‌شود.
        announcementLabel = new Label("", skin, "horror");
        announcementLabel.setFontScale(1.1f);
        announcementLabel.setColor(Color.RED);
        Table announcementWrapper = new Table();
        announcementWrapper.setFillParent(true);
        announcementWrapper.center();
        announcementWrapper.add(announcementLabel);
        stage.addActor(announcementWrapper);
        // اعلان ابتدای بازی («قبل از شروع موج زامبی‌ها در ابتدای هر بازی»)
        showAnnouncement("Get Ready!", 2.5f);

        // خورشید و غذای گیاه هم شمارنده‌ی عددی HUD هستند → فونت "hud-number".
        sunLabel = new Label("0", skin, "hud-number");
        plantFoodLabel = new Label("0", skin, "hud-number");
        modeStatusLabel = new Label("", skin);
        // نوار وضعیت «فصل - مرحله | Wave» با استایل "hud-title" (نه "title" —
        // که خیلی بزرگ بود و باعث می‌شد این متن از بالای صفحه بیرون بزند).
        chapterLevelWaveLabel = new Label("", skin, "hud-title");

        buildHud();
        buildSidebar();
        buildZombieProgressBar();
        lastKnownWave = session.getWaveManager().getCurrentWave();

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
        // همه‌ی شمارنده‌های عددی HUD (خورشید/غذای گیاه بالا + سکه/الماس اینجا)
        // با فونت پیکسلی "hud-number" نمایش داده می‌شوند.
        User user = session.getUser();
        Table coinBox = new Table();
        coinBox.add(new Image(ImageUtils.loadRegion(AssetPaths.ICON_COIN))).size(28f).padRight(4f);
        coinBox.add(new Label(String.valueOf(user.getCoins()), skin, "hud-number")).padRight(16f);

        Table diamondBox = new Table();
        diamondBox.add(new Image(ImageUtils.loadRegion(AssetPaths.ICON_DIAMOND))).size(28f).padRight(4f);
        diamondBox.add(new Label(String.valueOf(user.getDiamonds()), skin, "hud-number")).padRight(16f);

        hudTable.setFillParent(true);
        hudTable.top();

        // --- عنوان بالای صفحه: فصل، مرحله و شماره‌ی موج فعلی ---
        chapterLevelWaveLabel.setText(chapterLevelWaveText());
        hudTable.add(chapterLevelWaveLabel).padTop(6f).row();

        Table topRow = new Table();
        topRow.add(pauseButton).size(48f).padRight(16f);
        topRow.add(sunBox);
        topRow.add(foodBox);
        topRow.add(coinBox);
        topRow.add(diamondBox);

        // نکته: دکمه‌های تقلب دیگر اینجا (همیشه روی صفحه) نمایش داده نمی‌شوند؛
        // طبق درخواست، همه در بخش «Cheats» داخل منوی توقف (Pause) جمع شده‌اند
        // تا هم دسترسی وسط بازی داشته باشیم و هم صفحه‌ی اصلی شلوغ نباشد.

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

        // --- پیام کوتاه اطلاع‌رسانی (غذای گیاه/سکه/گلدان/الماس جمع‌آوری‌شده) ---
        hudTable.add(toastLabel).padTop(6f).row();

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

    /**
     * نوار پیشروی زامبی‌ها: در ابتدای مرحله خالی و در انتهای آن پر است. طبق
     * سند، جایگاه هر موج روی نوار باید مشخص باشد تا کاربر بداند چقدر تا موج
     * بعدی مانده؛ برای همین به‌جای یک تکه‌ی رنگی، نوار به تعداد موج‌ها بخش‌بخش
     * می‌شود (هر موجِ تمام‌شده قرمز، بقیه خاکستری) و بین بخش‌ها یک خط باریک
     * سفید به‌عنوان مرز موج کشیده می‌شود.
     */
    private void refreshZombieProgressBar() {
        zombieProgressBar.clear();
        int totalWaves = session.getWaveManager().getTotalWaves();
        int currentWave = session.getWaveManager().getCurrentWave();
        if (totalWaves <= 0) {
            return;
        }
        float totalWidth = 500f;
        float dividerWidth = 3f;
        float segmentWidth = (totalWidth - dividerWidth * (totalWaves - 1)) / totalWaves;

        for (int w = 1; w <= totalWaves; w++) {
            Table segment = new Table();
            boolean completed = w <= currentWave;
            segment.setBackground(skin.newDrawable("white", completed ? Color.RED : Color.DARK_GRAY));
            zombieProgressBar.add(segment).width(segmentWidth).height(24f);

            if (w < totalWaves) {
                Table divider = new Table();
                divider.setBackground(skin.newDrawable("white", Color.WHITE));
                zombieProgressBar.add(divider).width(dividerWidth).height(24f);
            }
        }
    }

    /**
     * اطلاع‌رسانی گرافیکی جمع‌آوری غذای گیاه/سکه/گلدان/الماس: طبق سند، کاربر
     * باید «به طریقی مطلع شود». چون مدل این رویدادها را فقط در کنسول چاپ
     * می‌کند و رویداد جداگانه‌ای برای گرافیک صادر نمی‌کند، اینجا با مقایسه‌ی
     * مقدار فعلی هرکدام با مقدار فریم قبل، افزایش را تشخیص می‌دهیم و یک پیام
     * کوتاه چند ثانیه‌ای نمایش می‌دهیم.
     */
    private void updatePickupNotifications(float delta) {
        User user = session.getUser();
        int plantFood = session.getPlantFoodCount();
        int coins = user.getCoins();
        int diamonds = user.getDiamonds();
        int pots = user.getPendingGreenhousePots();

        if (plantFood > lastKnownPlantFood) {
            showToast("+1 Plant Food!");
        } else if (coins > lastKnownCoins) {
            showToast("+" + (coins - lastKnownCoins) + " Coins!");
        } else if (diamonds > lastKnownDiamonds) {
            showToast("+" + (diamonds - lastKnownDiamonds) + " Diamond!");
        } else if (pots > lastKnownPots) {
            showToast("+1 Greenhouse Pot!");
        }

        lastKnownPlantFood = plantFood;
        lastKnownCoins = coins;
        lastKnownDiamonds = diamonds;
        lastKnownPots = pots;

        checkSunProducerReadyNotifications();

        if (toastTimeLeft > 0f) {
            toastTimeLeft -= delta;
            if (toastTimeLeft <= 0f) {
                toastTimeLeft = 0f;
                toastLabel.setText("");
            }
        }
    }

    /**
     * روی همه‌ی خانه‌های تخته می‌گردد و برای هر گیاهِ تولیدکننده‌ی خورشید که
     * تازه «آماده‌ی برداشت» شده (قبلاً آماده نبوده)، یک toast + صدای زنگ نشان
     * می‌دهد؛ وقتی گیاه برداشت می‌شود (یا هنوز در حال تولید است)، دوباره از
     * لیست اعلان‌شده‌ها خارج می‌شود تا دفعه‌ی بعد هم اطلاع‌رسانی شود.
     */
    private void checkSunProducerReadyNotifications() {
        Board board = session.getBoard();
        for (int r = 0; r < Board.ROWS; r++) {
            for (int c = 0; c < Board.COLS; c++) {
                Plant plant = board.getTile(r, c).getPlant();
                if (!(plant instanceof model.plant.interfaces.ISunProducer)) {
                    continue;
                }
                model.plant.interfaces.ISunProducer producer = (model.plant.interfaces.ISunProducer) plant;
                boolean ready = producer.isSunReady();
                boolean alreadyAnnounced = announcedReadyProducers.contains(plant);
                if (ready && !alreadyAnnounced) {
                    announcedReadyProducers.add(plant);
                    showToast(plant.getName() + ": Sun ready! Tap it to collect.");
                    SoundManager.playSound(AssetPaths.SFX_CHIME);
                } else if (!ready && alreadyAnnounced) {
                    announcedReadyProducers.remove(plant);
                }
            }
        }
    }

    private void showToast(String message) {
        toastLabel.setText(message);
        toastTimeLeft = 2.5f;
    }

    private void showAnnouncement(String text, float durationSeconds) {
        announcementLabel.setText(text);
        announcementTimeLeft = durationSeconds;
    }

    /**
     * بررسی وضعیت‌هایی که باید اعلان قرمز وسط صفحه نشان دهند: شروع موج بعدی
     * (طبق بخش «اعلان‌های حین بازی»)، نکرومنسی در قرون وسطا، و ظهور زامبی از
     * ساحل‌های پست در ساحل موج بزرگ. صرفاً یک نوشته‌ی ساده، بدون جلوه‌ی خاص.
     */
    private void updateAnnouncements(float delta) {
        int currentWave = session.getWaveManager().getCurrentWave();
        if (currentWave > lastKnownWave) {
            boolean isFinal = session.getWaveManager().isFinalWave();
            showAnnouncement(isFinal ? "The final wave has come." : "Wave " + currentWave + " started.", 2.5f);
            lastKnownWave = currentWave;
        } else if (session.consumeNecromancyTriggeredFlag()) {
            showAnnouncement("Necromancy! The dead are rising!", 2.5f);
        } else if (session.consumeTideChangedFlag()) {
            showAnnouncement("The tide is receding...", 2.5f);
        }

        if (announcementTimeLeft > 0f) {
            announcementTimeLeft -= delta;
            if (announcementTimeLeft <= 0f) {
                announcementTimeLeft = 0f;
                announcementLabel.setText("");
            }
        }
    }

    // === رفع باگ: وقتی همه‌ی ۸ اسلات گیاه پر می‌شد، نوار کناری (که قبلاً همه‌ی
    // کارت‌ها را در یک ستون عمودی می‌چید) از ارتفاع صفحه (۷۲۰) بیرون می‌زد و
    // دکمه‌های Shovel/Plant Food که بعد از کارت‌ها اضافه می‌شدند دیگر داخل
    // صفحه جا نمی‌شدند و کلاً دیده نمی‌شدند. راه‌حل: کارت‌ها را در یک شبکه‌ی
    // دو ستونه (به‌جای یک ستون تک با ۸ ردیف) بچینیم تا حداکثر ارتفاع کارت‌ها
    // با هر تعداد گیاه (۱ تا ۸) کوچک‌تر از فضای باقی‌مانده برای این دو دکمه
    // بماند؛ ارتفاع هر کارت هم کمی کوچک شده تا این شبکه در سایدبار جا شود.
    private static final int CARD_COLUMNS = 2;
    private static final float CARD_W = 62f;
    private static final float CARD_H = 78f;

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
        // این دو دکمه دیگر به تعداد کارت‌های بالا وابسته نیستند (چون کارت‌ها
        // حالا در شبکه‌ی دو ستونه چیده می‌شوند، نه یک ستون بلند)، پس همیشه
        // بلافاصله زیر کارت‌ها و داخل صفحه باقی می‌مانند.
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
        plantCardsByName.clear();
        dimOverlaysByName.clear();
        cooldownLabelsByName.clear();
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
        int col = 0;
        for (String plantName : plantsToShow) {
            cardsTable.add(buildPlantCard(plantName)).size(CARD_W, CARD_H).pad(2f);
            col++;
            if (col % CARD_COLUMNS == 0) {
                cardsTable.row();
            }
        }
    }

    /**
     * فقط ظاهر کارت‌های گیاه را (بدون بازساخت کل جدول/لیسنرها) هر فریم
     * به‌روزرسانی می‌کند: تیره‌شدن کارت + نمایش زمان باقی‌مانده روی گیاهانی
     * که در حال شارژ (Cooldown) هستند. طبق درخواست: کاربر باید بتواند از
     * روی خودِ عکس گیاه بفهمد که فعلاً نمی‌تواند دوباره آن را بکارد، بدون
     * نیاز به کلیک‌کردن و دیدن پیام خطا.
     */
    private void updatePlantCardCooldownOverlays() {
        for (java.util.Map.Entry<String, Stack> entry : plantCardsByName.entrySet()) {
            String plantName = entry.getKey();
            Stack cardStack = entry.getValue();
            boolean onCooldown = session.isPlantOnCooldown(plantName);
            Label cooldownLabel = cooldownLabelsByName.get(plantName);
            com.badlogic.gdx.scenes.scene2d.ui.Image dimOverlay = dimOverlaysByName.get(plantName);
            if (dimOverlay != null) {
                dimOverlay.setVisible(onCooldown);
            }
            if (cooldownLabel != null) {
                if (onCooldown) {
                    int ticksLeft = session.getPlantCooldownRemaining(plantName);
                    float secondsLeft = ticksLeft * SECONDS_PER_TICK;
                    cooldownLabel.setVisible(true);
                    cooldownLabel.setText(String.format("%.0fs", Math.ceil(secondsLeft)));
                } else {
                    cooldownLabel.setVisible(false);
                }
            }
            cardStack.setTouchable(onCooldown
                    ? com.badlogic.gdx.scenes.scene2d.Touchable.disabled
                    : com.badlogic.gdx.scenes.scene2d.Touchable.enabled);
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

        // === رفع مشکل «تیره‌شدن گیاه در حین Cooldown»: یک تصویر تک‌رنگ مشکی
        // نیمه‌شفاف روی کل کارت کشیده می‌شود تا خودِ عکس گیاه واضحاً تیره‌تر
        // دیده شود (دقیقاً مثل بازی اصلی)، و رویش زمان باقی‌مانده (به ثانیه)
        // نوشته می‌شود تا کاربر بدون کلیک‌کردن و دیدن پیغام خطا بفهمد چرا فعلاً
        // نمی‌تواند این گیاه را بکارد. مقدار visible/متن هر فریم توسط
        // updatePlantCardCooldownOverlays() به‌روز می‌شود. ===
        com.badlogic.gdx.scenes.scene2d.ui.Image dimOverlay =
                new com.badlogic.gdx.scenes.scene2d.ui.Image(skin.newDrawable("white", new Color(0f, 0f, 0f, 0.65f)));
        dimOverlay.setVisible(false);
        stack.add(dimOverlay);

        Label cooldownLabel = new Label("", skin, "hud-number");
        cooldownLabel.setFontScale(0.8f);
        cooldownLabel.setColor(Color.WHITE);
        cooldownLabel.setAlignment(com.badlogic.gdx.utils.Align.center);
        cooldownLabel.setVisible(false);
        Table cooldownWrapper = new Table();
        cooldownWrapper.center();
        cooldownWrapper.add(cooldownLabel);
        stack.add(cooldownWrapper);

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

        plantCardsByName.put(plantName, stack);
        dimOverlaysByName.put(plantName, dimOverlay);
        cooldownLabelsByName.put(plantName, cooldownLabel);
        return stack;
    }

    // ==================== منوی توقف ====================

    private void openPauseMenu() {
        game.setScreen(new PauseScreen(game, this, this::restartLevel, session));
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
        drawNetworkGridLines(stage.getBatch());
        drawHoverHighlight(stage.getBatch());
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
        chapterLevelWaveLabel.setText(chapterLevelWaveText());
        if (session.getLevelRules() instanceof model.levelrules.ConveyorBeltRules) {
            refreshPlantCards(); // لیست نوار نقاله مدام تغییر می‌کند
        }
        updatePlantCardCooldownOverlays(); // تیره‌کردن کارت‌های در حال Cooldown + نمایش زمان باقی‌مانده
        if (startWavesButton != null) {
            boolean wavesStarted = ((model.levelrules.PlantWhatYouGetRules) session.getLevelRules()).isWavesStarted();
            startWavesButton.setVisible(!wavesStarted);
        }
        refreshZombieProgressBar();
        updatePickupNotifications(delta);
        updateAnnouncements(delta);

        stage.act(delta);
        stage.draw();

        // نماد همراه موس (گیاه/بیلچه/برگ) باید روی همه‌چیز از جمله UI دیده شود
        stage.getBatch().begin();
        drawCursorFollower(stage.getBatch());
        stage.getBatch().end();

        handleBoardClick();
    }

    /** متن عنوان بالای صفحه: نام فصل، شماره‌ی مرحله و شماره‌ی موج فعلی از کل موج‌ها. */
    private String chapterLevelWaveText() {
        int currentWave = session.getWaveManager().getCurrentWave();
        int totalWaves = session.getWaveManager().getTotalWaves();
        String waveText = currentWave <= 0 ? "" : (" | Wave " + currentWave + "/" + totalWaves);
        return model.game.ChapterPlan.displayName(chapter) + " - Level " + level + waveText;
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

    /**
     * رسم یک TextureRegion داخل جعبه‌ی (x, y, w, h) با حفظ نسبت ابعاد اصلی
     * تصویر و «چسباندن» آن به کف جعبه (وسط-پایین)، به‌جای کش‌دادن/فشرده کردن
     * تصویر به‌زور داخل کل مستطیل تایل (که باعث می‌شد گیاه/زامبی نامتناسب و
     * انگار «جابه‌جا/بالاتر از سطر خودش» به نظر برسد — به بخش توضیحات نگاه کنید).
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
        float drawX = x + (w - drawW) / 2f; // وسط‌چین افقی
        float drawY = y; // چسبیده به کف تایل (نه وسط‌چین عمودی) تا روی زمین بایستد
        batch.draw(tex, drawX, drawY, drawW, drawH);
    }

    private void drawBoard(com.badlogic.gdx.graphics.g2d.Batch batch) {
        // پس‌زمینه‌ی خود تخته (اگر بخواهیم مجزا از پس‌زمینه‌ی کلی رسم شود) در همین‌جا اضافه می‌شود.
        // فعلاً چون BG_LAWN_* کل صفحه را می‌پوشاند، چیزی اضافه نمی‌کنیم.
    }

    /**
     * خانه‌ی هدفِ عملیات فعلی (کاشت/برداشت/غذادهی) را با یک قاب سفیدرنگ مشخص
     * می‌کند، دقیقاً طبق سند: «سطر و ستون محل کاشت سفیدرنگ می‌شوند» و همین برای
     * برداشت و غذادهی هم تکرار شده است.
     */
    private void drawHoverHighlight(com.badlogic.gdx.graphics.g2d.Batch batch) {
        if (selectedPlantToPlant == null && !shovelSelected && !plantFoodSelected) {
            return;
        }
        int[] hovered = hoveredTile();
        if (hovered == null) {
            return;
        }
        int row = hovered[0];
        int col = hovered[1];
        float x = tileX(col);
        float y = tileY(row);
        float border = 4f;

        batch.setColor(Color.WHITE);
        // چهار نوار باریک دور خانه (به‌جای پر کردن کل خانه، تا گیاه/زامبی زیرش هم دیده شود)
        batch.draw(skin.getRegion("white"), x, y + TILE_H - border, TILE_W, border); // بالا
        batch.draw(skin.getRegion("white"), x, y, TILE_W, border); // پایین
        batch.draw(skin.getRegion("white"), x, y, border, TILE_H); // چپ
        batch.draw(skin.getRegion("white"), x + TILE_W - border, y, border, TILE_H); // راست
    }

    /**
     * آیکون همراه اشاره‌گر موس در حالت‌های کاشت/برداشت/غذادهی: طبق سند، حالت
     * بیکار گیاه (هنگام کاشت)، نماد بیلچه (هنگام برداشت) یا نماد برگ/غذای گیاه
     * (هنگام غذادهی) باید روی/به‌جای نشانگر موس نمایش داده شود. این متد بعد از
     * رسم کامل صحنه و UI فراخوانی می‌شود تا همیشه روی همه‌چیز دیده شود.
     */
    private void drawCursorFollower(com.badlogic.gdx.graphics.g2d.Batch batch) {
        String iconPath = null;
        if (shovelSelected) {
            iconPath = AssetPaths.ICON_SHOVEL;
        } else if (plantFoodSelected) {
            iconPath = AssetPaths.ICON_PLANT_FOOD_LEAF;
        } else if (selectedPlantToPlant != null) {
            iconPath = AssetPaths.plantIcon(selectedPlantToPlant);
        }
        if (iconPath == null || iconPath.isEmpty()) {
            return;
        }
        com.badlogic.gdx.math.Vector2 p = currentPointerStageCoords();
        float size = 56f;
        TextureRegion tex = ImageUtils.loadRegion(iconPath);
        // === رفع باگ: کش‌شدن آیکون دنبال‌کننده‌ی موس ===
        // قبلاً این آیکون هم بدون حفظ نسبت ابعاد داخل مربع ۵۶×۵۶ کش می‌شد.
        // چون این یک آیکونِ شناور کنار موس است (نه گیاهی ایستاده روی خانه)،
        // باید کاملاً وسط‌چین (افقی و عمودی) شود، نه چسبیده به کف مثل
        // drawFitted؛ به همین دلیل این‌جا محاسبه مستقیماً انجام می‌شود.
        float texW = tex.getRegionWidth();
        float texH = tex.getRegionHeight();
        if (texW > 0 && texH > 0) {
            float scale = Math.min(size / texW, size / texH);
            float drawW = texW * scale;
            float drawH = texH * scale;
            batch.draw(tex, p.x - drawW / 2f, p.y - drawH / 2f, drawW, drawH);
        } else {
            batch.draw(tex, p.x - size / 2f, p.y - size / 2f, size, size);
        }
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
     * شبکه‌بندی زمین با خطوط قرمزرنگ (طبق تنظیمات کاربر در منوی تنظیمات:
     * «نمایش شبکه‌بندی زمین»). فقط وقتی این چک‌باکس فعال باشد رسم می‌شود.
     */
    private void drawNetworkGridLines(com.badlogic.gdx.graphics.g2d.Batch batch) {
        if (!session.getUser().isShowNetworkGrid()) {
            return;
        }
        batch.setColor(Color.RED);
        float lineThickness = 2f;
        for (int c = 0; c <= Board.COLS; c++) {
            float x = tileX(0) + c * TILE_W;
            batch.draw(skin.getRegion("white"), x, tileY(Board.ROWS - 1), lineThickness, TILE_H * Board.ROWS);
        }
        for (int r = 0; r <= Board.ROWS; r++) {
            float y = tileY(Board.ROWS - 1) + r * TILE_H;
            batch.draw(skin.getRegion("white"), tileX(0), y, TILE_W * Board.COLS, lineThickness);
        }
        batch.setColor(Color.WHITE);
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
        lawnMowers.sync(board, new LawnMowerVisualManager.TileMapper() {
            public float x(int col) { return tileX(col); }
            public float y(int row) { return tileY(row); }
        });
        lawnMowers.update(Gdx.graphics.getDeltaTime());
        lawnMowers.draw((com.badlogic.gdx.graphics.g2d.SpriteBatch) batch);
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
                // --- گرافیک گیاه ---
                // نکته‌ی مهم: منابع رسمی این پروژه (atlases/PLANT*.atlas) یک
                // تصویر کامل و یک‌تکه از هر گیاه ندارند؛ هرکدام ده‌ها ریجن
                // بسیار کوچک هستند (پرزهای گلبرگ، ساقه، برگ و ...) که در بازی
                // اصلی توسط یک اسکلت Spine (که فایل‌های .json آن اینجا موجود
                // نیست) سرهم و انیمیت می‌شوند. رسم مستقیم یکی از آن ریجن‌های
                // خرد (که AssetPaths.plantIcon قبلاً برمی‌گرداند) یعنی فقط یک
                // تکه‌ی بریده از گیاه با کش‌دادن روی کل کاشی نمایش داده شود —
                // دقیقاً همان چیزی که باعث می‌شد گیاه‌ها «نصفه» و «یک سطر
                // بالاتر از جای خودشان» به‌نظر برسند.
                // در عوض، از تصویرِ کاملِ بسته‌بذر (AssetPaths.plantSeedPacket)
                // استفاده می‌کنیم که یک ریجن کامل و بدون بُرش (untrimmed) برای
                // هر گیاه است؛ به‌علاوه با drawFitted نسبت ابعاد آن حفظ و به
                // کف کاشی چسبانده می‌شود (نه کش‌دادن کج‌وکوله به کل کاشی).
                String path = AssetPaths.plantSeedPacket(plant.getName());
                TextureRegion tex = ImageUtils.loadRegion(path);
                drawFitted(batch, tex, tileX(c) + 8f, tileY(r) + 6f, TILE_W - 16f, TILE_H - 12f);

                // نشانگر خورشیدِ آماده‌ی برداشت روی گیاهان تولیدکننده‌ی خورشید (مثل
                // آفتابگردان): قبلاً یک بج ۳۲×۳۲ در گوشه بود که به‌سختی دیده
                // می‌شد؛ الان بزرگ‌تر و وسط-بالای کاشی با کمی «تپش» (pulse)
                // نمایش داده می‌شود تا کاملاً مشخص باشد که باید رویش کلیک شود
                // (علاوه بر toast و صدای زنگی که در checkSunProducerReadyNotifications
                // یک‌بار وقتی آماده می‌شود پخش می‌شود).
                if (plant instanceof model.plant.interfaces.ISunProducer) {
                    model.plant.interfaces.ISunProducer producer = (model.plant.interfaces.ISunProducer) plant;
                    if (producer.isSunReady()) {
                        TextureRegion sunTex = ImageUtils.loadRegion(AssetPaths.SUN_NORMAL);
                        float pulse = 1f + 0.12f * (float) Math.sin(session.getTickCount() * 0.35);
                        float badgeSize = 40f * pulse;
                        float badgeX = tileX(c) + TILE_W / 2f - badgeSize / 2f;
                        float badgeY = tileY(r) + TILE_H - badgeSize + 6f;
                        batch.draw(sunTex, badgeX, badgeY, badgeSize, badgeSize);
                    }
                }

                // گیاه یخ‌زده: طبق سند، خود گیاه باید داخل بلوک یخ دیده شود (نه محو
                // یا فقط رنگ‌عوض‌شده)؛ پس روی همان اسپرایت گیاه، پوسته‌ی نیمه‌شفاف
                // یخ کشیده می‌شود.
                if (plant.isFrozenSolid()) {
                    TextureRegion iceOverlay = ImageUtils.loadRegion(AssetPaths.FROZEN_PLANT_ICE_OVERLAY);
                    batch.draw(iceOverlay, tileX(c) + 2f, tileY(r) + 2f, TILE_W - 4f, TILE_H - 4f);
                }

                // گیاه اختاپوس‌زده: طبق سند، روی گیاهانی که اختاپوس روی آن‌ها قرار
                // دارد (مانند یخ روی گیاهان یخ‌زده)، یک اختاپوس نمایش داده می‌شود.
                if (plant.isOctopused()) {
                    TextureRegion octopus = ImageUtils.loadRegion(AssetPaths.OCTOPUS_ON_PLANT);
                    batch.draw(octopus, tileX(c) + 4f, tileY(r) + 4f, TILE_W - 8f, TILE_H - 8f);
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
        zombieVisuals.sync(session.getAliveZombies(), new ZombieVisualManager.TileMapper() {
            public float x(double col) { return tileX(0) + (float) col * TILE_W; }
            public float y(int row) { return tileY(row); }
        });
        zombieVisuals.update(Gdx.graphics.getDeltaTime());
        PamAssets.get().update();
        zombieVisuals.draw((com.badlogic.gdx.graphics.g2d.SpriteBatch) batch);

        for (Zombie z : session.getAliveZombies()) {
            float x = tileX(0) + (float) z.getXPosition() * TILE_W;
            float y = tileY(z.getRow());

            if (z.getFrozenTicks() > 0) {
                TextureRegion iceBlock = ImageUtils.loadRegion(AssetPaths.FROZEN_ZOMBIE_ICE_BLOCK);
                batch.draw(iceBlock, x, y, TILE_W - 10f, TILE_H - 10f);
                continue;
            }

            if (z.isSpawnedByTornado() && session.getTickCount() - z.getSpawnTick() < 15) {
                TextureRegion tornado = ImageUtils.loadRegion(AssetPaths.TORNADO_EFFECT);
                batch.draw(tornado, x - 10f, y - 10f, TILE_W + 10f, TILE_H + 10f);
            }

            drawHealthBar(batch, x, y + TILE_H - 16f, TILE_W - 10f, z.getHealth() / (float) z.getMaxHealth());

            if (z.getXPosition() < 1.0) {
                batch.setColor(1f, 0.4f, 0.4f, 1f);
                batch.draw(ImageUtils.loadRegion(AssetPaths.zombieIcon(z.getTypeName())), x + 5f, y, TILE_W - 10f, TILE_H - 10f);
                batch.setColor(Color.WHITE);
            }
        }
    }

    private void drawProjectiles(com.badlogic.gdx.graphics.g2d.Batch batch) {
        for (Projectile p : session.getActiveProjectiles()) {
            if (p.isDead()) {
                continue;
            }
            String path = projectileTexturePath(p);
            TextureRegion tex = ImageUtils.loadRegion(path);
            float x = tileX(0) + (float) p.getX() * TILE_W;
            float y = tileY(p.getRow()) + TILE_H / 2f - 8f;
            batch.draw(tex, x, y, 20f, 20f);
        }
    }

    /**
     * انتخاب اسپرایت مناسب هر پرتابه، طبق دسته‌بندی سند: پرتابه‌ی معمولی/آتشین/
     * یخی، Pepper-pult (مساحتی)، هندوانه/هندوانه‌ی یخی (مساحتی)، و توده دود/خار
     * کاکتوس که از موانع عبور می‌کنند (StrikeThroughProjectile).
     */
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

    /** تبدیل مختصات صفحه (پیکسل واقعی پنجره) به مختصات صحنه (Stage)؛ برای هاور و کلیک هر دو استفاده می‌شود. */
    private com.badlogic.gdx.math.Vector2 currentPointerStageCoords() {
        return stage.screenToStageCoordinates(
                new com.badlogic.gdx.math.Vector2(Gdx.input.getX(), Gdx.input.getY()));
    }

    /** سطر/ستونِ خانه‌ای که اشاره‌گر موس همین الان روی آن است؛ اگر خارج از تخته باشد null برمی‌گرداند. */
    private int[] hoveredTile() {
        com.badlogic.gdx.math.Vector2 p = currentPointerStageCoords();
        int col = (int) ((p.x - BOARD_LEFT) / TILE_W);
        int row = (int) ((BOARD_TOP - p.y) / TILE_H);
        if (col < 0 || col >= Board.COLS || row < 0 || row >= Board.ROWS) {
            return null;
        }
        return new int[]{row, col};
    }

    private void handleBoardClick() {
        if (!Gdx.input.justTouched()) {
            return;
        }
        com.badlogic.gdx.math.Vector2 touch = currentPointerStageCoords();

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

        int[] hovered = hoveredTile();
        if (hovered == null) {
            return; // کلیک خارج از تخته بوده (مثلاً روی نوار کناری)
        }
        int row = hovered[0];
        int col = hovered[1];

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
