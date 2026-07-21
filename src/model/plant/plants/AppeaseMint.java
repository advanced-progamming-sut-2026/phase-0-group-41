package model.plant.plants;

import model.game.GameSession;
import model.plant.Plant;
import model.plant.PlantType;
import model.plant.interfaces.IExplosive;

public class AppeaseMint extends Plant implements IExplosive {
    private int currentSunCost = 0;
    private int currentCooldown = 85;
    private int durationBonusTicks = 0;
    private boolean hasTriggered = false;
    private int level = 1;

    public AppeaseMint() {
        super("appeasemint", PlantType.SHOOTER, 0, 85, 0);
    }

    @Override
    public void onTick(GameSession session) {
        if (isTransformedToCat() || isOctopused() || isFrozenSolid()) return;
        if (!hasTriggered) {
            explode(session);
            hasTriggered = true;
        }
    }

    @Override
    public void explode(GameSession session) {
        System.out.println(getName() + " فعال شد و Plant Food موقت به تمام گیاهان Shooter اعمال کرد!");
        this.takeDamage(9999);
    }

    @Override
    public void feed(GameSession session) {
        System.out.println(getName() + " مصرفی آنی است و فود دریافت نمی‌کند.");
    }

    public void applyUpgradeLevel(int newLevel) {
        this.level = newLevel;
        if (level >= 2) this.durationBonusTicks += 10;
        if (level >= 3) this.currentCooldown -= 5;
    }

    @Override
    public int getSunCost() { return currentSunCost; }
    @Override
    public int getCooldownTicks() { return currentCooldown; }
}