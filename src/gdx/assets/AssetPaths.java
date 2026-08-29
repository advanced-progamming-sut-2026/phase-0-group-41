package gdx.assets;

/**
 * تمام مسیرهای فایل‌های گرافیکی (Assets) در این کلاس نگه‌داری می‌شوند.
 * مقدار هر رشته را با آدرس فایل واقعی (نسبت به پوشه‌ی assets/) پر کنید.
 * مثال: public static final String MAIN_MENU_BG = "backgrounds/main_menu_bg.png";
 *
 * تا وقتی مقداردهی نشده‌اند، مقدار آن‌ها رشته‌ی خالی "" است.
 * کلاس ImageUtils در صورت خالی بودن مسیر، به‌جای بارگذاری تصویر
 * یک مستطیل رنگی جایگزین (Placeholder) رسم می‌کند تا برنامه کرش نکند.
 */
public final class AssetPaths {

    private AssetPaths() {
    }

    // ==================== فونت‌ها ====================
    public static final String FONT_DEFAULT = "";
    public static final String FONT_TITLE = "";
    public static final String UI_SKIN_JSON = "";
    public static final String UI_SKIN_ATLAS = "";

    // ==================== پس‌زمینه‌ها ====================
    public static final String BG_REGISTER = "";
    public static final String BG_LOGIN = "";
    public static final String BG_MAIN_MENU = "backgrounds/mainmenu_bg.png";
    public static final String BG_SETTINGS = "";
    public static final String BG_NEWS = "";
    public static final String BG_PROFILE = "";
    public static final String BG_SHOP = "";
    public static final String BG_QUEST = "";
    public static final String BG_LEADERBOARD = "";
    public static final String BG_GREENHOUSE = "backgrounds/greenhouse_bg.png";
    public static final String BG_PLANT_SELECTION = "";
    public static final String BG_CHAPTER_SELECT = "";
    public static final String BG_LEVEL_SELECT = "";
    public static final String BG_COLLECTION = "";

    // لوگوی رسمی Plants vs Zombies 2 هنوز آپلود نشده (فایل عکس لوگو مشکل داشت).
    // وقتی فایل لوگو رو جداگانه فرستادید، اینجا مسیرش رو بذارید، مثلاً: "backgrounds/pvz2_logo.png"
    public static final String LOGO_PVZ2 = "";

    // پس‌زمینه‌ی عمومی کارت‌ها (گیاه/آیتم) در فروشگاه و انتخاب گیاه
    public static final String CARD_BACKGROUND = "ui/card_bg.png";

    // ==================== آیکون‌های نوار بالا (HUD) ====================
    public static final String ICON_COIN = "";
    public static final String ICON_DIAMOND = "icons/diamond.png";
    public static final String ICON_STAR = "";
    public static final String ICON_LOCK = "";
    public static final String ICON_NEWS_BELL = "";
    public static final String ICON_SETTINGS_GEAR = "";
    public static final String ICON_BACK_ARROW = "";
    public static final String ICON_LOGOUT = "";
    public static final String ICON_SUN = "";
    public static final String ICON_PLANT_FOOD = "";

    // ==================== دکمه‌ها ====================
    public static final String BTN_DEFAULT_UP = "";
    public static final String BTN_DEFAULT_DOWN = "";
    public static final String BTN_PLAY_UP = "";
    public static final String BTN_PLAY_DOWN = "";
    public static final String BTN_CLOSE = "";

    // ==================== گیاهان (کارت‌ها/آیکون بسته بذر) ====================
    // کلید = نام داخلی گیاه (همان چیزی که در PlantFactory استفاده می‌شود)
    // مقدار = مسیر تصویر seed packet مربوطه. خالی بگذارید تا خودتان پر کنید.
    public static String plantIcon(String plantName) {
        // TODO: نگاشت نام گیاه به مسیر تصویر را اینجا تکمیل کنید
        return "";
    }

    // ==================== زامبی‌ها ====================
    public static String zombieIcon(String zombieName) {
        // TODO: نگاشت نام زامبی به مسیر تصویر را اینجا تکمیل کنید
        return "";
    }

    // ==================== گلخانه ====================
    public static final String GREENHOUSE_POT_EMPTY = "";
    public static final String GREENHOUSE_POT_LOCKED = "";
    public static final String GREENHOUSE_POT_GROWING = "";
    public static final String GREENHOUSE_POT_READY = "";

    // ==================== صدا / موسیقی ====================
    public static final String MUSIC_MENU = "";
    public static final String SFX_CLICK = "";
    public static final String SFX_ERROR = "";
}
