package model.user;

import java.time.LocalDate;
import model.greenhouse.Greenhouse;
import model.quest.PlayerProfile;
import model.quest.QuestManager;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import model.quest.QuestFactory;
import model.quest.QuestContext;
import view.ConsoleView;

public class User implements Serializable, PlayerProfile {

    private static final long serialVersionUID = 1L;
    private LocalDate lastLoginDate;
    private String username;
    private String passwordHash;
    private String nickname;
    private String email;
    private String gender;
    private int securityQuestionId;
    private String securityAnswer;
    private QuestContext questContext = new QuestContext();
    private int coins = 0;
    private int diamonds = 0;
    private int difficultyLevel = 3;

    private int gamesPlayed = 0;
    private int levelsCompleted = 0;
    private int maxMowPoints = 0;

    private Greenhouse greenhouse;
    private Map<String, Boolean> greenhouseBoosts;
    private QuestManager questManager;

    private final Set<String> unlockedPlants = new HashSet<>();
    private final Set<String> seenZombies = new HashSet<>();

    public User(String username, String passwordHash, String nickname, String email, String gender) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.nickname = nickname;
        this.email = email;
        this.gender = gender;
        // گیاهان پایه‌ای که از ابتدا در دسترس‌اند
        unlockedPlants.add("peashooter");
        unlockedPlants.add("sunflower");
        unlockedPlants.add("wallnut");
        this.greenhouse = new Greenhouse();
        this.greenhouseBoosts = new HashMap<>();
    }
    @Override
    public void addCoins(int amount) {
        this.coins += amount;
        // --- اتصال سیستم کوئست ---
        // ۱. آمار کل سکه‌های به دست آمده را آپدیت می‌کنیم
        QuestContext context = getQuestContext();
        context.setCoinsEarned(context.getCoinsEarned() + amount);

        // ۲. به منیجر می‌گوییم چک کند آیا کوئستی مربوط به سکه تکمیل شده یا نه
        getQuestManager().refreshCompletionStatus(context);
        ConsoleView.printMessage(amount + " سکه به کاربر اضافه شد.");
    }

    @Override
    public void addGems(int amount) {
        this.diamonds += amount;
        // --- اتصال سیستم کوئست ---
        // ۱. آمار کل سکه‌های به دست آمده را آپدیت می‌کنیم
        QuestContext context = getQuestContext();
        context.setCoinsEarned(context.getCoinsEarned() + amount);

        // ۲. به منیجر می‌گوییم چک کند آیا کوئستی مربوط به سکه تکمیل شده یا نه
        getQuestManager().refreshCompletionStatus(context);
        ConsoleView.printMessage(amount + " الماس به کاربر اضافه شد.");

    }

    @Override
    public void unlock(String unlockableId) {
        this.unlockedPlants.add(unlockableId);
        ConsoleView.printMessage("گیاه " + unlockableId + " آنلاک شد!");
    }

    @Override
    public void addItemToInventory(String itemId, int count) {

    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getGender() {
        return gender;
    }

    public int getSecurityQuestionId() {
        return securityQuestionId;
    }

    public void setSecurityQuestionId(int securityQuestionId) {
        this.securityQuestionId = securityQuestionId;
    }

    public String getSecurityAnswer() {
        return securityAnswer;
    }

    public void setSecurityAnswer(String securityAnswer) {
        this.securityAnswer = securityAnswer;
    }

    public int getCoins() {
        return coins;
    }


    public boolean spendCoins(int amount) {
        if (coins < amount) {
            return false;
        }
        coins -= amount;
        return true;
    }

    public int getDiamonds() {
        return diamonds;
    }

    public void addDiamonds(int amount) {
        this.diamonds += amount;
    }

    public boolean spendDiamonds(int amount) {
        if (diamonds < amount) {
            return false;
        }
        diamonds -= amount;
        return true;
    }

    public int getDifficultyLevel() {
        return difficultyLevel;
    }

    public void setDifficultyLevel(int difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
    }

    public int getGamesPlayed() {
        return gamesPlayed;
    }

    public void incrementGamesPlayed() {
        gamesPlayed++;
    }

    public int getLevelsCompleted() {
        return levelsCompleted;
    }

    public void incrementLevelsCompleted() {
        levelsCompleted++;
    }

    public int getMaxMowPoints() {
        return maxMowPoints;
    }

    public void updateMaxMowPoints(int points) {
        if (points > maxMowPoints) {
            maxMowPoints = points;
        }
    }

    public Set<String> getUnlockedPlants() {
        return unlockedPlants;
    }

    public Set<String> getSeenZombies() {
        return seenZombies;
    }

    public Greenhouse getGreenhouse() {
        if (greenhouse == null) {
            greenhouse = new Greenhouse();
        }
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

    public Map<String, Boolean> getGreenhouseBoosts() {
        if (greenhouseBoosts == null) {
            greenhouseBoosts = new HashMap<>();
        }
        return greenhouseBoosts;
    }

    public QuestManager getQuestManager() {
        // اگر کاربر هنوز کوئست منیجر نداشت، برایش یکی می‌سازیم
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
        this.lastLoginDate = LocalDate.now(); // تاریخ امروز را ذخیره می‌کند
    }
    public QuestContext getQuestContext() {
        if (this.questContext == null) {
            this.questContext = new QuestContext();
        }
        return this.questContext;
    }
}
