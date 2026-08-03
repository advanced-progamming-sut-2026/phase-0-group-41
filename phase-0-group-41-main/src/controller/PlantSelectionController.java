package controller;

import model.user.User;
import java.util.ArrayList;
import java.util.List;

public class PlantSelectionController {
    // ظرفیت پیش‌فرض ۸ گیاه برای هر مرحله
    private static final int MAX_SLOTS = 8;

    // لیست گیاهان انتخاب شده برای ورود به بازی
    private final List<String> selectedPlants = new ArrayList<>();

    // لیست گیاهانی که برای این مرحله بوست شده‌اند
    private final List<String> boostedPlants = new ArrayList<>();

    // متدی موقت برای شبیه‌سازی گیاهان باز شده کاربر (باید به دیتابیس گیاهان خودت متصل شود)
    public boolean isPlantUnlocked(User user, String plantType) {
        // در اینجا باید چک کنی آیا کاربر این گیاه را در Collection خود دارد یا خیر
        // فعلا برای تست فرض می‌کنیم همه نام‌ها معتبر و باز هستند (این بخش را به منطق خودت وصل کن)
        return true;
    }

    public List<String> getSelectedPlants() {
        return selectedPlants;
    }

    public String addPlant(User user, String plantType) {
        if (!isPlantUnlocked(user, plantType)) {
            return "ERR_LOCKED_OR_NOT_FOUND"; // گیاه قفل است یا وجود ندارد
        }
        if (selectedPlants.contains(plantType)) {
            return "ERR_ALREADY_SELECTED"; // قبلاً انتخاب شده
        }
        if (selectedPlants.size() >= MAX_SLOTS) {
            return "ERR_FULL"; // ظرفیت تکمیل است
        }

        selectedPlants.add(plantType);
        return "SUCCESS";
    }

    public String removePlant(String plantType) {
        if (!selectedPlants.contains(plantType)) {
            return "ERR_NOT_SELECTED"; // این گیاه اصلاً انتخاب نشده است
        }

        selectedPlants.remove(plantType);
        return "SUCCESS";
    }

    public String boostPlant(User user, String plantType) {
        if (!isPlantUnlocked(user, plantType)) {
            return "ERR_NOT_FOUND"; // گیاه وجود ندارد
        }
        if (boostedPlants.contains(plantType)) {
            return "ERR_ALREADY_BOOSTED"; // قبلا بوست شده
        }
        if (user.getDiamonds() < 2) {
            return "ERR_NOT_ENOUGH_DIAMONDS"; // الماس کافی نیست
        }

        // کسر دو الماس از کاربر
        user.setDiamonds(user.getDiamonds() - 2);
        boostedPlants.add(plantType);
        return "SUCCESS";
    }

    // متدی برای ریست کردن وضعیت قبل از شروع انتخاب‌های جدید
    public void resetSelection() {
        selectedPlants.clear();
        boostedPlants.clear();
    }
}