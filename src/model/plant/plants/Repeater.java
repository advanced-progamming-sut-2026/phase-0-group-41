package model.plant.plants;

import model.game.GameSession;
import model.plant.Plant;
import model.plant.PlantTag;
import model.plant.PlantType;
import model.plant.interfaces.IShooter;

public class Repeater extends Plant implements IShooter {

    private int damage = 20;
    private int shootInterval = 15;
    private int tickCounter = 0;
    private int currentSunCost = 200;
    private int level = 1;

    public Repeater() {
        super("repeater", PlantType.SHOOTER, 200, 5, 300, PlantTag.DAY);
    }

    @Override
    public void onTick(GameSession session) {
        // === تغییرات اینجاست ===
        if (isFrozenSolid()) {
            handleIceMelting(session);
            return;
        }
        if (isTransformedToCat() || isOctopused()) return;
        // =======================

        if (isFeedActive()) {
            shoot(session);
            decayFeedEffect();

            // اگر اثر فید در این تیک تمام شد، نخود غول‌پیکر را شلیک کن
            if (!isFeedActive()) {
                shootGiantPea(session);
            }
            return;
        }

        tickCounter++;
        if (tickCounter >= shootInterval) {
            shoot(session); // شلیک اول
            // شلیک دوم با فاصله بسیار کم (از طریق سیستم پرتابه هندل می‌شود، یا دو بار متوالی فراخوانی می‌شود)
            shoot(session);
            tickCounter = 0;
        }
    }

    @Override
    public void shoot(GameSession session) {
        System.out.println(getName() + " یک نخود با دمیج " + damage + " شلیک کرد.");
    }

    private void shootGiantPea(GameSession session) {
        int giantDamage = damage * 20;
        System.out.println(getName() + " ***نخود غول‌پیکر*** با دمیج " + giantDamage + " شلیک کرد!");
    }

    @Override
    public void feed(GameSession session) {
        super.feed(session);
        System.out.println("Plant Food فعال شد: شلیک رگباری سنگین Repeater!");
    }

    public void applyUpgradeLevel(int newLevel) {
        this.level = newLevel;
        if (level >= 2) this.damage += 10;
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