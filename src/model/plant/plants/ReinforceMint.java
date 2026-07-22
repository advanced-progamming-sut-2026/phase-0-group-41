package model.plant.plants;

import model.game.GameSession;
import model.plant.Plant;
import model.plant.PlantType;
import model.plant.interfaces.IExplosive;

public class ReinforceMint extends Plant implements IExplosive {
    private int currentCooldown = 85;
    private boolean hasTriggered = false;
    private int level = 1;

    public ReinforceMint() {
        super("reinforcemint", PlantType.WALL_NUT, 0, 85, 0);
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
        System.out.println(getName() + " فعال شد و Plant Food موقت به تمام گیاهان Wall-nut اعمال کرد!");
        this.takeDamage(9999);
    }

    @Override
    public void feed(GameSession session) {
        System.out.println(getName() + " مصرفی آنی است و فود دریافت نمی‌کند.");
    }

    public void applyUpgradeLevel(int newLevel) {
        this.level = newLevel;
        if (level >= 3) this.currentCooldown -= 5;
    }
    @Override
    public int getCooldownTicks() { return currentCooldown; }
}