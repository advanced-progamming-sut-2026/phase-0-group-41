package model.quest;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

public class QuestFactory {

    // ==================== موجود ====================

    public static Quest createUnlockPlantQuest() {
        return new Quest(
                "critical_unlock_peashooter",
                "Meet the Peashooter",
                "Unlock your first plant, the Peashooter",
                QuestPriority.CRITICAL,
                QuestPage.ADVENTURE,
                false,
                context -> context.getStagesCompleted() >= 1,
                Collections.singletonList(QuestReward.unlockable("peashooter"))
        );
    }

    public static Quest createEpicChallengeQuest() {
        return new Quest(
                "high_kill_500_zombies",
                "Zombie Slayer",
                "Defeat 500 zombies throughout the game",
                QuestPriority.HIGH,
                QuestPage.CHALLENGE,
                false,
                context -> context.getZombiesKilled() >= 500,
                Collections.singletonList(QuestReward.currency(QuestReward.CurrencyType.GEM, 15))
        );
    }

    public static Quest createDailyLoginQuest() {
        return new Quest(
                "daily_login",
                "Daily Visit",
                "Log into the game today",
                QuestPriority.DAILY,
                QuestPage.ADVENTURE,
                true,
                QuestContext::isLoggedInToday,
                Arrays.asList(
                        QuestReward.currency(QuestReward.CurrencyType.COIN, 200),
                        QuestReward.inventory("seed_packet_random", 1)
                )
        );
    }

    // ==================== آفتاب گیر روزانه ====================
    // sun_amount / 100 سکه — چند سطح 3000-4000-5000

    public static Quest createDailySunCollectorQuest(String id, int sunTarget, int coinReward) {
        return new Quest(
                id,
                "Sun Collector",
                "Collect " + sunTarget + " sun in a single day",
                QuestPriority.MEDIUM,
                QuestPage.ADVENTURE,
                true,
                context -> context.getSunAmountCollectedToday() >= sunTarget,
                Collections.singletonList(QuestReward.currency(QuestReward.CurrencyType.COIN, coinReward))
        );
    }

    public static List<Quest> createDailySunCollectorQuests() {
        List<Quest> quests = new ArrayList<>();
        quests.add(createDailySunCollectorQuest("daily_sun_3000", 3000, 30));
        quests.add(createDailySunCollectorQuest("daily_sun_4000", 4000, 40));
        quests.add(createDailySunCollectorQuest("daily_sun_5000", 5000, 50));
        return quests;
    }

    // ==================== شکارچی chapter ====================
    // شکست دادن 50 زامبی از فصل chapter - یک دانه برای هر chapter

    public static Quest createChapterHunterQuest(int chapterNumber) {
        return new Quest(
                "main_chapter_hunter_" + chapterNumber,
                "Chapter Hunter " + chapterNumber,
                "Defeat 50 zombies from chapter " + chapterNumber,
                QuestPriority.HIGH,
                QuestPage.MAIN,
                false,
                context -> context.getZombiesKilledInChapter(chapterNumber) >= 50,
                Collections.singletonList(QuestReward.inventory("seed_packet_random", 1))
        );
    }

    public static List<Quest> createChapterHunterQuests(int totalChapters) {
        List<Quest> quests = new ArrayList<>();
        for (int i = 1; i <= totalChapters; i++) {
            quests.add(createChapterHunterQuest(i));
        }
        return quests;
    }

    // ==================== plant باز حرفه‌ای ====================
    // ده تا زامبی را فقط با plant بکش — گیاه جدید شناسی

    public static Quest createPlantSpecialistQuest(String plantId) {
        return new Quest(
                "daily_plant_specialist_" + plantId,
                "Plant Specialist",
                "Kill ten zombies using only " + plantId,
                QuestPriority.HIGH,
                QuestPage.ADVENTURE,
                true,
                context -> context.getZombiesKilledOnlyBy(plantId) >= 10,
                Collections.singletonList(QuestReward.unlockable("random_new_plant"))
        );
    }

    // ==================== only cactus ====================
    // ده تا زامبی را فقط با کاکتوس بکش

    public static Quest createOnlyCactusQuest() {
        return new Quest(
                "daily_only_cactus",
                "Cactus Only",
                "Kill ten zombies using only the Cactus",
                QuestPriority.HIGH,
                QuestPage.ADVENTURE,
                true,
                context -> context.getZombiesKilledOnlyBy("cactus") >= 10,
                Collections.singletonList(QuestReward.currency(QuestReward.CurrencyType.GEM, 20))
        );
    }

