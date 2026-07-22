package model.zombie.zombies;

import model.game.GameSession;
import model.game.Tile;
import model.plant.Plant;
import model.sun.FallingSun;
import model.zombie.Zombie;

import java.util.ArrayList;
import java.util.List;

public class RaZombie extends Zombie {

    private int stolenSuns = 0; // نگهدارنده خورشیدهای دزدیده شده
    private GameSession currentSession; // ذخیره سشن برای لحظه مرگ

    public RaZombie() {
        // نام، جان (۲۵۰)، سرعت (۰.۰۱)، هزینه موج (۱۵۰)، دمیج (۱۰)
        super("ra", 250, 0.01, 150, 10);
    }

    @Override
    public void onTick(GameSession session) {
        this.currentSession = session; // ذخیره سشن

        if (isDead()) return;

        // اول بررسی می‌کنه ببینه خورشیدی رو زمین هست که بدزده
        stealSunsFromGround(session);

        // بعد مثل یک زامبی عادی راه میره یا گیاه رو گاز می‌گیره
        normalMoveOrAttack(session);
    }

    // 🌟 متد مخصوص دزدیدن خورشید
    private void stealSunsFromGround(GameSession session) {
        List<FallingSun> allSuns = session.getFallingSuns();

        // ساخت یک لیست موقت برای خورشیدهایی که باید پاک بشن (جلوگیری از ارور جاوا)
        List<FallingSun> sunsToSteal = new ArrayList<>();

        // می‌گرده ببینه کدوم خورشید رسیده به زمین
        for (FallingSun sun : allSuns) {
            if (sun.isLanded()) {
                sunsToSteal.add(sun); // علامت می‌زنه که اینو بدزد
                stolenSuns += 25;     // ارزش هر خورشید رو ۲۵ در نظر گرفتیم (می‌تونی تغییرش بدی)
            }
        }

        // حالا همه خورشیدهای علامت‌خورده رو یکجا از بازی پاک می‌کنه
        allSuns.removeAll(sunsToSteal);
    }

    // 🌟 متد مرگ و پس دادن خورشیدها
    @Override
    public void takeDamage(int amount) {
        boolean wasDead = isDead();
        super.takeDamage(amount);

        // اگر تازه مرده و خورشیدی هم دزدیده بوده
        if (!wasDead && isDead() && stolenSuns > 0 && currentSession != null) {
            // تمام خورشیدها رو مستقیم می‌ریزه به حساب بازیکن
            currentSession.getSunManager().addSun(stolenSuns);
            stolenSuns = 0; // جیبش رو خالی می‌کنه
        }
    }

    // متد حرکت عادی (مثل بقیه زامبی‌ها)
    private void normalMoveOrAttack(GameSession session) {
        int col = (int) Math.floor(getXPosition());
        Tile tile = session.getBoard().getTile(getRow(), Math.max(col, 0));
        Plant target = (tile != null) ? tile.getPlant() : null;

        if (target != null && !target.isDead() && Math.abs(getXPosition() - col) < 0.5) {
            setEating(true);
            target.takeDamage(getDamagePerTick());
            if (target.isDead()) {
                tile.setPlant(null);
                setEating(false);
            }
        } else {
            setEating(false);
            setXPosition(getXPosition() - getSpeed());
        }
    }
}