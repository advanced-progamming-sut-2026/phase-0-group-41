package model.quest;

/**
 * پیشرفت فعلی کاربر که برای بررسی شرط‌های کوئست لازم است.
 * این کلاس رابط بین سیستم کوئست و بقیه‌ی مدل بازی (پروفایل کاربر و ...) است.
 */
public class QuestContext {

    private int zombiesKilled;
    private int coinsEarned;
    private int gemsEarned;
    private int stagesCompleted;
    private int miniGamesCompleted;
    private int dailyQuestsCompleted;
    private int nonDailyQuestsCompleted;
    private int highestMuypoints;
    private boolean loggedInToday;

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
}