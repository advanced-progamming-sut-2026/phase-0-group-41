package model.plant.plants;

import model.game.GameSession;
import model.plant.Plant;
import model.plant.PlantTag;
import model.plant.PlantType;
import model.projectile.Projectile;

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
        // === تغییرات اینجاست ===
        if (isFrozenSolid()) {
            handleIceMelting(session);
            return;
        }
        if (isTransformedToCat() || isOctopused()) return;
        // =======================

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
    public void modifyProjectile(Projectile p) {
        if(p instanceof model.projectile.PeaProjectile) {
            if (!p.isFire()) {
                if(isBlueFlame) {
                    p.setDamage(p.getDamage() * 3);
                }
                else {
                    p.setDamage(p.getDamage() * 2);
                }

                p.setFire(true);
                p.setIce(false); // اگر پرتابه یخی بود، دیگر یخ نخواهد بود
                System.out.println("پرتابه از روی Torchwood گذشت و آتشین/تقویت شد!");
            }
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