package view;

import controller.PlantSelectionController;
import controller.MenuController;
import model.menu.MenuType;
import model.user.User;
import util.CommandLine;
import java.util.List;

public class PlantSelectionView {
    private final PlantSelectionController controller;
    private final ConsoleView consoleView;
    private final MenuController menuController;

    public PlantSelectionView(PlantSelectionController controller, ConsoleView consoleView, MenuController menuController) {
        this.controller = controller;
        this.consoleView = consoleView;
        this.menuController = menuController;
    }

    public boolean checkCommand(User user, List<String> t, CommandLine cmd) {

        // ۱. نمایش تمام گیاهان (show all plants)
        if (t.size() == 3 && t.get(0).equals("show") && t.get(1).equals("all") && t.get(2).equals("plants")) {
            consoleView.printMessage("--- All Plants ---");

            // دریافت لیست واقعی گیاهان از فکتوری شما
            List<String> allPlants = model.plant.PlantFactory.allPlantNames();

            // چاپ نام گیاهان به صورت ستونی یا خطی
            for (String plantName : allPlants) {
                consoleView.printMessage("- " + plantName);
            }
            return true;
        }

// ۲. نمایش گیاهان در دسترس (آنلاک شده) (show available plants)
        if (t.size() == 3 && t.get(0).equals("show") && t.get(1).equals("available") && t.get(2).equals("plants")) {
            // دریافت لیست گیاهان آنلاک شده از پروفایل کاربر
            java.util.Set<String> unlocked = user.getUnlockedPlants();
            
            if (unlocked.isEmpty()) {
                consoleView.printMessage("شما هیچ گیاهی در دسترس ندارید.");
            } else {
                consoleView.printMessage("--- Available Plants ---");
                for (String plant : unlocked) {
                    consoleView.printMessage("- " + plant);
                }
            }
            return true;
        }

        // نمایش گیاهانی که برای ورود به بازی انتخاب میشود. تو داک نیست خودم اضافه کردم (show selected plants)
        if (t.size() == 3 && t.get(0).equals("show") && t.get(1).equals("selected") && t.get(2).equals("plants")) {
            List<String> selected = controller.getSelectedPlants();
            if (selected.isEmpty()) {
                consoleView.printMessage("هیچ گیاهی انتخاب نشده است. (ظرفیت: 8)");
            } else {
                consoleView.printMessage("--- Selected Plants ---");
                for (String plant : selected) {
                    consoleView.printMessage("- " + plant);
                }
            }
            return true;
        }

        // ۳. اضافه کردن گیاه (add plant -t <type>)
        if (t.size() >= 2 && t.get(0).equals("add") && t.get(1).equals("plant")) {
            String type = cmd.get("t");
            if (type == null) {
                consoleView.printError("فرمت دستور اشتباه است. الگو: add plant -t <type>");
                return true;
            }

            String result = controller.addPlant(user, type);
            switch (result) {
                case "ERR_LOCKED_OR_NOT_FOUND":
                    consoleView.printError("این گیاه قفل است یا وجود ندارد.");
                    break;
                case "ERR_ALREADY_SELECTED":
                    consoleView.printError("این گیاه قبلاً انتخاب شده است.");
                    break;
                case "ERR_FULL":
                    consoleView.printError("ظرفیت انتخاب گیاه برای این مرحله پر شده است.");
                    break;
                case "SUCCESS":
                    consoleView.printMessage("گیاه " + type + " با موفقیت به لیست اضافه شد.");
                    break;
            }
            return true;
        }

        // ۴. حذف گیاه (remove plant -t <type>)
        if (t.size() >= 2 && t.get(0).equals("remove") && t.get(1).equals("plant")) {
            String type = cmd.get("t");
            if (type == null) {
                consoleView.printError("فرمت دستور اشتباه است. الگو: remove plant -t <type>");
                return true;
            }

            String result = controller.removePlant(type);
            if (result.equals("ERR_NOT_SELECTED")) {
                consoleView.printError("این گیاه در لیست انتخاب‌های شما وجود ندارد.");
            } else if (result.equals("SUCCESS")) {
                consoleView.printMessage("گیاه " + type + " با موفقیت از لیست حذف شد.");
            }
            return true;
        }

        // ۵. بوست کردن گیاه (boost plant -t <type>)
        if (t.size() >= 2 && t.get(0).equals("boost") && t.get(1).equals("plant")) {
            String type = cmd.get("t");
            if (type == null) {
                consoleView.printError("فرمت دستور اشتباه است. الگو: boost plant -t <type>");
                return true;
            }

            String result = controller.boostPlant(user, type);
            switch (result) {
                case "ERR_NOT_FOUND":
                    consoleView.printError("گیاه مورد نظر یافت نشد.");
                    break;
                case "ERR_ALREADY_BOOSTED":
                    consoleView.printError("این گیاه قبلاً برای این مرحله بوست شده است.");
                    break;
                case "ERR_NOT_ENOUGH_DIAMONDS":
                    consoleView.printError("الماس کافی برای بوست کردن این گیاه ندارید (نیاز به 2 الماس).");
                    break;
                case "SUCCESS":
                    consoleView.printMessage("گیاه " + type + " با موفقیت بوست شد!");
                    break;
            }
            return true;
        }

        // ۶. شروع بازی (start game)
        if (t.size() == 2 && t.get(0).equals("start") && t.get(1).equals("game")) {
            if (controller.getSelectedPlants().isEmpty()) {
                consoleView.printError("اخطار: شما هیچ گیاهی انتخاب نکرده‌اید! لطفا حداقل یک گیاه اضافه کنید.");
                return true;
            }
            // اینجا باید دستور ورود به منوی نبرد/بازی اصلی (Play/Battle) را صادر کنی
            consoleView.printMessage("در حال ورود به زمین بازی...");
            menuController.setCurrentMenu(MenuType.IN_GAME);
            menuController.startGame(controller.getSelectedPlants());
            controller.resetSelection(); // ریست کردن انتخاب‌ها بعد از شروع بازی
            return true;
        }

        return false;
    }
}