    // ==================== گیاه خوار اقتصادی ====================
    // پیروزی در یک مرحله بدون از دست دادن بیش از n گیاه — سطوح 0 تا 5

    public static Quest createEconomicPlantEaterQuest(int maxPlantsLost) {
        return new Quest(
                "main_economic_plant_eater_" + maxPlantsLost,
                "Economical Gardener",
                "Win a level while losing at most " + maxPlantsLost + " plants",
                QuestPriority.HIGH,
                QuestPage.MAIN,
                false,
                context -> context.isStageWonWithMaxPlantsLost(maxPlantsLost),
                Collections.singletonList(QuestReward.currency(QuestReward.CurrencyType.COIN, 500))
        );
    }

    public static List<Quest> createEconomicPlantEaterQuests() {
        List<Quest> quests = new ArrayList<>();
        for (int i = 0; i <= 5; i++) {
            quests.add(createEconomicPlantEaterQuest(i));
        }
        return quests;
    }

    // ==================== استاد دفاع ====================
    // اتمام یک مرحله دقیقا با صفر خورشید — CRITICAL priority

    public static Quest createMasterDefenderQuest() {
        return new Quest(
                "epic_master_defender",
                "Master Defender",
                "Finish a level with exactly zero sun",
                QuestPriority.CRITICAL,
                QuestPage.CHALLENGE,
                false,
                context -> context.isStageWonWithExactSun(0),
                Collections.singletonList(QuestReward.currency(QuestReward.CurrencyType.GEM, 200))
        );
    }

    // ==================== سرعت عمل ====================
    // کشتن 10 زامبی در کمتر از 30 ثانیه از شروع موج اول حمله زامبی‌ها

    public static Quest createQuickActionQuest() {
        return new Quest(
                "main_quick_action",
                "Quick Action",
                "Kill 10 zombies within 30 seconds of the first wave starting",
                QuestPriority.MEDIUM,
                QuestPage.MAIN,
                false,
                context -> context.getZombiesKilledWithinSecondsOfFirstWave(30) >= 10,
                Collections.singletonList(QuestReward.currency(QuestReward.CurrencyType.COIN, 500))
        );
    }

    // ==================== تخریب گر حرفه‌ای ====================
    // استفاده از 3 گیاه انفجاری در یک مرحله

    public static Quest createDemolitionExpertQuest() {
        return new Quest(
                "daily_demolition_expert",
                "Demolition Expert",
                "Use 3 explosive plants in a single level",
                QuestPriority.LOW,
                QuestPage.ADVENTURE,
                true,
                context -> context.getExplosivePlantsUsedInStage() >= 3,
                Collections.singletonList(QuestReward.currency(QuestReward.CurrencyType.COIN, 100))
        );
    }

    // ==================== تقارن ====================
    // باغچه بازی در نهایت باید متقارن باشد

    public static Quest createSymmetryQuest() {
        return new Quest(
                "daily_symmetry",
                "Symmetry",
                "The lawn must end up symmetrical",
                QuestPriority.HIGH,
                QuestPage.ADVENTURE,
                true,
                context -> context.isGardenSymmetricalAtEnd(),
                Collections.singletonList(QuestReward.currency(QuestReward.CurrencyType.COIN, 500))
        );
    }

    // ==================== کشتار خانوادگی ====================
    // تنها از گیاهان family_type برای کشتن زامبی‌ها استفاده شود

    public static Quest createFamilyMassacreQuest(String familyType) {
        return new Quest(
                "daily_family_massacre_" + familyType,
                "Family Massacre",
                "Kill zombies using only " + familyType + " plants",
                QuestPriority.MEDIUM,
                QuestPage.ADVENTURE,
                true,
                context -> context.wereAllKillsByFamily(familyType),
                Collections.singletonList(QuestReward.currency(QuestReward.CurrencyType.COIN, 1000))
        );
    }

    // ==================== شکوفایی در محدودیت‌ها ====================
    // برای برد در مرحله از هیچ گیاهی از خانواده family_type استفاده نشود

    public static Quest createBloomInLimitationsQuest(String forbiddenFamilyType) {
        return new Quest(
                "daily_bloom_in_limitations_" + forbiddenFamilyType,
                "Bloom In Limitations",
                "Win the level without using any " + forbiddenFamilyType + " plants",
                QuestPriority.HIGH,
                QuestPage.ADVENTURE,
                true,
                context -> context.wasStageWonWithoutFamily(forbiddenFamilyType),
                Collections.singletonList(QuestReward.currency(QuestReward.CurrencyType.GEM, 100))
        );
    }

