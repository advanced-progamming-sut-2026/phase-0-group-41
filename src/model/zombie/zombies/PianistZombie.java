package model.zombie.zombies;

import model.game.Board;
import model.game.GameSession;
import model.game.Tile;
import model.plant.Plant;
import model.zombie.Zombie;

import java.util.List;
import java.util.Random;

public class PianistZombie extends Zombie {

    private static final int DANCE_COOLDOWN_TICKS = 40; // هر ۴ ثانیه (با فرض ۱۰ تیک در ثانیه)
    private int ticksSinceLastDance = 0;
    private final Random random = new Random();

    public PianistZombie() {
        // نام، جان (معمولاً برای پیانیست بالاست مثلاً ۱۰۰۰)، سرعت بسیار کم، هزینه موج، دمیج ۹۹۹۹ برای له کردن
        super("pianist", 1000, 0.005, 300, 9999);
    }

    @Override
    public void onTick(GameSession session) {
        if (isDead()) return;

        // ۱. منطق رقص و جابه‌جایی زامبی‌ها
        ticksSinceLastDance++;
        if (ticksSinceLastDance >= DANCE_COOLDOWN_TICKS) {
            makeZombiesDance(session);
            ticksSinceLastDance = 0;
        }

        // ۲. منطق حرکت و له کردن گیاهان (به‌جای حرکت ساده رو به جلو)
        moveOrCrush(session);
    }

    // 🌟 متد جدید: حرکت با قابلیت له‌کنندگی
    private void moveOrCrush(GameSession session) {
        int col = (int) Math.floor(getXPosition());
        Tile tile = session.getBoard().getTile(getRow(), Math.max(col, 0));
        Plant target = (tile != null) ? tile.getPlant() : null;

        // شرط برخورد فیزیکی دقیق (فاصله کمتر مساوی ۰.۵)
        if (target != null && !target.isDead() && (getXPosition() - col) <= 0.5) {
            setEating(true); // انیمیشن درگیری
            target.takeDamage(getDamagePerTick()); // وارد کردن دمیج ۹۹۹۹ به گیاه

            // برداشتن جسد گیاه از روی زمین برای باز شدن راه
            if (target.isDead()) {
                tile.setPlant(null);
                setEating(false);
            }
        } else {
            // اگر گیاهی نبود، با آرامش حرکت می‌کند
            setEating(false);
            setXPosition(getXPosition() - getSpeed());
        }
    }

    private void makeZombiesDance(GameSession session) {
        List<Zombie> aliveZombies = session.getAliveZombies();

        for (Zombie z : aliveZombies) {
            // پیانیست خودش تغییر لاین نمی‌دهد
            if (z != this && random.nextBoolean()) {
                changeZombieLane(z);
            }
        }
    }

    private void changeZombieLane(Zombie zombie) {
        int currentRow = zombie.getRow();
        boolean canGoUp = currentRow > 0;
        boolean canGoDown = currentRow < Board.ROWS - 1;

        // تغییر سطر به یکی از همسایه‌ها
        if (canGoUp && canGoDown) {
            int newRow = random.nextBoolean() ? currentRow - 1 : currentRow + 1;
            zombie.spawn(newRow, zombie.getXPosition());
        } else if (canGoUp) {
            zombie.spawn(currentRow - 1, zombie.getXPosition());
        } else if (canGoDown) {
            zombie.spawn(currentRow + 1, zombie.getXPosition());
        }
    }
}