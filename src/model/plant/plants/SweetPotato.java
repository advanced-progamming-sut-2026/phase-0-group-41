package model.plant.plants;

import model.game.GameSession;
import model.plant.Plant;
import model.plant.PlantTag;
import model.plant.PlantType;

public class SweetPotato extends Plant {

    private int currentCooldown = 20;
    private int level = 1;

    public SweetPotato() {
        super("sweetpotato", PlantType.WALL_NUT, 150, 20, 3000, PlantTag.MOVE_ZOMBIES);
    }

    @Override
    public void onTick(GameSession session) {
        if (isTransformedToCat() || isOctopused() || isFrozenSolid()) return;

        // عمل جذب کردن در خود GameSession مدیریت می‌شود (زامبی‌ها مسیرشان را به سمت این گیاه کج می‌کنند)

        if (isFeedActive()) {
            System.out.println("Plant Food: جذب تمام زامبی‌های اطراف و بازیابی کامل جان!");
            this.setHealth(this.getMaxHealth());
            // جلسه بازی زامبی‌ها را فوراً به این تایل می‌کشد
            decayFeedEffect();
            return;
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