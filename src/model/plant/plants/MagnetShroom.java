package model.plant.plants;

import model.game.GameSession;
import model.plant.Plant;
import model.plant.PlantTag;
import model.plant.PlantType;

public class MagnetShroom extends Plant {

    private int actionInterval = 100; // 10 ثانیه = 100 تیک
    private int tickCounter = 0;
    private int rangeBonus = 0;
    private int currentCooldown = 150;
    private int level = 1;

    public MagnetShroom() {
        super("magnetshroom", PlantType.HOMING, 100, 150, 300, PlantTag.SHROOM, PlantTag.MAGIC);
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
            System.out.println("Plant Food: جذب همزمان چندین فلز از سر زامبی‌ها در نقشه و هضم فوری آن‌ها!");
            // session.disarmMultipleZombies();
            decayFeedEffect();
            return;
        }

        tickCounter += 1;
        if (tickCounter >= actionInterval) {
            disarmZombie(session);
            tickCounter = 0; // بعد از جذب فلز، برای هضم کردن زمان می‌برد
        }
    }

    public void disarmZombie(GameSession session) {
        System.out.println(getName() + " تلاش برای جذب یک شی فلزی در برد مجاز (+ " + rangeBonus + " Tile)...");
    }

    public void applyUpgradeLevel(int newLevel) {
        this.level = newLevel;
        if (level >= 2) this.rangeBonus += 1;
        if (level >= 3) this.currentCooldown -= 50;
        if (level >= 4) {
            this.setMaxHealth(this.getMaxHealth() + 200);
            this.setHealth(this.getMaxHealth());
        }
    }

    @Override
    public int getCooldownTicks() { return currentCooldown; }
}