    // ==================== شب یا صبح ====================
    // به پایان رساندن بازی روز با گیاهان mushroom ها (شب)

    public static Quest createNightOwlQuest() {
        return new Quest(
                "epic_night_owl",
                "Night Owl",
                "Finish a day level using only mushroom plants (night)",
                QuestPriority.HIGH,
                QuestPage.CHALLENGE,
                false,
                context -> context.wasStageWonUsingOnlyFamily("mushroom") && context.isNightLevel(),
                Collections.singletonList(QuestReward.currency(QuestReward.CurrencyType.GEM, 20))
        );
    }

    // ==================== برد پشت برد ====================
    // 5 مرحله را پشت سر هم با بیشترین سختی ببر

    public static Quest createWinStreakQuest() {
        return new Quest(
                "daily_win_streak",
                "Win Streak",
                "Win 5 consecutive levels on the hardest difficulty",
                QuestPriority.MEDIUM,
                QuestPage.ADVENTURE,
                true,
                context -> context.getConsecutiveHardestDifficultyWins() >= 5,
                Collections.singletonList(QuestReward.currency(QuestReward.CurrencyType.COIN, 5000))
        );
    }

    // ==================== تقریبا پیروز ====================
    // 10 زامبی را در ستون اول ردیفی بکش که چمن‌زن ندارد

    public static Quest createAlmostWinnerQuest() {
        return new Quest(
                "daily_almost_winner",
                "Almost Winner",
                "Kill 10 zombies in the first column of a row without a lawnmower",
                QuestPriority.MEDIUM,
                QuestPage.ADVENTURE,
                true,
                context -> context.getZombiesKilledInFirstColumnWithoutLawnmower() >= 10,
                Collections.singletonList(QuestReward.currency(QuestReward.CurrencyType.COIN, 300))
        );
    }

    // ==================== OCD نظم ====================
    // حالتی مرحله را ببر که هیچ تقارنی در باغچه وجود نداشت (به جز ردیف وسط)

    public static Quest createOCDQuest() {
        return new Quest(
                "daily_ocd",
                "OCD",
                "Win a level where the lawn has no symmetry (except the middle row)",
                QuestPriority.MEDIUM,
                QuestPage.ADVENTURE,
                true,
                context -> context.wasStageWonWithNoSymmetryExceptMiddleRow(),
                Collections.singletonList(QuestReward.currency(QuestReward.CurrencyType.COIN, 800))
        );
    }
    // ==================== کوئست‌های مینی‌گیم ====================

    public static Quest createMinigameQuest(String minigameId, String displayName) {
        return new Quest(
                "minigame_complete_" + minigameId,
                "Minigame Master: " + displayName,
                "Successfully complete the " + displayName + " minigame",
                QuestPriority.NORMAL,
                QuestPage.MINIGAMES,
                false,
                context -> context.getMiniGamesCompleted() >= 1, // یا شرط دقیق‌تر بر اساس آی‌دی مینی‌گیم
                Collections.singletonList(QuestReward.currency(QuestReward.CurrencyType.COIN, 300))
        );
    }

    public static List<Quest> createMinigameQuests() {
        List<Quest> quests = new ArrayList<>();
        quests.add(createMinigameQuest("vasebreaker", "Vasebreaker"));
        quests.add(createMinigameQuest("wallnut_bowling", "Wallnut Bowling"));
        quests.add(createMinigameQuest("i_zombie", "I, Zombie"));
        quests.add(createMinigameQuest("beghouled", "Beghouled"));
        quests.add(createMinigameQuest("zombotany", "Zombotany"));
        return quests;
    }
    // ==================== روز ابری ====================
    // یک مرحله را تنها با 3 تا گیاه تولیدکننده خورشید بزن

    public static Quest createCloudyDayQuest() {
        return new Quest(
                "daily_cloudy_day",
                "Cloudy Day",
                "Win a level using only 3 sun-producing plants",
                QuestPriority.HIGH,
                QuestPage.ADVENTURE,
                true,
                context -> context.wasStageWonWithMaxSunProducers(3),
                Collections.singletonList(QuestReward.currency(QuestReward.CurrencyType.GEM, 10))
        );
    }

    // ==================== به ستون کمتر ====================
    // یک مرحله را در صورتی ببرد که در ستون n ام گیاهی نکاشته باشد

