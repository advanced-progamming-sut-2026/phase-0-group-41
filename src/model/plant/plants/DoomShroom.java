package model.plant.plants;

import model.game.GameSession;
import model.plant.Plant;
import model.plant.PlantTag;
import model.plant.PlantType;
import model.plant.interfaces.IExplosive;

public class DoomShroom extends Plant implements IExplosive {

    private int damage = 1800;
    private int currentSunCost = 125;
    private int currentCooldown = 15;
    private boolean hasExploded = false;
    private int level = 1;

    public DoomShroom() {
        super("doomshroom", PlantType.EXPLOSIVE, 125, 15, 0, PlantTag.SHROOM);
    }

    @Override
    public void onTick(GameSession session) {
        if (isTransformedToCat() || isOctopused() || isFrozenSolid()) return;

        if (!hasExploded) {
            explode(session);
            hasExploded = true;
        }
    }

    @Override
    public void explode(GameSession session) {
        System.out.println(getName() + " انفجار مهیب در کل باغچه! (دمیج: " + damage + ") ایجاد یک گودال غیرقابل کشت.");
        this.takeDamage(9999);
    }

    @Override
    public void feed(GameSession session) {
        System.out.println(getName() + " مصرفی آنی است و Plant Food دریافت نمی‌کند.");
    }

    public void applyUpgradeLevel(int newLevel) {
        this.level = newLevel;
        if (level >= 2) this.currentCooldown -= 5;
        if (level >= 3) this.damage += 800;
        if (level >= 4) this.currentSunCost -= 50;
    }

    @Override
    public int getSunCost() { return currentSunCost; }

    @Override
    public int getCooldownTicks() { return currentCooldown; }
}