package model.plant.plants;

import model.game.GameSession;
import model.plant.Plant;
import model.plant.PlantTag;
import model.plant.PlantType;
import model.plant.interfaces.IExplosive;

public class IcebergLettuce extends Plant implements IExplosive {

    private int currentSunCost = 0;
    private int currentCooldown = 20;
    private int freezeTimeTicks = 100;
    private int level = 1;

    public IcebergLettuce() {
        super("iceberglettuce", PlantType.EXPLOSIVE, 0, 20, 300, PlantTag.TRAP, PlantTag.ICE);
    }

    @Override
    public void onTick(GameSession session) {
        if (isTransformedToCat() || isOctopused() || isFrozenSolid()) return;

        if (isFeedActive()) {
            System.out.println("Plant Food: یخ زدن تمامی زامبی‌های موجود در تصویر! (مدت: " + (freezeTimeTicks/10) + " ثانیه)");
            // for (Zombie z : session.getAllZombies()) { z.applyFreeze(freezeTimeTicks); }
            decayFeedEffect();
            return;
        }
    }

    @Override
    public void explode(GameSession session) {
        System.out.println(getName() + " منفجر شد و زامبی را فریز کرد! (مدت: " + (freezeTimeTicks/10) + " ثانیه)");
        this.takeDamage(9999);
    }

    public void applyUpgradeLevel(int newLevel) {
        this.level = newLevel;
        if (level >= 2) this.currentCooldown -= 2;
        if (level >= 3) this.freezeTimeTicks += 20;
        if (level >= 4) this.currentSunCost = 0;
    }

    @Override
    public int getSunCost() { return currentSunCost; }

    @Override
    public int getCooldownTicks() { return currentCooldown; }
}