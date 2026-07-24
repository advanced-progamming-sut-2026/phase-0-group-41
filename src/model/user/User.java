package model.user;

import model.greenhouse.Greenhouse;
import java.io.Serializable;
import java.util.*;

import java.time.LocalDate;
import model.quest.PlayerProfile;
import model.quest.QuestManager;
import model.quest.QuestFactory;
import model.quest.QuestContext;

// اضافه شدن implements PlayerProfile
public class User implements Serializable, PlayerProfile {

    private static final long serialVersionUID = 1L;

    // === متغیرهای پایه ===
    private String username;
    private String passwordHash;
    private String nickname;
    private String email;
    private String gender;
    private int securityQuestionId;
    private String securityAnswer;

    // === متغیرهای سیستم ارتقاء گیاهان ===
    private Map<String, Integer> plantLevels;
    private Map<String, Integer> seedPackets;

    // === متغیرهای لیدربورد ===
    private int lastCompletedChapter = 0;
    private int lastCompletedLevel = 0;
    private int miniGamesCompleted = 0;
    private int dailyQuestsCompleted = 0;
    private int nonDailyQuestsCompleted = 0;
    private int highScore = 0;

    // === متغیرهای مالی و پیشرفت ===
    private int coins = 0;
    private int diamonds = 0;
    private int difficultyLevel = 3;
    private int gamesPlayed = 0;
    private int levelsCompleted = 0;
    private int maxMowPoints = 0;

    // === متغیرهای گلخانه و کالکشن ===
    private Greenhouse greenhouse;
    private Map<String, Boolean> greenhouseBoosts;
    private final Set<String> unlockedPlants = new HashSet<>();
    private final Set<String> seenZombies = new HashSet<>();
    
    // === اخبار ===
    private List<NewsMessage> newsList = new ArrayList<>();

    // ==========================================
    // === متغیرهای اضافه شده برای فروشگاه و کوئست ===
    // ==========================================
    private LocalDate lastLoginDate;
    private QuestContext questContext = new QuestContext();
    private QuestManager questManager;
    private String dailyOfferPlant;
    private String dailyOfferDate; 
    private boolean dailyOfferPurchased;
    private int storedPlantFood = 0; 
    private int pendingGreenhousePots = 0;


    public User(String username, String passwordHash, String nickname, String email, String gender) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.nickname = nickname;
        this.email = email;
        this.gender = gender;

        // مقداردهی اولیه سیستم ارتقاء
        this.plantLevels = new HashMap<>();
        this.seedPackets = new HashMap<>();
        // گیاهان پایه از لول ۱ شروع می‌کنند
        this.plantLevels.put("peashooter", 1);
        this.plantLevels.put("sunflower", 1);
        this.plantLevels.put("wallnut", 1);

