package model.quest;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * پیشرفت فعلی کاربر که برای بررسی شرط‌های کوئست لازم است.
 * این کلاس رابط بین سیستم کوئست و بقیه‌ی مدل بازی (پروفایل کاربر و ...) است.
 */
public class QuestContext {

    // ==================== فیلدهای اصلی موجود ====================
    private int zombiesKilled;
    private int coinsEarned;
    private int gemsEarned;
    private int stagesCompleted;
    private int miniGamesCompleted;
    private int dailyQuestsCompleted;
    private int nonDailyQuestsCompleted;
    private int highestMuypoints;
    private boolean loggedInToday;

    // ==================== فیلدهای اضافه‌شده برای کوئست‌های جدید ====================

    // آفتاب گیر روزانه
    private int sunAmountCollectedToday;

    // شکارچی chapter -> تعداد زامبی کشته‌شده در هر فصل
    private Map<Integer, Integer> zombiesKilledPerChapter = new HashMap<>();

    // plant باز حرفه‌ای / only cactus -> تعداد زامبی کشته‌شده فقط توسط یک گیاه خاص در مرحله جاری
    private Map<String, Integer> zombiesKilledOnlyByPlant = new HashMap<>();

    // گیاه خوار اقتصادی -> تعداد گیاه از دست رفته در مرحله‌ی برد شده جاری
    private boolean stageWon;
    private int plantsLostInStage;

    // استاد دفاع -> مقدار خورشید باقی‌مانده در پایان مرحله برد شده
    private int remainingSunAtStageEnd;

    // سرعت عمل -> تعداد زامبی کشته‌شده در بازه n ثانیه‌ای از شروع موج اول
    private int zombiesKilledWithinSecondsOfFirstWave;

    // تخریب گر حرفه‌ای -> تعداد گیاهان انفجاری استفاده‌شده در مرحله جاری
    private int explosivePlantsUsedInStage;

    // تقارن -> آیا باغچه در پایان بازی متقارن است
    private boolean gardenSymmetricalAtEnd;

    // کشتار خانوادگی / شکوفایی در محدودیت‌ها -> خانواده‌های استفاده‌شده در کشتن‌ها و کاشته‌شده در مرحله
    private Set<String> plantFamiliesUsedForKills = new HashSet<>();
    private Set<String> plantFamiliesPlantedInStage = new HashSet<>();

    // شب یا صبح
    private boolean nightLevel;

    // برد پشت برد
    private int consecutiveHardestDifficultyWins;

    // تقریبا پیروز -> زامبی کشته‌شده در ستون اول بدون چمن‌زن
    private int zombiesKilledInFirstColumnWithoutLawnmower;

    // OCD -> آیا باغچه (به‌جز ردیف وسط) بدون تقارن بوده و برد شده
    private boolean stageWonWithNoSymmetryExceptMiddleRow;

    // روز ابری -> تعداد گیاهان تولیدکننده خورشید کاشته‌شده در مرحله برد شده
    private int sunProducerPlantsUsedInStage;

    // به ستون کمتر / سطر بی دفاع / صلیب بی دفاع
    private int emptyColumnsInWonStage;
    private int emptyRowsInWonStage;
    private boolean crossEmptyInWonStage;

    // وقت چمن‌زنی
    private int zombiesKilledByLawnmower;

    // ==================== getter/setter های موجود ====================

    public int getZombiesKilled() {
        return zombiesKilled;
    }

    public void setZombiesKilled(int zombiesKilled) {
        this.zombiesKilled = zombiesKilled;
    }

    public int getCoinsEarned() {
        return coinsEarned;
    }

    public void setCoinsEarned(int coinsEarned) {
        this.coinsEarned = coinsEarned;
    }

    public int getGemsEarned() {
        return gemsEarned;
    }

    public void setGemsEarned(int gemsEarned) {
        this.gemsEarned = gemsEarned;
    }

    public int getStagesCompleted() {
        return stagesCompleted;
    }

    public void setStagesCompleted(int stagesCompleted) {
        this.stagesCompleted = stagesCompleted;
    }

    public int getMiniGamesCompleted() {
        return miniGamesCompleted;
    }

    public void setMiniGamesCompleted(int miniGamesCompleted) {
        this.miniGamesCompleted = miniGamesCompleted;
    }

    public int getDailyQuestsCompleted() {
        return dailyQuestsCompleted;
    }

    public void setDailyQuestsCompleted(int dailyQuestsCompleted) {
        this.dailyQuestsCompleted = dailyQuestsCompleted;
    }

    public int getNonDailyQuestsCompleted() {
        return nonDailyQuestsCompleted;
    }

    public void setNonDailyQuestsCompleted(int nonDailyQuestsCompleted) {
        this.nonDailyQuestsCompleted = nonDailyQuestsCompleted;
    }

    public int getHighestMuypoints() {
        return highestMuypoints;
    }

