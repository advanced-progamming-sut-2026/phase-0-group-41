package controller;

import model.plant.Plant;
import model.plant.PlantFactory;
import model.user.User;
import model.user.UserManager;
import model.zombie.Zombie;
import model.zombie.ZombieFactory;
import util.CommandLine;
import view.CollectionView;

import java.util.List;

public class CollectionController {

    private final UserManager userManager;
    private final CollectionView view;

    public CollectionController(UserManager userManager) {
        this.userManager = userManager;
        this.view = new CollectionView();
    }

    // متد اصلی پردازش دستورات کلکسیون
    public boolean handle(User loggedInUser, List<String> t, CommandLine cmd) {
        if (t.size() < 3) {
            return false;
        }

        String subCommand = t.get(2);

        switch (subCommand) {
            case "show-plants":
                view.printList(loggedInUser.getUnlockedPlants(), "لیست گیاهان کسب شده");
                return true;

            case "show-all-plants":
                view.printList(PlantFactory.allPlantNames(), "لیست تمام گیاهان بازی");
                return true;

            case "show-zombies":
                view.printList(loggedInUser.getSeenZombies(), "لیست زامبی‌های مشاهده شده");
                return true;

            case "show-all-zombies":
                // نکته: اگر متد allZombieNames در ZombieFactory نیست، باید آن را اضافه کنید
                view.printList(ZombieFactory.allZombieNames(), "لیست تمام زامبی‌های بازی");
                return true;

            case "show-plant":
                return handleShowPlant(loggedInUser, cmd);

            case "show-zombie":
                return handleShowZombie(cmd);

            case "upgrade-plant":
                return handleUpgradePlant(loggedInUser, cmd);

            case "purchase-plant":
                return handlePurchasePlant(loggedInUser, cmd);

            default:
                return false;
        }
    }

    private boolean handleShowPlant(User user, CommandLine cmd) {
        String plantName = cmd.get("p");
        if (plantName == null) {
            view.printError("نام گیاه وارد نشده است.");
            return true;
        }
        if (!PlantFactory.isKnown(plantName)) {
            view.printError("این گیاه در بازی وجود ندارد.");
            return true;
        }
        Plant plant = PlantFactory.create(plantName);
        int level = user.getPlantLevel(plantName);
        int packets = user.getSeedPackets(plantName);
        view.printPlantDetails(plant, level, packets);
        return true;
    }

    private boolean handleShowZombie(CommandLine cmd) {
        String zombieName = cmd.get("z");
        if (zombieName == null) {
            view.printError("نام زامبی وارد نشده است.");
            return true;
        }
        try {
            Zombie zombie = ZombieFactory.create(zombieName);
            view.printZombieDetails(zombie);
        } catch (IllegalArgumentException e) {
            view.printError("این زامبی در بازی وجود ندارد.");
        }
        return true;
    }

    private boolean handleUpgradePlant(User user, CommandLine cmd) {
        String plantName = cmd.get("p");
        if (plantName == null) {
            view.printError("نام گیاه وارد نشده است.");
            return true;
        }
        if (!user.getUnlockedPlants().contains(plantName)) {
            view.printError("شما هنوز این گیاه را کسب نکرده‌اید.");
            return true;
        }

        // مقادیر پیش‌فرض ارتقا (طبق داکیومنت بعداً نهایی می‌شود)
        int upgradeCost = 1000;
        int requiredPackets = 50;

        if (user.upgradePlant(plantName, upgradeCost, requiredPackets)) {
            userManager.save();
            view.printMessage("ارتقای گیاه با موفقیت انجام شد. سطح جدید: " + user.getPlantLevel(plantName));
        } else {
            view.printError("سکه یا Seed Packet کافی برای ارتقای این گیاه ندارید.");
        }
        return true;
    }

    private boolean handlePurchasePlant(User user, CommandLine cmd) {
        String plantName = cmd.get("p");
        if (plantName == null) {
            view.printError("نام گیاه وارد نشده است.");
            return true;
        }
        if (user.getUnlockedPlants().contains(plantName)) {
            view.printError("شما قبلاً این گیاه را خریداری کرده‌اید.");
            return true;
        }
        if (!PlantFactory.isKnown(plantName)) {
            view.printError("این گیاه در بازی وجود ندارد.");
            return true;
        }

        // هزینه خرید ۲۰۰۰ سکه
        if (!user.spendCoins(2000)) {
            view.printError("سکه کافی برای خرید این گیاه ندارید (۲۰۰۰ سکه نیاز است).");
            return true;
        }

        user.getUnlockedPlants().add(plantName);
        userManager.save();
        view.printMessage("گیاه " + plantName + " با موفقیت خریداری شد.");
        return true;
    }
}