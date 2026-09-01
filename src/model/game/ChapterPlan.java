package model.game;

import model.levelrules.LevelMode;

/**
 * منبع واحد حقیقت برای «کدام فصل، کدام مرحله، چه فصل بازی (Season) و چه نوع
 * مرحله‌ (LevelMode) دارد» — هم توسط کنترلر کنسول (AppController) و هم توسط
 * لایه‌ی گرافیکی (PvZGame) استفاده می‌شود تا هر دو دقیقاً یک رفتار داشته باشند.
 *
 * طبق سند فاز یک:
 *  - علاوه بر ۴ فصل اصلی (مصر باستان، غارهای یخی، ساحل موج بزرگ، قرون وسطی)،
 *    یک فصل «Beginner» (شماره‌ی ۰) وجود دارد که هیچ قانون خاصی ندارد و مراحل
 *    ویژه هم در آن نیست (هر ۴ مرحله‌اش NORMAL هستند).
 *  - هر یک از فصل‌های ۱ تا ۴ دقیقاً ۴ مرحله دارد: مرحله‌ی ۱ و ۴ عادی هستند،
 *    مرحله‌ی ۲ و ۳ «مراحل ویژه»اند. در کل بازی دقیقاً ۸ مرحله‌ی ویژه وجود دارد
 *    (۴ فصل × ۲ اسلات) که هر یک از ۸ نوع مرحله‌ی ویژه‌ی موجود (LevelMode) دقیقاً
 *    یک‌بار در این ۸ اسلات به‌کار می‌رود.
 *  - هر مرحله‌ی بعدی در همان فصل باید از مرحله‌ی قبلی سخت‌تر باشد.
 */
public final class ChapterPlan {

    public static final int BEGINNER_CHAPTER = 0;
    public static final int FIRST_REAL_CHAPTER = 1;
    public static final int LAST_CHAPTER = 4;
    public static final int LEVELS_PER_CHAPTER = 4;

    private ChapterPlan() {
    }

    /**
     * انتساب ثابت (طراحی‌شده یک‌بار، نه به‌ازای هر کاربر) ۸ نوع مرحله‌ی ویژه به
     * جایگاه‌های (فصل، مرحله) که مرحله‌ی ۲ یا ۳ آن‌ها هستند. ترتیب هشت‌گانه‌ی زیر
     * با model.levelrules.LevelMode.values() (به‌جز NORMAL) یک‌به‌یک منطبق است؛
     * چون این آرایه به‌صورت shuffle ثابت (نه رندوم در هر اجرا) تعریف شده،
     * نتیجه‌ی بازی برای همه‌ی کاربران یکسان و قابل پیش‌بینی/تست است.
     */
    private static final LevelMode[][] SPECIAL_ASSIGNMENT = {
            // فصل ۱ (مصر باستان): مرحله ۲، مرحله ۳
            {LevelMode.CONVEYOR_BELT, LevelMode.DEAD_LINE},
            // فصل ۲ (غارهای یخی): مرحله ۲، مرحله ۳
            {LevelMode.LOCKED_PLANTS, LevelMode.SAVE_OUR_SEEDS},
            // فصل ۳ (ساحل موج بزرگ): مرحله ۲، مرحله ۳
            {LevelMode.LOVE_YOUR_PLANTS, LevelMode.PLANT_WHAT_YOU_GET},
            // فصل ۴ (قرون وسطی): مرحله ۲، مرحله ۳
            {LevelMode.TIMED_WAR, LevelMode.NIGHT_OPS},
    };

    /** فصل شماره‌ی chapter را به فصل بازی (Season) نگاشت می‌کند. Beginner همیشه NORMAL است. */
    public static Season seasonFor(int chapter) {
        switch (chapter) {
            case 1: return Season.ANCIENT_EGYPT;
            case 2: return Season.FROSTBITE_CAVES;
            case 3: return Season.BIG_WAVE_BEACH;
            case 4: return Season.DARK_AGES;
            default: return Season.NORMAL; // Beginner یا هر مقدار نامعتبر
        }
    }

