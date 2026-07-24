package model.zombie.zombies;

import model.game.Board;
import model.game.GameSession;
import model.game.Season; // اضافه شد
import model.game.Tile;
import model.plant.Plant;
import model.zombie.Zombie;

public class HunterZombie extends Zombie {

    private static final int THROW_COOLDOWN_TICKS = 30;
    private int cooldownTimer = 0;

    public HunterZombie() {
        // نام، جان (۳۵۰)، سرعت، هزینه موج (۲۵۰)، دمیج (۱۰)، فصل غار یخی
        super("hunter", 350, 0.01, 250, 10);
    }

    // === مصونیت در برابر سرما (طبق قوانین غارهای یخی) ===
    @Override
    public void applyChilled(int seconds) { }

    @Override
    public void applyFrozen(int seconds) { }

    @Override
    public void onTick(GameSession session) {
        if (isDead()) return;

        Plant nearestPlant = findNearestPlantInRow(session);
        int col = (int) Math.floor(getXPosition());

        // ۱. منطق پرتاب یخ (فقط به گیاهانی که هنوز یخ نزده‌اند)
        if (nearestPlant != null) {
            if (cooldownTimer <= 0) {
                throwIceAt(nearestPlant);
                cooldownTimer = THROW_COOLDOWN_TICKS;
            } else {
                cooldownTimer--;
            }
        }

        // ۲. منطق حرکت و گاز گرفتن (جدا از منطق پرتاب یخ)
        Tile currentTile = session.getBoard().getTile(getRow(), Math.max(col, 0));
        Plant plantInFront = (currentTile != null) ? currentTile.getPlant() : null;

        // اگر فیزیکی به یک گیاه (یا قالب یخ) رسید، می‌ایستد و گاز می‌گیرد
        if (plantInFront != null && !plantInFront.isDead() && Math.abs(getXPosition() - col) < 0.5) {
            setEating(true);
            plantInFront.takeDamage(getDamagePerTick());

            if (plantInFront.isDead()) {
                currentTile.setPlant(null);
                setEating(false);
            }
        } else {
            // اگر راه جلویش باز بود، حرکت می‌کند
            setEating(false);
            setXPosition(getXPosition() - getSpeed());
        }
    }

    private Plant findNearestPlantInRow(GameSession session) {
        int myCol = (int) Math.floor(getXPosition());
        Board board = session.getBoard();

        for (int c = myCol; c >= 0; c--) {
            Tile tile = board.getTile(getRow(), c);
            if (tile != null) {
                Plant potentialTarget = tile.getPlant();
                // فقط به گیاهی گیر می‌دهد که زنده باشد و هنوز کامل یخ نزده باشد
                if (potentialTarget != null && !potentialTarget.isDead() && !potentialTarget.isFrozenSolid()) {
                    return potentialTarget;
                }
            }
        }
        return null;
    }

    private void throwIceAt(Plant target) {
        target.receiveIceHit();
    }
}