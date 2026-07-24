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

        // انتخاب یک گیاه تصادفی برای کاشت
        List<String> allPlants = PlantFactory.allPlantNames();
        String randomPlant = allPlants.get(random.nextInt(allPlants.size()));
        
        // زمان رشد تصادفی بین ۱ تا ۳ ساعت
        long durationMs = (1 + random.nextInt(3)) * 3600000L; 
        
        gh.plantAt(row, col, randomPlant, durationMs);
        userManager.save();
        return "SUCCESS_" + randomPlant;
    }

    public String harvest(User user, int row, int col) {
        Greenhouse gh = user.getGreenhouse();
        if (!gh.isValidCoordinate(row, col)) return "ERR_INVALID_COORD";
        if (gh.isLocked(row, col) || gh.isEmpty(row, col)) return "ERR_EMPTY";
        if (!gh.isReady(row, col)) return "ERR_NOT_READY";

        String plantName = gh.getPlantName(row, col);
        gh.clearPot(row, col);
        
        // پاداش برداشت: ۱ پکت بذر از همون گیاه + ۱۰۰ سکه
        user.addSeedPackets(plantName, 1);
        user.addCoins(100);
        
        userManager.save();
        return "SUCCESS_" + plantName;
    }

    public String accelerate(User user, int row, int col) {
        Greenhouse gh = user.getGreenhouse();
        if (!gh.isValidCoordinate(row, col)) return "ERR_INVALID_COORD";
        if (gh.isLocked(row, col) || gh.isEmpty(row, col)) return "ERR_EMPTY";
        if (gh.isReady(row, col)) return "ERR_ALREADY_READY";

        int cost = 2; // هزینه تسریع رشد: ۲ الماس
        if (!user.spendDiamonds(cost)) {
            return "ERR_NOT_ENOUGH_DIAMONDS";
        }
        
        gh.acceleratePot(row, col);
        userManager.save();
        return "SUCCESS";
    }
}