    /** نوع مرحله (عادی یا یکی از ۸ مرحله‌ی ویژه) را برای (فصل، مرحله) داده‌شده برمی‌گرداند. */
    public static LevelMode levelModeFor(int chapter, int level) {
        if (chapter < FIRST_REAL_CHAPTER || chapter > LAST_CHAPTER) {
            return LevelMode.NORMAL; // فصل Beginner یا نامعتبر: همیشه عادی
        }
        if (level == 2) {
            return SPECIAL_ASSIGNMENT[chapter - 1][0];
        }
        if (level == 3) {
            return SPECIAL_ASSIGNMENT[chapter - 1][1];
        }
        return LevelMode.NORMAL; // مرحله‌ی ۱ و ۴ همیشه عادی‌اند
    }

    /**
     * تعداد موج‌های مرحله؛ هر مرحله از مرحله‌ی قبلی همان فصل سخت‌تر است
     * (تعداد موج بیشتر). فصل Beginner ساده‌ترین است.
     */
    public static int totalWavesFor(int chapter, int level) {
        int base = (chapter == BEGINNER_CHAPTER) ? 3 : 5;
        return base + (Math.max(1, Math.min(level, LEVELS_PER_CHAPTER)) - 1); // +0..+3
    }

    /**
     * هزینه‌ی موج اول؛ هر مرحله از مرحله‌ی قبلی همان فصل سخت‌تر است (هزینه‌ی
     * موج اول بیشتر یعنی زامبی‌های بیشتر/قوی‌تر از همان ابتدا).
     */
    public static double baseWaveCostFor(int chapter, int level) {
        double base = (chapter == BEGINNER_CHAPTER) ? 30 : 50;
        int lvl = Math.max(1, Math.min(level, LEVELS_PER_CHAPTER));
        return base + (lvl - 1) * 15.0; // +0, +15, +30, +45
    }

    /** آیا شماره‌ی فصل داده‌شده معتبر است؟ (۰ = Beginner تا ۴) */
    public static boolean isValidChapter(int chapter) {
        return chapter >= BEGINNER_CHAPTER && chapter <= LAST_CHAPTER;
    }

    /** آیا شماره‌ی مرحله در بازه‌ی مجاز هر فصل (۱ تا ۴) است؟ */
    public static boolean isValidLevel(int level) {
        return level >= 1 && level <= LEVELS_PER_CHAPTER;
    }

    /**
     * آیا فصل داده‌شده برای این کاربر باز (Unlock) است؟ فصل Beginner همیشه باز
     * است؛ فصل N>0 وقتی باز می‌شود که فصل قبلی آن (N-1) به‌طور کامل (هر ۴
     * مرحله) تمام شده باشد.
     */
    public static boolean isChapterUnlocked(model.user.User user, int chapter) {
        if (!isValidChapter(chapter)) {
            return false;
        }
        if (chapter == BEGINNER_CHAPTER) {
            return true;
        }
        return user.getLastCompletedChapter() >= chapter - 1;
    }

    /**
     * آیا مرحله‌ی level از فصل chapter برای این کاربر باز است؟ مرحله‌ی ۱ هر
     * فصلِ بازشده همیشه باز است؛ مرحله‌ی L>1 وقتی باز می‌شود که مرحله‌ی L-1
     * همان فصل قبلاً تمام شده باشد.
     */
    public static boolean isLevelUnlocked(model.user.User user, int chapter, int level) {
        if (!isChapterUnlocked(user, chapter) || !isValidLevel(level)) {
            return false;
        }
        if (level == 1) {
            return true;
        }
        if (chapter == BEGINNER_CHAPTER) {
            return level <= user.getBeginnerLastCompletedLevel() + 1;
        }
        // اگر این فصل قبلاً به‌طور کامل تمام شده باشد (lastCompletedChapter >= chapter)،
        // یعنی همه‌ی ۴ مرحله‌اش باز است. در غیر این صورت، فقط اگر همین الان
        // «فصل در حال پیشرفت» کاربر همین فصل باشد (lastCompletedChapter == chapter-1)،
        // مقدار lastCompletedLevel معنا دارد.
        if (user.getLastCompletedChapter() >= chapter) {
            return true;
        }
        if (user.getLastCompletedChapter() == chapter - 1) {
            return level <= user.getLastCompletedLevel() + 1;
        }
        return false;
    }

    /** نام نمایشی فصل (برای پیام‌ها/HUD). */
    public static String displayName(int chapter) {
        switch (chapter) {
            case BEGINNER_CHAPTER: return "Beginner";
            case 1: return "Ancient Egypt";
            case 2: return "Frostbite Caves";
            case 3: return "Big Wave Beach";
            case 4: return "Dark Ages";
            default: return "Unknown";
        }
    }

