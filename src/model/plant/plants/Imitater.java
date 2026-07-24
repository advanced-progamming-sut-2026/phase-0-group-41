package model.plant.plants;

import model.game.GameSession;
import model.plant.Plant;
import model.plant.PlantType;

public class Imitater extends Plant {

    private Plant copiedPlant;
    private int currentCooldown = 0;
    private boolean plantFoodOnEntrance = false; // برای لول 4
    private int level = 1;

    public Imitater(Plant plantToCopy) {
        super("imitater", PlantType.MODIFIER, 0, 0, 0);
        this.copiedPlant = plantToCopy;
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
            System.out.println("Plant Food: اجرای قابلیت فود بر اساس گیاه کپی‌شده (" + copiedPlant.getName() + ")");
            decayFeedEffect();
            return;
        }
    }

    @Override
    public void feed(GameSession session) {
        System.out.println(getName() + " فود را روی گیاه کپی‌شده اعمال می‌کند.");
    }

    public void applyUpgradeLevel(int newLevel) {
        this.level = newLevel;
        if (level >= 2) this.currentCooldown = Math.max(0, this.currentCooldown - 2);
        if (level >= 3) {
            // کاهش هزینه یا تنظیمات مربوطه
        }
        if (level >= 4) this.plantFoodOnEntrance = true;
    }

    @Override
    public int getCooldownTicks() { return currentCooldown; }
}