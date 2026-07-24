package model.plant.plants;

import model.game.GameSession;
import model.plant.Plant;
import model.plant.PlantType;
import model.plant.interfaces.IExplosive;

public class Grapeshot extends Plant implements IExplosive {

    private int damage = 1800;
    private int currentSunCost = 150;
    private int currentCooldown = 35;
    private int bounceCount = 3;
    private boolean hasExploded = false;
    private int level = 1;

    public Grapeshot() {
        super("grapeshot", PlantType.EXPLOSIVE, 150, 35, 0);
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

        if (!hasExploded) {
            explode(session);
            hasExploded = true;
        }
    }

    @Override
    public void explode(GameSession session) {
        System.out.println(getName() + " منفجر شد! (مساحت ۳x۳، دمیج: " + damage + ")");
        System.out.println("پرتاب حبه‌های کمانه‌کننده (تعداد کمانه: " + bounceCount + ") که پس از ۵ ثانیه نابود می‌شوند.");
        this.takeDamage(9999);
    }

    @Override
    public void feed(GameSession session) {
        System.out.println(getName() + " مصرفی آنی است و Plant Food دریافت نمی‌کند.");
    }

    public void applyUpgradeLevel(int newLevel) {
        this.level = newLevel;
        if (level >= 2) this.damage += 600;
        if (level >= 3) this.bounceCount += 1;
        if (level >= 4) this.currentSunCost -= 25;
    }

    @Override
    public int getSunCost() { return currentSunCost; }

    @Override
    public int getCooldownTicks() { return currentCooldown; }
}