package model.plant.plants;

import model.game.GameSession;
import model.game.TerrainType;
import model.game.Tile;
import model.plant.Plant;
import model.plant.PlantTag;
import model.plant.PlantType;
import model.plant.interfaces.IExplosive;

public class DoomShroom extends Plant implements IExplosive {

    private int damage = 1800;
    private int currentSunCost = 125;
    private int currentCooldown = 150;
    private boolean hasExploded = false;
    private int level = 1;

    public DoomShroom() {
        super("doomshroom", PlantType.EXPLOSIVE, 125, 150, 0, PlantTag.SHROOM);
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
        // شعاع وسیع‌تر برای نابودی (فاصله ۳ ردیف و ۳ ستون)
        for (model.zombie.Zombie z : session.getAliveZombies()) {
            if (!z.isDead() && Math.abs(z.getRow() - getRow()) <= 3 && Math.abs(z.getXPosition() - getCol()) <= 3.5) {
                z.takeDamage(damage, model.zombie.DamageType.NORMAL);
            }
        }
        
        // تبدیل خانه فعلی به گودال
        model.game.Tile myTile = session.getBoard().getTile(getRow(), getCol());
        if (myTile != null) {
            myTile.setTerrainType(model.game.TerrainType.CRATER);
        }
        
        System.out.println(getName() + " انفجار هسته‌ای کرد و زمین به گودال تبدیل شد!");
        this.takeDamage(9999);
    }

    @Override
    public void feed(GameSession session) {
        System.out.println(getName() + " مصرفی آنی است و Plant Food دریافت نمی‌کند.");
    }

    public void applyUpgradeLevel(int newLevel) {
        this.level = newLevel;
        if (level >= 2) this.currentCooldown -= 50;
        if (level >= 3) this.damage += 800;
        if (level >= 4) this.currentSunCost -= 50;
    }

    @Override
    public int getSunCost() { return currentSunCost; }

    @Override
    public int getCooldownTicks() { return currentCooldown; }
}