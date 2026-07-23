package model.zombie.zombies;

import model.game.GameSession;
import model.game.Tile;
import model.game.Board;
import model.plant.Plant;
import model.zombie.Zombie;

import java.util.List;

public class TroglobiteZombie extends Zombie {

    public TroglobiteZombie() {
        // نام، جان، سرعت (کمی کند به خاطر هل دادن یخ)، دمیج پایه، هزینه موج
        super("troglobite", 500, 0.008, 150, 15);
    }

    @Override
    public void onTick(GameSession session) {
        if (isDead()) return;

        int currentCol = (int) Math.floor(getXPosition());
        int nextCol = currentCol - 1; // ستون جلویی زامبی (سمت چپ)

        Board board = session.getBoard();
        Tile currentTile = board.getTile(getRow(), Math.max(currentCol, 0));
        Tile nextTile = (nextCol >= 0) ? board.getTile(getRow(), nextCol) : null;

        // ۱. بررسی منطق هل دادن یخ
        // فرض می‌کنیم کلاس Tile متدی مثل hasIceBlock() یا getIceBlock() دارد.
        if (currentTile != null && currentTile.hasIceBlock()) {

            // اگر کاشی جلویی وجود داشته باشد
            if (nextTile != null) {
                Plant targetPlant = nextTile.getPlant();
                List<Zombie> targetZombies = nextTile.getZombies(); // زامبی‌های مستقر در کاشی جلو
                Zombie hitHypnotizedZombie = null;
                // شرط برخورد یخ با گیاه یا زامبی هیپنوتیزم شده (Hypnotized)
                boolean hitPlant = (targetPlant != null && !targetPlant.isDead());
                for (Zombie zombie : targetZombies) {
                    if (zombie.isHypnotized() && !zombie.isDead()) {
                        hitHypnotizedZombie = zombie;
                        break;
                    }
                }
                boolean hitZombie = (hitHypnotizedZombie != null);

                if (hitPlant || hitZombie) {
                    // الف) یخ درجا از بین می‌رود
                    currentTile.removeIceBlock();

                    // ب) اعمال آسیب سنگین به مانع (گیاه یا زامبی هیپنوتیزم شده)
                    if (hitPlant) {
                        targetPlant.takeDamage(500); // یخ خرد می‌شود و دمیج بالایی می‌زند
                        if (targetPlant.isDead()) nextTile.setPlant(null);
                    } else {
                        hitHypnotizedZombie.takeDamage(500);
                    }
                } else if (!nextTile.hasIceBlock()) {
                    // ج) اگر راه باز بود و یخ دیگری جلو نبود، یخ فعلی یک خانه به جلو هل داده می‌شود
                    currentTile.removeIceBlock();
                    nextTile.setIceBlock(true);
                }
            } else {
                // اگر یخ به انتهای سمت چپ نقشه (خانه بازیکن) رسید، از بین می‌رود
                currentTile.removeIceBlock();
            }
        }

        // ۲. حرکت عادی زامبی یا گاز گرفتن (اگر یخی مانع حرکتش نباشد)
        // اگر زامبی به گیاهی چسبیده باشد، شروع به خوردن می‌کند
        if (currentTile != null && currentTile.getPlant() != null && !currentTile.getPlant().isDead()) {
            setEating(true);
            currentTile.getPlant().takeDamage(getDamagePerTick());
            if (currentTile.getPlant().isDead()) {
                currentTile.setPlant(null);
                setEating(false);
            }
        } else {
            // حرکت به جلو
            setEating(false);
            setXPosition(getXPosition() - getSpeed());
        }
    }
}