package model.plant.plants;

import model.game.GameSession;
import model.plant.Plant;
import model.plant.PlantTag;
import model.plant.PlantType;
import model.plant.interfaces.IShooter;

public class Citron extends Plant implements IShooter {

    private int damage = 800;
    private int shootInterval = 90; // 9 ثانیه
    private int tickCounter = 0;
    private int currentSunCost = 350;
    private int level = 1;

    public Citron() {
        super("citron", PlantType.SHOOTER, 350, 5, 300, PlantTag.CHARGE);
    }

    @Override
    public void onTick(GameSession session) {
        if (isTransformedToCat() || isOctopused() || isFrozenSolid()) return;

        if (isFeedActive()) {
            System.out.println("Plant Food: شلیک توپ پلاسمایی پاکسازی‌کننده کل لاین!");
            decayFeedEffect();
            return;
        }

        tickCounter++;
        if (tickCounter >= shootInterval) {
            shoot(session);
            tickCounter = 0; // شروع مجدد شارژ
        }
    }

    @Override
    public void shoot(GameSession session) {
        System.out.println(getName() + " شلیک تیر سنگین مستقیم پلاسما! (دمیج: " + damage + ")");
    }

    public void applyUpgradeLevel(int newLevel) {
        this.level = newLevel;
        if (level >= 2) this.shootInterval -= 10; // کاهش 1 ثانیه از زمان شارژ
        if (level >= 3) this.damage += 150;
        if (level >= 4) this.currentSunCost -= 50;
    }

    @Override
    public int getSunCost() {
        return currentSunCost;
    }
}