    public static Quest createFewerColumnsQuest(int emptyColumnCount) {
        return new Quest(
                "daily_fewer_columns_" + emptyColumnCount,
                "Fewer Columns",
                "Win a level while leaving " + emptyColumnCount + " columns empty",
                QuestPriority.HIGH,
                QuestPage.ADVENTURE,
                true,
                context -> context.wasStageWonWithEmptyColumns(emptyColumnCount),
                Collections.singletonList(QuestReward.currency(QuestReward.CurrencyType.GEM, 10))
        );
    }

    // ==================== سطر بی دفاع ====================
    // یک مرحله را در صورتی ببرد که در سطر n ام هیچ گیاهی نکاشته باشد

    public static Quest createRowlessDefenseQuest(int emptyRowCount) {
        return new Quest(
                "daily_rowless_defense_" + emptyRowCount,
                "Rowless Defense",
                "Win a level while leaving " + emptyRowCount + " rows empty",
                QuestPriority.HIGH,
                QuestPage.ADVENTURE,
                true,
                context -> context.wasStageWonWithEmptyRows(emptyRowCount),
                Collections.singletonList(QuestReward.currency(QuestReward.CurrencyType.GEM, 20))
        );
    }

    // ==================== صلیب بی دفاع ====================
    // یک مرحله را در صورتی ببرد که ستون و ردیف n ام خالی باشد (صلیب)

    public static Quest createCrossDefenselessQuest() {
        return new Quest(
                "daily_cross_defenseless",
                "Cross Defenseless",
                "Win a level while leaving the center column and row empty",
                QuestPriority.HIGH,
                QuestPage.ADVENTURE,
                true,
                context -> context.wasStageWonWithEmptyCross(),
                Collections.singletonList(QuestReward.currency(QuestReward.CurrencyType.GEM, 25))
        );
    }

    // ==================== وقت چمن‌زنی ====================
    // حداقل n زامبی را با چمن‌زن بکش — سطوح 10 تا 50

    public static Quest createLawnmowerTimeQuest(String id, int zombiesTarget) {
        return new Quest(
                id,
                "Lawnmower Time",
                "Kill at least " + zombiesTarget + " zombies using a lawnmower",
                QuestPriority.MEDIUM,
                QuestPage.CHALLENGE,
                false,
                context -> context.getZombiesKilledByLawnmower() >= zombiesTarget,
                Collections.singletonList(QuestReward.currency(QuestReward.CurrencyType.GEM, zombiesTarget))
        );
    }

    public static List<Quest> createLawnmowerTimeQuests() {
        List<Quest> quests = new ArrayList<>();
        int[] targets = {10, 20, 30, 40, 50};
        for (int target : targets) {
            quests.add(createLawnmowerTimeQuest("epic_lawnmower_time_" + target, target));
        }
        return quests;
    }

    // ==================== جمع‌کننده کل ====================

    public static List<Quest> createDefaultQuests() {
        List<Quest> quests = new ArrayList<>();

        // اصلی
        // توجه: این کوئست قبلا دوبار اضافه می‌شد (دو Quest جدا با آیدی یکسان
        // "critical_unlock_peashooter"). چون claim بر اساس آیدی انجام می‌شود،
        // کلیک روی هرکدام از آن دو ردیف در UI باعث claim شدن همان یک کوئست
        // می‌شد و کلیک دوم همیشه خطای "قبلا دریافت شده" می‌داد.
        quests.add(createUnlockPlantQuest());
        quests.addAll(createEconomicPlantEaterQuests());
        quests.add(createQuickActionQuest());
        quests.addAll(createChapterHunterQuests(5)); // تعداد فصل‌ها را متناسب با بازی تنظیم کن

        // روزانه
        quests.add(createDailyLoginQuest());
        quests.addAll(createDailySunCollectorQuests());
        quests.add(createOnlyCactusQuest());
        quests.add(createDemolitionExpertQuest());
        quests.add(createSymmetryQuest());
        quests.add(createFamilyMassacreQuest("normal"));
        quests.add(createBloomInLimitationsQuest("mushroom"));
        quests.add(createWinStreakQuest());
        quests.add(createAlmostWinnerQuest());
        quests.add(createOCDQuest());
        quests.add(createCloudyDayQuest());
        quests.add(createFewerColumnsQuest(1));
        quests.add(createRowlessDefenseQuest(1));
        quests.add(createCrossDefenselessQuest());
        quests.addAll(createMinigameQuests());

        // چالش / اپیک
        quests.add(createEpicChallengeQuest());
        quests.add(createMasterDefenderQuest());
        quests.add(createNightOwlQuest());
        quests.addAll(createLawnmowerTimeQuests());
        return quests;
    }
}