        // گیاهان پایه‌ای که از ابتدا در دسترس‌اند
        unlockedPlants.add("peashooter");
        unlockedPlants.add("sunflower");
        unlockedPlants.add("wallnut");
        this.greenhouse = new Greenhouse();
        this.greenhouseBoosts = new HashMap<>();
    }

    // ==========================================
    // پیاده‌سازی متدهای PlayerProfile
    // ==========================================
    
    @Override
    public void addCoins(int amount) {
        this.coins += amount;
        // --- اتصال سیستم کوئست ---
        QuestContext context = getQuestContext();
        context.setCoinsEarned(context.getCoinsEarned() + amount);
        getQuestManager().refreshCompletionStatus(context);
        // (پرینت گرفتن حذف شد و باید در لایه View انجام شود)
    }

    @Override
    public void addGems(int amount) {
        this.diamonds += amount;
        // --- اتصال سیستم کوئست ---
        QuestContext context = getQuestContext();
        context.setCoinsEarned(context.getCoinsEarned() + amount);
        getQuestManager().refreshCompletionStatus(context);
    }

    public void addDiamonds(int amount) {
        // برای یکپارچگی، متد قبلی شما متد جدید رفیقت را صدا می‌زند
        addGems(amount);
    }

    @Override
    public void unlock(String unlockableId) {
        this.unlockedPlants.add(unlockableId);
    }

    @Override
    public void addItemToInventory(String itemId, int count) {
        // در صورت نیاز پیاده‌سازی شود
    }

    // ==========================================
    // متدهای فروشگاه و کوئست (اضافه شده از کد رفیقت)
    // ==========================================
    public QuestManager getQuestManager() {
        if (this.questManager == null) {
            this.questManager = new QuestManager(QuestFactory.createDefaultQuests());
        }
        return this.questManager;
    }

    public void setQuestManager(QuestManager questManager) {
        this.questManager = questManager;
    }

    public LocalDate getLastLoginDate() {
        return this.lastLoginDate;
    }

    public void updateLastLoginDate() {
        this.lastLoginDate = LocalDate.now();
    }

    public QuestContext getQuestContext() {
        if (this.questContext == null) {
            this.questContext = new QuestContext();
        }
        return this.questContext;
    }

    public String getDailyOfferPlant() { return dailyOfferPlant; }
    public void setDailyOfferPlant(String dailyOfferPlant) { this.dailyOfferPlant = dailyOfferPlant; }
    
    public String getDailyOfferDate() { return dailyOfferDate; }
    public void setDailyOfferDate(String dailyOfferDate) { this.dailyOfferDate = dailyOfferDate; }
    
    public boolean isDailyOfferPurchased() { return dailyOfferPurchased; }
    public void setDailyOfferPurchased(boolean dailyOfferPurchased) { this.dailyOfferPurchased = dailyOfferPurchased; }
    
    public void addStoredPlantFood(int amount) { this.storedPlantFood = Math.min(3, storedPlantFood + amount); }
    public void addPendingGreenhousePots(int amount) { this.pendingGreenhousePots += amount; }


    // ==========================================
    // گترها و سترهای پایه کلاس خودت
    // ==========================================

    public List<NewsMessage> getNewsList() {
        return newsList;
    }

    public void addNews(String content) {
        newsList.add(new NewsMessage(content));
    }

    public boolean hasUnreadNews() {
        for (NewsMessage msg : newsList) {
            if (!msg.isRead()) {
                return true;
            }
        }
        return false;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getGender() { return gender; }

    public int getSecurityQuestionId() { return securityQuestionId; }
    public void setSecurityQuestionId(int securityQuestionId) { this.securityQuestionId = securityQuestionId; }

    public String getSecurityAnswer() { return securityAnswer; }
    public void setSecurityAnswer(String securityAnswer) { this.securityAnswer = securityAnswer; }

    public int getCoins() { return coins; }
    public void setCoins(int coins) { this.coins = coins; }
    
    public boolean spendCoins(int amount) {
        if (coins < amount) return false;
        coins -= amount;
        return true;
    }

    public int getDiamonds() { return diamonds; }
    public void setDiamonds(int diamonds) { this.diamonds = diamonds; }
    
    public boolean spendDiamonds(int amount) {
        if (diamonds < amount) return false;
        diamonds -= amount;
        return true;
    }

    public int getDifficultyLevel() { return difficultyLevel; }
    public void setDifficultyLevel(int difficultyLevel) { this.difficultyLevel = difficultyLevel; }

    public int getGamesPlayed() { return gamesPlayed; }
    public void incrementGamesPlayed() { gamesPlayed++; }

    public int getLevelsCompleted() { return levelsCompleted; }
    public void incrementLevelsCompleted() { levelsCompleted++; }

    public int getMaxMowPoints() { return maxMowPoints; }
    public void updateMaxMowPoints(int points) {
        if (points > maxMowPoints) maxMowPoints = points;
    }

    public Set<String> getUnlockedPlants() { return unlockedPlants; }
    public Set<String> getSeenZombies() { return seenZombies; }

    public Greenhouse getGreenhouse() {
        if (greenhouse == null) greenhouse = new Greenhouse();
        return greenhouse;
    }

    public boolean hasGreenhouseBoost(String plantName) {
        return getGreenhouseBoosts().getOrDefault(plantName, false);
    }

    public void addGreenhouseBoost(String plantName) {
        getGreenhouseBoosts().putIfAbsent(plantName, true);
    }

    public void consumeGreenhouseBoost(String plantName) {
        getGreenhouseBoosts().remove(plantName);
    }

    public int getPendingGreenhousePots() { return pendingGreenhousePots; }

    public Map<String, Boolean> getGreenhouseBoosts() {
        if (greenhouseBoosts == null) greenhouseBoosts = new HashMap<>();
        return greenhouseBoosts;
    }

    // ==========================================
    // متدهای مدیریت ارتقاء و لیدربورد (کدهای خودت)
    // ==========================================

    public int getPlantLevel(String plantName) {
        if (plantLevels == null) plantLevels = new HashMap<>();
        return plantLevels.getOrDefault(plantName, 1);
    }

    public int getSeedPackets(String plantName) {
        if (seedPackets == null) seedPackets = new HashMap<>();
        return seedPackets.getOrDefault(plantName, 0);
    }

    public void addSeedPackets(String plantName, int amount) {
        if (seedPackets == null) seedPackets = new HashMap<>();
        seedPackets.put(plantName, getSeedPackets(plantName) + amount);
    }

    public boolean upgradePlant(String plantName, int costInCoins, int requiredPackets) {
        int currentPackets = getSeedPackets(plantName);

        if (this.coins >= costInCoins && currentPackets >= requiredPackets) {
            this.coins -= costInCoins;
            this.seedPackets.put(plantName, currentPackets - requiredPackets);
            int newLevel = getPlantLevel(plantName) + 1;
            this.plantLevels.put(plantName, newLevel);
            return true;
        }
        return false;
    }

    public int getLastCompletedChapter() { return lastCompletedChapter; }
    public void setLastCompletedChapter(int lastCompletedChapter) { this.lastCompletedChapter = lastCompletedChapter; }

    public int getLastCompletedLevel() { return lastCompletedLevel; }
    public void setLastCompletedLevel(int lastCompletedLevel) { this.lastCompletedLevel = lastCompletedLevel; }

    public int getMiniGamesCompleted() { return miniGamesCompleted; }
    public void setMiniGamesCompleted(int miniGamesCompleted) { this.miniGamesCompleted = miniGamesCompleted; }

    public int getDailyQuestsCompleted() { return dailyQuestsCompleted; }
    public void setDailyQuestsCompleted(int dailyQuestsCompleted) { this.dailyQuestsCompleted = dailyQuestsCompleted; }

    public int getNonDailyQuestsCompleted() { return nonDailyQuestsCompleted; }
    public void setNonDailyQuestsCompleted(int nonDailyQuestsCompleted) { this.nonDailyQuestsCompleted = nonDailyQuestsCompleted; }

    public int getHighScore() { return highScore; }
    public void setHighScore(int highScore) {
        if (highScore > this.highScore) {
            this.highScore = highScore;
        }
    }
}