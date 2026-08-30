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
        if (sortBy == null) sortBy = "score";
        
        switch (sortBy.toLowerCase()) {
            case "username": case "u": case "name":
                return Comparator.comparing(User::getUsername, String.CASE_INSENSITIVE_ORDER);
            case "chapter": case "level": case "stage":
                return Comparator.comparingInt(User::getLastCompletedChapter)
                        .thenComparingInt(User::getLastCompletedLevel);
            case "minigame": case "minigames": case "m":
                return Comparator.comparingInt(User::getMiniGamesCompleted);
            case "daily": case "daily_quest": case "dq":
                return Comparator.comparingInt(User::getDailyQuestsCompleted);
            case "nondaily": case "normal": case "ndq":
                return Comparator.comparingInt(User::getNonDailyQuestsCompleted);
            case "score": case "highscore": case "s": default:
                return Comparator.comparingInt(User::getHighScore);
        }
    }
}