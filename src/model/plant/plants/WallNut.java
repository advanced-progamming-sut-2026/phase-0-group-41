package model.plant.plants;

import model.game.GameSession;
import model.plant.Plant;
import model.plant.PlantType;

public class WallNut extends Plant {

    private int armorHP = 0;
    private int currentCooldown = 20;
    private int level = 1;

    public WallNut() {
        super("wallnut", PlantType.WALL_NUT, 50, 20, 4000);
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
            System.out.println("Plant Food: " + getName() + " زره دائمی به اندازه ۴۰۰۰ جان اضافه‌تر دریافت کرد!");
            this.armorHP = 4000;
            decayFeedEffect();
            return;
        }
    }

    // متدی برای دریافت دمیج که اول زره را کم می‌کند
    @Override
    public void takeDamage(int amount) {
        if (armorHP > 0) {
            armorHP -= amount;
            if (armorHP < 0) {
                super.takeDamage(Math.abs(armorHP));
                armorHP = 0;
            }
        } else {
            super.takeDamage(amount);
        }
    }

    public void applyUpgradeLevel(int newLevel) {
        this.level = newLevel;
        if (level >= 2) {
            this.setMaxHealth(this.getMaxHealth() + 1000);
            this.setHealth(this.getMaxHealth());
        }
        if (level >= 3) this.currentCooldown -= 5;
        if (level >= 4) {
            this.setMaxHealth(this.getMaxHealth() + 1500);
            this.setHealth(this.getMaxHealth());
        }
    }

    @Override
    public int getCooldownTicks() { return currentCooldown; }
}