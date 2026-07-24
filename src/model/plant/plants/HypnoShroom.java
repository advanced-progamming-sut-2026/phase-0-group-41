package model.plant.plants;

import model.game.GameSession;
import model.plant.Plant;
import model.plant.PlantTag;
import model.plant.PlantType;

public class HypnoShroom extends Plant {

    private boolean zombieHpBuff = false; // برای Lvl3
    private boolean zombieDmgBuff = false; // برای Lvl4
    private int currentSunCost = 125;
    private int currentCooldown = 20;
    private int level = 1;

    public HypnoShroom() {
        super("hypnoshroom", PlantType.MODIFIER, 125, 20, 300, PlantTag.SHROOM, PlantTag.MAGIC);
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

        // قارچ هیپنوتیزم در محیط تغییری ایجاد نمی‌کند، بلکه منتظر می‌ماند تا خورده شود.
        // وقتی بازیکن به آن فود می‌دهد، isFeedActive برابر true می‌شود اما درجا از بین نمی‌رود،
        // بلکه می‌ماند تا زامبی آن را بخورد.
    }

    // متدی که GameSession به هنگام خورده شدن گیاه صدا می‌زند
    public void onEaten(GameSession session /*, Zombie attacker*/) {
        if (isFeedActive()) {
            System.out.println("Plant Food فعال بود! زامبی خورنده تبدیل به غول متفق (Gargantuar) شد!");
            decayFeedEffect(); // خاموش کردن فود
        } else {
            System.out.println(getName() + " خورده شد! زامبی به نفع بازیکن هیپنوتیزم شد.");
        }

        if (zombieHpBuff) System.out.println("-> زامبی هیپنوتیزم شده جان بیشتری دارد! (Lvl 3 Buff)");
        if (zombieDmgBuff) System.out.println("-> زامبی هیپنوتیزم شده دمیج بیشتری می‌دهد! (Lvl 4 Buff)");

        this.takeDamage(9999); // نابودی گیاه
    }

    public void applyUpgradeLevel(int newLevel) {
        this.level = newLevel;
        if (level >= 2) this.currentSunCost -= 25;
        if (level >= 3) this.zombieHpBuff = true;
        if (level >= 4) this.zombieDmgBuff = true;
    }

    @Override
    public int getSunCost() { return currentSunCost; }

    @Override
    public int getCooldownTicks() { return currentCooldown; }
}