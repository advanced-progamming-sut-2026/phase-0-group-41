package controller;

import model.greenhouse.Greenhouse;
import model.user.User;
import model.user.UserManager;
import model.plant.PlantFactory;

import java.util.List;
import java.util.Random;

public class GreenhouseController {
    private final UserManager userManager;
    private final Random random = new Random();

    public GreenhouseController(UserManager userManager) {
        this.userManager = userManager;
    }

    public String unlockPot(User user, int row, int col) {
        Greenhouse gh = user.getGreenhouse();
        if (!gh.isValidCoordinate(row, col)) return "ERR_INVALID_COORD";
        if (gh.isUnlocked(row, col)) return "ERR_ALREADY_UNLOCKED";
        
        if (user.getPendingGreenhousePots() <= 0) {
            return "ERR_NO_POTS";
        }
        
        user.addPendingGreenhousePots(-1); // کسر یک گلدان از انبار کاربر
        gh.unlock(row, col);
        userManager.save();
        return "SUCCESS";
    }

    public String plant(User user, int row, int col) {
        Greenhouse gh = user.getGreenhouse();
        if (!gh.isValidCoordinate(row, col)) return "ERR_INVALID_COORD";
        if (gh.isLocked(row, col)) return "ERR_LOCKED";
        if (gh.hasPlant(row, col)) return "ERR_NOT_EMPTY";

        String plantName;
        long durationMs;

        // ۵۰ درصد احتمال برای گل معمولی (Marigold)
        if (random.nextBoolean()) {
            plantName = "marigold";
            durationMs = 2 * 3600000L; // 2 ساعت
        } else {
            // ۵۰ درصد احتمال برای یکی از گیاهان آنلاک‌شده کاربر
            java.util.List<String> unlocked = new java.util.ArrayList<>(user.getUnlockedPlants());
            if (unlocked.isEmpty()) {
                plantName = "peashooter"; // محض احتیاط اگر لیستی نبود
            } else {
                plantName = unlocked.get(random.nextInt(unlocked.size()));
            }
            durationMs = 8 * 3600000L; // 8 ساعت
        }
        
        gh.plantAt(row, col, plantName, durationMs);
        userManager.save();
        return "SUCCESS_" + plantName;
    }

    public String harvest(User user, int row, int col) {
        Greenhouse gh = user.getGreenhouse();
        if (!gh.isValidCoordinate(row, col)) return "ERR_INVALID_COORD";
        if (gh.isLocked(row, col) || gh.isEmpty(row, col)) return "ERR_EMPTY";
        if (!gh.isReady(row, col)) return "ERR_NOT_READY";

        String plantName = gh.getPlantName(row, col);
        gh.clearPot(row, col);
        
        if (plantName.equals("marigold")) {
            user.addCoins(500); // پاداش گل معمولی
        } else {
            user.addGreenhouseBoost(plantName); // ذخیره بوست برای گیاه
            user.addCoins(100);
            user.addSeedPackets(plantName, 1);
        }
        
        userManager.save();
        return "SUCCESS_" + plantName;
    }

    public String accelerate(User user, int row, int col) {
        Greenhouse gh = user.getGreenhouse();
        if (!gh.isValidCoordinate(row, col)) return "ERR_INVALID_COORD";
        if (gh.isLocked(row, col) || gh.isEmpty(row, col)) return "ERR_EMPTY";
        if (gh.isReady(row, col)) return "ERR_ALREADY_READY";

        long remainingMs = gh.getRemainingMillis(row, col);
        // محاسبه سقف ساعت‌های باقی‌مانده
        int cost = (int) Math.ceil(remainingMs / 3600000.0); 

        if (!user.spendDiamonds(cost)) {
            return "ERR_NOT_ENOUGH_DIAMONDS";
        }
        
        gh.acceleratePot(row, col);
        userManager.save();
        return "SUCCESS";
    }
}