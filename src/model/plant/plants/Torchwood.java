package model.plant.plants;

import model.game.GameSession;
import model.plant.Plant;
import model.plant.PlantTag;
import model.plant.PlantType;

public class Torchwood extends Plant {

    private boolean isBlueFlame = false;
    private boolean applyAoEOnDeath = false; // برای Lvl3
    private int currentSunCost = 175;
    private int currentCooldown = 5;
    private int level = 1;

    public Torchwood() {
        super("torchwood", PlantType.MODIFIER, 175, 5, 300, PlantTag.FIRE);
    }

    @Override
    public void onTick(GameSession session) {
        if (isTransformedToCat() || isOctopused() || isFrozenSolid()) return;

        if (isFeedActive()) {
            System.out.println("Plant Food: ایجاد شعله آبی دائم! (دمیج تیرهای عبوری ۳ برابر می‌شود)");
            this.isBlueFlame = true;
            decayFeedEffect();
            return;
        }

        if (getHealth() <= 0 && applyAoEOnDeath) {
            System.out.println(getName() + " نابود شد و انفجار آتشین مساحتی رخ داد! (AoE on Death)");
        }
    }

    // پرتابه پژشوتورها وقتی از این تایل رد می‌شوند این متد را می‌خوانند
    public void modifyProjectile(/* Projectile p */) {
        if (isBlueFlame) {
            // p.setDamage(p.getDamage() * 3);
            // p.setFlameType("BLUE");
        } else {
            // p.setDamage(p.getDamage() * 2);
            // p.setFlameType("NORMAL");
        }
    }

    public void applyUpgradeLevel(int newLevel) {
        this.level = newLevel;
        if (level >= 2) {
            this.setMaxHealth(this.getMaxHealth() + 300);
            this.setHealth(this.getMaxHealth());
        }
        if (level >= 3) this.applyAoEOnDeath = true;
        if (level >= 4) this.currentSunCost -= 25;
    }

    @Override
    public int getSunCost() { return currentSunCost; }

    @Override
    public int getCooldownTicks() { return currentCooldown; }
}