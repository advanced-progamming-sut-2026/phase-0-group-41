package model.plant.plants;

import model.game.GameSession;
import model.plant.Plant;
import model.plant.PlantTag;
import model.plant.PlantType;
import model.plant.interfaces.IMeleeAttacker;

public class PhatBeet extends Plant implements IMeleeAttacker {

    private int damage = 15;
    private int actionInterval = 20; // 2 ثانیه
    private int tickCounter = 0;
    private int currentCooldown = 50;
    private int level = 1;

    public PhatBeet() {
        super("phatbeet", PlantType.MELEE_ATTACKER, 150, 50, 300, PlantTag.AOE);
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
            System.out.println("Plant Food: ضربه صوتی بسیار قدرتمند به تمام زامبی‌های اطراف!");
            decayFeedEffect();
            return;
        }

        tickCounter += 1.0;
        if (tickCounter >= actionInterval) {
            attackMelee(session);
            tickCounter -= actionInterval;
        }
    }

    @Override
    public void attackMelee(GameSession session) {
        boolean hit = false;
        // اسکن تمام زامبی‌های بازی برای پیدا کردن آن‌هایی که در شعاع ۳x۳ هستند
        for (model.zombie.Zombie z : session.getAliveZombies()) {
            if (!z.isDead() && Math.abs(z.getRow() - getRow()) <= 1 && Math.abs(z.getXPosition() - getCol()) <= 1.5) {
                z.takeDamage(damage, model.zombie.DamageType.NORMAL);
                hit = true;
            }
        }
        if (hit) System.out.println(getName() + " ضربه صوتی مساحتی وارد کرد! (دمیج: " + damage + ")");
    }

    public void applyUpgradeLevel(int newLevel) {
        this.level = newLevel;
        if (level >= 2) this.damage += 10;
        if (level >= 3) this.actionInterval *= 0.9;
        if (level >= 4) {
            this.setMaxHealth(this.getMaxHealth() + 200);
            this.setHealth(this.getMaxHealth());
        }
    }

    @Override
    public int getCooldownTicks() { return currentCooldown; }
}