package model.zombie.zombies;

import model.game.Board;
import model.game.GameSession;
import model.game.Tile;
import model.plant.Plant;
import model.zombie.Zombie;

public class FishermanZombie extends Zombie {

    private static final int HOOK_COOLDOWN_TICKS = 50; // هر ۵ ثانیه (۵۰ تیک) قلاب می‌اندازه
    private int ticksSinceLastHook = 0;

    public FishermanZombie() {
        // نام (fisherman)، جان (۳۵۰)، سرعت (۰.۰)، هزینه (۱۰۰)، دمیج گاز گرفتن (۰)
        // سرعت صفره چون طبق داک این زامبی همیشه تو راست‌ترین ستون ثابت می‌مونه
        super("fisherman", 350, 0.0, 100, 0);
    }

    @Override
    public void onTick(GameSession session) {
        if (isDead()) return;

        // تایمر قلاب رو می‌بریم جلو
        ticksSinceLastHook++;

        if (ticksSinceLastHook >= HOOK_COOLDOWN_TICKS) {
            castHook(session); // قلاب رو بنداز
            ticksSinceLastHook = 0; // تایمر رو صفر کن برای قلاب بعدی
        }
    }

    private void castHook(GameSession session) {
        Board board = session.getBoard();

        // حلقه for از راست به چپ:
        // چون قلاب از سمت زامبی (راست) میاد، اولین گیاهی که سر راهش باشه رو می‌گیره
        for (int col = Board.COLS - 1; col >= 0; col--) {
            Tile tile = board.getTile(getRow(), col);
            if (tile != null && tile.getPlant() != null) {
                // اگر تو این خونه گیاهی بود، عملیات کشیدن یا کشتن رو انجام بده
                pullOrDestroyPlant(board, tile, col);
                break; // 🌟 مهم: فقط همون اولین گیاه رو می‌کشه، پس حلقه رو می‌شکنیم
            }
        }
    }

    private void pullOrDestroyPlant(Board board, Tile currentTile, int col) {
        Plant targetPlant = currentTile.getPlant();
        int zombieCol = (int) Math.floor(getXPosition()); // ستونی که زامبی توشه (مثلاً 8)

        // ۱. حالت نابودی (پرتاب کردن):
        // طبق داک: "در صورتی که گیاه در کنارش باشد، آن را پرتاب و نابود می‌کند"
        // یعنی اگر گیاه دقیقاً تو ستون قبلی زامبی (zombieCol - 1) باشه
        if (col >= zombieCol - 1) {
            targetPlant.takeDamage(9999); // کشتن درجا
            currentTile.setPlant(null);   // پاک کردن گیاه از روی زمین
            return;
        }

        // ۲. حالت کشیدن (Pull):
        // اگر گیاه دور بود، باید یک خونه بیاد سمت راست (col + 1)
        Tile nextTile = board.getTile(getRow(), col + 1);

        // طبق داک: "خانه سمت راست گیاه باید خالی باشد"
        if (nextTile != null && nextTile.getPlant() == null) {

            currentTile.setPlant(null); // گیاه رو از خونه فعلیش بلند می‌کنیم
            nextTile.setPlant(targetPlant); // می‌کاریمش تو خونه جلویی

            // نکته: اگر تو کلاس Plant متدی برای ثبت مختصات داری (مثل setCol یا place)،
            // باید اینجا اون رو هم صدا بزنی تا خود گیاه هم بدونه جابجا شده. مثلاً:
            // targetPlant.setCol(col + 1);
        }
    }
}