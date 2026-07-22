package model.plant.plants;

import model.game.GameSession;
import model.plant.Plant;
import model.plant.PlantType;

public class Endurian extends Plant {

    private int armorHP = 0;
    private int reflectDamage = 20;
    private int currentSunCost = 100;
    private int currentCooldown = 15;
    private int level = 1;

    public Endurian() {
        super("endurian", PlantType.WALL_NUT, 100, 15, 3000);
    }

    @Override
    public void onTick(GameSession session) {
        if (isTransformedToCat() || isOctopused() || isFrozenSolid()) return;

        if (isFeedActive()) {
            System.out.println("Plant Food: " + getName() + " زره فلزی گرفت و دمیج انعکاسی‌اش موقتاً افزایش یافت!");
            this.armorHP = 3000; // فرض پایه برای زره فلزی
            decayFeedEffect();
            return;
        }
    }

    // متدی که GameSession وقتی زامبی این گیاه را می‌جود صدا می‌زند
    public void onEaten(GameSession session /*, Zombie attacker*/) {
        System.out.println(getName() + " دمیج بازتابی (" + reflectDamage + ") به زامبی مهاجم وارد کرد!");
        // attacker.takeDamage(reflectDamage);
    }

    public void applyUpgradeLevel(int newLevel) {
        this.level = newLevel;
        if (level >= 2) this.reflectDamage += 5;
        if (level >= 3) {
            this.setMaxHealth(this.getMaxHealth() + 1000);
            this.setHealth(this.getMaxHealth());
        }
        if (level >= 4) this.currentSunCost -= 25;
    }

    @Override
    public int getSunCost() { return currentSunCost; }

    @Override
    public int getCooldownTicks() { return currentCooldown; }
}