package model.plant.plants;

import model.game.GameSession;
import model.plant.Plant;
import model.plant.PlantTag;
import model.plant.PlantType;

public class Garlic extends Plant {

    private int currentCooldown = 20;
    private int level = 1;

    public Garlic() {
        super("garlic", PlantType.WALL_NUT, 50, 20, 300, PlantTag.MOVE_ZOMBIES);
    }

    @Override
    public void onTick(GameSession session) {
        if (isTransformedToCat() || isOctopused() || isFrozenSolid()) return;

        if (isFeedActive()) {
            System.out.println("Plant Food: انتقال اجباری تمام زامبی‌های این لاین به لاین‌های دیگر!");
            // session.forceMoveZombiesInLane(this.getLane());
            decayFeedEffect();
            return;
        }
    }

    // توسط GameSession صدا زده می‌شود
    public void onEaten(GameSession session /*, Zombie attacker*/) {
        System.out.println("زامبی سیر را گاز گرفت و به لاین مجاور فرار کرد!");
        // attacker.moveToAdjacentLane();
    }

    public void applyUpgradeLevel(int newLevel) {
        this.level = newLevel;
        if (level >= 2) {
            this.setMaxHealth(this.getMaxHealth() + 150);
            this.setHealth(this.getMaxHealth());
        }
        if (level >= 3) this.currentCooldown -= 3;
        if (level >= 4) {
            this.setMaxHealth(this.getMaxHealth() + 250);
            this.setHealth(this.getMaxHealth());
        }
    }

    @Override
    public int getCooldownTicks() { return currentCooldown; }
}