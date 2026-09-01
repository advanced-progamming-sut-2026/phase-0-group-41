package controller;

import model.user.User;
import model.user.UserManager;
import network.NetworkManager;
import network.NetworkMessage;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class LeaderboardController {

    // UserManager محلی فقط به‌عنوان Fallback نگه داشته شده (مثلاً اگر سرور در
    // دسترس نبود)؛ منبع اصلی داده طبق سند فاز ۳ باید سرور باشد، نه فایل محلی.
    private final UserManager userManager;

    public LeaderboardController(UserManager userManager) {
        this.userManager = userManager;
    }

    /**
     * پس از پایان هر دور «بازی امتیازی» (بخش امتیازی، تحت شبکه)، امتیاز کاربر
     * را به سرور می‌فرستد. طبق سند: «اگر امتیاز جدید بیشتر از رکورد قبلی کاربر
     * باشد، رکورد او در سرور به‌روزرسانی شود» — این مقایسه سمت سرور انجام
     * می‌شود (model.user.User#updateMaxMowPoints، همان فیلد maxMowPoints قبلی)
     * تا تقلب سمت کلاینت ممکن نباشد.
     */
    public boolean submitScoreGameResult(String username, int score) {
        NetworkMessage req = new NetworkMessage("SUBMIT_SCORE_GAME_RESULT");
        req.data.put("username", username);
        req.data.put("score", String.valueOf(score));
        NetworkMessage res = NetworkManager.sendRequest(req);
        return res != null && res.success;
    }

    // متدی که فقط داده‌های مرتب‌شده را به View پاس می‌دهد
    public List<User> getSortedLeaderboard(String sortBy, boolean ascending) {
        List<User> users = fetchUsers();
        if (users.isEmpty()) {
            return users;
        }

        Comparator<User> comparator = getComparator(sortBy);
        if (!ascending) {
            comparator = comparator.reversed();
        }

        List<User> sortedList = new ArrayList<>(users);
        sortedList.sort(comparator);
        return sortedList;
    }

    @SuppressWarnings("unchecked")
    private List<User> fetchUsers() {
        NetworkMessage req = new NetworkMessage("GET_ALL_USERS");
        NetworkMessage res = NetworkManager.sendRequest(req);

        if (res != null && res.success && res.payload instanceof List) {
            return (List<User>) res.payload;
        }
        // اگر ارتباط با سرور برقرار نشد، به‌جای کرش کردن، لیست محلی (احتمالاً خالی) را برمی‌گردانیم
        return userManager.getAllUsers();
    }

    private Comparator<User> getComparator(String sortBy) {
        if (sortBy == null) sortBy = "chapter";

        switch (sortBy.toLowerCase()) {
            case "minigame": case "minigames": case "m":
                return Comparator.comparingInt(User::getMiniGamesCompleted);
            case "quest": case "quests": case "q":
                return Comparator.comparingInt(
                        u -> u.getDailyQuestsCompleted() + u.getNonDailyQuestsCompleted());
            case "mypoint": case "point": case "p":
                // کاربرانی که هنوز بازی امتیازی را انجام نداده‌اند باید همیشه
                // ته لیست بمانند، نه اینکه با ۰ امتیازِ واقعی قاطی شوند
                return Comparator.comparing((User u) -> u.hasPlayedScoreGame())
                        .thenComparingInt(User::getMaxMowPoints);
            case "chapter": case "level": case "stage": default:
                return Comparator.comparingInt(User::getLastCompletedChapter)
                        .thenComparingInt(User::getLastCompletedLevel);
        }
    }
}