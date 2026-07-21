package model.plant.plants;

import model.game.GameSession;
import model.plant.Plant;
import model.plant.PlantTag;
import model.plant.PlantType;
import model.plant.interfaces.IShooter;

public class PeaPod extends Plant implements IShooter {

    private int damagePerPea = 20;
    private int shootInterval = 15; // 1.5s
    private int tickCounter = 0;
    private int currentSunCost = 125;
    private int heads = 1; // بین 1 تا 5
    private int level = 1;

    public PeaPod() {
        super("peapod", PlantType.SHOOTER, 125, 5, 300, PlantTag.PEA, PlantTag.STACK);
    }

    // متدی برای اضافه کردن سر جدید (وقتی بازیکن دوباره روی این گیاه کلیک می‌کند)
    public void addHead() {
        if (this.heads < 5) {
            this.heads++;
            System.out.println("یک سر جدید به Pea Pod اضافه شد! تعداد سرها: " + heads);
        }
    }

    @Override
    public void onTick(GameSession session) {
        if (isTransformedToCat() || isOctopused() || isFrozenSolid()) return;

        if (isFeedActive()) {
            shootGiantPeas(session);
            decayFeedEffect();
            return;
        }

        tickCounter++;
        if (tickCounter >= shootInterval) {
            shoot(session);
            tickCounter = 0;
        }
    }

    @Override
    public void shoot(GameSession session) {
        int totalDamage = heads * damagePerPea;
        System.out.println(getName() + " تعداد " + heads + " نخود با مجموع دمیج " + totalDamage + " شلیک کرد.");
    }

    private void shootGiantPeas(GameSession session) {
        int giantDamage = damagePerPea * 20;
        System.out.println("Plant Food: شلیک " + heads + " نخود غول‌پیکر با دمیج " + giantDamage + " به ازای هر سر!");
    }

    @Override
    public void feed(GameSession session) {
        super.feed(session);
    }

    public void applyUpgradeLevel(int newLevel) {
        this.level = newLevel;
        if (level >= 2) this.damagePerPea += 10;
        if (level >= 3) {
            this.setMaxHealth(this.getMaxHealth() + 200);
            this.setHealth(this.getMaxHealth());
        }
        if (level >= 4) this.currentSunCost -= 25;
    }

    @Override
    public int getSunCost() {
        return currentSunCost;
    }
}