    /**
     * ستون خط مرگ برای مُد «ددلاین»؛ در مرحله‌ی ۳ (سخت‌تر) نسبت به مرحله‌ی ۲
     * ستون بزرگ‌تری داده می‌شود تا فضای کمتری برای واکنش باقی بماند... در واقع
     * برعکس: عدد ستون کوچک‌تر یعنی خط نزدیک‌تر به خانه‌ی بازیکن و فرصت کمتر؛
     * پس برای سخت‌تر شدن، ستون را کوچک‌تر می‌دهیم.
     */
    public static int deadLineColumnFor(int level) {
        return (level == 3) ? 1 : 2;
    }

    /** حداکثر گیاهان مجاز هم‌زمان برای مُد «از دست نده»؛ مرحله‌ی سخت‌تر سقف کمتری دارد. */
    public static int loveYourPlantsMaxFor(int level) {
        return (level == 3) ? 10 : 15;
    }

    /** خورشید اولیه‌ی ثابت برای مُد «هرچه رسد بکار»؛ مرحله‌ی سخت‌تر خورشید کمتری می‌دهد. */
    public static int plantWhatYouGetStartingSunFor(int level) {
        return (level == 3) ? 500 : 800;
    }

    /** خورشید کمکی اولیه برای مُد «شب عملیات»؛ مرحله‌ی سخت‌تر کمک کمتری می‌دهد. */
    public static int nightOpsStartingSunFor(int level) {
        return (level == 3) ? 50 : 100;
    }

    /** فاصله‌ی زمانی (بر حسب تیک) تولید گیاه جدید روی نوار در مُد «نوار کناری»؛ مرحله‌ی سخت‌تر سریع‌تر است. */
    public static int conveyorBeltSpeedTicksFor(int level) {
        return (level == 3) ? 90 : 130;
    }

    /** هدف و زمان مُد «نبرد زمان‌دار»: مرحله‌ی ۲ کشتن زامبی، مرحله‌ی ۳ تولید خورشید (طبق دو مثال سند). */
    public static model.levelrules.TimedWarRules.Objective timedWarObjectiveFor(int level) {
        return (level == 3) ? model.levelrules.TimedWarRules.Objective.PRODUCE_SUN
                             : model.levelrules.TimedWarRules.Objective.KILL_ZOMBIES;
    }

    public static int timedWarTargetFor(int level) {
        return (level == 3) ? 3000 : 10; // ۳۰۰۰ خورشید یا ۱۰ زامبی، طبق مثال‌های سند
    }

    public static int timedWarSecondsFor(int level) {
        return (level == 3) ? 60 : 5; // طبق مثال سند: ۱۰ زامبی در ۵ ثانیه
    }

    /** توضیح کوتاه هر مُد ویژه، برای نمایش در صفحه‌ی «آغاز مرحله» قبل از شروع بازی. */
    public static java.util.List<String> objectivesFor(int chapter, int level) {
        LevelMode mode = levelModeFor(chapter, level);
        switch (mode) {
            case CONVEYOR_BELT:
                return java.util.List.of("Plants arrive randomly on a conveyor belt — plant them before they scroll past.");
            case LOCKED_PLANTS:
                return java.util.List.of("Only a limited set of plants is available in this level.");
            case SAVE_OUR_SEEDS:
                return java.util.List.of("Protect the marked plants — losing even one means defeat.");
            case TIMED_WAR:
                return java.util.List.of(timedWarObjectiveFor(level) == model.levelrules.TimedWarRules.Objective.PRODUCE_SUN
                        ? ("Produce " + timedWarTargetFor(level) + " sun within " + timedWarSecondsFor(level) + " seconds.")
                        : ("Kill " + timedWarTargetFor(level) + " zombies within " + timedWarSecondsFor(level) + " seconds."));
            case NIGHT_OPS:
                return java.util.List.of("No sun falls from the sky — rely on sun-producing plants.");
            case DEAD_LINE:
                return java.util.List.of("Don't let any zombie cross the marked line.");
            case LOVE_YOUR_PLANTS:
                return java.util.List.of("Keep no more than " + loveYourPlantsMaxFor(level) + " plants on the lawn at once.");
            case PLANT_WHAT_YOU_GET:
                return java.util.List.of("You start with " + plantWhatYouGetStartingSunFor(level)
                        + " sun and no more falls. Plant freely, then start the waves yourself.");
            case NORMAL:
            default:
                return java.util.Collections.emptyList();
        }
    }
}