    public void setHighestMuypoints(int highestMuypoints) {
        this.highestMuypoints = highestMuypoints;
    }

    public boolean isLoggedInToday() {
        return loggedInToday;
    }

    public void setLoggedInToday(boolean loggedInToday) {
        this.loggedInToday = loggedInToday;
    }

    // ==================== getter/setter های جدید ====================

    // آفتاب گیر روزانه
    public int getSunAmountCollectedToday() {
        return sunAmountCollectedToday;
    }

    public void setSunAmountCollectedToday(int sunAmountCollectedToday) {
        this.sunAmountCollectedToday = sunAmountCollectedToday;
    }

    public void addSunCollectedToday(int amount) {
        this.sunAmountCollectedToday += amount;
    }

    // شکارچی chapter
    public int getZombiesKilledInChapter(int chapterNumber) {
        return zombiesKilledPerChapter.getOrDefault(chapterNumber, 0);
    }

    public void setZombiesKilledInChapter(int chapterNumber, int count) {
        zombiesKilledPerChapter.put(chapterNumber, count);
    }

    public void incrementZombiesKilledInChapter(int chapterNumber) {
        zombiesKilledPerChapter.merge(chapterNumber, 1, Integer::sum);
    }

    // plant باز حرفه‌ای / only cactus
    public int getZombiesKilledOnlyBy(String plantId) {
        return zombiesKilledOnlyByPlant.getOrDefault(plantId, 0);
    }

    public void setZombiesKilledOnlyBy(String plantId, int count) {
        zombiesKilledOnlyByPlant.put(plantId, count);
    }

    public void resetZombiesKilledOnlyBy() {
        zombiesKilledOnlyByPlant.clear();
    }

    // گیاه خوار اقتصادی
    public boolean isStageWon() {
        return stageWon;
    }

    public void setStageWon(boolean stageWon) {
        this.stageWon = stageWon;
    }

    public int getPlantsLostInStage() {
        return plantsLostInStage;
    }

    public void setPlantsLostInStage(int plantsLostInStage) {
        this.plantsLostInStage = plantsLostInStage;
    }

    public boolean isStageWonWithMaxPlantsLost(int maxPlantsLost) {
        return stageWon && plantsLostInStage <= maxPlantsLost;
    }

    // استاد دفاع
    public int getRemainingSunAtStageEnd() {
        return remainingSunAtStageEnd;
    }

    public void setRemainingSunAtStageEnd(int remainingSunAtStageEnd) {
        this.remainingSunAtStageEnd = remainingSunAtStageEnd;
    }

    public boolean isStageWonWithExactSun(int exactSun) {
        return stageWon && remainingSunAtStageEnd == exactSun;
    }

    // سرعت عمل
    public int getZombiesKilledWithinSecondsOfFirstWave(int seconds) {
        // فرض: مقدار از قبل برای بازه‌ی seconds محاسبه و ست شده است
        return zombiesKilledWithinSecondsOfFirstWave;
    }

    public void setZombiesKilledWithinSecondsOfFirstWave(int count) {
        this.zombiesKilledWithinSecondsOfFirstWave = count;
    }

    // تخریب گر حرفه‌ای
    public int getExplosivePlantsUsedInStage() {
        return explosivePlantsUsedInStage;
    }

    public void setExplosivePlantsUsedInStage(int explosivePlantsUsedInStage) {
        this.explosivePlantsUsedInStage = explosivePlantsUsedInStage;
    }

    public void incrementExplosivePlantsUsedInStage() {
        this.explosivePlantsUsedInStage++;
    }

    // تقارن
    public boolean isGardenSymmetricalAtEnd() {
        return gardenSymmetricalAtEnd;
    }

    public void setGardenSymmetricalAtEnd(boolean gardenSymmetricalAtEnd) {
        this.gardenSymmetricalAtEnd = gardenSymmetricalAtEnd;
    }

    // کشتار خانوادگی / شکوفایی در محدودیت‌ها
    public boolean wereAllKillsByFamily(String familyType) {
        return !plantFamiliesUsedForKills.isEmpty()
                && plantFamiliesUsedForKills.size() == 1
                && plantFamiliesUsedForKills.contains(familyType);
    }

    public void addPlantFamilyUsedForKill(String familyType) {
        plantFamiliesUsedForKills.add(familyType);
    }

    public void resetPlantFamiliesUsedForKills() {
        plantFamiliesUsedForKills.clear();
    }

    public boolean wasStageWonWithoutFamily(String forbiddenFamilyType) {
        return stageWon && !plantFamiliesPlantedInStage.contains(forbiddenFamilyType);
    }

    public boolean wasStageWonUsingOnlyFamily(String familyType) {
        return stageWon
                && !plantFamiliesPlantedInStage.isEmpty()
                && plantFamiliesPlantedInStage.size() == 1
                && plantFamiliesPlantedInStage.contains(familyType);
    }

