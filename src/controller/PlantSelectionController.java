package controller;

import model.user.User;
import java.util.ArrayList;
import java.util.List;

public class PlantSelectionController {
    // ظرفیت پیش‌فرض ۸ گیاه برای هر مرحله
    private static final int MAX_SLOTS = 8;

    // لیست گیاهان انتخاب شده برای ورود به بازی
    private final List<String> selectedPlants = new ArrayList<>();

    public boolean isPlantUnlocked(User user, String plantType) {
        String plantNameLowerCase = plantType.toLowerCase();
        
        if (!model.plant.PlantFactory.isKnown(plantNameLowerCase)) {
            return false;
        }
        // بررسی باز بودن گیاه در کلکسیون
        return user.getUnlockedPlants().contains(plantNameLowerCase);
    }

    public List<String> getSelectedPlants() {
        return selectedPlants;
    }

    public String addPlant(User user, String plantType) {
        String plantNameLowerCase = plantType.toLowerCase();
        
        if (!isPlantUnlocked(user, plantNameLowerCase)) {
            return "ERR_LOCKED_OR_NOT_FOUND"; // گیاه قفل است یا وجود ندارد
        }
        if (selectedPlants.contains(plantNameLowerCase)) {
            return "ERR_ALREADY_SELECTED"; // قبلاً انتخاب شده
        }
        if (selectedPlants.size() >= MAX_SLOTS) {
            return "ERR_FULL"; // ظرفیت تکمیل است
        }

        selectedPlants.add(plantNameLowerCase);
        return "SUCCESS";
    }

    public String removePlant(String plantType) {
        String plantNameLowerCase = plantType.toLowerCase();
        
        if (!selectedPlants.contains(plantNameLowerCase)) {
            return "ERR_NOT_SELECTED"; // این گیاه اصلاً انتخاب نشده است
        }

        selectedPlants.remove(plantNameLowerCase);
        return "SUCCESS";
    }

    public String boostPlant(User user, String plantType) {
        String plantNameLowerCase = plantType.toLowerCase();
        
        if (!isPlantUnlocked(user, plantNameLowerCase)) {
            return "ERR_NOT_FOUND"; // گیاه وجود ندارد یا در کلکسیون نیست
        }
        if (user.hasGreenhouseBoost(plantNameLowerCase)) {
            return "ERR_ALREADY_BOOSTED"; // قبلا بوست شده
        }
        if (user.getDiamonds() < 2) {
            return "ERR_NOT_ENOUGH_DIAMONDS"; // الماس کافی نیست
        }

        // کسر دو الماس به صورت اصولی
        user.spendDiamonds(2);
        // ثبت بوست در پروفایل کاربر تا GameController بتواند آن را بخواند
        user.addGreenhouseBoost(plantNameLowerCase);
        return "SUCCESS";
    }

    // متدی برای ریست کردن وضعیت قبل از شروع انتخاب‌های جدید
    public void resetSelection() {
        selectedPlants.clear();
        // لیست boostedPlants پاک شد چون حالا مستقیم روی User اعمال می‌شود
    }
}