    public void addPlantFamilyPlantedInStage(String familyType) {
        plantFamiliesPlantedInStage.add(familyType);
    }

    public void resetPlantFamiliesPlantedInStage() {
        plantFamiliesPlantedInStage.clear();
    }

    // شب یا صبح
    public boolean isNightLevel() {
        return nightLevel;
    }

    public void setNightLevel(boolean nightLevel) {
        this.nightLevel = nightLevel;
    }

    // برد پشت برد
    public int getConsecutiveHardestDifficultyWins() {
        return consecutiveHardestDifficultyWins;
    }

    public void setConsecutiveHardestDifficultyWins(int consecutiveHardestDifficultyWins) {
        this.consecutiveHardestDifficultyWins = consecutiveHardestDifficultyWins;
    }

    // تقریبا پیروز
    public int getZombiesKilledInFirstColumnWithoutLawnmower() {
        return zombiesKilledInFirstColumnWithoutLawnmower;
    }

    public void setZombiesKilledInFirstColumnWithoutLawnmower(int count) {
        this.zombiesKilledInFirstColumnWithoutLawnmower = count;
    }

    // OCD
    public boolean wasStageWonWithNoSymmetryExceptMiddleRow() {
        return stageWonWithNoSymmetryExceptMiddleRow;
    }

    public void setStageWonWithNoSymmetryExceptMiddleRow(boolean value) {
        this.stageWonWithNoSymmetryExceptMiddleRow = value;
    }

    // روز ابری
    public boolean wasStageWonWithMaxSunProducers(int maxSunProducers) {
        return stageWon && sunProducerPlantsUsedInStage <= maxSunProducers;
    }

    public int getSunProducerPlantsUsedInStage() {
        return sunProducerPlantsUsedInStage;
    }

    public void setSunProducerPlantsUsedInStage(int count) {
        this.sunProducerPlantsUsedInStage = count;
    }

    // به ستون کمتر
    public boolean wasStageWonWithEmptyColumns(int emptyColumnCount) {
        return stageWon && emptyColumnsInWonStage >= emptyColumnCount;
    }

    public int getEmptyColumnsInWonStage() {
        return emptyColumnsInWonStage;
    }

    public void setEmptyColumnsInWonStage(int emptyColumnsInWonStage) {
        this.emptyColumnsInWonStage = emptyColumnsInWonStage;
    }

    // سطر بی دفاع
    public boolean wasStageWonWithEmptyRows(int emptyRowCount) {
        return stageWon && emptyRowsInWonStage >= emptyRowCount;
    }

    public int getEmptyRowsInWonStage() {
        return emptyRowsInWonStage;
    }

    public void setEmptyRowsInWonStage(int emptyRowsInWonStage) {
        this.emptyRowsInWonStage = emptyRowsInWonStage;
    }

    // صلیب بی دفاع
    public boolean wasStageWonWithEmptyCross() {
        return stageWon && crossEmptyInWonStage;
    }

    public void setCrossEmptyInWonStage(boolean crossEmptyInWonStage) {
        this.crossEmptyInWonStage = crossEmptyInWonStage;
    }

    // وقت چمن‌زنی
    public int getZombiesKilledByLawnmower() {
        return zombiesKilledByLawnmower;
    }

    public void setZombiesKilledByLawnmower(int zombiesKilledByLawnmower) {
        this.zombiesKilledByLawnmower = zombiesKilledByLawnmower;
    }

    public void incrementZombiesKilledByLawnmower() {
        this.zombiesKilledByLawnmower++;
    }

    // ==================== متدهای کمکی ریست دوره‌ای ====================

    /**
     * باید در ابتدای هر روز جدید (بعد از تعیین loggedInToday) فراخوانی شود
     * تا شمارنده‌های مخصوص روزانه صفر شوند.
     */
    public void resetDailyCounters() {
        this.sunAmountCollectedToday = 0;
    }

    /**
     * باید در شروع هر مرحله‌ی جدید فراخوانی شود تا وضعیت مخصوص مرحله‌ی
     * قبلی باقی نماند.
     */
    public void resetStageCounters() {
        this.stageWon = false;
        this.plantsLostInStage = 0;
        this.remainingSunAtStageEnd = 0;
        this.zombiesKilledWithinSecondsOfFirstWave = 0;
        this.explosivePlantsUsedInStage = 0;
        this.gardenSymmetricalAtEnd = false;
        this.nightLevel = false;
        this.zombiesKilledInFirstColumnWithoutLawnmower = 0;
        this.stageWonWithNoSymmetryExceptMiddleRow = false;
        this.sunProducerPlantsUsedInStage = 0;
        this.emptyColumnsInWonStage = 0;
        this.emptyRowsInWonStage = 0;
        this.crossEmptyInWonStage = false;
        resetZombiesKilledOnlyBy();
        resetPlantFamiliesUsedForKills();
        resetPlantFamiliesPlantedInStage();
    }
}